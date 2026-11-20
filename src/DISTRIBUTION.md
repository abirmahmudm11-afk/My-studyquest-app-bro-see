# StudyQuest Distribution Guide

This folder contains the debug-signed Android packages for **StudyQuest** (`com.aistudio.studyquest.bdxkpq`):

- `StudyQuest-download.apk` — Ready-to-install debug-signed Android APK for direct download on Android phones and **itch.io**.
- `StudyQuest-google-play-console-itch.aab` — Debug-signed Android App Bundle (`.aab`) generated from the latest verified build.

> **Note on Binary Files**: `.apk` and `.aab` files are compiled binary archives, not text files. Download them directly from the file explorer rather than opening them inside the text editor.

---

## 1. Installing & Testing on Your Android Phone

1. Download `src/StudyQuest-download.apk` and transfer it to your Android device.
2. Open the `.apk` file on your device and allow **Install unknown apps** for your file manager/browser if prompted.
3. Launch **StudyQuest** and sign in with **Email/Password** or **Continue with Google**.
   - **Firebase Project ID**: `gen-lang-client-0309270439`
   - **Firestore Database ID**: `ai-studio-android-mystudyq-376f8234-00ff-4c1d-9bbd-33197f904eea`
   - **Debug Signing SHA-1**: `D0:C9:F0:C2:20:F8:BD:6D:76:77:38:48:B1:E5:A9:56:89:D0:BC:5F`
   - **Debug Signing SHA-256**: `C6:F3:1E:B1:40:04:09:86:B1:5D:47:6E:80:A5:98:A2:EB:65:9A:02:3F:E7:B4:0E:6C:30:DC:BA:FB:09:1C:5B`
   - **Email/Password Auth**: Ensure **Email/Password** is enabled under **Firebase Console → Authentication → Sign-in method** for project `gen-lang-client-0309270439`.

---

## 2. AI Provider & Secrets Configuration

- **Firebase AI Logic & App Check**: App Check is initialized at startup (`AppCheckInitializer.kt`) for Firebase AI Logic (`gemini-2.5-flash` / `gemini-3.8-flash` fallback) without requiring a Gemini API key.
- **OpenRouter (Optional / Admin Studio)**: Uses `BuildConfig.OPENROUTER_API_KEY` injected via the **AI Studio Secrets panel** (`OPENROUTER_API_KEY`).
- **Before Play Store Release**: Never ship a raw OpenRouter secret inside a public release APK—proxy OpenRouter requests through a Firebase Cloud Function / backend gateway before distributing publicly.
- **Pay-As-You-Go AI**: **Puter.js** runs in an isolated WebView for students who want direct Pay-As-You-Go billing or model choice without using StudyQuest plan credits.

---

## 3. Uploading to itch.io

1. Open your **itch.io Creator Dashboard** → **Create new project** (or edit an existing project).
2. Set **Kind of project** to **Downloadable**.
3. Upload `src/StudyQuest-download.apk` (Android users install `.apk` directly from itch.io) and check the **Android** platform checkbox.
4. Click **Save & view page**.

---

## 4. Publishing to Google Play Console (When Ready for Store Release)

While `src/StudyQuest-google-play-console-itch.aab` is a valid debug-signed `.aab`, **Google Play Console** requires an upload-key-signed **release** `.aab` (`bundleRelease`) rather than a debug-signed bundle:

1. **Generate a signed release AAB**: Use AI Studio's Settings menu (**Generate APK/AAB**) with your upload keystore credentials (`KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`), or export the project and build locally in Android Studio via **Build → Generate Signed Bundle / APK** (`my-upload-key.jks`, alias `upload`).
2. **Register Play App Signing & Upload Key fingerprints in Firebase**:
   - In **Firebase Console → Project Settings → Your Android App (`com.aistudio.studyquest.bdxkpq`)**, add the SHA-1 and SHA-256 fingerprints of both your upload keystore and **Google Play Console → Setup → App signing** so users downloading from Google Play can sign in with Google.
3. **Create your Play Store release**: Upload the release-signed `.aab` under **Google Play Console → Testing / Production → Create new release**. Keep your Play Store listing title within 30 characters without emojis or promotional buzzwords.
