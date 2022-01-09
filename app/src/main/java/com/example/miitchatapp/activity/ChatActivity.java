package com.example.miitchatapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.miitchatapp.R;
import com.example.miitchatapp.adapter.MessageAdapter;
import com.example.miitchatapp.modules.Messages;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    String msgReceiverUsername, msgReceiverUserId, msgReceiverUserImage, msgSenderId;
    Toolbar toolbar;
    TextView username, userLastSeen;
    EditText msgInputs;
    CircleImageView userProfile;
    RecyclerView userMsgList;

    ImageButton send, sendFile;
    FirebaseAuth auth;
    DatabaseReference reference;

    String saveCurrentDate, saveCurrentTime;
    String checker = "", myUrl = "";
    Uri fileUri;
    StorageTask uploadTask;

    ProgressDialog loadingBar;

    final List<Messages> msgList = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    MessageAdapter messageAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        msgReceiverUserId = getIntent().getStringExtra("visit_user_id");
        msgReceiverUsername = getIntent().getStringExtra("visit_user_name");
        msgReceiverUserImage = getIntent().getStringExtra("visit_user_image");

        auth = FirebaseAuth.getInstance();
        msgSenderId = auth.getCurrentUser().getUid();

        reference = FirebaseDatabase.getInstance().getReference();

        init();

        username.setText(msgReceiverUsername);
        Picasso.get().load(msgReceiverUserImage).placeholder(R.drawable.profile).into(userProfile);

        DisplayLastSeen();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMsg();
            }
        });

        sendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]{
                    "Image",
                    "PDF Files",
                    "MS Word Files"
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the send_file");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            checker = "image";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image"), 438);
                        }

                        if (which == 1){
                            checker = "pdf";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select PDF Files"), 438);
                        }

                        if (which == 2){
                            checker = "docx";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "Select MS Word Files"), 438);
                        }
                    }
                });
                builder.show();
            }
        });

        reference.child("Messages").child(msgSenderId).child(msgReceiverUserId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Messages messages = snapshot.getValue(Messages.class);
                        msgList.add(messages);
                        messageAdapter.notifyDataSetChanged();
                        userMsgList.smoothScrollToPosition(userMsgList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                        Messages messages = snapshot.getValue(Messages.class);
                        messageAdapter.deleteMessage(messages);
                        userMsgList.smoothScrollToPosition(userMsgList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 438 && resultCode == RESULT_OK && data.getData() != null){
            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Please wait");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();

            if (!checker.equals("image")){
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                String msgSenderRef = "Messages/"+ msgSenderId + "/"+ msgReceiverUserId;
                String msgReceiverRef = "Messages/"+ msgReceiverUserId +"/"+ msgSenderId;

                DatabaseReference userMsgKeyRef = reference.child("Messages")
                        .child(msgSenderId)
                        .child(msgReceiverUserId).push();

                final String msgPushId = userMsgKeyRef.getKey();

                StorageReference filePath = storageReference.child(msgPushId + "." + checker);

                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();

                                Map msgTextBody = new HashMap();
                                msgTextBody.put("message", downloadUrl);
                                msgTextBody.put("type", checker);
                                msgTextBody.put("from", msgSenderId);
                                msgTextBody.put("to", msgReceiverUserId);
                                msgTextBody.put("messageId", msgPushId);
                                msgTextBody.put("date", saveCurrentDate);
                                msgTextBody.put("time", saveCurrentTime);
                                msgTextBody.put("name", fileUri.getLastPathSegment());

                                Map msgBodyDetail = new HashMap();
                                msgBodyDetail.put(msgSenderRef +"/"+ msgPushId, msgTextBody);
                                msgBodyDetail.put(msgReceiverRef +"/"+ msgPushId, msgTextBody);

                                reference.updateChildren(msgBodyDetail);
                                loadingBar.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            }else if (checker.equals("image")){
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                String msgSenderRef = "Messages/"+ msgSenderId + "/"+ msgReceiverUserId;
                String msgReceiverRef = "Messages/"+ msgReceiverUserId +"/"+ msgSenderId;

                DatabaseReference userMsgKeyRef = reference.child("Messages")
                        .child(msgSenderId)
                        .child(msgReceiverUserId).push();

                final String msgPushId = userMsgKeyRef.getKey();

                StorageReference filePath = storageReference.child(msgPushId + "." + "jpg");

                uploadTask = filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            Map msgTextBody = new HashMap();
                            msgTextBody.put("message", myUrl);
                            msgTextBody.put("type", checker);
                            msgTextBody.put("from", msgSenderId);
                            msgTextBody.put("to", msgReceiverUserId);
                            msgTextBody.put("messageId", msgPushId);
                            msgTextBody.put("date", saveCurrentDate);
                            msgTextBody.put("time", saveCurrentTime);
                            msgTextBody.put("name", fileUri.getLastPathSegment());

                            Map msgBodyDetail = new HashMap();
                            msgBodyDetail.put(msgSenderRef +"/"+ msgPushId, msgTextBody);
                            msgBodyDetail.put(msgReceiverRef +"/"+ msgPushId, msgTextBody);

                            reference.updateChildren(msgBodyDetail).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(ChatActivity.this, "Message sent...", Toast.LENGTH_SHORT).show();
                                    }else {
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    loadingBar.dismiss();
                                    msgInputs.setText("");
                                }
                            });
                        }
                    }
                });
            }else {
                loadingBar.dismiss();
                Toast.makeText(this, "Nothing selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void SendMsg() {
        String msgTxt = msgInputs.getText().toString();
        if (TextUtils.isEmpty(msgTxt)){
            Toast.makeText(this, "Enter message", Toast.LENGTH_SHORT).show();
        }else {
            String msgSenderRef = "Messages/"+msgSenderId+"/"+msgReceiverUserId;
            String msgReceiverRef = "Messages/"+msgReceiverUserId+"/"+msgSenderId;

            DatabaseReference userMsgKeyRef = reference.child("Messages")
                    .child(msgSenderId)
                    .child(msgReceiverUserId).push();

            String msgPushId = userMsgKeyRef.getKey();

            Map msgTextBody = new HashMap();
            msgTextBody.put("message", msgTxt);
            msgTextBody.put("type", "text");
            msgTextBody.put("from", msgSenderId);
            msgTextBody.put("to", msgReceiverUserId);
            msgTextBody.put("messageId", msgPushId);
            msgTextBody.put("date", saveCurrentDate);
            msgTextBody.put("time", saveCurrentTime);

            Map msgBodyDetail = new HashMap();
            msgBodyDetail.put(msgSenderRef + "/" + msgPushId, msgTextBody);
            msgBodyDetail.put(msgReceiverRef + "/" + msgPushId, msgTextBody);

            reference.updateChildren(msgBodyDetail).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Message sent..", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }

                    msgInputs.setText("");
                }
            });

        }
    }

    private void DisplayLastSeen(){
        reference.child("Users").child(msgReceiverUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child("UserState").hasChild("state")){
                            String state = snapshot.child("UserState").child("state").getValue().toString();
                            String date = snapshot.child("UserState").child("date").getValue().toString();
                            String time = snapshot.child("UserState").child("time").getValue().toString();

                            if (state.equals("online")){
                                userLastSeen.setText("Online");
                            }else if (state.equals("offline")){
                                userLastSeen.setText("Last seen: "+ date +" "+ time);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void init() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        username = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_user_last_seen);
        userProfile = findViewById(R.id.custom_profile_image);

        msgInputs = findViewById(R.id.input_msg);

        send = findViewById(R.id.send_msg_btn);
        sendFile = findViewById(R.id.send_files_btn);

        userMsgList = findViewById(R.id.msg_list);
        messageAdapter = new MessageAdapter(msgList);

        linearLayoutManager = new LinearLayoutManager(this);
        userMsgList.setLayoutManager(linearLayoutManager);
        userMsgList.setAdapter(messageAdapter);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM, dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        loadingBar = new ProgressDialog(this);
    }

}