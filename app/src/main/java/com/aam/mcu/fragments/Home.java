package com.aam.mcu.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aam.mcu.AddPost;
import com.aam.mcu.R;
import com.aam.mcu.adapters.HomeRecycler;
import com.aam.mcu.data.Post;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class Home extends Fragment {

    RecyclerView recyclerView;
    FloatingActionButton fab;

    ArrayList<Post> posts;
    ArrayList<String> keys;
    RecyclerView.Adapter adapter;
    LinearLayoutManager linearLayoutManager;

    DatabaseReference databaseReference;

    public Home() {

    }

    public Home(TextView titleBarTitle) {
        titleBarTitle.setText("Home");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        databaseReference.child("posts").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                posts.add(0, snapshot.getValue(Post.class));
                keys.add(0, snapshot.getKey());

                adapter.notifyItemInserted(0);
                recyclerView.scrollToPosition(0);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                int position = keys.indexOf(snapshot.getKey());

                posts.remove(position);
                keys.remove(position);

                adapter.notifyItemRemoved(position);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), AddPost.class));
            }
        });
    }

    private void init(View view) {
        recyclerView = view.findViewById(R.id.fh_recycler_view);
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        fab = view.findViewById(R.id.fh_fab);

        posts = new ArrayList<>();
        keys = new ArrayList<>();
        posts.clear();

        databaseReference = FirebaseDatabase.getInstance().getReference();

        adapter = new HomeRecycler(getContext(), posts);
        recyclerView.setAdapter(adapter);
    }
}