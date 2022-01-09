package com.example.miitchatapp.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.miitchatapp.R;
import com.example.miitchatapp.activity.ChatActivity;
import com.example.miitchatapp.modules.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.zip.InflaterInputStream;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatFragment extends Fragment {

    RecyclerView chatList;

    FirebaseAuth auth;
    String currentUserId;
    DatabaseReference chatRef, userRef;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        chatList = view.findViewById(R.id.chat_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Contacts model) {
                        final String userIds = getRef(position).getKey();
                        final String[] retImage = {"default image"};

                        userRef.child(userIds).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    if (snapshot.hasChild("image")){
                                        retImage[0] = snapshot.child("image").getValue().toString();

                                        Picasso.get().load(retImage[0]).placeholder(R.drawable.profile).into(holder.profileImage);
                                    }
                                    final String retUsername = snapshot.child("name").getValue().toString();

                                    holder.profileUsername.setText(retUsername);

                                    if (snapshot.child("UserState").hasChild("state")){
                                        String state = snapshot.child("UserState").child("state").getValue().toString();
                                        String date = snapshot.child("UserState").child("date").getValue().toString();
                                        String time = snapshot.child("UserState").child("time").getValue().toString();

                                        if (state.equals("online")){
                                            holder.profileUserAbout.setText("Online now");
                                            holder.onlineIcon.setVisibility(View.VISIBLE);
                                        }else if (state.equals("offline")){
                                            holder.profileUserAbout.setText("Last Seen: " + date + " " + time);
                                            holder.onlineIcon.setVisibility(View.INVISIBLE);
                                        }
                                    }else {
                                        holder.profileUserAbout.setText("Offline");
                                        holder.onlineIcon.setVisibility(View.INVISIBLE);
                                    }

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(getContext(), ChatActivity.class);
                                            intent.putExtra("visit_user_id", userIds);
                                            intent.putExtra("visit_user_name", retUsername);
                                            intent.putExtra("visit_user_image", retImage[0]);
                                            startActivity(intent);
                                        }
                                    });
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);
                        return new ViewHolder(view);
                    }
                };

        chatList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView profileUsername, profileUserAbout;
        CircleImageView profileImage;
        ImageView onlineIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileUsername = itemView.findViewById(R.id.profile_name);
            profileUserAbout = itemView.findViewById(R.id.profile_about);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            onlineIcon = itemView.findViewById(R.id.user_online_status);
        }
    }
}