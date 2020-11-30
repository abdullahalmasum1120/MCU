package com.aam.mcu.dialogues;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.aam.mcu.R;
import com.aam.mcu.data.Post;
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

public class EditPost extends Dialog {
    private EditText et_postBody;
    private TextInputLayout postBodyParent;
    private ImageView iv_close;
    private CircularProgressButton btn_save;
    private DatabaseReference databaseReference;

    public EditPost(@NonNull final Context context, final Post post) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_edit_post);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCancelable(false);

        init();

        databaseReference.child("posts/" + post.getPostId() + "/postBody")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            et_postBody.setText(snapshot.getValue(String.class));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postBodyParent.setErrorEnabled(false);
                String postBody = et_postBody.getText().toString();

                if (!TextUtils.isEmpty(postBody.trim())){
                    //save postBody
                    if (!btn_save.isAnimating()){
                        btn_save.startAnimation();
                    }
                    databaseReference.child("posts/" + post.getPostId() + "/postBody").setValue(postBody)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                if (btn_save.isAnimating()){
                                    btn_save.stopAnimation();
                                }
                                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
                                dismiss();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            postBodyParent.setError(e.getMessage());
                            if (btn_save.isAnimating()){
                                btn_save.stopAnimation();
                                btn_save.revertAnimation();
                            }
                        }
                    });
                } else {
                    postBodyParent.setError("Please type something first");
                    if (btn_save.isAnimating()){
                        btn_save.stopAnimation();
                        btn_save.revertAnimation();
                    }
                }
            }
        });
    }

    private void init() {
        et_postBody = findViewById(R.id.dePost_et_postBody);
        postBodyParent = findViewById(R.id.dePost_et_postBody_parent);
        iv_close = findViewById(R.id.dePost_iv_close);
        btn_save = findViewById(R.id.dePost_btn_save);

        databaseReference = FirebaseDatabase.getInstance().getReference();
    }
}
