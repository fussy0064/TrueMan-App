package com.fussy.trueman;

import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class BrowserActivity extends AppCompatActivity {

    private WebView webView;
    private EditText etUrl;
    private DatabaseHelper dbHelper;
    private List<String> blockedSites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        dbHelper = new DatabaseHelper(this);
        blockedSites = dbHelper.getAllBlockedDomains();

        webView = findViewById(R.id.webView);
        etUrl = findViewById(R.id.etUrl);
        Button btnGo = findViewById(R.id.btnGo);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new SafeWebViewClient());

        btnGo.setOnClickListener(v -> {
            String url = etUrl.getText().toString();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            webView.loadUrl(url);
        });

        // Load a default safe page
        webView.loadUrl("https://www.wikipedia.org");
    }

    private class SafeWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();

            for (String word : blockedSites) {
                if (url.toLowerCase().contains(word.toLowerCase())) {
                    Toast.makeText(getApplicationContext(),
                            "This website is blocked for safety reasons", Toast.LENGTH_LONG).show();
                    return true; // Cancel the load
                }
            }
            return false; // Proceed with normal loading
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            for (String word : blockedSites) {
                if (url.toLowerCase().contains(word.toLowerCase())) {
                    Toast.makeText(getApplicationContext(),
                            "This website is blocked for safety reasons", Toast.LENGTH_LONG).show();
                    return true; // Cancel the load
                }
            }
            return false; // Proceed with normal loading
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
