package com.tanzirdev.apav;
import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    public static SharedPreferences sp(Context c) { return c.getApplicationContext().getSharedPreferences("apav", 0); }
    public static boolean b(Context c, String k, boolean d) { return sp(c).getBoolean(k, d); }
    public static int i(Context c, String k, int d) { return sp(c).getInt(k, d); }
}
