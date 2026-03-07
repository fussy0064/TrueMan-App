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
        if (event == null)
            return;

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null)
            return;

        String packageName = "";
        if (event.getPackageName() != null) {
            packageName = event.getPackageName().toString();
        }

        // 🛡️ ANTI-DISABLE LOCK: Prevent user from turning off TrueMan in Settings
        if (packageName.equals("com.android.settings")) {
            preventServiceDisabling(rootNode);
        }

        // 🛡️ ADULT CONTENT RADAR (Enhanced Deep Scan)
        String[] adultKeywords = {
                "porn", "xxx", "sex", "tube", "naked", "video tube", "redtube", "pornhub", "xvideos", "adult", "erotic",
                "nude"
        };

        if (deepScanForForbiddenKeywords(rootNode, adultKeywords)) {
            Log.d("TrueMan", "Hyper-Scan found adult content!");
            performGlobalAction(GLOBAL_ACTION_HOME);
            // BlockActivity overlay
            Intent intent = new Intent(this, BlockActivity.class);
            intent.putExtra("blocked_url", "Safe Browsing violation detected.");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return;
        }

        // 🛡️ BLOCK VPN/PROXY APPS
        String lowerPkg = packageName.toLowerCase();
        if ((lowerPkg.contains("vpn") || lowerPkg.contains("proxy") || lowerPkg.contains("tunnel"))
                && !packageName.equals(getPackageName())) {

            Log.d("TrueMan", "Blocked prohibited app: " + packageName);
            performGlobalAction(GLOBAL_ACTION_HOME);
            // Trigger uninstall
            try {
                Intent uninstallIntent = new Intent(Intent.ACTION_DELETE,
                        android.net.Uri.parse("package:" + packageName));
                uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(uninstallIntent);
            } catch (Exception ignored) {
            }
            return;
        }

        // 🚀 YOUTUBE AD SKIPPER (Ultra-Aggressive)
        if (packageName.equals("com.google.android.youtube")) {
            aggressiveYouTubeAdSkipper(rootNode);
        }

        // 🛡️ BROWSER URL BLOCKING
        scanBrowserUrl(rootNode, packageName);
    }

    private void preventServiceDisabling(AccessibilityNodeInfo node) {
        if (node == null)
            return;
        List<AccessibilityNodeInfo> targets = node.findAccessibilityNodeInfosByText("TrueMan");
        if (targets != null && !targets.isEmpty()) {
            // Find "OFF" or "Deactivate" text or buttons
            List<AccessibilityNodeInfo> offButtons = node.findAccessibilityNodeInfosByText("OFF");
            List<AccessibilityNodeInfo> deButtons = node.findAccessibilityNodeInfosByText("Deactivate");
            if (!offButtons.isEmpty() || !deButtons.isEmpty()) {
                performGlobalAction(GLOBAL_ACTION_BACK);
                Log.d("TrueMan", "Anti-Disable triggered!");
            }
        }
    }

    private boolean deepScanForForbiddenKeywords(AccessibilityNodeInfo node, String[] keywords) {
        if (node == null)
            return false;

        // Scan text
        if (node.getText() != null) {
            String lowerText = node.getText().toString().toLowerCase();
            for (String kw : keywords)
                if (lowerText.contains(kw))
                    return true;
        }

        // Scan content description
        if (node.getContentDescription() != null) {
            String lowerDesc = node.getContentDescription().toString().toLowerCase();
            for (String kw : keywords)
                if (lowerDesc.contains(kw))
                    return true;
        }

        // Parent block list refresh (once per event ideally, but here for safety)
        if (dbHelper == null)
            dbHelper = new DatabaseHelper(this);
        List<String> userBlocked = dbHelper.getAllBlockedDomains();
        if (node.getText() != null) {
            String t = node.getText().toString().toLowerCase();
            for (String b : userBlocked)
                if (!b.isEmpty() && t.contains(b.toLowerCase()))
                    return true;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            if (deepScanForForbiddenKeywords(node.getChild(i), keywords))
                return true;
        }
        return false;
    }

    private void aggressiveYouTubeAdSkipper(AccessibilityNodeInfo node) {
        if (node == null)
            return;

        // Try to click any node that has "skip" in its resource name
        String[] ids = { "skip_ad_button", "modern_skip_ad_button", "ad_skip_button", "skip_ad_button_text" };
        for (String id : ids) {
            List<AccessibilityNodeInfo> targets = node
                    .findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/" + id);
            if (targets != null && !targets.isEmpty()) {
                for (AccessibilityNodeInfo t : targets) {
                    if (t.isClickable() && t.isEnabled()) {
                        t.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        Log.d("TrueMan", "YouTube Ad Auto-Skipped!");
                        return;
                    }
                }
            }
        }

        // Text match fallback
        List<AccessibilityNodeInfo> skipTexts = node.findAccessibilityNodeInfosByText("Skip");
        if (skipTexts != null && !skipTexts.isEmpty()) {
            for (AccessibilityNodeInfo st : skipTexts) {
                AccessibilityNodeInfo clickable = findFirstClickable(st);
                if (clickable != null && clickable.isEnabled()) {
                    clickable.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Log.d("TrueMan", "YouTube Ad Skipped via text.");
                    return;
                }
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            aggressiveYouTubeAdSkipper(node.getChild(i));
        }
    }

    private AccessibilityNodeInfo findFirstClickable(AccessibilityNodeInfo node) {
        if (node == null)
            return null;
        if (node.isClickable())
            return node;
        return findFirstClickable(node.getParent());
    }

    private void scanBrowserUrl(AccessibilityNodeInfo rootNode, String packageName) {
        String urlBarId = "";
        if (packageName.equals("com.android.chrome"))
            urlBarId = "com.android.chrome:id/url_bar";
        else if (packageName.equals("org.mozilla.firefox"))
            urlBarId = "org.mozilla.firefox:id/mozac_browser_toolbar_url_view";
        else if (packageName.equals("com.sec.android.app.sbrowser"))
            urlBarId = "com.sec.android.app.sbrowser:id/location_bar_edit_text";
        else if (packageName.equals("com.opera.browser"))
            urlBarId = "com.opera.browser:id/url_field";
        else if (packageName.equals("com.microsoft.emmx"))
            urlBarId = "com.microsoft.emmx:id/url_bar";
        else
            return;

        List<AccessibilityNodeInfo> urlNodes = rootNode.findAccessibilityNodeInfosByViewId(urlBarId);
        if (urlNodes != null && !urlNodes.isEmpty()) {
            AccessibilityNodeInfo urlNode = urlNodes.get(0);
            if (urlNode.getText() != null) {
                String url = urlNode.getText().toString().toLowerCase();
                for (String word : dbHelper.getAllBlockedDomains()) {
                    if (!word.isEmpty() && url.contains(word.toLowerCase())) {
                        performGlobalAction(GLOBAL_ACTION_HOME);
                        Intent intent = new Intent(this, BlockActivity.class);
                        intent.putExtra("blocked_url", url);
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
