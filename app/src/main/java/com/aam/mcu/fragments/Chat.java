package com.aam.mcu.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.aam.mcu.R;
import com.aam.mcu.adapters.ChatRecycler;
import com.aam.mcu.data.User;
import com.aam.mcu.extra.DrawerToggle;
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

public class Chat extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<com.aam.mcu.data.Chat> chats;

    private ImageView btnSend;
    private EditText et_message;
    private RecyclerView.Adapter adapter;

    private DatabaseReference databaseReference;
    private HashMap<String, String> hashMap;
    private String username, userId, userProfileImageUrl;
    private SimpleDateFormat simpleDateFormat;

    private FirebaseUser firebaseUser;
    private DrawerToggle toggle;

    public Chat() {

    }

    public Chat(TextView titleBarTitle, DrawerToggle toggle) {
        titleBarTitle.setText("Chat");
        this.toggle = toggle;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        databaseReference.child("chats").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                com.aam.mcu.data.Chat chat = snapshot.getValue(com.aam.mcu.data.Chat.class);
                chats.add(0, chat);

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

        databaseReference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            username = user.getUsername();
                            userId = user.getUserId();
                            userProfileImageUrl = user.getProfileImageUrl();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = et_message.getText().toString().trim();
                String messageId = System.currentTimeMillis() + "";

                if (!message.isEmpty()) {
                    hashMap.put("message", message);
                    hashMap.put("uid", userId);
                    hashMap.put("username", username);
                    hashMap.put("profileImageUrl", userProfileImageUrl);
                    hashMap.put("sendingTime", simpleDateFormat.format(Calendar.getInstance().getTime()));
                    hashMap.put("messageId", messageId);

                    databaseReference.child("chats").child(messageId).setValue(hashMap);
                } else {
                    Toast.makeText(getContext(), "Message can not be empty", Toast.LENGTH_SHORT).show();
                }

                et_message.setText("");
            }
        });
    }

    private void init(View view) {
        databaseReference = FirebaseDatabase.getInstance().getReference();

        recyclerView = view.findViewById(R.id.chat_recycler);
        et_message = view.findViewById(R.id.fc_et_chat);
        btnSend = view.findViewById(R.id.fc_btn_send_message);

        hashMap = new HashMap<>();
        chats = new ArrayList<>();
        simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy 'at' hh:mm:ss", Locale.getDefault());

        adapter = new ChatRecycler(getContext(), chats, toggle);
        recyclerView.setAdapter(adapter);
    }
}