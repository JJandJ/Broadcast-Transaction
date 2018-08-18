package app.jjandj.broadcaster;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Spinner currencySpinner = findViewById(R.id.spinner1);

        // Get our button from the layout resource,
        // and attach an event to it
        Button qrbutton = findViewById(R.id.myButton);

        qrbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent qrActivity = new Intent(getBaseContext(), QrCodeActivity.class);
                qrActivity.putExtra("currency", currencySpinner.getSelectedItem().toString()); //Optional parameters
               startActivity(qrActivity);
            }
        });


        Button textbutton = findViewById(R.id.textbutton);

        textbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent txActivity = new Intent(getBaseContext(), TextActivity.class);
                txActivity.putExtra("currency", currencySpinner.getSelectedItem().toString()); //Optional parameters
                startActivity(txActivity);
            }
        });

        //currencySpinner.performClick();

    }


}
