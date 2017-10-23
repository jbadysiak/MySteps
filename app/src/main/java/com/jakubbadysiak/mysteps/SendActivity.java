package com.jakubbadysiak.mysteps;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SendActivity extends AppCompatActivity {

    private Button sendMail, sendToSby, showMap;
    private Intent intentSendMe;
    private Intent intentSendTo;
    private Intent intentShowMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        sendMail = (Button) findViewById(R.id.btnSendMe);
        sendToSby = (Button) findViewById(R.id.btnSendTo);
        showMap = (Button) findViewById(R.id.btnShowMap);

        sendMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        sendToSby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
