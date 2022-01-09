package com.example.miitchatapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.miitchatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    EditText email, pass, confirmPass;
    CheckBox check;
    Button create;
    TextView txt, warnEmail, warnPass, warnConfirmPass, warnAgree;

    FirebaseAuth auth;
    DatabaseReference userRef;

    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        init();

        auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference();

        loadingBar = new ProgressDialog(this);

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateAcc();
            }
        });

        txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void CreateAcc() {
        String userEmail = email.getText().toString();
        String userPass = pass.getText().toString();
        String checkPass = confirmPass.getText().toString();

        if (TextUtils.isEmpty(userEmail)){
            warnEmail.setVisibility(View.VISIBLE);
        }else {
            warnEmail.setVisibility(View.INVISIBLE);
        }

        if (TextUtils.isEmpty(userPass)){
            warnPass.setVisibility(View.VISIBLE);
        }else {
            warnPass.setVisibility(View.INVISIBLE);
        }

        if (TextUtils.isEmpty(checkPass)){
            warnConfirmPass.setVisibility(View.VISIBLE);
        }else {
            warnConfirmPass.setVisibility(View.INVISIBLE);
        }

        if (!userPass.equals(checkPass)){
            warnConfirmPass.setText("Password must be same");
            warnConfirmPass.setVisibility(View.VISIBLE);
        }else {
            warnConfirmPass.setVisibility(View.INVISIBLE);
        }

        if (!check.isChecked()){
            warnAgree.setVisibility(View.VISIBLE);
        }

        else {
            warnAgree.setVisibility(View.INVISIBLE);

            loadingBar.setTitle("Creating new account");
            loadingBar.setMessage("Please wait");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            auth.createUserWithEmailAndPassword(userEmail, userPass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                String currentUserId = auth.getCurrentUser().getUid();
                                userRef.child("Users").child(currentUserId).setValue("");

                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();

                                Toast.makeText(RegisterActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                            }else {
                                String msg = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error: "+msg, Toast.LENGTH_SHORT).show();
                            }

                            loadingBar.dismiss();
                        }
                    });
        }
    }

    private void init() {
        email = findViewById(R.id.register_email);
        pass = findViewById(R.id.register_pass);
        confirmPass = findViewById(R.id.register_confirm_pass);

        check = findViewById(R.id.check);
        create = findViewById(R.id.create_btn);

        txt = findViewById(R.id.already_have_an_acc_txt);
        warnEmail = findViewById(R.id.warn_email);
        warnPass = findViewById(R.id.warn_pass);
        warnConfirmPass = findViewById(R.id.warn_confirm_pass);
        warnAgree = findViewById(R.id.warn_agree);
    }
}