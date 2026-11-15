# StudyQuest Distribution Guide

This folder contains the updated debug-signed Android packages for **StudyQuest** (`com.aistudio.studyquest.bdxkpq`):

- `StudyQuest-debug.apk` — Ready-to-install Android APK for testing on Android phones and **itch.io**.
- `StudyQuest-debug.aab` — Android App Bundle (debug-signed) generated from the latest verified build.

> **Note on Binary Files**: `.apk` and `.aab` files are compiled binary archives, not text files. Download them directly from the file explorer rather than opening them inside the text editor.

---

## 1. Installing & Testing on Your Android Phone

1. Download `src/StudyQuest-debug.apk` and transfer it to your Android device.
2. Open the `.apk` file on your device and allow **Install unknown apps** for your file manager/browser if prompted.
3. Launch **StudyQuest** and tap **Continue with Google**.
   - The debug signing certificate SHA-1 (`8F:22:9F:C4:74:CC:B3:0A:48:BA:13:60:30:1E:BB:AB:9C:F8:EB:38`) is registered in the provisioned Firebase project (`gen-lang-client-0309270439`).

---

## 2. Uploading to itch.io

1. Open your **itch.io Creator Dashboard** → **Create new project** (or edit an existing project).
2. Set **Kind of project** to **Downloadable**.
3. Upload `src/StudyQuest-debug.apk` and check the **Android** platform checkbox.
4. Click **Save & view page**.

---

## 3. Publishing to Google Play Console (When Ready for Store Release)

Google Play requires an upload-key-signed release `.aab` (`gradle :app:bundleRelease`) rather than a debug build:

1. Create an Android upload keystore (`my-upload-key.jks`, alias `upload`) locally or via Android Studio **Build → Generate Signed Bundle / APK**, and register its SHA-1 certificate fingerprint in **Firebase Console → Project Settings → Your Android App (`com.aistudio.studyquest.bdxkpq`)**.
2. Build the signed release `.aab`, upload it in **Google Play Console → Testing / Production → Create new release**, and also add Google Play App Signing's SHA-1 certificate fingerprint to Firebase Console so Play Store downloads can sign in with Google.
