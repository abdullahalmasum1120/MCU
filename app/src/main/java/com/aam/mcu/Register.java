package com.aam.mcu;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aam.mcu.extra.PasswordValidator;
import com.aam.mcu.services.ImageUploadService;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tooltip.Tooltip;

import java.util.HashMap;
import java.util.Objects;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import de.hdodenhof.circleimageview.CircleImageView;

public class Register extends AppCompatActivity {

    CircleImageView civ_addProfileImage;
    EditText et_username, et_email, et_rollNo, et_mobileNo, et_password;
    TextInputLayout usernameParent, emailParent, rollNoParent, mobileNoParent, passwordParent;
    CircularProgressButton btn_register;
    TextView tv_logIn, tv_showInfo;

    final int MCU_PROFILE_IMAGE_REQUEST = 1111;

    Uri profileImageUri;
    String username, email, rollNo, mobileNo, password;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    private String TAG = "--Register--";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        init();

        civ_addProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, MCU_PROFILE_IMAGE_REQUEST);
            }
        });

        tv_logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = et_username.getText().toString();
                email = et_email.getText().toString();
                rollNo = et_rollNo.getText().toString();
                mobileNo = et_mobileNo.getText().toString();
                password = et_password.getText().toString();

                if (!TextUtils.isEmpty(username.trim())) {
                    if (username.length() <= 20) {
                        showStatusOk(usernameParent);
                        if (!TextUtils.isEmpty(email.trim())) {
                            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                showStatusOk(emailParent);
                                if (!TextUtils.isEmpty(rollNo.trim())) {
                                    if ((Integer.parseInt(rollNo) <= 100) && (Integer.parseInt(rollNo) >= 1)) {
                                        showStatusOk(rollNoParent);
                                        if (!TextUtils.isEmpty(mobileNo.trim())) {
                                            if (Patterns.PHONE.matcher(mobileNo).matches()) {
                                                showStatusOk(mobileNoParent);
                                                if (!TextUtils.isEmpty(password.trim())) {
                                                    if (profileImageUri != null) {
                                                        checkPassword(password);
                                                    } else {
                                                        //select an image for profile photo
                                                        showTooltip("Click here to select an image for your profile", Color.RED, Color.WHITE, civ_addProfileImage);
                                                    }
                                                } else {
                                                    showError(passwordParent, "Type a password to create your account");
                                                }
                                            } else {
                                                showError(mobileNoParent, "You typed an invalid number");
                                            }
                                        } else {
                                            showError(mobileNoParent, "Type your mobile number here");
                                        }
                                    } else {
                                        showError(rollNoParent, "Must be between 1 to 100");
                                    }
                                } else {
                                    showError(rollNoParent, "Type your roll here");
                                }
                            } else {
                                showError(usernameParent, "Your email is invalid");
                            }
                        } else {
                            showError(emailParent, "Type your email here");
                        }
                    } else {
                        showError(usernameParent, "Very long name");
                    }
                } else {
                    showError(usernameParent, "Type your name here");
                }
            }
        });
    }

    private void checkPassword(String password) {
        PasswordValidator passwordValidator = new PasswordValidator();
        int error = passwordValidator.isValid(password);

        if (error == passwordValidator.LENGTH_ERROR) {
            showError(passwordParent, "length must be between 8 to 15");
        } else if (error == passwordValidator.SPACE_ERROR) {
            showError(passwordParent, "password can not contain space");
        } else if (error == passwordValidator.DIGIT_ERROR) {
            showError(passwordParent, "your password should contain at least one digit");
        } else if (error == passwordValidator.SPECIAL_CHAR_ERROR) {
            showError(passwordParent, "your password should contain at least one special character");
        } else if (error == passwordValidator.CAPITAL_LETTER_ERROR) {
            showError(passwordParent, "your password should contain at least one capital letter");
        } else if (error == passwordValidator.SMALL_LETTER_ERROR) {
            showError(passwordParent, "your password should contain at least one small letter");
        } else if (error == passwordValidator.NO_ERROR) {
            showStatusOk(passwordParent);
            createAccount(email, password);
        } else {
            showError(passwordParent, "unknown error occurred, try with another one");
        }
    }

    private void createAccount(final String email, final String password) {
        if (tv_showInfo.isShown()) {
            tv_showInfo.setVisibility(View.GONE);
        }

        if (!btn_register.isAnimating()) {
            btn_register.startAnimation();
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //subscribe for a topic
                    FirebaseMessaging.getInstance().subscribeToTopic("/topics/chat")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        Log.d(TAG, "onComplete: Topic subscribed");
                                    }
                                }
                            });
                    //sign up success
                    //upload data to firebase
                    uploadProfileImg(profileImageUri);
                    uploadData(username, email, rollNo, mobileNo, password, Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showInfo(e.getMessage(), Color.RED);

                if (btn_register.isAnimating()) {
                    btn_register.stopAnimation();
                    btn_register.revertAnimation();
                }

                btn_register.setError(e.getMessage());
            }
        });
    }

    private void uploadData(String username, String email, String rollNo, String mobileNo, String password, String uid) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("username", username);
        hashMap.put("email", email);
        hashMap.put("rollNo", rollNo);
        hashMap.put("phoneNo", mobileNo);
        hashMap.put("profileImageUrl", "default");
        hashMap.put("userId", uid);
        hashMap.put("password", password);

        databaseReference.child("users").child(uid).setValue(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        firebaseAuth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                if (btn_register.isAnimating()) {
                                    btn_register.stopAnimation();
                                }

                                showInfo("A verification Email has been sent to your email,\n" +
                                        "Please verify your email", Color.GREEN);

                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(5 * 1000);

                                            Intent intent = new Intent(Register.this, LogIn.class);
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
                                if (btn_register.isAnimating()) {
                                    btn_register.stopAnimation();
                                    btn_register.revertAnimation();
                                }

                                showInfo(e.getMessage(), Color.RED);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (btn_register.isAnimating()) {
                    btn_register.stopAnimation();
                    btn_register.revertAnimation();
                }
                showInfo(e.getMessage(), Color.RED);
            }
        });
    }

    private void init() {
        civ_addProfileImage = findViewById(R.id.ar_add_profileImg);

        et_email = findViewById(R.id.ar_et_email);
        et_username = findViewById(R.id.ar_et_username);
        et_rollNo = findViewById(R.id.ar_et_roll);
        et_mobileNo = findViewById(R.id.ar_et_mobile);
        et_password = findViewById(R.id.ar_et_password);

        usernameParent = findViewById(R.id.ar_et_username_parent);
        emailParent = findViewById(R.id.ar_et_email_parent);
        rollNoParent = findViewById(R.id.ar_et_roll_parent);
        mobileNoParent = findViewById(R.id.ar_et_mobile_parent);
        passwordParent = findViewById(R.id.ar_et_password_parent);

        btn_register = findViewById(R.id.ar_btn_register);
        tv_logIn = findViewById(R.id.ar_tv_login);
        tv_showInfo = findViewById(R.id.ar_show_info);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == MCU_PROFILE_IMAGE_REQUEST) {
            profileImageUri = data.getData();

            if (profileImageUri != null) {
                Glide.with(this).load(profileImageUri).into(civ_addProfileImage);
            }
        }
    }

    private void showError(TextInputLayout layout, String message) {
        if (layout.isEndIconVisible()) {
            layout.setEndIconVisible(false);
        }
        layout.setError(message);
    }

    private void showStatusOk(TextInputLayout layout) {
        if (layout.isErrorEnabled()) {
            layout.setErrorEnabled(false);
        }
//        layout.setEndIconDrawable(R.drawable.ic_ok);
        layout.setEndIconVisible(true);
    }

    private void showInfo(String message, int color) {
        if (!tv_showInfo.isShown()) {
            tv_showInfo.setVisibility(View.VISIBLE);
        }
        tv_showInfo.setText(message);
        tv_showInfo.setTextColor(color);
    }

    private void showTooltip(String message, int backgroundColor, int textColor, View view) {
        Tooltip.Builder builder = new Tooltip.Builder(view)
                .setCancelable(true)
                .setTextColor(textColor)
                .setBackgroundColor(backgroundColor)
                .setDismissOnClick(true)
                .setGravity(Gravity.TOP)
                .setText(message);
        builder.show();
    }

    private void uploadProfileImg(Uri profileImageUri) {
        String uid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        String dbRef = "users/" + uid;
        String stRef = "profilePhotos/" + uid + "."
                + getFileExtension(profileImageUri);

        Intent intent = new Intent(Register.this, ImageUploadService.class);
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