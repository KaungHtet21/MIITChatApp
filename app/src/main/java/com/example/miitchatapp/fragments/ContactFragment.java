package com.example.miitchatapp.fragments;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactFragment extends Fragment {

    RecyclerView contactList;
    DatabaseReference userRef, contactRef;
    FirebaseAuth auth;
    String currentUserId;

    public ContactFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        contactList = view.findViewById(R.id.contacts_list);
        contactList.setLayoutManager(new LinearLayoutManager(getContext()));

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Contacts model) {
                        final String userIds = getRef(position).getKey();
                        userRef.child(userIds)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()){
                                            if (snapshot.child("UserState").hasChild("state")){
                                                String state = snapshot.child("UserState").child("state").getValue().toString();

                                                if (state.equals("online")){
                                                    holder.onlineIcon.setVisibility(View.VISIBLE);
                                                }else if (state.equals("offline")){
                                                    holder.onlineIcon.setVisibility(View.INVISIBLE);
                                                }
                                            }else {
                                                holder.onlineIcon.setVisibility(View.INVISIBLE);
                                            }

                                            if (snapshot.hasChild("image")){
                                                String username = snapshot.child("name").getValue().toString();
                                                String userAbout = snapshot.child("about").getValue().toString();
                                                String userProfile = snapshot.child("image").getValue().toString();

                                                holder.profileUsername.setText(username);
                                                holder.profileUserAbout.setText(userAbout);

                                                Picasso.get().load(userProfile).placeholder(R.drawable.profile).into(holder.profileImage);
                                            }else {
                                                String username = snapshot.child("name").getValue().toString();
                                                String userAbout = snapshot.child("about").getValue().toString();

                                                holder.profileUsername.setText(username);
                                                holder.profileUserAbout.setText(userAbout);
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

        contactList.setAdapter(adapter);
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