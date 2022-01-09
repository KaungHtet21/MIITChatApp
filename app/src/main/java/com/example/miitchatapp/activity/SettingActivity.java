package com.example.miitchatapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.miitchatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    EditText username, userAbout;
    Button update;
    CircleImageView profileImage;

    Toolbar toolbar;

    FirebaseAuth auth;
    String currentUserId;
    DatabaseReference userRef;
    StorageReference profileRef;

    String photoUrl;

    ProgressDialog loadingBar;

    private static final int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        init();
        loadingBar = new ProgressDialog(this);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference();
        profileRef = FirebaseStorage.getInstance().getReference().child("Profile Image");

        toolbar = findViewById(R.id.setting_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Setting");

        RetrieveUserInfo();

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSetting();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_PICK);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK){
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();

                final StorageReference filePath = profileRef.child(currentUserId + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();

                                userRef.child("Users").child(currentUserId).child("image").setValue(downloadUrl)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    Toast.makeText(SettingActivity.this, "Profile uploaded successfully", Toast.LENGTH_SHORT).show();
                                                }else {
                                                    String msg = task.getException().toString();
                                                    Toast.makeText(SettingActivity.this, "Error: "+ msg, Toast.LENGTH_SHORT).show();
                                                }
                                                loadingBar.dismiss();
                                            }
                                        });
                            }
                        });
                    }
                });
            }
        }
    }

    private void RetrieveUserInfo() {
        userRef.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                       if ((snapshot.exists()) && (snapshot.hasChild("name")) && (snapshot.hasChild("image"))){
                            String retrieveUsername = snapshot.child("name").getValue().toString();
                            String retrieveUserAbout = snapshot.child("about").getValue().toString();
                            String retrieveUserProfile = snapshot.child("image").getValue().toString();

                            photoUrl = retrieveUserProfile;

                            username.setText(retrieveUsername);
                            userAbout.setText(retrieveUserAbout);

                            if (retrieveUserProfile.isEmpty()){
                                profileImage.setImageResource(R.drawable.profile);
                            }else {
                                Picasso.get().load(retrieveUserProfile).into(profileImage);
                            }

                        }else  if (snapshot.exists() && snapshot.hasChild("name")){
                           String retrieveUsername = snapshot.child("name").getValue().toString();
                           String retrieveUserAbout = snapshot.child("about").getValue().toString();

                           username.setText(retrieveUsername);
                           userAbout.setText(retrieveUserAbout);
                       } else {
                            Toast.makeText(SettingActivity.this, "Set Profile", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void UpdateSetting() {
        String setUsername = username.getText().toString();
        String setUserAbout = userAbout.getText().toString();

        if (TextUtils.isEmpty(setUsername)){
            Toast.makeText(this, "Enter username", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(setUserAbout)){
            Toast.makeText(this, "Enter your status", Toast.LENGTH_SHORT).show();
        }

        else {
            update.setEnabled(false);
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("uid", currentUserId);
            hashMap.put("name", setUsername);
            hashMap.put("about", setUserAbout);
            hashMap.put("image", photoUrl);

            userRef.child("Users").child(currentUserId).updateChildren(hashMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();

                                Toast.makeText(SettingActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            }else {
                                String msg = task.getException().toString();
                                Toast.makeText(SettingActivity.this, "Error: "+ msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void init() {
        username = findViewById(R.id.set_user_name);
        userAbout = findViewById(R.id.set_user_about);
        update = findViewById(R.id.update_btn);
        profileImage = findViewById(R.id.set_profile_image);
    }
}