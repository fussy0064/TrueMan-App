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

                // Configure the dummy VPN securely
                // Configure the dummy VPN securely
                builder.setSession("TrueMan Security VPN");

                // Restrict the VPN ONLY to the TrueMan app.
                // This removes the "!" (no internet) mark from the status bar.
                // It prevents the VPN from interfering with other apps' data.
                builder.addAllowedApplication(getPackageName());

                builder.addAddress("10.255.255.255", 32);

                // Set AdGuard DNS (Blocks Ads & Trackers)
                builder.addDnsServer("94.140.14.14");
                builder.addDnsServer("94.140.15.15");

                // Explicitly allow other traffic to bypass
                builder.allowBypass();

                // Establish the VPN connection
                vpnInterface = builder.establish();
                Log.i("TrueManVpnService", "Safe VPN Established. ! mark removed. Internet is free.");

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
