package com.aam.mcu.dialogues;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.aam.mcu.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

public class EditProfile extends Dialog {

    private EditText et_phoneNo;
    private TextInputLayout phoneNoParent;
    private ImageView iv_close;
    private CircularProgressButton btn_save;

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;

    public EditProfile(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_edit_profile);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCancelable(false);

        init();

        databaseReference.child("users/" + firebaseUser.getUid() + "/phoneNo")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            et_phoneNo.setText(snapshot.getValue(String.class));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNo = et_phoneNo.getText().toString();

                if (!TextUtils.isEmpty(phoneNo.trim())) {
                    if (Patterns.PHONE.matcher(phoneNo).matches()) {
                        if (!btn_save.isAnimating()){
                            btn_save.startAnimation();
                        }

                        databaseReference.child("users/" + firebaseUser.getUid() + "/phoneNo").setValue(phoneNo).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (btn_save.isAnimating()){
                                    btn_save.stopAnimation();
                                }
                                Toast.makeText(getContext(), "saved", Toast.LENGTH_SHORT).show();
                                dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                if (btn_save.isAnimating()){
                                    btn_save.stopAnimation();
                                    btn_save.revertAnimation();
                                }
                                phoneNoParent.setError(e.getMessage());
                            }
                        });
                    } else {
                        phoneNoParent.setError("Enter a valid mobile number");
                        if (btn_save.isAnimating()){
                            btn_save.stopAnimation();
                            btn_save.revertAnimation();
                        }
                    }
                } else {
                    phoneNoParent.setError("Enter your mobile number");
                    if (btn_save.isAnimating()){
                        btn_save.stopAnimation();
                        btn_save.revertAnimation();
                    }
                }
            }
        });

        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void init() {
        et_phoneNo = findViewById(R.id.dep_et_phone);
        phoneNoParent = findViewById(R.id.dep_et_phone_parent);
        iv_close = findViewById(R.id.dep_iv_close);
        btn_save = findViewById(R.id.dep_btn_save);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }
}
