package com.tanzirdev.apav;
import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.text.DateFormat;
import java.util.*;

public class HomeFragment extends Fragment {
    SwipeRefreshLayout sr; TextView empty, sub; Adapter ad = new Adapter();

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) { return i.inflate(R.layout.fragment_home, c, false); }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        RecyclerView rv = v.findViewById(R.id.list);
        rv.setLayoutManager(new LinearLayoutManager(getContext())); rv.setAdapter(ad);
        sr = v.findViewById(R.id.swipe); empty = v.findViewById(R.id.empty); sub = v.findViewById(R.id.sub);
        sr.setColorSchemeResources(R.color.teal, R.color.amber);
        sr.setOnRefreshListener(() -> {
            Context ctx = requireContext().getApplicationContext();
            Db.IO.execute(() -> { int n = Checker.runAll(ctx); if (getActivity() != null) getActivity().runOnUiThread(() -> { sr.setRefreshing(false); U.toast(ctx, n > 0 ? n + " update(s) found" : "Everything is up to date"); load(); }); });
        });
        getParentFragmentManager().setFragmentResultListener("apps", getViewLifecycleOwner(), (k, b) -> load());
        load();
    }

    void load() {
        Context ctx = requireContext().getApplicationContext();
        Db.IO.execute(() -> {
            List<AppItem> l = Db.get(ctx).dao().all();
            int ups = 0; for (AppItem a : l) if (a.hasUpdate) ups++;
            final int u = ups, total = l.size();
            if (Prefs.b(ctx, "only_updates", false)) { List<AppItem> f = new ArrayList<>(); for (AppItem a : l) if (a.hasUpdate) f.add(a); l = f; }
            int sort = Prefs.i(ctx, "sort", 0);
            Collections.sort(l, (a, b) -> sort == 1 ? String.valueOf(a.name).compareToIgnoreCase(String.valueOf(b.name))
                : sort == 2 ? Long.compare(b.addedAt, a.addedAt)
                : (a.hasUpdate != b.hasUpdate ? (a.hasUpdate ? -1 : 1) : Long.compare(b.addedAt, a.addedAt)));
            final List<AppItem> out = l;
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                ad.items = out; ad.notifyDataSetChanged();
                empty.setVisibility(out.isEmpty() ? View.VISIBLE : View.GONE);
                sub.setText(total + " apps  •  " + u + " updates");
            });
        });
    }

    class Adapter extends RecyclerView.Adapter<Adapter.H> {
        List<AppItem> items = new ArrayList<>();
        class H extends RecyclerView.ViewHolder {
            ImageView icon, more; TextView name, meta, badge; View card;
            H(View v) { super(v); card = v; icon = v.findViewById(R.id.icon); more = v.findViewById(R.id.more); name = v.findViewById(R.id.name); meta = v.findViewById(R.id.meta); badge = v.findViewById(R.id.badge); }
        }
        @NonNull @Override public H onCreateViewHolder(@NonNull ViewGroup p, int t) { return new H(LayoutInflater.from(p.getContext()).inflate(R.layout.item_app, p, false)); }
        @Override public int getItemCount() { return items.size(); }
        @Override public void onBindViewHolder(@NonNull H h, int pos) {
            AppItem a = items.get(pos); Context c = h.card.getContext();
            h.name.setText(a.name);
            h.meta.setText(a.version != null ? "v" + a.version + (a.updatedText != null ? "  •  " + a.updatedText : "") : a.packageName);
            h.badge.setVisibility(a.hasUpdate ? View.VISIBLE : View.GONE);
            com.google.android.material.card.MaterialCardView cv = (com.google.android.material.card.MaterialCardView) h.card;
            cv.setStrokeColor(a.hasUpdate ? 0xFFE39A1B : 0x1F0B8A87);
            cv.setStrokeWidth((int) U.dp(c, a.hasUpdate ? 2 : 1));
            cv.setCardElevation(a.hasUpdate ? U.dp(c, 6) : 0);
            if (android.os.Build.VERSION.SDK_INT >= 28) { cv.setOutlineAmbientShadowColor(0xFFE39A1B); cv.setOutlineSpotShadowColor(0xFFE39A1B); }
            Glide.with(c).load(a.iconUrl).transform(new RoundedCorners((int) U.dp(c, 14))).into(h.icon);
            h.card.setOnClickListener(v -> { seen(a); if (Prefs.b(c, "tap_open", true)) U.openStore(c, a.packageName); });
            View.OnClickListener menu = v -> {
                PopupMenu pm = new PopupMenu(c, h.more);
                pm.getMenu().add(0, 1, 0, "Open in Play Store"); pm.getMenu().add(0, 2, 1, "Mark as seen"); pm.getMenu().add(0, 3, 2, "Delete");
                pm.setOnMenuItemClickListener(m -> {
                    if (m.getItemId() == 1) U.openStore(c, a.packageName);
                    else if (m.getItemId() == 2) seen(a);
                    else new MaterialAlertDialogBuilder(c).setTitle("Delete " + a.name + "?").setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete", (d, w) -> Db.IO.execute(() -> { Db.get(c).dao().delete(a); if (getActivity() != null) getActivity().runOnUiThread(HomeFragment.this::load); })).show();
                    return true;
                });
                pm.show();
            };
            h.more.setOnClickListener(menu); h.card.setOnLongClickListener(v -> { menu.onClick(v); return true; });
        }
        void seen(AppItem a) {
            if (!a.hasUpdate) return;
            a.hasUpdate = false;
            Context c = requireContext().getApplicationContext();
            Db.IO.execute(() -> { Db.get(c).dao().update(a); if (getActivity() != null) getActivity().runOnUiThread(HomeFragment.this::load); });
        }
    }
}
