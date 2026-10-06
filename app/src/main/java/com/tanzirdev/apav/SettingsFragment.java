package com.tanzirdev.apav;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import androidx.work.*;

public class SettingsFragment extends Fragment {
    LinearLayout box; Context ctx;
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) { return i.inflate(R.layout.fragment_settings, c, false); }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        box = v.findViewById(R.id.box); ctx = requireContext();
        head("Notifications");
        sw("Update notifications", "Notify when a tracked app gets a new version", "notify", true, false);
        act("System notification settings", "Sound, vibration and priority", () -> startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, ctx.getPackageName())));
        head("Checking");
        act("Check interval", "Every " + Prefs.i(ctx, "interval", 6) + " hours", () -> {
            int[] h = {1, 3, 6, 12, 24}; String[] t = {"1 hour", "3 hours", "6 hours", "12 hours", "24 hours"};
            new MaterialAlertDialogBuilder(ctx).setTitle("Check interval").setItems(t, (d, w) -> { Prefs.sp(ctx).edit().putInt("interval", h[w]).apply(); UpdateWorker.schedule(ctx); rebuild(); }).show();
        });
        sw("Wi-Fi only", "Don't check on mobile data", "wifi", false, true);
        act("Check now", "Look for updates right away", () -> {
            U.toast(ctx, "Checking...");
            Db.IO.execute(() -> { int n = Checker.runAll(ctx.getApplicationContext()); if (getActivity() != null) getActivity().runOnUiThread(() -> U.toast(ctx, n > 0 ? n + " update(s) found" : "Everything is up to date")); });
        });
        head("List");
        act("Sort order", new String[]{"Updates first", "Name A–Z", "Newest added"}[Prefs.i(ctx, "sort", 0)], () ->
            new MaterialAlertDialogBuilder(ctx).setTitle("Sort order").setItems(new String[]{"Updates first", "Name A–Z", "Newest added"}, (d, w) -> { Prefs.sp(ctx).edit().putInt("sort", w).apply(); rebuild(); }).show());
        sw("Show only apps with updates", "Hide up-to-date apps on Home", "only_updates", false, false);
        sw("Tap opens Play Store", "Tapping a card opens the app page", "tap_open", true, false);
        head("Backup");
        sw("Auto backup to Google Drive", "Back up after every check (needs Drive connected)", "auto_backup", false, false);
        head("Data");
        act("Clear all UPDATE badges", "Mark every app as seen", () -> Db.IO.execute(() -> { Db.get(ctx).dao().clearFlags(); post("Badges cleared"); }));
        act("Delete all apps", "Remove every tracked app from this device", () ->
            new MaterialAlertDialogBuilder(ctx).setTitle("Delete all apps?").setMessage("This can't be undone (Drive backup stays).").setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> Db.IO.execute(() -> { Db.get(ctx).dao().clear(); post("All apps deleted"); })).show());
        head("About");
        act("Apav", "Version 1.0.0  •  by TanzirDev", () -> {});
    }

    void post(String m) { if (getActivity() != null) getActivity().runOnUiThread(() -> U.toast(ctx, m)); }
    void rebuild() { getParentFragmentManager().beginTransaction().replace(R.id.container, new SettingsFragment()).commit(); }
    void head(String t) {
        TextView h = new TextView(ctx); h.setText(t.toUpperCase()); h.setTextSize(11); h.setLetterSpacing(0.12f);
        h.setTextColor(0xFFE39A1B); h.setPadding(6, (int) U.dp(ctx, 22), 0, 0); box.addView(h);
    }
    View row(String t, String s) {
        View r = LayoutInflater.from(ctx).inflate(R.layout.item_setting, box, false);
        ((TextView) r.findViewById(R.id.t)).setText(t); ((TextView) r.findViewById(R.id.s)).setText(s); box.addView(r); return r;
    }
    void act(String t, String s, Runnable r) { View v = row(t, s); v.findViewById(R.id.sw).setVisibility(View.GONE); v.setOnClickListener(x -> r.run()); }
    void sw(String t, String s, String key, boolean def, boolean reschedule) {
        View v = row(t, s); MaterialSwitch m = v.findViewById(R.id.sw); m.setChecked(Prefs.b(ctx, key, def));
        m.setOnCheckedChangeListener((b, on) -> { Prefs.sp(ctx).edit().putBoolean(key, on).apply(); if (reschedule) UpdateWorker.schedule(ctx); });
        v.setOnClickListener(x -> m.toggle());
    }
}
