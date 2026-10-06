package com.tanzirdev.apav;
import android.content.Context;
import java.util.List;

public class Checker {
    /** সব অ্যাপ চেক করে; কতগুলোতে নতুন আপডেট পাওয়া গেল তা ফেরত দেয়। */
    public static synchronized int runAll(Context c) {
        AppDao d = Db.get(c).dao();
        int found = 0;
        List<AppItem> list = d.all();
        for (AppItem a : list) {
            try {
                PlayScraper.Info i = PlayScraper.fetch(a.packageName);
                boolean changed = (i.version != null && a.version != null && !i.version.equals(a.version))
                        || (i.updated != null && a.updatedText != null && !i.updated.equals(a.updatedText));
                if (changed) { a.hasUpdate = true; found++; Notifier.show(c, a); }
                if (i.version != null) a.version = i.version;
                if (i.updated != null) a.updatedText = i.updated;
                if (i.icon != null) a.iconUrl = i.icon;
                if ((a.name == null || a.name.isEmpty()) && i.title != null) a.name = i.title;
                a.lastChecked = System.currentTimeMillis();
                d.update(a);
                Thread.sleep(1200); // Play-কে ধীরে ধীরে request দিন
            } catch (Exception ignored) {}
        }
        if (Prefs.b(c, "auto_backup", false) && DriveHelper.account(c) != null) {
            try { DriveHelper.backup(c); } catch (Exception ignored) {}
        }
        return found;
    }
}
