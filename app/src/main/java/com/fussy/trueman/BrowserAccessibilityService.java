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

        // 🛡️ SYSTEM WHITELIST: Don't block system UI or common launchers
        if (packageName.equals("com.android.systemui") ||
                packageName.equals("com.android.launcher") ||
                packageName.equals("com.google.android.apps.nexuslauncher") ||
                packageName.equals("com.ss.squarehome2") ||
                packageName.contains("launcher")) {
            return;
        }

        // 🛡️ ADULT CONTENT RADAR (Enhanced Deep Scan)
        if (packageName.equals("com.google.android.youtube")) {
            aggressiveYouTubeAdSkipper(rootNode);
            return; // Skip adult keyword scan for YouTube itself
        }

        String[] adultKeywords = {
                "porn", "xxx", "sex", "naked", "redtube", "pornhub", "xvideos", "erotic",
                "nude", "horny", "slut", "milf", "blowjob", "hot video", "sexy", "booty",
                "vagina", "penis", "fuck", "dick", "pussy"
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
                && !packageName.equals(getPackageName())
                && !packageName.equals("com.android.vpndialogs")) {

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

        // 🛡️ GENERAL AD BLOCKER
        // Scan for generic ad labels like "Sponsored" or "Advertisement" and try to
        // hide them if possible
        // Note: Full destruction of app ads needs deeper hooks, but this helps on many
        // platforms
        if (packageName.contains("browser") || packageName.equals("com.android.chrome")) {
            scanForGeneralAds(rootNode);
        }

        // 🛡️ BROWSER URL BLOCKING
        scanBrowserUrl(rootNode, packageName);
    }

    private void scanForGeneralAds(AccessibilityNodeInfo node) {
        if (node == null)
            return;

        String[] adTriggers = { "Sponsored", "Advertisement", "Promoted", "Sponsorisé", "Anuncio" };
        for (String label : adTriggers) {
            List<AccessibilityNodeInfo> adNodes = node.findAccessibilityNodeInfosByText(label);
            if (adNodes != null && !adNodes.isEmpty()) {
                Log.d("TrueMan", "🚩 Ad Detected: " + label);
                // We can't easily "remove" views, but we can log them or attempt to click close
                // buttons nearby
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            scanForGeneralAds(node.getChild(i));
        }
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

        // 1. FAST RESOURCE ID SKIP
        String[] ids = {
                "skip_ad_button", "modern_skip_ad_button", "ad_skip_button",
                "skip_ad_button_text", "btn_skip", "skip_button"
        };
        for (String id : ids) {
            List<AccessibilityNodeInfo> targets = node
                    .findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/" + id);
            if (targets != null && !targets.isEmpty()) {
                for (AccessibilityNodeInfo t : targets) {
                    if (t.isClickable() && t.isEnabled()) {
                        t.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        Log.d("TrueMan", "🚀 YouTube Ad KILLED via Resource ID!");
                        return;
                    }
                }
            }
        }

        // 2. TEXT-BASED SKIP (Multilingual support)
        String[] skipTextsArr = { "Skip", "Skip Ad", "Sauter", "Omitir", "Saltar" };
        for (String text : skipTextsArr) {
            List<AccessibilityNodeInfo> targets = node.findAccessibilityNodeInfosByText(text);
            if (targets != null && !targets.isEmpty()) {
                for (AccessibilityNodeInfo t : targets) {
                    AccessibilityNodeInfo clickable = findFirstClickable(t);
                    if (clickable != null && clickable.isEnabled()) {
                        clickable.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        Log.d("TrueMan", "🚀 YouTube Ad KILLED via Text: " + text);
                        return;
                    }
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
