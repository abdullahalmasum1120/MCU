package com.aam.mcu;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

public class ForgotPassword extends AppCompatActivity {

    EditText et_email;
    CircularProgressButton btn_sendLink;
    TextInputLayout et_emailParent;
    TextView tv_showTips;

    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_forgot_password);

        init();

        btn_sendLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = et_email.getText().toString();
                //set to default tips
                tv_showTips.setText("we will email you a link to reset your password");
                tv_showTips.setTextColor(getColor(R.color.grayText));

                if (!TextUtils.isEmpty(email)) {
                    if (!btn_sendLink.isAnimating()) {
                        btn_sendLink.startAnimation();
                        btn_sendLink.setClickable(false);
                    }

                    FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            showTips("An Email will be sent soon\n" +
                                    "follow the email to reset your password", Color.GREEN);

                            if (btn_sendLink.isAnimating()) {
                                btn_sendLink.stopAnimation();
                            }

                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(5 * 1000);
                                        FirebaseAuth.getInstance().signOut();
                                        Intent intent = new Intent(ForgotPassword.this, LogIn.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            thread.start();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showTips(e.getMessage(), Color.RED);
                            if (!btn_sendLink.isClickable()) {
                                btn_sendLink.setClickable(true);
                            }

                            if (btn_sendLink.isAnimating()) {
                                btn_sendLink.stopAnimation();
                                btn_sendLink.revertAnimation();
                            }
                        }
                    });
                } else {
                    showError(et_emailParent, "Please type your email");
                }
            }
        });
    }

    private void init() {
        et_email = findViewById(R.id.afp_et_email);
        et_emailParent = findViewById(R.id.afp_et_email_parent);
        btn_sendLink = findViewById(R.id.afp_btn_send_link);
        tv_showTips = findViewById(R.id.afp_tv_tips);
    }

    private void showError(TextInputLayout layout, String message) {
        if (layout.isEndIconVisible()) {
            layout.setEndIconVisible(false);
        }
        layout.setError(message);
    }

    private void showTips(String message, int color) {
        if (!tv_showTips.isShown()) {
            tv_showTips.setVisibility(View.VISIBLE);
        }
        tv_showTips.setText(message);
        tv_showTips.setTextColor(color);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}