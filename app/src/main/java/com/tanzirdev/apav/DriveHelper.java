package com.tanzirdev.apav;
import android.content.Context;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import java.io.ByteArrayOutputStream;
import java.util.*;
import org.json.*;

public class DriveHelper {
    static final String SCOPE = DriveScopes.DRIVE_APPDATA;   // শুধু অ্যাপের লুকানো ফোল্ডার
    static final String FILE = "apav_backup.json";

    public static GoogleSignInClient client(Context c) {
        GoogleSignInOptions o = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail().requestScopes(new Scope(SCOPE)).build();
        return GoogleSignIn.getClient(c, o);
    }
    public static GoogleSignInAccount account(Context c) {
        GoogleSignInAccount a = GoogleSignIn.getLastSignedInAccount(c);
        return (a != null && GoogleSignIn.hasPermissions(a, new Scope(SCOPE))) ? a : null;
    }
    static Drive svc(Context c) {
        GoogleAccountCredential cr = GoogleAccountCredential.usingOAuth2(c, Collections.singleton(SCOPE));
        cr.setSelectedAccount(account(c).getAccount());
        return new Drive.Builder(new com.google.api.client.http.javanet.NetHttpTransport(), GsonFactory.getDefaultInstance(), cr).setApplicationName("Apav").build();
    }
    static String findId(Drive d) throws Exception {
        List<com.google.api.services.drive.model.File> f = d.files().list().setSpaces("appDataFolder")
            .setQ("name='" + FILE + "'").setFields("files(id)").execute().getFiles();
        return (f == null || f.isEmpty()) ? null : f.get(0).getId();
    }
    public static void backup(Context c) throws Exception {
        JSONArray arr = new JSONArray();
        for (AppItem a : Db.get(c).dao().all()) {
            arr.put(new JSONObject().put("name", a.name).put("pkg", a.packageName).put("link", a.link)
                .put("icon", a.iconUrl).put("ver", a.version).put("upd", a.updatedText).put("added", a.addedAt));
        }
        byte[] data = new JSONObject().put("app", "Apav").put("apps", arr).toString().getBytes("UTF-8");
        ByteArrayContent content = new ByteArrayContent("application/json", data);
        Drive d = svc(c);
        String id = findId(d);
        if (id != null) d.files().update(id, new com.google.api.services.drive.model.File(), content).execute();
        else d.files().create(new com.google.api.services.drive.model.File().setName(FILE).setParents(Collections.singletonList("appDataFolder")), content).setFields("id").execute();
        Prefs.sp(c).edit().putLong("last_backup", System.currentTimeMillis()).apply();
    }
    /** Restore করে কতগুলো নতুন অ্যাপ যোগ হলো তা ফেরত দেয়। -1 = Drive-এ backup নেই */
    public static int restore(Context c) throws Exception {
        Drive d = svc(c);
        String id = findId(d);
        if (id == null) return -1;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        d.files().get(id).executeMediaAndDownloadTo(out);
        JSONArray arr = new JSONObject(out.toString("UTF-8")).getJSONArray("apps");
        AppDao dao = Db.get(c).dao();
        int n = 0;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            String pkg = o.optString("pkg");
            if (pkg.isEmpty() || dao.exists(pkg) > 0) continue;
            AppItem a = new AppItem();
            a.name = o.optString("name"); a.packageName = pkg; a.link = o.optString("link");
            a.iconUrl = o.optString("icon", null); a.version = o.optString("ver", null); a.updatedText = o.optString("upd", null);
            a.addedAt = o.optLong("added", System.currentTimeMillis());
            dao.insert(a); n++;
        }
        return n;
    }
}
