package com.tanzirdev.apav;
import android.os.*;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.*;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddSheet extends BottomSheetDialogFragment {
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = inf.inflate(R.layout.sheet_add, c, false);
        TextInputEditText inName = v.findViewById(R.id.in_name), inLink = v.findViewById(R.id.in_link);
        MaterialButton btn = v.findViewById(R.id.btn_add);
        btn.setOnClickListener(x -> {
            String link = String.valueOf(inLink.getText()).trim();
            String pkg = PlayScraper.pkgFromLink(link);
            if (pkg == null) { inLink.setError("Invalid Play Store link"); return; }
            String name = String.valueOf(inName.getText()).trim();
            btn.setEnabled(false); btn.setText("Checking...");
            android.content.Context ctx = requireContext().getApplicationContext();
            Db.IO.execute(() -> {
                AppDao d = Db.get(ctx).dao();
                String err = null;
                if (d.exists(pkg) > 0) err = "Already added";
                else {
                    AppItem a = new AppItem();
                    a.packageName = pkg; a.link = link; a.name = name; a.addedAt = System.currentTimeMillis();
                    try {
                        PlayScraper.Info i = PlayScraper.fetch(pkg);
                        a.iconUrl = i.icon; a.version = i.version; a.updatedText = i.updated;
                        if (name.isEmpty()) a.name = i.title;
                        a.lastChecked = System.currentTimeMillis();
                    } catch (Exception e) {
                        if (name.isEmpty()) a.name = pkg;
                        err = "Added, but couldn't read Play page now (" + e.getMessage() + ")";
                    }
                    d.insert(a);
                }
                final String m = err;
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (m != null) U.toast(ctx, m);
                    getParentFragmentManager().setFragmentResult("apps", new Bundle());
                    if (m == null || m.startsWith("Added")) dismissAllowingStateLoss();
                    else { btn.setEnabled(true); btn.setText("Add & track"); }
                });
            });
        });
        return v;
    }
}
