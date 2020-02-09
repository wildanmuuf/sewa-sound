package com.unpam.sewasound;

import android.content.Intent;
import android.os.Handler;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        startService(new Intent(this, NotificationService.class));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //setelah loading maka akan langsung berpindah ke home activity
                Intent home=new Intent(getApplicationContext(), MainActivity.class);
                startActivity(home);
                finish();
            }
        },3000);
    }
}
