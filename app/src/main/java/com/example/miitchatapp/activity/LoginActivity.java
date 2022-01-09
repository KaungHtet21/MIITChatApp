package com.example.miitchatapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.miitchatapp.R;
import com.example.miitchatapp.SharePreferenceHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import static com.example.miitchatapp.SharePreferenceHelper.KEY_LOGIN;

public class LoginActivity extends AppCompatActivity {

    EditText email, pass;
    TextView forgotPass, createAcc;
    Button login, phoneLogin;

    ProgressDialog loadingBar;

    FirebaseAuth auth;
    private SharePreferenceHelper sharePreferenceHelper;

    public static void start(Context context){
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();

        sharePreferenceHelper = new SharePreferenceHelper(this);

        auth = FirebaseAuth.getInstance();

        loadingBar = new ProgressDialog(this);

        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        createAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SentToRegisterActivity();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogIn();
            }
        });

        phoneLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void AllowUserToLogIn() {
        String userEmail = email.getText().toString();
        String userPass = pass.getText().toString();

        if (TextUtils.isEmpty(userEmail)){
            Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(userPass)){
            Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show();
        }

        else {
            login.setEnabled(false);

            loadingBar.setTitle("Signing account");
            loadingBar.setMessage("Please wait");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            auth.signInWithEmailAndPassword(userEmail, userPass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){

                                sharePreferenceHelper.save(KEY_LOGIN,true);

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }else {
                                String msg = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Error: "+msg, Toast.LENGTH_SHORT).show();
                                login.setEnabled(true);
                            }
                            loadingBar.dismiss();
                        }
                    });
        }
    }

    private void SentToRegisterActivity() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void init() {
        email = findViewById(R.id.login_email);
        pass = findViewById(R.id.login_password);
        forgotPass = findViewById(R.id.forgotPass_txt);
        createAcc = findViewById(R.id.createAcc_txt);
        login = findViewById(R.id.login_btn);
        phoneLogin = findViewById(R.id.phone_log_in_btn);
    }
}