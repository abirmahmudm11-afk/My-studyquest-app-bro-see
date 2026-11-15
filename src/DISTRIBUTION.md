# StudyQuest Distribution Guide

This folder contains the debug-signed Android packages for **StudyQuest** (`com.aistudio.studyquest.bdxkpq`):

- `StudyQuest-debug.apk` — Ready-to-install Android APK for testing on Android phones and **itch.io**.
- `StudyQuest-debug.aab` — Android App Bundle (debug-signed) generated from the latest verified build.

> **Note on Binary Files**: `.apk` and `.aab` files are compiled binary archives, not text files. Download them directly from the file explorer rather than opening them inside the text editor.

---

## 1. Installing & Testing on Your Android Phone

1. Download `src/StudyQuest-debug.apk` and transfer it to your Android device.
2. Open the `.apk` file on your device and allow **Install unknown apps** for your file manager/browser if prompted.
3. Launch **StudyQuest** and tap **Continue with Google**.
   - The debug signing certificate SHA-1 (`1B:A0:E0:ED:58:C0:59:BE:32:BD:38:1A:91:4A:96:22:EF:48:66:37`) and SHA-256 (`54:2A:D6:FC:E7:E0:20:43:B0:24:E2:A9:1D:42:D1:3F:E7:74:A2:EB:5D:EF:56:17:B5:5A:97:09:23:09:D4:35`) are registered in the provisioned Firebase project (`gen-lang-client-0291195118`, Firestore database `ai-studio-android-mystudyq-3c348ead-429d-448f-9a07-4be9722d2e92`).

---

## 2. AI Provider & Secrets Configuration

- **Subscription AI (`StudyQuest AI` ৳150/mo & Quest Plans)**: Uses `BuildConfig.OPENROUTER_API_KEY` injected via the **AI Studio Secrets panel** (`OPENROUTER_API_KEY`) with `StudyQuestModelRouter` (`gpt-6-luna` default, `gpt-6.1-sol` reasoning, `gpt-6-astra` admin-only) and Firebase AI (`gemini-3.8-flash`) fallback.
- **Before Play Store Release**: Never ship a raw OpenRouter secret inside a public release APK—proxy OpenRouter requests through a Firebase Cloud Function / backend gateway, and rotate any key previously pasted in chat.
- **Pay-As-You-Go AI**: **Puter.js** runs in an isolated WebView for students who want direct Pay-As-You-Go billing or model choice without using StudyQuest plan credits.
- **Subscription Scope**: All 7 Quest Plans cover AI Helper credits, animated short-video credits, and ad-free study (with **Video Boost** at ৳20 one-time for +1 60s video credit). There are no storage plans or storage add-ons.

---

## 3. Uploading to itch.io

1. Open your **itch.io Creator Dashboard** → **Create new project** (or edit an existing project).
2. Set **Kind of project** to **Downloadable**.
3. Upload `src/StudyQuest-debug.apk` and check the **Android** platform checkbox.
4. Click **Save & view page**.

---

## 4. Publishing to Google Play Console (When Ready for Store Release)

Google Play Console requires an upload-key-signed **release** `.aab` (`bundleRelease`) rather than a debug build:

1. **Generate a signed release AAB**: Either use AI Studio's Settings menu (**Generate APK/AAB**) with your upload keystore credentials (`KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`), or export the project and build locally in Android Studio via **Build → Generate Signed Bundle / APK** (`my-upload-key.jks`, alias `upload`).
2. **Register Play App Signing & Upload Key fingerprints in Firebase**:
   - In **Firebase Console → Project Settings → Your Android App (`com.aistudio.studyquest.bdxkpq`)**, add the SHA-1 and SHA-256 fingerprints of both your upload keystore and **Google Play Console → Setup → App signing** so users downloading from Google Play can sign in with Google.
3. **Create your Play Store release**: Upload the signed release `.aab` under **Google Play Console → Testing / Production → Create new release**. Keep your Play Store listing title within 30 characters without emojis or promotional buzzwords.
