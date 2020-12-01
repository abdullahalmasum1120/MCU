package com.aam.mcu.dialogues;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.aam.mcu.R;

public class ProgressBar extends Dialog {

    private android.widget.ProgressBar progressBar;
    TextView tv_progress;

    public ProgressBar(@NonNull Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_progress_bar);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCancelable(false);

        init();
    }

    public void setProgress(String progress){
        tv_progress.setText(progress);
    }

    public int getProgress(){
        return Integer.parseInt(tv_progress.getText().toString());
    }

    private void init() {
        tv_progress = findViewById(R.id.dpb_tv_progress);
        progressBar = findViewById(R.id.dpb_progressBar);
    }
}
