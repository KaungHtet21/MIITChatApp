package com.example.miitchatapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.miitchatapp.R;
import com.example.miitchatapp.adapter.TabsAccessorAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    DrawerLayout drawer;

    TabsAccessorAdapter adapter;

    FirebaseAuth auth;

    DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("MIIT Chat App");

        viewPager = findViewById(R.id.main_tabs_pager);
        adapter = new TabsAccessorAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        tabLayout = findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        auth = FirebaseAuth.getInstance();

        userRef = FirebaseDatabase.getInstance().getReference();

        if (auth.getCurrentUser() != null){
            onStart();
        }else {
            SentToLoginActivity();
        }

//        if (savedInstanceState == null){
//            navigationView.setCheckedItem(R.id.main_find_fri_option);
//        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        UpdateUserStatus("online");
        VerifyUserExistence();
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null){
            UpdateUserStatus("online");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null){
            UpdateUserStatus("offline");
        }
    }

    private void VerifyUserExistence() {
        String currentUserId = auth.getCurrentUser().getUid();
        userRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("name").exists()){
                    NavigationView navigationView = findViewById(R.id.nav_view);
                    View headerView = navigationView.getHeaderView(0);

                    TextView profileName = headerView.findViewById(R.id.nav_profile_name);
                    TextView profileAbout = headerView.findViewById(R.id.nav_profile_about);
                    CircleImageView profileImage = headerView.findViewById(R.id.nav_profile_image);

                    userRef.child("Users").child(currentUserId)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.hasChild("image")){
                                        String username = snapshot.child("name").getValue().toString();
                                        String userAbout = snapshot.child("about").getValue().toString();
                                        String userProfile = snapshot.child("image").getValue().toString();

                                        profileName.setText(username);
                                        profileAbout.setText(userAbout);
                                        Picasso.get().load(userProfile).placeholder(R.drawable.profile).into(profileImage);
                                    }else {
                                        String username = snapshot.child("name").getValue().toString();
                                        String userAbout = snapshot.child("about").getValue().toString();

                                        profileName.setText(username);
                                        profileAbout.setText(userAbout);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }else {
                    SentToSettingActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SentToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void SentToSettingActivity() {
        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.main_find_fri_option:
                Intent intent = new Intent(MainActivity.this, FindFriendActivity.class);
                startActivity(intent);
                break;
            case R.id.main_create_group_option:
                break;
            case R.id.main_setting_option:
                SentToSettingActivity();
                break;
            case R.id.main_log_out_option:
                logout();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.logout_dialog);

        int width = WindowManager.LayoutParams.MATCH_PARENT;
        int height = WindowManager.LayoutParams.WRAP_CONTENT;

        dialog.getWindow().setLayout(width, height);
        dialog.show();

        TextView cancel = dialog.findViewById(R.id.dialog_cancel);
        Button logoutBtn = dialog.findViewById(R.id.dialog_btn_logout);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                UpdateUserStatus("offline");
                auth.signOut();

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.remove("isLogin");

                SentToLoginActivity();
            }
        });
    }

    private void UpdateUserStatus(String state){
        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM, dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("date", saveCurrentDate);
        hashMap.put("time", saveCurrentTime);
        hashMap.put("state", state);

        String currentUserId = auth.getCurrentUser().getUid();
        userRef.child("Users").child(currentUserId).child("UserState")
                .updateChildren(hashMap);
    }

}