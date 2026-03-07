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

                // Set Cloudflare Family DNS (Blocks Malware & Adult Content automatically)
                builder.addDnsServer("1.1.1.3");
                builder.addDnsServer("1.0.0.3");

                // Add a dummy IPv4 address for the VPN interface
                builder.addAddress("10.0.0.2", 24);

                // Route all DNS traffic (Port 53) through the VPN to force the Safe DNS
                builder.addRoute("0.0.0.0", 0);

                // Establish the VPN connection
                vpnInterface = builder.establish();
                Log.i("TrueManVpnService", "Safe VPN Established with Adult-Blocking DNS.");

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
