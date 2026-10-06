package com.tanzirdev.apav;
import android.app.Application;

public class App extends Application {
    @Override public void onCreate() {
        super.onCreate();
        Notifier.channel(this);
        UpdateWorker.schedule(this);
    }
}
