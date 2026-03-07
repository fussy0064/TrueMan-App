package com.fussy.trueman;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.util.Log;

import java.util.List;

public class BrowserAccessibilityService extends AccessibilityService {

    private DatabaseHelper dbHelper;
    private List<String> blockedSites;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        dbHelper = new DatabaseHelper(this);
        blockedSites = dbHelper.getAllBlockedDomains();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null)
            return;

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null)
            return;

        String packageName = event.getPackageName().toString();

        // BLOCK OTHER VPN APPS INSTANTLY
        String lowerPkg = packageName.toLowerCase();
        if ((lowerPkg.contains("vpn") || lowerPkg.contains("proxy") ||
                lowerPkg.contains("tunnel") || lowerPkg.contains("nord") ||
                lowerPkg.contains("express")) && !packageName.equals(getPackageName())) {

            Log.d("TrueMan", "Blocked VPN App from opening: " + packageName);
            performGlobalAction(GLOBAL_ACTION_HOME);

            Intent intent = new Intent(this, BlockActivity.class);
            intent.putExtra("blocked_url", "Unauthorized VPN / Proxy App");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return;
        }

        // Try to find the URL bar
        String urlBarId = "";

        if (packageName.equals("com.android.chrome")) {
            urlBarId = "com.android.chrome:id/url_bar";
        } else if (packageName.equals("org.mozilla.firefox")) {
            urlBarId = "org.mozilla.firefox:id/mozac_browser_toolbar_url_view";
        } else if (packageName.equals("com.sec.android.app.sbrowser")) {
            urlBarId = "com.sec.android.app.sbrowser:id/location_bar_edit_text";
        } else if (packageName.equals("com.opera.browser")) {
            urlBarId = "com.opera.browser:id/url_field";
        } else if (packageName.equals("com.microsoft.emmx")) {
            urlBarId = "com.microsoft.emmx:id/url_bar";
        } else {
            return;
        }

        List<AccessibilityNodeInfo> urlBars = rootNode.findAccessibilityNodeInfosByViewId(urlBarId);
        if (urlBars != null && !urlBars.isEmpty()) {
            AccessibilityNodeInfo urlNode = urlBars.get(0);
            if (urlNode.getText() != null) {
                String capturedUrl = urlNode.getText().toString();
                // Check if blocked
                // Update blockedSites if needed (ideally via BroadcastReceiver, but this works
                // for basic setup)
                blockedSites = dbHelper.getAllBlockedDomains();

                for (String word : blockedSites) {
                    if (capturedUrl.toLowerCase().contains(word.toLowerCase())) {
                        Log.d("TrueMan", "Blocked URL accessed: " + capturedUrl);

                        // Action 1: Navigate Back (or home)
                        performGlobalAction(GLOBAL_ACTION_HOME);

                        // Action 2: Show Blocked screen overlay
                        Intent intent = new Intent(this, BlockActivity.class);
                        intent.putExtra("blocked_url", capturedUrl);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
    }
}
