package com.tanzirdev.apav;
import androidx.room.*;
import java.util.List;

@Dao
public interface AppDao {
    @Query("SELECT * FROM AppItem") List<AppItem> all();
    @Insert long insert(AppItem a);
    @Update void update(AppItem a);
    @Delete void delete(AppItem a);
    @Query("SELECT COUNT(*) FROM AppItem WHERE packageName = :p") int exists(String p);
    @Query("UPDATE AppItem SET hasUpdate = 0") void clearFlags();
    @Query("DELETE FROM AppItem") void clear();
}
