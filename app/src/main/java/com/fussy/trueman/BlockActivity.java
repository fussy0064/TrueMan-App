package com.fussy.trueman;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class BlockActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block);

        TextView tvBlockedUrl = findViewById(R.id.tvBlockedUrl);
        Button btnReturnHome = findViewById(R.id.btnReturnHome);

        String blockedUrl = getIntent().getStringExtra("blocked_url");
        if (blockedUrl != null) {
            tvBlockedUrl.setText("Site URL contains restricted word: " + blockedUrl);
        }

        btnReturnHome.setOnClickListener(v -> finish());
    }

    @Override
    public void onBackPressed() {
        // Prevent going back to the browser
        super.onBackPressed();
        finish();
    }
}
