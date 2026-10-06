package com.tanzirdev.apav;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class AppItem {
    @PrimaryKey(autoGenerate = true) public long id;
    public String name, packageName, link, iconUrl, version, updatedText;
    public boolean hasUpdate;
    public long addedAt, lastChecked;
}
