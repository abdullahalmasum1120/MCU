package com.aam.mcu.dialogues;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.aam.mcu.ForgotPassword;
import com.aam.mcu.R;
import com.aam.mcu.data.User;
import com.aam.mcu.extra.PasswordValidator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

public class ChangePassword extends Dialog {

    private EditText et_oldPassword, et_firstPassword, et_secondPassword;
    private ImageView btn_cancel;
    private CircularProgressButton btn_change;
    private TextView tv_forgotPassword;
    private TextInputLayout old_passwordParent, first_passwordParent, second_passwordParent;

    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private User user;

    private String oldPass, firstPass, secondPass;

    public ChangePassword(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialoge_password_reset);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCancelable(false);

        init();

        databaseReference.child("users/" + firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldPass = et_oldPassword.getText().toString();
                firstPass = et_firstPassword.getText().toString();
                secondPass = et_secondPassword.getText().toString();

                first_passwordParent.setErrorEnabled(false);
                second_passwordParent.setErrorEnabled(false);
                old_passwordParent.setErrorEnabled(false);


                if (!TextUtils.isEmpty(oldPass)) {
                    if (oldPass.equals(user.getPassword())) {
                        if (!TextUtils.isEmpty(firstPass)) {
                            if (!TextUtils.isEmpty(secondPass)) {
                                if (firstPass.length() > 5) {
                                    if (firstPass.equals(secondPass)) {
                                        checkPassword(firstPass);
                                    } else {
                                        second_passwordParent.setError("Password does not matched, please type carefully");
                                    }
                                } else {
                                    //length short
                                    first_passwordParent.setError("Your password should contain at least 6 character");
                                }
                            } else {
                                //empty second pass
                                second_passwordParent.setError("Please type your new password again");
                            }
                        } else {
                            //empty first pass
                            first_passwordParent.setError("Please give your new password");
                        }
                    } else {
                        //not matched with previous
                        et_oldPassword.setError("Wrong password, please try again");
                    }
                } else {
                    //empty old Password
                    et_oldPassword.setError("Please Give your Old password");
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        tv_forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ForgotPassword.class);
                getContext().startActivity(intent);
                dismiss();
            }
        });
    }

    private void changePassword(final String password) {
        if (!btn_change.isAnimating()) {
            btn_change.startAnimation();
        }
        firebaseUser.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                databaseReference.child("users/" + firebaseUser.getUid()).child("password")
                        .setValue(password);

                if (btn_change.isAnimating()) {
                    btn_change.stopAnimation();
                    btn_change.revertAnimation();
                }
                Toast.makeText(getContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
    }

    private void init() {
        et_oldPassword = findViewById(R.id.dpr_et_old_password);
        et_firstPassword = findViewById(R.id.dpr_et_new_password_first);
        et_secondPassword = findViewById(R.id.dpr_et_new_password_second);
        btn_cancel = findViewById(R.id.dpr_btn_cancel);
        btn_change = findViewById(R.id.dpr_btn_change);
        tv_forgotPassword = findViewById(R.id.dpr_tv_forgot_password);

        old_passwordParent = findViewById(R.id.dpr_et_old_password_parent);
        first_passwordParent = findViewById(R.id.dpr_et_new_password_first_parent);
        second_passwordParent = findViewById(R.id.dpr_et_new_password_second_parent);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void checkPassword(String password) {
        PasswordValidator passwordValidator = new PasswordValidator();
        int error = passwordValidator.isValid(password);

        if (error == passwordValidator.LENGTH_ERROR) {
            first_passwordParent.setError("length must be between 8 to 15");
            second_passwordParent.setError("length must be between 8 to 15");
        } else if (error == passwordValidator.SPACE_ERROR) {
            first_passwordParent.setError("password can not contain space");
            second_passwordParent.setError("password can not contain space");
        } else if (error == passwordValidator.DIGIT_ERROR) {
            first_passwordParent.setError("your password should contain at least one digit");
            second_passwordParent.setError("your password should contain at least one digit");
        } else if (error == passwordValidator.SPECIAL_CHAR_ERROR) {
            first_passwordParent.setError("your password should contain at least one special character");
            second_passwordParent.setError("your password should contain at least one special character");
        } else if (error == passwordValidator.CAPITAL_LETTER_ERROR) {
            first_passwordParent.setError("your password should contain at least one capital letter");
            second_passwordParent.setError("your password should contain at least one capital letter");
        } else if (error == passwordValidator.SMALL_LETTER_ERROR) {
            first_passwordParent.setError("your password should contain at least one small letter");
            second_passwordParent.setError("your password should contain at least one small letter");
        } else if (error == passwordValidator.NO_ERROR) {
            changePassword(password);
        } else {
            first_passwordParent.setError("unknown error occurred, try with another one");
            second_passwordParent.setError("unknown error occurred, try with another one");
        }
    }
}
