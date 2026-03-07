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
                builder.setSession("TrueMan AdBlocker VPN");

                // Route an unroutable testing subnet so Android considers the VPN active
                // but no real user traffic gets intercepted or blocked by our VPN thread.
                builder.addAddress("10.0.0.2", 32);
                builder.addRoute("192.0.2.0", 24);

                // Set AdGuard DNS (Blocks Ads & Trackers globally)
                builder.addDnsServer("94.140.14.14");
                builder.addDnsServer("94.140.15.15");

                // Allow other apps to bypass this VPN, ensuring normal Wi-Fi/Cellular works
                // 100%
                builder.allowBypass();

                // Establish the VPN connection
                vpnInterface = builder.establish();
                Log.i("TrueManVpnService", "Safe Dummy VPN Established. VPN Slot Locked. Internet untouched.");

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
