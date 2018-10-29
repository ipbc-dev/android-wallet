package com.m2049r.xmrwallet;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        goToLogin();
    }


    private void goToLogin(){
        // TODO: Load some data instead of faking 500ms
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), PreLoginActivity.class));
                finish();
            }
        }, 500);
    }
}
