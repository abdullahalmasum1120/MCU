package com.aam.mcu;

import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.aam.mcu.adapters.CommentRecycler;
import com.aam.mcu.data.Comment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class Comments extends AppCompatActivity {

    private ArrayList<Comment> comments;
    private HashMap<String, String> hashMap;
    private SimpleDateFormat simpleDateFormat;

    private RecyclerView recyclerView;
    private ImageView iv_back, iv_send;
    private EditText et_comment;

    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;

    private String postId;
    private RecyclerView.Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_comments);

        postId = getIntent().getStringExtra("postId");

        init();

        databaseReference.child("posts/" + postId).child("comments").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                comments.add(0, snapshot.getValue(Comment.class));

                adapter.notifyItemInserted(0);
                recyclerView.scrollToPosition(0);
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

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        iv_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = et_comment.getText().toString();
                et_comment.setText("");

                if (!comment.trim().isEmpty()) {
                    String commentId = System.currentTimeMillis() + "";
                    databaseReference.child("users/" + firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            hashMap.put("username", snapshot.child("username").getValue().toString());
                            hashMap.put("profileImageUrl", snapshot.child("profileImageUrl").getValue().toString());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    hashMap.put("commentTime", simpleDateFormat.format(Calendar.getInstance().getTime()));
                    hashMap.put("comment", comment);
                    hashMap.put("uid", firebaseUser.getUid());
                    hashMap.put("commentId", commentId);

                    databaseReference.child("posts/" + postId + "/comments")
                            .child(commentId).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(Comments.this, "comment added", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Toast.makeText(Comments.this, "comment can't be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void init() {
        recyclerView = findViewById(R.id.comment_recycler);

        hashMap = new HashMap<>();
        comments = new ArrayList<>();
        simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy 'at' hh:mm:ss", Locale.getDefault());

        iv_back = findViewById(R.id.ac_iv_back);
        iv_send = findViewById(R.id.ac_btn_add_comment);
        et_comment = findViewById(R.id.ac_et_comment);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        adapter = new CommentRecycler(Comments.this, comments);
        recyclerView.setAdapter(adapter);
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