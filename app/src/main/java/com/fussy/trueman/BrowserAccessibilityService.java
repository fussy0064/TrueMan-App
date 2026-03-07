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

        // Refresh blocked sites manually added by parents
        blockedSites = dbHelper.getAllBlockedDomains();

        // BLOCK OTHER VPN APPS INSTANTLY AND TRIGGER UNINSTALL
        String lowerPkg = packageName.toLowerCase();
        boolean isVpnApp = lowerPkg.contains("vpn") || lowerPkg.contains("proxy") ||
                lowerPkg.contains("tunnel") || lowerPkg.contains("nord") ||
                lowerPkg.contains("express");

        boolean isManuallyBlockedApp = false;
        for (String word : blockedSites) {
            if (!word.isEmpty() && lowerPkg.contains(word.toLowerCase())) {
                isManuallyBlockedApp = true;
                break;
            }
        }

        if ((isVpnApp || isManuallyBlockedApp) && !packageName.equals(getPackageName())) {

            Log.d("TrueMan", "Blocked App from opening: " + packageName);
            performGlobalAction(GLOBAL_ACTION_HOME);

            if (isVpnApp) {
                // Force user to uninstall the unauthorized VPN!
                try {
                    Intent uninstallIntent = new Intent(Intent.ACTION_DELETE,
                            android.net.Uri.parse("package:" + packageName));
                    uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(uninstallIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Intent intent = new Intent(this, BlockActivity.class);
            intent.putExtra("blocked_url", "Unauthorized Application Blocked\n(" + packageName + ")");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return;
        }

        // YouTube Ad Skipping Logic
        if (packageName.equals("com.google.android.youtube")) {
            findAndSkipYouTubeAds(rootNode);
        }

        // ADULT CONTENT DEEP SCAN (Fallback if URL bar ID is missing)
        // This scans all text visible on the screen for forbidden words
        String[] defaultAdultWords = { "porn", "sex", "xxx", "adult", "naked", "video tube", "redtube", "xvideos",
                "pornhub" };
        for (String word : defaultAdultWords) {
            if (scanForTextMatch(rootNode, word)) {
                Log.d("TrueMan", "Blocked Content detected via Deep Scan: " + word);
                performGlobalAction(GLOBAL_ACTION_HOME);
                Intent intent = new Intent(this, BlockActivity.class);
                intent.putExtra("blocked_url", "Inappropriate Content Detected");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return;
            }
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
            // Even if not a known browser, check the URL manually added by parents
            for (String word : blockedSites) {
                if (!word.isEmpty() && scanForTextMatch(rootNode, word)) {
                    performGlobalAction(GLOBAL_ACTION_HOME);
                    return;
                }
            }
            return;
        }

        List<AccessibilityNodeInfo> urlBars = rootNode.findAccessibilityNodeInfosByViewId(urlBarId);
        if (urlBars != null && !urlBars.isEmpty()) {
            AccessibilityNodeInfo urlNode = urlBars.get(0);
            if (urlNode.getText() != null) {
                String capturedUrl = urlNode.getText().toString();

                // Check against manual block list
                for (String word : blockedSites) {
                    if (!word.isEmpty() && capturedUrl.toLowerCase().contains(word.toLowerCase())) {
                        Log.d("TrueMan", "Blocked URL accessed: " + capturedUrl);
                        performGlobalAction(GLOBAL_ACTION_HOME);
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

    private boolean scanForTextMatch(AccessibilityNodeInfo node, String text) {
        if (node == null || text == null)
            return false;

        List<AccessibilityNodeInfo> matches = node.findAccessibilityNodeInfosByText(text);
        if (matches != null && !matches.isEmpty()) {
            return true;
        }
        return false;
    }

    private void findAndSkipYouTubeAds(AccessibilityNodeInfo node) {
        if (node == null)
            return;

        // More aggressive list of YouTube skip button IDs
        String[] skipButtonIds = {
                "com.google.android.youtube:id/skip_ad_button",
                "com.google.android.youtube:id/modern_skip_ad_button",
                "com.google.android.youtube:id/skip_ad_button_container",
                "com.google.android.youtube:id/ad_skip_button",
                "com.google.android.youtube:id/skip_ad_button_text",
                "com.google.android.youtube:id/action_button"
        };

        for (String id : skipButtonIds) {
            List<AccessibilityNodeInfo> targets = node.findAccessibilityNodeInfosByViewId(id);
            if (targets != null && !targets.isEmpty()) {
                for (AccessibilityNodeInfo target : targets) {
                    if (target.isClickable() && target.isEnabled()) {
                        target.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        Log.d("TrueMan", "YouTube Ad Skipped via ID: " + id);
                        return;
                    }
                }
            }
        }

        // Search for nodes with "Skip" text in various languages (English, etc.)
        searchAndClickText(node, "Skip Ad");
        searchAndClickText(node, "Skip");
    }

    private boolean searchAndClickText(AccessibilityNodeInfo node, String text) {
        List<AccessibilityNodeInfo> targets = node.findAccessibilityNodeInfosByText(text);
        if (targets != null && !targets.isEmpty()) {
            for (AccessibilityNodeInfo target : targets) {
                AccessibilityNodeInfo clickableNode = findClickableParent(target);
                if (clickableNode != null && clickableNode.isEnabled()) {
                    clickableNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Log.d("TrueMan", "YouTube Ad Skipped via text: " + text);
                    return true;
                }
            }
        }
        return false;
    }

    private AccessibilityNodeInfo findClickableParent(AccessibilityNodeInfo node) {
        if (node == null)
            return null;
        if (node.isClickable())
            return node;
        return findClickableParent(node.getParent());
    }

    @Override
    public void onInterrupt() {
    }
}
