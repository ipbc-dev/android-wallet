package com.bittube.wallet;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bittube.wallet.network.Callback;
import com.bittube.wallet.util.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkUserLogin();
            }
        }, 200);

    }


    private void checkUserLogin() {
        FirebaseUtil.saveUserToken(new Callback<String>() {
            @Override
            public void success(String token) {
                goToLogin();
            }

            @Override
            public void error(String errMsg) {
                goToPrelogin();
            }
        });
    }

    private void goToLogin() {
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }

    private void goToPrelogin() {
        startActivity(new Intent(getApplicationContext(), PreLoginActivity.class));
        finish();
    }
}
