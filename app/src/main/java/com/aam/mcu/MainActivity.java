package com.aam.mcu;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.aam.mcu.data.User;
import com.aam.mcu.extra.DrawerToggle;
import com.aam.mcu.fragments.Chat;
import com.aam.mcu.fragments.Contest;
import com.aam.mcu.fragments.Home;
import com.aam.mcu.fragments.Members;
import com.aam.mcu.fragments.Quiz;
import com.aam.mcu.notification.APIService;
import com.aam.mcu.notification.Message;
import com.aam.mcu.notification.Notification;
import com.aam.mcu.notification.Response;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;


import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity {

    private final String topic = "/topics/chat";
    private final String TAG = "-----MainActivity----";

    private static final String CHAT = "chat";
    private static final String HOME = "home";
    private static final String MEMBERS = "members";
    private static final String QUIZ = "quiz";
    private static final String CONTEST = "contest";
    private int notificationCount;
    private APIService apiService;

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;

    private CircleImageView civ_profileImage, civ_titleBsrProfileImage;
    private TextView tv_username, tv_email, titleBarTitle;

    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private DrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_main);

        init();
        //set custom action bar
        setSupportActionBar(toolbar);

        //setting drawer
        toggle = new DrawerToggle(MainActivity.this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setBadgeEnabled(false);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //load user data from server
        databaseReference.child("users").child(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            if (!(user.getProfileImageUrl() == null || user.getProfileImageUrl().equals("default"))) {
                                Glide.with(MainActivity.this).load(user.getProfileImageUrl()).into(civ_profileImage);
                                Glide.with(MainActivity.this).load(user.getProfileImageUrl()).into(civ_titleBsrProfileImage);
                            }
                            tv_username.setText(user.getUsername());
                            tv_email.setText(user.getEmail());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //start action
        getSupportFragmentManager().beginTransaction().replace(R.id.am_host_fragment, new Home(titleBarTitle), HOME).commit();
        navigationView.setCheckedItem(R.id.home);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        getSupportFragmentManager().beginTransaction().replace(R.id.am_host_fragment, new Home(titleBarTitle), HOME).commit();
                        break;
                    case R.id.chat:
                        getSupportFragmentManager().beginTransaction().replace(R.id.am_host_fragment, new Chat(titleBarTitle, toggle), CHAT).commit();
                        break;
                    case R.id.members:
                        getSupportFragmentManager().beginTransaction().replace(R.id.am_host_fragment, new Members(titleBarTitle), MEMBERS).commit();
                        break;
                    case R.id.quiz:
                        getSupportFragmentManager().beginTransaction().replace(R.id.am_host_fragment, new Quiz(titleBarTitle), QUIZ).commit();
                        break;
                    case R.id.share:
                    case R.id.about:
                        Toast.makeText(MainActivity.this, "On development", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.sign_out:
                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                        alertDialog.setMessage("Do you really want to log out?");
                        alertDialog.setCancelable(false);
                        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseAuth.getInstance().signOut();
                                status("offline");
                                Intent intent = new Intent(MainActivity.this, LogIn.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                        break;
                    case R.id.contest:
                        getSupportFragmentManager().beginTransaction().replace(R.id.am_host_fragment, new Contest(titleBarTitle), CONTEST).commit();
                        break;
                }
                if (drawerLayout.isDrawerOpen(GravityCompat.START))
                    drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        civ_titleBsrProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Settings.class);
                startActivity(intent);
            }
        });

        civ_profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Profile.class);
                intent.putExtra("uid", firebaseUser.getUid());
                startActivity(intent);
            }
        });

        databaseReference.child("chats").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                com.aam.mcu.data.Chat chat = snapshot.getValue(com.aam.mcu.data.Chat.class);
                assert chat != null;
                sendNotificationDot(snapshot, chat);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void init() {
        navigationView = findViewById(R.id.am_nav_drawer);
        View navHeader = navigationView.getHeaderView(0);

        civ_profileImage = navHeader.findViewById(R.id.nh_civ_profile_image);
        civ_titleBsrProfileImage = findViewById(R.id.am_title_civ_profile_image);
        tv_username = navHeader.findViewById(R.id.nh_tv_username);
        tv_email = navHeader.findViewById(R.id.nh_tv_email);
        titleBarTitle = findViewById(R.id.am_titleBar_title);

        drawerLayout = findViewById(R.id.am_drawer_layout);
        toolbar = findViewById(R.id.am_toolbar);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        apiService = APIService.retrofit.create(APIService.class);
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getVisibleFragment();
        assert fragment != null;
        assert fragment.getTag() != null;
        if (fragment.getTag().equals(HOME)) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setMessage("Do you really want to close this app?");
                alertDialog.setCancelable(true);
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        moveTaskToBack(true);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }
        } else if (fragment.getTag().equals(CHAT)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.am_host_fragment, new Home(titleBarTitle), HOME).commit();
            navigationView.setCheckedItem(R.id.home);
        } else if (fragment.getTag().equals(MEMBERS)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.am_host_fragment, new Home(titleBarTitle), HOME).commit();
            navigationView.setCheckedItem(R.id.home);
        } else if (fragment.getTag().equals(QUIZ)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.am_host_fragment, new Home(titleBarTitle), HOME).commit();
            navigationView.setCheckedItem(R.id.home);
        } else if (fragment.getTag().equals(CONTEST)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.am_host_fragment, new Home(titleBarTitle), HOME).commit();
            navigationView.setCheckedItem(R.id.home);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        status("active");
    }

    @Override
    protected void onPause() {
        super.onPause();

        status("offline");
    }


    private void status(String status) {
        databaseReference.child("users/" + firebaseUser.getUid() + "/status").setValue(status);
    }

    private Fragment getVisibleFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragments != null && fragment.isVisible()) {
                return fragment;
            }
        }
        return null;
    }

    private void sendNotificationDot(DataSnapshot snapshot, final com.aam.mcu.data.Chat chat) {
        Fragment fragment = getVisibleFragment();
        assert fragment != null;
        assert fragment.getTag() != null;
        if (!fragment.getTag().equals(CHAT)) {
            if (chat != null) {
                if (!chat.getUid().equals(firebaseUser.getUid())) {
                    databaseReference.child("chats").child(chat.getMessageId())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.hasChild("seen")) {
                                        if (!snapshot.child("seen").hasChild(firebaseUser.getUid())) {
                                            sendNotification("New message", chat.getMessage());

                                            if (!toggle.isBadgeEnabled()) {
                                                toggle.setBadgeEnabled(true);
                                            }
                                            if (toggle.getBadgeText() != null) {
                                                notificationCount = Integer.parseInt(toggle.getBadgeText());
                                                toggle.setBadgeText(String.valueOf(++notificationCount));
                                            } else {
                                                toggle.setBadgeText("1");
                                            }

                                        } else {
                                            if (toggle.isBadgeEnabled()) {
                                                toggle.setBadgeText("0");
                                                toggle.setBadgeEnabled(false);
                                            }
                                        }
                                    } else {
                                        sendNotification("New message", chat.getMessage());

                                        if (!toggle.isBadgeEnabled()) {
                                            toggle.setBadgeEnabled(true);
                                        }
                                        if (toggle.getBadgeText() != null) {
                                            notificationCount = Integer.parseInt(toggle.getBadgeText());
                                            notificationCount++;
                                            toggle.setBadgeText(String.valueOf(notificationCount));
                                        } else {
                                            toggle.setBadgeText("1");
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                } else {
                    if (toggle.isBadgeEnabled()) {
                        toggle.setBadgeText("0");
                        toggle.setBadgeEnabled(false);
                    }
                }
            } else {
                if (toggle.isBadgeEnabled()) {
                    toggle.setBadgeText("0");
                    toggle.setBadgeEnabled(false);
                }
            }
        }

    }

    private void sendNotification(final String title, final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Notification notification = new Notification(title, message);

                apiService.createNotification(new Message(notification, topic))
                        .enqueue(new Callback<Response>() {
                    @Override
                    public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                        if (!response.isSuccessful()){
                            try {
                                assert response.errorBody() != null;
                                Log.d(TAG, "onResponse: " + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Response> call, Throwable t) {
                        Log.d(TAG, "onFailure: " + t.getMessage());
                    }
                });
            }
        }).start();
    }
}