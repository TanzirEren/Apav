package com.tanzirdev.apav;
import android.net.Uri;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.*;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/** Play Store পেজ থেকে নাম, আইকন, ভার্সন ও আপডেট তারিখ বের করে। Play পেজ বদলালে নিচের regex ঠিক করতে হতে পারে। */
public class PlayScraper {
    public static class Info { public String title, icon, version, updated; }
    static final OkHttpClient HTTP = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS).build();
    static final Pattern VER = Pattern.compile("\\[\\[\\[\"(\\d+(?:\\.\\d+)+[^\"\\\\]{0,20})\"\\]\\]");
    static final Pattern UPD = Pattern.compile("Updated on.{0,300}?>\\s*([A-Z][a-z]{2,8} \\d{1,2}, \\d{4})\\s*<", Pattern.DOTALL);

    public static String pkgFromLink(String link) {
        if (link == null) return null;
        link = link.trim();
        if (link.matches("^[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+$")) return link;
        try { return Uri.parse(link).getQueryParameter("id"); } catch (Exception e) { return null; }
    }

    public static Info fetch(String pkg) throws IOException {
        Request r = new Request.Builder().url("https://play.google.com/store/apps/details?id=" + pkg + "&hl=en&gl=US")
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36")
            .header("Accept-Language", "en-US,en;q=0.9").build();
        try (Response resp = HTTP.newCall(r).execute()) {
            if (resp.code() == 404) throw new IOException("App not found on Play Store");
            if (!resp.isSuccessful() || resp.body() == null) throw new IOException("HTTP " + resp.code());
            String html = resp.body().string();
            Document d = Jsoup.parse(html);
            Info i = new Info();
            String t = d.select("meta[property=og:title]").attr("content");
            if (t.isEmpty()) t = d.title();
            i.title = t.replace(" - Apps on Google Play", "").trim();
            String ic = d.select("meta[property=og:image]").attr("content");
            if (ic.isEmpty()) ic = d.select("img[itemprop=image]").attr("src");
            i.icon = ic.isEmpty() ? null : ic;
            Matcher m = VER.matcher(html);
            if (m.find()) i.version = m.group(1);
            m = UPD.matcher(html);
            if (m.find()) i.updated = m.group(1);
            return i;
        }
    }
}
