package app.jjandj.broadcaster;

import android.app.ActionBar;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Response;
import com.google.bitcoin.core.Utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static app.jjandj.broadcaster.Base43.byteArrayToHexString;
import static com.android.volley.Request.Method.POST;

public class TextActivity extends AppCompatActivity {

    private Button broadcastNowButton, enterTransactionButton;
    private TextView textViewName, textViewAddress;
    String currencyString = "";
    String transactionDataString = "";
    String network = "";

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
            currencyString = extras.getString("currency");
            transactionDataString = extras.getString("transactionData");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);

        broadcastNowButton = (Button) findViewById(R.id.broadcastNowButton);
        enterTransactionButton = (Button) findViewById(R.id.enterTransaction);
        enterTransactionButton.setVisibility(View.GONE);
        broadcastNowButton.setVisibility(View.GONE);

        textViewName = (TextView) findViewById(R.id.textViewName);
        textViewAddress = (TextView) findViewById(R.id.textViewAddress);
        textViewName.setText(currencyString);
        try {textViewAddress.setText(transactionDataString);} catch(Exception e) {}
        textViewAddress.setMovementMethod(new ScrollingMovementMethod());

        broadcastNowButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                broadcastTransactionNow(currencyString, transactionDataString);
            }
        });

        enterTransactionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EnterTransaction(currencyString);
            }
        });

        //broadcast without prompt
        if (transactionDataString != null) { broadcastTransactionNow(currencyString, transactionDataString); }
        else
            {
                EnterTransaction(currencyString);
            }
    }

    protected void EnterTransaction(String currency)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter " + currency + " Transaction Data");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                transactionDataString = input.getText().toString();
                if (transactionDataString.contains("-"))
                {
                    //TRANSACTION WAS ENTERED IN BASE43 FORMAT, NEED TO FORMAT IT TO HEX TO BROADCAST
                    transactionDataString = convertBase43ToHex(transactionDataString);
                }
                textViewAddress.setText(transactionDataString);
                broadcastNowButton.setVisibility(View.VISIBLE);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                enterTransactionButton.setVisibility(View.VISIBLE);
            }
        });

        builder.show();
    }

    protected void broadcastTransactionNow(String currency, final String transactionData){

// Instantiate the RequestQueue.
        Toast.makeText(this, "Broadcasting...", Toast.LENGTH_SHORT).show();
        RequestQueue queue = Volley.newRequestQueue(this);

        if (currencyString.equals("Bitcoin TESTNET")) {network = "BTCTEST";}
        if (currencyString .equals("Bitcoin")) {network = "BTC";}
        if (currencyString .equals("Litecoin")) {network = "LTC";}
        if (currencyString .equals("DASH")) {network = "DASH";}
        if (currencyString .equals("ZCASH")) {network = "ZCASH";}
        if (currencyString .equals("DOGECOIN")) {network = "DOGE";}
        if (currencyString .equals("Litecoin TESTNET")) {network = "LTCTEST";}
        if (currencyString .equals("DASH TESTNET")) {network = "DASHTEST";}
        if (currencyString .equals("ZCASH TESTNET")) {network = "ZCASHTEST";}
        if (currencyString .equals("DOGECOIN TESTNET")) {network = "DOGETEST";}

        String url ="https://chain.so/api/v2/send_tx/"+network;

        if (currencyString.equals("Bitcoin Cash")) {url = "http://rest.bitcoin.com/v1/rawtransactions/sendRawTransaction/" + transactionData;}
        if (currencyString.equals("Bitcoin Cash TESTNET")) {url = "http://trest.bitcoin.com/v1/rawtransactions/sendRawTransaction/" + transactionData;}

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest( POST
                , url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 1000 characters of the response string.
                        textViewAddress.setText("Response is: "+ response + " \n \n The txid has been copied to the clipboard.");
                        if ((currencyString.equals("Bitcoin Cash")) || (currencyString.equals("Bitcoin Cash TESTNET")))
                        {
                            String txid = response.substring(1, response.length()-1);
                            CopyTxIdToClipboard(txid);
                        }
                        else
                                {
                            int index1 = response.indexOf("txid");
                            String txid = response.substring(index1 + 9, response.length() - 7);
                            CopyTxIdToClipboard(txid);
                            }

                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textViewAddress.setText(
                        //"Error: Status = " + ((Map.Entry)((TreeMap)error.networkResponse.headers).entrySet().toArray()[13]).getValue() +
                        //", Network Response: " + error.networkResponse +
                        //", Message: " + error.getMessage() +
                        //", Localized Message: " + error.getLocalizedMessage() +
                         "Transaction not broadcast: " + "\n" +
                                 "Check that you have selected the correct currency." + "\n" +
                                 "The transaction data must be for a transaction that is valid, " +
                                 "signed and has not been broadcast already. " + "\n" +
                                 "The transaction data must be a hexadecimal string.");
            }
        }){
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<String, String>();
                params.put("tx_hex", transactionData);
                return params;


            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Accept-Language", "application/x-www-form-urlencoded");

                return params;
            }

        };
// Add the request to the RequestQueue.
        broadcastNowButton.setVisibility(View.GONE);
        queue.add(stringRequest);
    }

    void CopyTxIdToClipboard(String txid)
    {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("TXID", txid);
        clipboard.setPrimaryClip(clip);
        //Toast.makeText(this, "TXID: " + txid + " copied to the clipboard", Toast.LENGTH_LONG).show();
    }


    public String convertBase43ToHex(String transactionDataString)
    {
        byte[] tx = Base43.decode(transactionDataString);
        transactionDataString = byteArrayToHexString(tx);
        return transactionDataString;
    }
}