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
import com.aam.mcu.adapters.QuizRecycler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Quiz extends Fragment {

    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;

    private ArrayList<com.aam.mcu.data.Quiz> quizzes;
    private RecyclerView.Adapter<QuizRecycler.ViewHolder> adapter;

    RecyclerView recyclerView;

    public Quiz() {
    }

    public Quiz (TextView titleBarTitle) {
        titleBarTitle.setText("Quiz");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);
        recyclerView.setAdapter(adapter);

        databaseReference.child("quiz").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                quizzes.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    quizzes.add(dataSnapshot.getValue(com.aam.mcu.data.Quiz.class));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void init(View view) {
        recyclerView  = view.findViewById(R.id.fq_recycler);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        quizzes = new ArrayList<>();
        adapter = new QuizRecycler(getContext(), quizzes);
    }
}