package com.tanzirdev.apav;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.*;
import java.util.concurrent.TimeUnit;

public class UpdateWorker extends Worker {
    public UpdateWorker(@NonNull Context c, @NonNull WorkerParameters p) { super(c, p); }
    @NonNull @Override public Result doWork() { Checker.runAll(getApplicationContext()); return Result.success(); }

    public static void schedule(Context c) {
        int h = Prefs.i(c, "interval", 6);
        NetworkType nt = Prefs.b(c, "wifi", false) ? NetworkType.UNMETERED : NetworkType.CONNECTED;
        PeriodicWorkRequest r = new PeriodicWorkRequest.Builder(UpdateWorker.class, h, TimeUnit.HOURS)
            .setConstraints(new Constraints.Builder().setRequiredNetworkType(nt).build()).build();
        WorkManager.getInstance(c).enqueueUniquePeriodicWork("apav_check", ExistingPeriodicWorkPolicy.UPDATE, r);
    }
}
