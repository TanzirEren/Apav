package com.tanzirdev.apav;

import android.content.Context;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.Scope;
import java.util.List;
import okhttp3.*;
import org.json.*;

/** Google Drive REST API (appDataFolder) সরাসরি OkHttp দিয়ে — কোনো বাড়তি Drive library লাগে না। */
public class DriveHelper {
    static final String SCOPE = "https://www.googleapis.com/auth/drive.appdata";
    static final String FILE = "apav_backup.json";
    static final String API = "https://www.googleapis.com/drive/v3/files";
    static final String UPLOAD = "https://www.googleapis.com/upload/drive/v3/files";
    static final MediaType JSON = MediaType.parse("application/json; charset=UTF-8");

    public static GoogleSignInClient client(Context c) {
        GoogleSignInOptions o = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail().requestScopes(new Scope(SCOPE)).build();
        return GoogleSignIn.getClient(c, o);
    }
    public static GoogleSignInAccount account(Context c) {
        GoogleSignInAccount a = GoogleSignIn.getLastSignedInAccount(c);
        return (a != null && GoogleSignIn.hasPermissions(a, new Scope(SCOPE))) ? a : null;
    }
    static String token(Context c) throws Exception {
        return GoogleAuthUtil.getToken(c, account(c).getAccount(), "oauth2:" + SCOPE);
    }
    static String send(Request.Builder rb, String tok) throws Exception {
        try (Response r = PlayScraper.HTTP.newCall(rb.header("Authorization", "Bearer " + tok).build()).execute()) {
            String body = r.body() != null ? r.body().string() : "";
            if (!r.isSuccessful()) throw new Exception("Drive error " + r.code() + ": " + body);
            return body;
        }
    }
    static String findId(String tok) throws Exception {
        HttpUrl u = HttpUrl.parse(API).newBuilder().addQueryParameter("spaces", "appDataFolder")
            .addQueryParameter("q", "name='" + FILE + "'").addQueryParameter("fields", "files(id)").build();
        JSONArray f = new JSONObject(send(new Request.Builder().url(u), tok)).getJSONArray("files");
        return f.length() == 0 ? null : f.getJSONObject(0).getString("id");
    }
    public static void backup(Context c) throws Exception {
        JSONArray arr = new JSONArray();
        for (AppItem a : Db.get(c).dao().all()) {
            arr.put(new JSONObject().put("name", a.name).put("pkg", a.packageName).put("link", a.link)
                .put("icon", a.iconUrl).put("ver", a.version).put("upd", a.updatedText).put("added", a.addedAt));
        }
        String data = new JSONObject().put("app", "Apav").put("apps", arr).toString();
        String tok = token(c);
        String id = findId(tok);
        if (id != null) {
            send(new Request.Builder().url(UPLOAD + "/" + id + "?uploadType=media").patch(RequestBody.create(data, JSON)), tok);
        } else {
            String meta = new JSONObject().put("name", FILE).put("parents", new JSONArray().put("appDataFolder")).toString();
            MultipartBody mb = new MultipartBody.Builder().setType(MediaType.parse("multipart/related"))
                .addPart(RequestBody.create(meta, JSON)).addPart(RequestBody.create(data, JSON)).build();
            send(new Request.Builder().url(UPLOAD + "?uploadType=multipart").post(mb), tok);
        }
        Prefs.sp(c).edit().putLong("last_backup", System.currentTimeMillis()).apply();
    }
    /** কতগুলো নতুন অ্যাপ যোগ হলো তা ফেরত দেয়। -1 = Drive-এ backup নেই */
    public static int restore(Context c) throws Exception {
        String tok = token(c);
        String id = findId(tok);
        if (id == null) return -1;
        String json = send(new Request.Builder().url(API + "/" + id + "?alt=media"), tok);
        JSONArray arr = new JSONObject(json).getJSONArray("apps");
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
