package com.aam.mcu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Welcome extends AppCompatActivity {

    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        init();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                doWait();
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null && firebaseUser.isEmailVerified()){
                    Intent intent = new Intent(Welcome.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                else {
                    Intent intent = new Intent(Welcome.this, LogIn.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                finish();
            }
        });
        thread.start();
    }

    private void init() {
        progressBar = findViewById(R.id.aw_hz_progressbar);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void doWait() {
        for (int progress = 0; progress <= 100; progress += 30) {
            try {
                Thread.sleep(200);
                progressBar.setProgress(progress);
                if (progress == 30) progress += 10;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}