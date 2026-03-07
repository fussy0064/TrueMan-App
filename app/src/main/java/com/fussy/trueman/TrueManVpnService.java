package com.fussy.trueman;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.IOException;

public class TrueManVpnService extends VpnService {

    private ParcelFileDescriptor vpnInterface;
    private Thread vpnThread;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (vpnThread != null) {
            vpnThread.interrupt();
        }

        vpnThread = new Thread(() -> {
            try {
                Builder builder = new Builder();

                // Configure the VPN to route all traffic and enforce Safe DNS
                builder.setSession("TrueMan Safe VPN");

                // Only attach the VPN to TrueMan itself, leaving the rest of the phone's
                // network completely untouched. This removes the "!" (no internet) mark.
                // The global Android VPN slot is still occupied, successfully blocking other
                // VPN apps.
                builder.addAllowedApplication(getPackageName());

                // Allow other apps to bypass this VPN, ensuring normal Wi-Fi/Cellular works
                // 100%
                builder.allowBypass();

                // Add a dummy IPv4 address for the VPN interface
                builder.addAddress("10.0.0.2", 24);

                // Establish the VPN connection
                vpnInterface = builder.establish();
                Log.i("TrueManVpnService", "Safe Dummy VPN Established. VPN Slot Locked.");

                // Keep the thread alive while the VPN is running
                while (!Thread.interrupted()) {
                    Thread.sleep(10000);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (vpnInterface != null) {
                        vpnInterface.close();
                        vpnInterface = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        vpnThread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (vpnThread != null) {
            vpnThread.interrupt();
        }
        super.onDestroy();
    }
}
