package com.aam.mcu;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aam.mcu.notification.APIService;
import com.aam.mcu.notification.Message;
import com.aam.mcu.notification.Notification;
import com.aam.mcu.notification.Response;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.Objects;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import retrofit2.Call;
import retrofit2.Callback;

public class LogIn extends AppCompatActivity {

    private final String topic = "/topics/chat";
    private final String TAG = "----LogIn Activity----";

    private EditText et_email, et_password;
    private TextInputLayout emailParent, passwordParent;
    private CircularProgressButton btn_logIn;
    private TextView tv_forgotPassword, tv_register, tv_showMessage;

    private FirebaseAuth firebaseAuth;

    private String email, password;
    private APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_log_in);

        init();

        btn_logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tv_showMessage.isShown()) {
                    tv_showMessage.setVisibility(View.GONE);
                }

                email = et_email.getText().toString();
                password = et_password.getText().toString();
                et_password.setText("");

                if (!TextUtils.isEmpty(email)) {
                    if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        //show email is ok
                        showStatusOk(emailParent);
                        if (!TextUtils.isEmpty(password)) {
                            //show password is ok
                            showStatusOk(passwordParent);
                            //start logIn
                            logIn(email, password);
                        } else {
                            showError(passwordParent, "Please type your password");
                        }
                    } else {
                        showError(emailParent, "Invalid email");
                    }
                } else {
                    showError(emailParent, "Please give an email");
                }
            }
        });

        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogIn.this, Register.class);
                startActivity(intent);
            }
        });

        tv_forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogIn.this, ForgotPassword.class);
                startActivity(intent);
            }
        });
    }

    private void init() {
        et_email = findViewById(R.id.ali_et_email);
        et_password = findViewById(R.id.ali_et_password);
        tv_forgotPassword = findViewById(R.id.ali_tv_forgot_password);
        emailParent = findViewById(R.id.ali_et_email_parent);
        passwordParent = findViewById(R.id.ali_et_password_parent);
        btn_logIn = findViewById(R.id.ali_btn_Log_in);
        tv_register = findViewById(R.id.ali_tv_register);
        tv_showMessage = findViewById(R.id.ali_tv_show_message);

        firebaseAuth = FirebaseAuth.getInstance();

        apiService = APIService.retrofit.create(APIService.class);
    }

    private void showError(TextInputLayout layout, String message) {
        if (layout.isEndIconVisible()) {
            layout.setEndIconVisible(false);
        }
        layout.setError(message);
    }

    private void logIn(String email, String password) {

        if (!btn_logIn.isAnimating()){
            btn_logIn.startAnimation();
        }

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseMessaging.getInstance().subscribeToTopic("/topics/chat")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        Log.d("TAG", "onComplete: Topic subscribed");
                                    }
                                }
                            });
                    FirebaseDatabase.getInstance().getReference("users")
                            .child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid())
                            .child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            sendNotification("Welcome", "Welcome here " + snapshot.getValue());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                    //redirected to main activity if user is verified
                    if (firebaseAuth.getCurrentUser().isEmailVerified()){
                        Intent intent = new Intent(LogIn.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        //send an email to verify user account
                        firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                showInfo("Am email with link has been sent to your email." +
                                        "Please verify your account and then try again", Color.RED);

                                if (btn_logIn.isAnimating()) {
                                    btn_logIn.stopAnimation();
                                    btn_logIn.revertAnimation();
                                }
                            }
                        });
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (btn_logIn.isAnimating()) {
                    btn_logIn.stopAnimation();
                    btn_logIn.revertAnimation();
                }
                showInfo(e.getMessage(), Color.RED);
                tv_forgotPassword.setVisibility(View.VISIBLE);

                btn_logIn.setClickable(true);
            }
        });
    }

    private void showInfo(String message, int color) {
        if (!tv_showMessage.isShown()){
            tv_showMessage.setVisibility(View.VISIBLE);
        }
        tv_showMessage.setText(message);
        tv_showMessage.setTextColor(color);
    }

    private void showStatusOk(TextInputLayout layout) {
        if (layout.isErrorEnabled()) {
            layout.setErrorEnabled(false);
        }
        layout.setEndIconVisible(true);
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