package com.fussy.trueman;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ParentalControlActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parental_control);

        dbHelper = new DatabaseHelper(this);

        EditText etDomainBlock = findViewById(R.id.etDomainBlock);
        Button btnBlockDomain = findViewById(R.id.btnBlockDomain);

        btnBlockDomain.setOnClickListener(v -> {
            String domain = etDomainBlock.getText().toString().trim();
            if (!domain.isEmpty()) {
                dbHelper.addBlockedDomain(domain);
                Toast.makeText(this, "Domain '" + domain + "' added to block list.", Toast.LENGTH_SHORT).show();
                etDomainBlock.setText("");
            } else {
                Toast.makeText(this, "Please enter a valid domain to block.", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnEnableAccessibility = findViewById(R.id.btnEnableAccessibility);
        btnEnableAccessibility.setOnClickListener(v -> {
            Toast.makeText(this, "Please enable TrueMan Accessibility Service", Toast.LENGTH_LONG).show();
            android.content.Intent intent = new android.content.Intent(
                    android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });
    }
}
