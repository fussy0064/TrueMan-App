package com.fussy.trueman;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.IOException;

public class TrueManVpnService extends VpnService {

    private ParcelFileDescriptor vpnInterface;
    private Thread vpnThread;
    private static final String CHANNEL_ID = "TrueManVpnChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Run as Foreground Service to prevent auto-turn-off
        startForeground(1, getNotification());

        if (vpnThread != null) {
            vpnThread.interrupt();
        }

        vpnThread = new Thread(() -> {
            try {
                Builder builder = new Builder();
                builder.setSession("TrueMan Security VPN");

                // CRITICAL FIX: To remove the "!" mark, we ONLY route a private local address.
                builder.addAddress("10.255.255.1", 32);
                builder.addRoute("10.255.255.1", 32);

                // Re-enable AdGuard DNS for Global Ad & Tracker Blocking
                builder.addDnsServer("94.140.14.14");
                builder.addDnsServer("94.140.15.15");

                // Keep the slot locked but don't touch ANY external traffic
                builder.allowBypass();

                // Establish the VPN connection
                vpnInterface = builder.establish();
                Log.i("TrueManVpnService", "VPN Established. Slot Locked. ! mark fixed.");

                while (!Thread.interrupted()) {
                    Thread.sleep(5000);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stopVPN();
            }
        });

        vpnThread.start();
        return START_STICKY;
    }

    private void stopVPN() {
        try {
            if (vpnInterface != null) {
                vpnInterface.close();
                vpnInterface = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "TrueMan Security Service",
                    NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null)
                manager.createNotificationChannel(channel);
        }
    }

    private Notification getNotification() {
        Intent intent = new Intent(this, ParentalControlActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("TrueMan Protection Active")
                .setContentText("Safe Browsing and Ad-Blocking is running.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    @Override
    public void onDestroy() {
        if (vpnThread != null) {
            vpnThread.interrupt();
        }
        stopVPN();
        super.onDestroy();
    }
}
