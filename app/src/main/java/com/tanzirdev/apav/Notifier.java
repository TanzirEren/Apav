package com.tanzirdev.apav;
import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class Notifier {
    public static final String CH = "apav_updates";
    public static void channel(Context c) {
        NotificationChannel ch = new NotificationChannel(CH, "App updates", NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription("New versions of the apps you track");
        c.getSystemService(NotificationManager.class).createNotificationChannel(ch);
    }
    public static void show(Context c, AppItem a) {
        if (!Prefs.b(c, "notify", true)) return;
        if (android.os.Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(c, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
        PendingIntent pi = PendingIntent.getActivity(c, 0, new Intent(c, MainActivity.class), PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification n = new NotificationCompat.Builder(c, CH).setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setColor(0xFF0B8A87).setContentTitle(a.name + " has an update")
            .setContentText("A new version is available on Google Play").setContentIntent(pi).setAutoCancel(true).build();
        try { NotificationManagerCompat.from(c).notify((int) a.id, n); } catch (SecurityException ignored) {}
    }
}
