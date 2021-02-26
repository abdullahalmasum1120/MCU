package com.aam.mcu;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aam.mcu.services.ImageUploadService;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class AddPost extends AppCompatActivity {

    private final int ADD_IMAGE_REQUEST = 2;

    private ImageView iv_post_image;
    private EditText et_post_text;
    private Button btn_add_post;

    private String postText;
    private Uri postImageUri;
    private HashMap<String, String> hashMap;
    private SimpleDateFormat simpleDateFormat;

    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_add_post);

        init();

        iv_post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, ADD_IMAGE_REQUEST);
            }
        });

        btn_add_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postText = et_post_text.getText().toString();

                String postId = String.valueOf(System.currentTimeMillis());
                String dbRef = "posts/" + postId;

                if ((!TextUtils.isEmpty(postText)) || (postImageUri != null)) {
                    if (postImageUri != null) {
                        String stRef = "posts/" + firebaseUser.getUid() + "/"
                                + System.currentTimeMillis() + "/"
                                + postImageUri.getLastPathSegment();

                        Intent intent = new Intent(AddPost.this, ImageUploadService.class);
                        intent.putExtra("url", postImageUri.toString());
                        intent.putExtra("databaseRef", dbRef + "/postImageUrl");
                        intent.putExtra("storageRef", stRef);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(intent);
                        } else
                            startService(intent);
                        ////
                    }

                    hashMap.put("postTime", simpleDateFormat.format(Calendar.getInstance().getTime()));
                    if (!postText.isEmpty()) {
                        hashMap.put("postBody", postText);
                    }
                    if (postImageUri != null){
                        hashMap.put("postImageUrl", "default");
                    }
                    hashMap.put("postId", postId);
                    hashMap.put("uid", firebaseUser.getUid());

                    databaseReference.child(dbRef)
                            .setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            startActivity(new Intent(AddPost.this, MainActivity.class));
                            finish();
                        }
                    });
                } else {
                    Toast.makeText(AddPost.this, "Please type something or add an image to post", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void init() {
        hashMap = new HashMap<>();
        simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy 'at' hh:mm:ss", Locale.getDefault());

        iv_post_image = findViewById(R.id.add_post_image);
        et_post_text = findViewById(R.id.add_post_text);
        btn_add_post = findViewById(R.id.btn_add_post);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_IMAGE_REQUEST && resultCode == RESULT_OK) {
            postImageUri = data.getData();
            if (postImageUri != null) {
                Glide.with(this).load(postImageUri).into(iv_post_image);
            }
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
        databaseReference.child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/status").setValue(status);
    }
}