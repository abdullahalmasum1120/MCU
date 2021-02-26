package com.aam.mcu.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.aam.mcu.R;
import com.aam.mcu.adapters.MemberRecycler;
import com.aam.mcu.data.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Members extends Fragment {

    private RecyclerView recyclerView;

    private ArrayList<User> users;
    private DatabaseReference databaseReference;
    private RecyclerView.Adapter adapter;

    public Members() {

    }

    public Members (TextView titleBarTitle) {
        titleBarTitle.setText("Members");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_members, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);
        recyclerView.setAdapter(adapter);

        databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                    users.add(dataSnapshot1.getValue(User.class));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void init(View view) {
        recyclerView = view.findViewById(R.id.member_recycler_view);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        users = new ArrayList<>();
        adapter = new MemberRecycler(getContext(), users);
    }
}