package com.tanzirdev.apav;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.material.button.MaterialButton;
import java.text.DateFormat;
import java.util.Date;

public class DriveFragment extends Fragment {
    TextView status, email, last; MaterialButton connect, backup, restore, out;
    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r ->
        GoogleSignIn.getSignedInAccountFromIntent(r.getData())
            .addOnSuccessListener(a -> { U.toast(getContext(), "Google Drive connected"); refresh(); })
            .addOnFailureListener(e -> U.toast(getContext(), "Sign-in failed: " + e.getMessage() + "\n(OAuth client / SHA-1 ঠিক আছে কিনা দেখুন)")));

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) { return i.inflate(R.layout.fragment_drive, c, false); }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        status = v.findViewById(R.id.status); email = v.findViewById(R.id.email); last = v.findViewById(R.id.last);
        connect = v.findViewById(R.id.b_connect); backup = v.findViewById(R.id.b_backup); restore = v.findViewById(R.id.b_restore); out = v.findViewById(R.id.b_out);
        Context ctx = requireContext().getApplicationContext();
        connect.setOnClickListener(x -> DriveHelper.client(ctx).signOut().addOnCompleteListener(t -> launcher.launch(DriveHelper.client(ctx).getSignInIntent()))); // signOut → সবসময় account chooser আসবে
        out.setOnClickListener(x -> DriveHelper.client(ctx).signOut().addOnCompleteListener(t -> { U.toast(ctx, "Disconnected"); refresh(); }));
        backup.setOnClickListener(x -> run("Backing up...", () -> { DriveHelper.backup(ctx); return "Backup saved to Google Drive"; }));
        restore.setOnClickListener(x -> run("Restoring...", () -> {
            int n = DriveHelper.restore(ctx);
            if (n >= 0) { Activity a = getActivity(); if (a != null) a.runOnUiThread(() -> getParentFragmentManager().setFragmentResult("apps", new Bundle())); }
            return n < 0 ? "No backup found on Drive" : n + " app(s) restored";
        }));
        refresh();
    }

    interface Job { String go() throws Exception; }
    void run(String msg, Job j) {
        Context ctx = requireContext().getApplicationContext();
        U.toast(ctx, msg);
        Db.IO.execute(() -> {
            String r; try { r = j.go(); } catch (Exception e) { r = "Failed: " + e.getMessage(); }
            final String m = r;
            Activity a = getActivity(); if (a != null) a.runOnUiThread(() -> { U.toast(ctx, m); refresh(); });
        });
    }

    void refresh() {
        if (!isAdded()) return;
        GoogleSignInAccount a = DriveHelper.account(requireContext());
        boolean on = a != null;
        status.setText(on ? "Connected" : "Google Drive");
        email.setText(on ? a.getEmail() : "Connect to back up your app list");
        long t = Prefs.sp(requireContext()).getLong("last_backup", 0);
        last.setText(on && t > 0 ? "Last backup: " + DateFormat.getDateTimeInstance().format(new Date(t)) : "");
        connect.setVisibility(on ? View.GONE : View.VISIBLE);
        backup.setVisibility(on ? View.VISIBLE : View.GONE); restore.setVisibility(on ? View.VISIBLE : View.GONE); out.setVisibility(on ? View.VISIBLE : View.GONE);
    }
}
