package com.fussy.trueman;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;

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

        Button btnEnableDeviceAdmin = findViewById(R.id.btnEnableDeviceAdmin);
        btnEnableDeviceAdmin.setOnClickListener(v -> {
            ComponentName compName = new ComponentName(this, TrueManDeviceAdminReceiver.class);
            android.content.Intent intent = new android.content.Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Enabling TrueMan as Device Administrator will prevent users from uninstalling the application or modifying VPN settings to bypass the safety filters.");
            startActivity(intent);
        });

        Button btnEnableVpn = findViewById(R.id.btnEnableVpn);
        btnEnableVpn.setOnClickListener(v -> {
            android.content.Intent vpnIntent = android.net.VpnService.prepare(this);
            if (vpnIntent != null) {
                startActivityForResult(vpnIntent, 0);
            } else {
                onActivityResult(0, RESULT_OK, null);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            startService(new android.content.Intent(this, TrueManVpnService.class));
            Toast.makeText(this, "Safe DNS VPN Activated. Other VPNs are now blocked.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed to start VPN", Toast.LENGTH_SHORT).show();
        }
    }
}
