# Apav — Play Store update tracker

Play Store link দিয়ে অ্যাপ যোগ করুন; নতুন version এলে notification আসবে ও কার্ডে **UPDATE** highlight হবে।

**Stack:** Java • Material 3 • Room • WorkManager • Jsoup/OkHttp • Glide • Google Drive (appDataFolder) • Poppins font

## GitHub Action দিয়ে build
1. এই ফোল্ডারটা GitHub repo-তে push করুন (branch: `main`)
2. Actions → *Build Apav APK* → Artifacts থেকে APK নিন
3. Google Drive চালু করতে `SETUP_GOOGLE_DRIVE.md` দেখুন

## Features
Splash animation • Glass pill bottom nav (Home / + / Drive / Settings) • Update badge + glow • Pull to refresh • Background check (1–24h) • Notifications • Drive backup/restore + auto backup • Sort/filter • Wi-Fi only • আরও অনেক setting
