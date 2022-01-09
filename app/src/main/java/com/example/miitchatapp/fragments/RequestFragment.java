package com.example.miitchatapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.miitchatapp.R;
import com.example.miitchatapp.modules.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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


public class RequestFragment extends Fragment {

    RecyclerView requestList;

    FirebaseAuth auth;
    String currentUserId;

    DatabaseReference userRef, chatRef, contactRef;

    public RequestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request, container, false);
        requestList = view.findViewById(R.id.request_list);
        requestList.setLayoutManager(new LinearLayoutManager(getContext()));

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRef.child(currentUserId), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Contacts model) {
                        holder.accept.setVisibility(View.VISIBLE);
                        holder.decline.setVisibility(View.VISIBLE);

                        final String userIds = getRef(position).getKey();

                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    String type = snapshot.getValue().toString();
                                    if (type.equals("received")){
                                        userRef.child(userIds).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.hasChild("image")){
                                                    final String requestUserProfile = snapshot.child("image").getValue().toString();

                                                    Picasso.get().load(requestUserProfile).placeholder(R.drawable.profile).into(holder.profileImage);
                                                }
                                                final String requestUsername = snapshot.child("name").getValue().toString();

                                                holder.profileUsername.setText(requestUsername);
                                                holder.profileUserAbout.setText("wants to connect with you");

                                                holder.accept.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        contactRef.child(currentUserId).child(userIds).child("contacts")
                                                                .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    contactRef.child(userIds).child(currentUserId).child("contacts")
                                                                            .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                chatRef.child(currentUserId).child(userIds)
                                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()){
                                                                                            chatRef.child(userIds).child(currentUserId)
                                                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    Toast.makeText(getContext(), "Contact add successfully", Toast.LENGTH_SHORT).show();
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
                                                });

                                                holder.decline.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        chatRef.child(currentUserId).child(userIds)
                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    chatRef.child(userIds).child(currentUserId)
                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            Toast.makeText(getContext(), "Contact deleted", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }else if (type.equals("sent")){
                                        holder.accept.setText("Request Sent");
                                        holder.decline.setText("Cancel");

                                        userRef.child(userIds).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.hasChild("image")){
                                                    final String requestUserImage = snapshot.child("image").getValue().toString();

                                                    Picasso.get().load(requestUserImage).placeholder(R.drawable.profile).into(holder.profileImage);
                                                }
                                                final String requestUsername = snapshot.child("name").getValue().toString();

                                                holder.profileUsername.setText(requestUsername);
                                                holder.profileUserAbout.setText("You have sent request to "+ requestUsername);

                                                holder.decline.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        chatRef.child(currentUserId).child(userIds)
                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    chatRef.child(userIds).child(currentUserId)
                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            Toast.makeText(getContext(), "Contact deleted", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
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
        requestList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView profileUsername, profileUserAbout;
        CircleImageView profileImage;
        Button accept, decline;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileUsername = itemView.findViewById(R.id.profile_name);
            profileUserAbout = itemView.findViewById(R.id.profile_about);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            accept = itemView.findViewById(R.id.accept_btn);
            decline = itemView.findViewById(R.id.decline_btn);
        }
    }
}