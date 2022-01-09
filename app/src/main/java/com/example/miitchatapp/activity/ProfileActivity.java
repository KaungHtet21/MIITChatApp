package com.example.miitchatapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.miitchatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView userProfile;
    TextView username, userAbout;
    Button sendReq, declineReq;

    DatabaseReference userRef, chatRef, contactRef;
    FirebaseAuth auth;
    String senderId, receiverId, current_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        init();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        auth = FirebaseAuth.getInstance();

        receiverId = getIntent().getExtras().get("visit_user_id").toString();
        senderId = auth.getCurrentUser().getUid();
        current_state = "new";

        RetrieveUserInfo();

    }

    private void RetrieveUserInfo() {
        userRef.child(receiverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if ((snapshot.exists()) && (snapshot.hasChild("image"))){
                            String setUsername = snapshot.child("name").getValue().toString();
                            String setUserAbout = snapshot.child("about").getValue().toString();
                            String setUserProfile = snapshot.child("image").getValue().toString();

                            username.setText(setUsername);
                            userAbout.setText(setUserAbout);
                            Picasso.get().load(setUserProfile).placeholder(R.drawable.profile).into(userProfile);

                            ManageChatReq();
                        }else {
                            String setUsername = snapshot.child("name").getValue().toString();
                            String setUserAbout = snapshot.child("about").getValue().toString();

                            username.setText(setUsername);
                            userAbout.setText(setUserAbout);

                            ManageChatReq();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void ManageChatReq() {
        chatRef.child(senderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(receiverId)){
                            String request_type = snapshot.child(receiverId).child("request_type").getValue().toString();

                            if (request_type.equals("sent")){
                                current_state = "request_sent";
                                sendReq.setText("Cancel Chat Request");
                            }else if (request_type.equals("received")){
                                current_state = "request_received";
                                sendReq.setText("Accept the request");

                                declineReq.setVisibility(View.VISIBLE);
                                declineReq.setEnabled(true);
                                declineReq.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelChatReq();
                                    }
                                });
                            }
                        }else {
                            contactRef.child(senderId)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.hasChild(receiverId)){
                                                current_state = "friends";
                                                sendReq.setText("Remove Contact");
                                            }else {
                                                current_state = "new";
                                                sendReq.setText("Send Message Request");

                                                declineReq.setEnabled(false);
                                                declineReq.setVisibility(View.INVISIBLE);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        if (!senderId.equals(receiverId)){
            sendReq.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendReq.setEnabled(false);
                    if (current_state.equals("new")){
                        SendChatReq();
                    }

                    if (current_state.equals("request_sent")){
                        CancelChatReq();
                    }

                    if (current_state.equals("request_received")){
                        AcceptChatReq();
                    }

                    if (current_state.equals("friends")){
                        RemoveContact();
                    }
                }
            });
        }else {
            sendReq.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveContact() {
        contactRef.child(senderId).child(receiverId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            contactRef.child(receiverId).child(senderId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                current_state = "new";

                                                sendReq.setEnabled(true);
                                                sendReq.setText("Send Message Request");

                                                declineReq.setEnabled(false);
                                                declineReq.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatReq() {
        contactRef.child(senderId).child(receiverId)
                .child("contacts").setValue("saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            contactRef.child(receiverId).child(senderId)
                                    .child("contacts").setValue("saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                chatRef.child(senderId).child(receiverId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    chatRef.child(receiverId).child(senderId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){
                                                                                        current_state = "friends";

                                                                                        sendReq.setEnabled(true);
                                                                                        sendReq.setText("Remove Contact");

                                                                                        declineReq.setEnabled(false);
                                                                                        declineReq.setVisibility(View.INVISIBLE);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelChatReq() {
        chatRef.child(senderId).child(receiverId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            chatRef.child(receiverId).child(senderId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            sendReq.setEnabled(true);
                                            current_state = "new";
                                            sendReq.setText("Send Message Request");

                                            declineReq.setEnabled(false);
                                            declineReq.setVisibility(View.INVISIBLE);
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendChatReq() {
        chatRef.child(senderId).child(receiverId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            chatRef.child(receiverId).child(senderId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            sendReq.setEnabled(true);
                                            current_state = "request_sent";
                                            sendReq.setText("Cancel Chat Request");
                                        }
                                    });
                        }
                    }
                });
    }

    private void init() {
        username = findViewById(R.id.profile_name);
        userAbout = findViewById(R.id.profile_about);
        userProfile = findViewById(R.id.profile_image);

        sendReq = findViewById(R.id.send_req_btn);
        declineReq = findViewById(R.id.decline_req_btn);
    }
}