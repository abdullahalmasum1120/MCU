package com.aam.mcu;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aam.mcu.data.User;
import com.aam.mcu.dialogues.ChangePassword;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class Settings extends AppCompatActivity {
    private CircleImageView iv_profileImage;
    private TextView tv_username, tv_userPhone, tv_logout, tv_changePassword, tv_editProfile, tv_language;
    private ImageView iv_back;
    private RelativeLayout iv_goProfile;

    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_settings);

        init();
        //set user info
        databaseReference.child("users/" + firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                if (!user.getProfileImageUrl().equals("default")) {
                    Glide.with(Settings.this).load(user.getProfileImageUrl()).into(iv_profileImage);
                }
                tv_username.setText(user.getUsername());
                tv_userPhone.setText(user.getPhoneNo());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        iv_goProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.this, Profile.class);
                intent.putExtra("uid", firebaseUser.getUid());
                startActivity(intent);
            }
        });

        tv_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Settings.this);
                alertDialog.setMessage("Do you really want to log out?");
                alertDialog.setCancelable(false);
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        status("offline");
                        Intent intent = new Intent(Settings.this, LogIn.class);
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
            }
        });

        tv_changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangePassword changePassword = new ChangePassword(Settings.this);
                changePassword.show();
            }
        });

        tv_editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.this, Profile.class);
                intent.putExtra("uid", firebaseUser.getUid());
                intent.putExtra("editProfile", "editableProfile");
                startActivity(intent);
            }
        });
        tv_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Settings.this, "On development", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void init() {
        iv_profileImage = findViewById(R.id.profileCircleImageView);
        tv_username = findViewById(R.id.usernameTextView);
        tv_userPhone = findViewById(R.id.s_tv_phone);
        iv_goProfile = findViewById(R.id.iv_go_profile);
        tv_logout = findViewById(R.id.s_tv_logout);
        tv_changePassword = findViewById(R.id.s_tv_change_password);
        tv_editProfile = findViewById(R.id.as_tv_edit_profile);
        iv_back = findViewById(R.id.as_iv_back);
        tv_language = findViewById(R.id.s_tv_language);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
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
}