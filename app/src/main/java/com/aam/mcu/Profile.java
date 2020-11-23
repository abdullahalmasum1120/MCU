package com.aam.mcu;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aam.mcu.data.User;
import com.aam.mcu.dialogues.EditProfile;
import com.aam.mcu.services.ImageUploadService;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tooltip.Tooltip;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    private static final String ACTIVE = "active";
    private static final String OFFLINE = "offline";
    private final int MCU_PROFILE_IMAGE_REQUEST = 123;

    TextView tvRoll, tvName, tvPhone, tvEmail;
    CircleImageView civ_ProfileImg, civ_add_profile_image;
    ImageView iv_back, iv_edit_profile;
    View active_status;
    FloatingActionButton fab_update;

    String uid;
    DatabaseReference databaseReference;
    FirebaseUser firebaseUser;
    User user;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_profile);

        uid = getIntent().getStringExtra("uid");

        init();

        if (Objects.equals(getIntent().getStringExtra("editProfile"), "editableProfile")){
            if (!iv_edit_profile.isShown()){
                iv_edit_profile.setVisibility(View.VISIBLE);
            } else {
                iv_edit_profile.setVisibility(View.GONE);
            }
            if (!civ_add_profile_image.isShown()){
                civ_add_profile_image.setVisibility(View.VISIBLE);
            } else {
                civ_add_profile_image.setVisibility(View.GONE);
            }
        }

        //show info of profile
        if (uid != null) {
            databaseReference.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    user = snapshot.getValue(User.class);

                    if (!(user.getProfileImageUrl() == null || user.getProfileImageUrl().equals("default"))) {
                        Glide.with(Profile.this).load(user.getProfileImageUrl()).into(civ_ProfileImg);
                    }

                    tvName.setText(user.getUsername());

                    tvRoll.setText("Roll : " + user.getRollNo());
                    tvPhone.setText("Phone : " + user.getPhoneNo());
                    tvEmail.setText("E-mail : " + user.getEmail());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            civ_ProfileImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFullImage(Uri.parse(user.getProfileImageUrl()));
                }
            });
        }

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        civ_add_profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, MCU_PROFILE_IMAGE_REQUEST);
            }
        });

        iv_edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditProfile editProfile = new EditProfile(Profile.this);
                editProfile.show();
            }
        });

        fab_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uri != null){
                    uploadProfileImg(uri);
                    if (fab_update.isShown()){
                        fab_update.setVisibility(View.GONE);
                    }
                }
            }
        });

        if (!uid.equals(firebaseUser.getUid())) {
            if (civ_add_profile_image.isShown()){
                civ_add_profile_image.setVisibility(View.GONE);
            }
            databaseReference.child("users").child(uid).child("status")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                if (snapshot.getValue().equals(ACTIVE)) {
                                    if (!active_status.isShown()) {
                                        active_status.setVisibility(View.VISIBLE);
                                    }
                                } else if (snapshot.getValue().equals(OFFLINE)) {
                                    if (active_status.isShown()) {
                                        active_status.setVisibility(View.GONE);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

    private void init() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        civ_ProfileImg = findViewById(R.id.p_civ_profile_image);
        civ_add_profile_image = findViewById(R.id.ap_add_profile_Image);
        tvName = findViewById(R.id.p_tv_username);

        tvRoll = findViewById(R.id.p_tv_roll);
        tvPhone = findViewById(R.id.p_tv_phone);
        tvEmail = findViewById(R.id.p_tv_email);
        iv_back = findViewById(R.id.p_iv_back);
        iv_edit_profile = findViewById(R.id.ap_iv_edit_profile);

        active_status = findViewById(R.id.p_active_status);

        fab_update = findViewById(R.id.ap_fab_update);
    }

    private void showFullImage(Uri uri) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == MCU_PROFILE_IMAGE_REQUEST){
            uri = data.getData();

            if (uri != null){
                Glide.with(Profile.this).load(uri).into(civ_ProfileImg);

                if (!fab_update.isShown()){
                    fab_update.setVisibility(View.VISIBLE);
                }
                Tooltip.Builder builder = new Tooltip.Builder(fab_update)
                        .setCancelable(true)
                        .setDismissOnClick(true)
                        .setGravity(Gravity.TOP)
                        .setArrowHeight(50f)
                        .setTextColor(Color.WHITE)
                        .setText("Click here to save changes");
                builder.show();
            }
        }
    }

    private void uploadProfileImg(Uri profileImageUri) {
        String uid = firebaseUser.getUid();

        String dbRef = "users/" + uid;
        String stRef = "profilePhotos/" + uid + "."
                + getFileExtension(profileImageUri);

        Intent intent = new Intent(Profile.this, ImageUploadService.class);
        intent.putExtra("url", profileImageUri.toString());
        intent.putExtra("databaseRef", dbRef + "/profileImageUrl");
        intent.putExtra("storageRef", stRef);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}