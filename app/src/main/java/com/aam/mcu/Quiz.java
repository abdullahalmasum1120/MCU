package com.aam.mcu;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aam.mcu.dialogues.ProgressBar;

import de.hdodenhof.circleimageview.CircleImageView;

public class Quiz extends AppCompatActivity {

    private WebView webView;
    private CircleImageView civ_close, civ_reload;

    private String url;

    private com.aam.mcu.dialogues.ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_quiz);

        url = getIntent().getStringExtra("url");

        init();

        if (url != null) {
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setLoadsImagesAutomatically(true);

            webView.setWebChromeClient(new WebChromeClient() {
                public void onProgressChanged(WebView view, int progress) {
                    progressBar.setProgress(progress + " %");
                    progressBar.show();
                    if (progress == 100) {
                        progressBar.dismiss();
                    }
                }
            });

            webView.loadUrl(url);
            webView.setWebViewClient(new WebViewClient());
        }

        civ_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        civ_reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        });
    }

    private void init() {
        webView = findViewById(R.id.q_webView);
        civ_close = findViewById(R.id.aq_civ_close);
        civ_reload = findViewById(R.id.aq_civ_reload);

        progressBar = new ProgressBar(Quiz.this);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else
            super.onBackPressed();
    }
}