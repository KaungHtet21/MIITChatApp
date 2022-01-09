package com.example.miitchatapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.miitchatapp.R;
import com.example.miitchatapp.modules.Contacts;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;

    DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        toolbar = findViewById(R.id.find_fri_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        recyclerView = findViewById(R.id.find_fri_recycler_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(userRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ViewHolder holder, final int position, @NonNull Contacts model) {
                        holder.profileUsername.setText(model.getName());
                        holder.profileAbout.setText(model.getAbout());
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.profile).into(holder.profileImage);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String visit_user_id = getRef(position).getKey();
                                Intent intent = new Intent(FindFriendActivity.this, ProfileActivity.class);
                                intent.putExtra("visit_user_id", visit_user_id);
                                startActivity(intent);
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
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView profileImage;
        TextView profileUsername, profileAbout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileAbout = itemView.findViewById(R.id.profile_about);
            profileUsername = itemView.findViewById(R.id.profile_name);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }

}