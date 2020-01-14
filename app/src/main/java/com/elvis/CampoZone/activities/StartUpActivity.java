package com.elvis.CampoZone.activities;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.elvis.CampoZone.R;

public class StartUpActivity extends AppCompatActivity {

    FloatingActionButton venueOwnerBtn, clientBtn;
    Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);

        //find views
        venueOwnerBtn = findViewById(R.id.venue_owner_btn);
        clientBtn = findViewById(R.id.client_btn);
        loginBtn = findViewById(R.id.login_btn);

        //initialize signupactivity intent
        final Intent i = new Intent(StartUpActivity.this, SignUpActivity.class);

        //set onclicklisteners on buttons
        venueOwnerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //to distinguish client from venue holder
                i.putExtra("isClient", false);
                startActivity(i);
            }
        });
        clientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //to distinguish client from venue holder
                i.putExtra("isClient", true);
                startActivity(i);
            }
        });
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start login activity
                Intent intent = new Intent(StartUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
