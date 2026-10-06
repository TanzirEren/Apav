package com.tanzirdev.apav;
import android.content.Context;
import androidx.room.*;
import java.util.concurrent.*;

@Database(entities = {AppItem.class}, version = 1, exportSchema = false)
public abstract class Db extends RoomDatabase {
    public abstract AppDao dao();
    public static final ExecutorService IO = Executors.newFixedThreadPool(2);
    private static Db i;
    public static synchronized Db get(Context c) {
        if (i == null) i = Room.databaseBuilder(c.getApplicationContext(), Db.class, "apav.db").build();
        return i;
    }
}
