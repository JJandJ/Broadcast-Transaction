package app.jjandj.broadcaster;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.bitcoin.core.Utils;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.math.BigInteger;
import java.util.Arrays;

import static app.jjandj.broadcaster.Base43.byteArrayToHexString;

public class QrCodeActivity extends AppCompatActivity implements View.OnClickListener {

    //View Objects
    private Button buttonScan, broadcastButton;
    private TextView textViewName, textViewAddress;
    String currency = "";
    String transactionData ="";
    int ranalready = 0;

    //qr code scanner object
    private IntentIntegrator qrScan;

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();

        Bundle extras = getIntent().getExtras();
        if(extras !=null)
        {
            currency = extras.getString("currency");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        //View objects
        buttonScan = (Button) findViewById(R.id.buttonScan);
        textViewName = (TextView) findViewById(R.id.textViewName);
        textViewName.setText(currency);
        textViewAddress = (TextView) findViewById(R.id.textViewAddress);
        broadcastButton = (Button) findViewById(R.id.broadcastButton);
        broadcastButton.setVisibility(View.GONE);

        //intializing scan object
        qrScan = new IntentIntegrator(this);

        //attaching onclick listener
        buttonScan.setOnClickListener(this);

        broadcastButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent txActivity = new Intent(getBaseContext(), TextActivity.class);
                txActivity.putExtra("currency", currency); //Optional parameters
                txActivity.putExtra("transactionData", transactionData); //Optional parameters
                startActivity(txActivity);
            }
        });

        //start camera without prompt on first load
        if (ranalready == 0)
            {
                ranalready = 1;
                qrScan.initiateScan();
            }
    }

    //Getting the scan results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //if qrcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            } else {
                //if qr contains data
                try {
                    //converting the data to json
                    JSONObject obj = new JSONObject(result.getContents());
                    //setting values to textviews
                    textViewName.setText(obj.getString("name"));
                    textViewAddress.setText(obj.getString("address"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    //if control comes here
                    //that means the encoded format not matches
                    //in this case you can display whatever data is available on the qrcode
                    //to a toast
                    //Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                    transactionData = result.getContents();
                    textViewName.setText(currency);

                    if (transactionData.contains("-"))
                    {
                        //QR CODE WAS IN BASE43 FORMAT, NEED TO FORMAT IT TO HEX TO BROADCAST

                        convertBase43ToHex();
                    }

                    textViewAddress.setText(transactionData);
                    buttonScan.setVisibility(View.GONE);
                    broadcastButton.setVisibility(View.VISIBLE);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    @Override
    public void onClick(View view) {
        //initiating the qr code scan
        qrScan.initiateScan();
    }

    public void convertBase43ToHex()
    {
        byte[] tx = Base43.decode(transactionData);
        transactionData = byteArrayToHexString(tx);

    }
}


class Base43
{
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ$*+-./:";
    private static final BigInteger BASE = BigInteger.valueOf(ALPHABET.length());

    public static String encode(byte[] input)
    {
        // TODO: This could be a lot more efficient.
        BigInteger bi = new BigInteger(1, input);
        StringBuffer s = new StringBuffer();
        while (bi.compareTo(BASE) >= 0)
        {
            BigInteger mod = bi.mod(BASE);
            s.insert(0, ALPHABET.charAt(mod.intValue()));
            bi = bi.subtract(mod).divide(BASE);
        }
        s.insert(0, ALPHABET.charAt(bi.intValue()));
        // Convert leading zeros too.
        for (byte anInput : input)
        {
            if (anInput == 0)
                s.insert(0, ALPHABET.charAt(0));
            else
                break;
        }
        return s.toString();
    }

    public static byte[] decode(String input) throws IllegalArgumentException
    {
        byte[] bytes = decodeToBigInteger(input).toByteArray();
        // We may have got one more byte than we wanted, if the high bit of the next-to-last byte was not zero. This
        // is because BigIntegers are represented with twos-compliment notation, thus if the high bit of the last
        // byte happens to be 1 another 8 zero bits will be added to ensure the number parses as positive. Detect
        // that case here and chop it off.
        boolean stripSignByte = bytes.length > 1 && bytes[0] == 0 && bytes[1] < 0;
        // Count the leading zeros, if any.
        int leadingZeros = 0;
        for (int i = 0; input.charAt(i) == ALPHABET.charAt(0); i++)
        {
            leadingZeros++;
        }
        // Now cut/pad correctly. Java 6 has a convenience for this, but Android can't use it.
        byte[] tmp = new byte[bytes.length - (stripSignByte ? 1 : 0) + leadingZeros];
        System.arraycopy(bytes, stripSignByte ? 1 : 0, tmp, leadingZeros, tmp.length - leadingZeros);
        return tmp;
    }

    public static BigInteger decodeToBigInteger(String input) throws IllegalArgumentException
    {
        BigInteger bi = BigInteger.valueOf(0);
        // Work backwards through the string.
        for (int i = input.length() - 1; i >= 0; i--)
        {
            int alphaIndex = ALPHABET.indexOf(input.charAt(i));
            if (alphaIndex == -1)
            {
                throw new IllegalArgumentException("Illegal character " + input.charAt(i) + " at " + i);
            }
            bi = bi.add(BigInteger.valueOf(alphaIndex).multiply(BASE.pow(input.length() - 1 - i)));
        }
        return bi;
    }

    /**
     * Uses the checksum in the last 4 bytes of the decoded data to verify the rest are correct. The checksum is removed
     * from the returned data.
     *
     * @throws IllegalArgumentException
     *             if the input is not base 43 or the checksum does not validate.
     */
    public static byte[] decodeChecked(String input) throws IllegalArgumentException
    {
        byte[] tmp = decode(input);
        if (tmp.length < 4)
            throw new IllegalArgumentException("Input too short");
        byte[] checksum = new byte[4];
        System.arraycopy(tmp, tmp.length - 4, checksum, 0, 4);
        byte[] bytes = new byte[tmp.length - 4];
        System.arraycopy(tmp, 0, bytes, 0, tmp.length - 4);
        tmp = Utils.doubleDigest(bytes);
        byte[] hash = new byte[4];
        System.arraycopy(tmp, 0, hash, 0, 4);
        if (!Arrays.equals(hash, checksum))
            throw new IllegalArgumentException("Checksum does not validate");
        return bytes;
    }


    public static String byteArrayToHexString(byte[] array) {
        StringBuffer hexString = new StringBuffer();
        for (byte b : array) {
            int intVal = b & 0xff;
            if (intVal < 0x10)
                hexString.append("0");
            hexString.append(Integer.toHexString(intVal));
        }
        return hexString.toString();
    }

}