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
        startForeground(1, getNotification());

        if (vpnThread == null || !vpnThread.isAlive()) {
            vpnThread = new Thread(() -> {
                try {
                    Builder builder = new Builder();
                    builder.setSession("TrueMan Locking System");

                    // Route ONLY local dummy address to lock VPN slot WITHOUT showing "!" mark
                    builder.addAddress("10.0.0.1", 32);
                    builder.addRoute("10.0.0.1", 32);

                    // allowBypass critical for connectivity
                    builder.allowBypass();

                    vpnInterface = builder.establish();
                    Log.i("TrueManVpnService", "TrueMan Lockdown Established.");

                    while (true) {
                        Thread.sleep(10000);
                    }
                } catch (Exception e) {
                    Log.e("TrueManVpnService", "VPN Thread crashed", e);
                } finally {
                    stopVPN();
                }
            });
            vpnThread.start();
        }

        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // Restart self if swiped away
        Intent intent = new Intent(this, TrueManVpnService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        super.onTaskRemoved(rootIntent);
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
        // Send broadcast to restart self or manually trigger startup
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.fussy.trueman.RESTART_SERVICE");
        sendBroadcast(broadcastIntent);

        if (vpnThread != null)
            vpnThread.interrupt();
        stopVPN();
        super.onDestroy();
    }
}
