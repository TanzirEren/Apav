package com.tanzirdev.apav;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class U {
    public static void toast(Context c, String m) { if (c != null) Toast.makeText(c, m, Toast.LENGTH_LONG).show(); }
    public static float dp(Context c, float v) { return v * c.getResources().getDisplayMetrics().density; }
    public static void openStore(Context c, String pkg) {
        try { c.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkg)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); }
        catch (Exception e) { c.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + pkg)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); }
    }
}
