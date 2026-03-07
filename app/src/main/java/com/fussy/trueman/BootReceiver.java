package com.fussy.trueman;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("TrueMan", "Boot completed. Checking VPN status...");
            Intent vpnIntent = VpnService.prepare(context);
            if (vpnIntent == null) {
                // Permission already granted, we can auto-start
                context.startService(new Intent(context, TrueManVpnService.class));
                Log.d("TrueMan", "Auto-starting TrueMan VPN on boot.");
            } else {
                Log.d("TrueMan", "Missing VPN permission, cannot auto-start on boot.");
            }
        }
    }
}
