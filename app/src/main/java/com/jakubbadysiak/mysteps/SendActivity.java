package com.jakubbadysiak.mysteps;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SendActivity extends AppCompatActivity {

    private String phone = null;
    private TextView tvPhoneDetails;
    private Button btnSendTo, btnSendMe;
    private Intent intentChooseApp,chosenIntent;
    private String chooserTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        chooserTitle = getString(R.string.chooser);
        tvPhoneDetails = (TextView) findViewById(R.id.tvPhoneDetails);
        btnSendTo = (Button) findViewById(R.id.btnSendTo);
        btnSendMe = (Button) findViewById(R.id.btnSendMe);

        phone = getIntent().getStringExtra("phone");

        tvPhoneDetails.setText(phone);

        intentChooseApp = new Intent(Intent.ACTION_SEND);
        chosenIntent = Intent.createChooser(intentChooseApp, chooserTitle);

        btnSendMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btnSendTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                intentChooseApp.setType("text/plain");
                intentChooseApp.putExtra(Intent.EXTRA_TEXT, phone);
                startActivity(chosenIntent);
            }
        });
    }
}
