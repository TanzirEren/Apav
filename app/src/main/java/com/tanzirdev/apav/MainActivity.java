package com.tanzirdev.apav;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {
    int[] ids = {R.id.ic_home, -1, R.id.ic_drive, R.id.ic_settings};
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), r -> {}).launch(Manifest.permission.POST_NOTIFICATIONS);
        findViewById(R.id.nav_home).setOnClickListener(v -> select(0));
        findViewById(R.id.nav_add).setOnClickListener(v -> new AddSheet().show(getSupportFragmentManager(), "add"));
        findViewById(R.id.nav_drive).setOnClickListener(v -> select(2));
        findViewById(R.id.nav_settings).setOnClickListener(v -> select(3));
        if (b == null) select(0);
    }
    void select(int i) {
        Fragment f = i == 0 ? new HomeFragment() : i == 2 ? new DriveFragment() : new SettingsFragment();
        getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).replace(R.id.container, f).commit();
        for (int k = 0; k < ids.length; k++) {
            if (ids[k] < 0) continue;
            ImageView v = findViewById(ids[k]);
            v.setColorFilter(k == i ? Color.parseColor("#0B8A87") : Color.parseColor("#8A9A9D"));
            v.setBackgroundResource(k == i ? R.drawable.bg_nav_sel : 0);
        }
    }
}
