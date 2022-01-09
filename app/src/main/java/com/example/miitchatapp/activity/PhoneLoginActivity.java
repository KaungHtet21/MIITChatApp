package com.example.miitchatapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.miitchatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    CardView cardView;
    Button sendVerCodeBtn, verify;
    EditText inputPhNumb, inputVerCode;

    FirebaseAuth auth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    ProgressDialog loadingBar;

    String phoneNumb;
    CountryCodePicker ccp;

    String mVerCodeId;
    PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        init();
        ccp.registerCarrierNumberEditText(inputPhNumb);

        auth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);

        sendVerCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumb = ccp.getFullNumberWithPlus();
                if (TextUtils.isEmpty(inputPhNumb.getText().toString())){
                    Toast.makeText(PhoneLoginActivity.this, "Enter phone number", Toast.LENGTH_SHORT).show();
                }else if (inputPhNumb.getText().length() < 9){
                    Toast.makeText(PhoneLoginActivity.this, "Invalid phone number", Toast.LENGTH_SHORT).show();
                } else {
                    loadingBar.setTitle("Code sending");
                    loadingBar.setMessage("Please wait");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(auth)
                                    .setPhoneNumber(phoneNumb)       // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity(PhoneLoginActivity.this)                 // Activity (for callback binding)
                                    .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }
            }
        });

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardView.setVisibility(View.INVISIBLE);
                sendVerCodeBtn.setVisibility(View.INVISIBLE);

                String verCode = inputVerCode.getText().toString();

                if (TextUtils.isEmpty(verCode)){
                    Toast.makeText(PhoneLoginActivity.this, "Enter verification code", Toast.LENGTH_SHORT).show();
                }else {
                    loadingBar.setTitle("Verifying");
                    loadingBar.setMessage("Please wait");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerCodeId, verCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid phone number", Toast.LENGTH_SHORT).show();

                sendVerCodeBtn.setVisibility(View.VISIBLE);
                cardView.setVisibility(View.VISIBLE);

                verify.setVisibility(View.INVISIBLE);
                inputVerCode.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                mVerCodeId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Code has been sent", Toast.LENGTH_SHORT).show();

                sendVerCodeBtn.setVisibility(View.INVISIBLE);
                cardView.setVisibility(View.INVISIBLE);

                verify.setVisibility(View.VISIBLE);
                inputVerCode.setVisibility(View.VISIBLE);
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            loadingBar.dismiss();

                            Intent intent = new Intent(PhoneLoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }else {
                            String msg = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error: "+ msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void init() {
        ccp = findViewById(R.id.ccp);
        cardView = findViewById(R.id.card_view);
        inputPhNumb = findViewById(R.id.phone_number_input);
        inputVerCode = findViewById(R.id.ver_code_input);
        sendVerCodeBtn = findViewById(R.id.send_ver_code_btn);
        verify = findViewById(R.id.verify_btn);
    }
}