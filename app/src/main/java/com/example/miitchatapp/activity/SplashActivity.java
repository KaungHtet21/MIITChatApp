package com.example.miitchatapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.widget.Space;

import com.example.miitchatapp.R;
import com.example.miitchatapp.SharePreferenceHelper;
import com.google.firebase.auth.FirebaseAuth;

import static com.example.miitchatapp.SharePreferenceHelper.KEY_LOGIN;

public class SplashActivity extends AppCompatActivity {

    private SharePreferenceHelper sharePreferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sharePreferenceHelper = new SharePreferenceHelper(this);

        new CountDownTimer(3000,500) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if (sharePreferenceHelper.get(KEY_LOGIN,false)){
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    LoginActivity.start(SplashActivity.this);
                }
            }
        }.start();

    }

}