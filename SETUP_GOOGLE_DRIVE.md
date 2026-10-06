# Google Drive Connect — ধাপে ধাপে সেটআপ (একবারই করতে হবে)

অ্যাপে কোনো Client ID বা secret কোডে বসাতে হয় না। শুধু Google Cloud-এ **package name + SHA-1** দিয়ে একটা Android OAuth client বানাতে হয়।

**Package name:** `com.tanzirdev.apav`

## ধাপ ১ — Keystore বানান (একবার)
কম্পিউটারে বা Termux-এ:
```
keytool -genkeypair -v -keystore apav.jks -alias apav -keyalg RSA -keysize 2048 -validity 10000
```
Password/alias মনে রাখুন। **এই ফাইল হারাবেন না**, প্রতিবার একই keystore দিয়ে build করতে হবে।

SHA-1 বের করুন:
```
keytool -list -v -keystore apav.jks -alias apav
```
`SHA1:` লাইনটা কপি করুন।

## ধাপ ২ — GitHub Secrets যোগ করুন
Repo → Settings → Secrets and variables → Actions → New repository secret:

| Name | Value |
|---|---|
| `KEYSTORE_BASE64` | `base64 -w0 apav.jks` এর output |
| `KEYSTORE_PASSWORD` | keystore password |
| `KEY_ALIAS` | `apav` |
| `KEY_PASSWORD` | key password |

এগুলো থাকলে debug ও release দুই APK-ই এই keystore দিয়ে sign হবে, তাই SHA-1 সবসময় একই থাকবে।

## ধাপ ৩ — Google Cloud Console
1. https://console.cloud.google.com → **New Project** (নাম: Apav)
2. **APIs & Services → Library → Google Drive API → Enable**
3. **OAuth consent screen**
   - User type: **External** → App name: Apav, support email দিন
   - **Scopes → Add:** `.../auth/drive.appdata`
   - **Test users:** আপনার Gmail যোগ করুন (Testing mode-এ শুধু test user লগইন করতে পারবে)
4. **Credentials → Create credentials → OAuth client ID**
   - Application type: **Android**
   - Package name: `com.tanzirdev.apav`
   - SHA-1: ধাপ ১-এর SHA-1
   - Create

(ইচ্ছা হলে debug keystore-এর SHA-1 দিয়ে আরেকটা client বানাতে পারেন, কিন্তু উপরের secrets দিলে দরকার নেই।)

## ধাপ ৪ — Build ও test
1. GitHub-এ push করুন → **Actions** ট্যাবে build চলবে → **Artifacts → Apav-APK** ডাউনলোড করুন
2. APK install করুন → নিচের বারে **Cloud আইকন** চাপুন → **Connect Google Drive**
3. Gmail account বেছে নিন → Allow → Connected দেখাবে
4. **Backup now / Restore** টেস্ট করুন

## সমস্যা হলে
| সমস্যা | সমাধান |
|---|---|
| `Sign-in failed: 10` | SHA-1 বা package name ভুল / OAuth client বানানো হয়নি |
| `Sign-in failed: 12500` | OAuth consent screen অসম্পূর্ণ, বা Gmail test user-এ নেই |
| `403 accessNotConfigured` | Drive API Enable করা হয়নি |
| Keystore বদলালে | নতুন SHA-1 দিয়ে নতুন Android OAuth client বানান |
