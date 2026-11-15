# StudyQuest — Learn. Connect. Compete. Achieve.

Bangladesh-first Android educational mobile application built with **Kotlin**, **Jetpack Compose (Material 3)**, **Room** (offline-first caching & spaced repetition), **Cloud Firestore**, **Firebase Authentication (Google Sign-In via Credential Manager)**, **Firebase AI Logic (`gemini-3.8-flash`)**, **App Check**, **WorkManager**, and **DataStore**.

---

## Key Features

### 1. Bilingual Experience (Bangla & English)
- First-launch Bangla (`বাংলা`) / English language selector before onboarding.
- Independent **App UI Language** and **Academic Content / Question Language** preferences persisted in DataStore and user profile.
- Bundled local typography (`Noto Sans Bengali`, `Plus Jakarta Sans`, `JetBrains Mono`) and Material 3 adaptive color system.

### 2. Academic Excellence (Classes 6–10 & SSC)
- **NCTB-Aligned Curriculum Catalog**: Classes 6–10 and SSC across Bangla Version & English Version, with SSC Group support (Science, Business Studies, Humanities) and labeled future-ready placeholders for HSC & Admission Prep.
- **Offline-First Chapter Downloads**: Download chapters to Room (`CachedLessonEntity`, `CachedQuestionEntity`) for offline reading, formula inspection, and practice.
- **Multi-Format Question Bank & Mock Exams**:
  - 9 question formats: Single-Choice MCQ, Multi-Select MCQ, True/False, Fill-in-the-Blank, Short Answer, Creative Question (CQ with ক/খ/গ/ঘ stems & marking rubric), Matching, Ordering/Steps, and Formula/Numeric.
  - Filters for Class, Subject, Chapter, Source/Board, Difficulty, Question Type, High-Yield, Offline-Only, and Unanswered.
  - Quick Practice and Timed Mock Exams with configurable negative marking, explicit CQ self-assessment vs. objective grading clarity, and weak-topic identification.
- **SM-2 Spaced Repetition & Study Planner**: Bookmark cards, schedule SM-2 review intervals (`LocalRevisionEntity`), and manage personal study plans (`StudyPlanDoc`).
- **AI Doubt Solver**: Curriculum-aware, streaming step-by-step explanations powered by **Firebase AI Logic** (`Firebase.ai.generativeModel("gemini-3.8-flash")`) with App Check verification and Admin kill-switch (`aiSolverEnabled`).

### 3. Social Learning & Safe Student Community
- Unique `SQ-XXXXXX` StudyQuest IDs and unique `@username` discovery.
- Friend requests, accepted friendships, online/offline presence indicators (respecting user privacy settings), and user blocking/reporting.
- Direct 1:1 chats and invitation-based Study Groups with message replies, emoji reactions, mute controls, and suspicious-link warnings.

### 4. Healthy Competition & Gamification
- **Live 1v1 Matchmaking & Private Rooms**: Genuine Firestore matchmaking queue (with explicit wait/cancel controls and zero fake bot opponents), 6-character private competition room codes, and direct friend/group challenges.
- **Deterministic Answer Submissions**: `${uid}_${questionId}` answer document IDs prevent duplicate scoring or replay exploits.
- **Tournaments & Multi-Scope Leaderboards**: Seasonal tournaments, Friends, Class, Subject, Opt-in National, Weekly, and Seasonal rankings.
- **Progression & Quests**: XP, StudyCoins, overall and subject levels, daily/weekly quests, achievements, and coin-based cosmetic avatar shop.

### 5. Role-Gated In-App Admin Studio
- Access gated in both UI and `firestore.rules` by verified role records (`owner`, `administrator`, `content_editor`, `academic_reviewer`, `moderator`, `support_staff`), plus bootstrapped owner email (`abirmahmudm116@gmail.com` with `email_verified == true`).
- 6-stage question editorial workflow (`draft` → `pending_review` → `changes_requested` → `approved` → `published` → `archived`), moderation queue resolution, AI/Season/Maintenance feature flags, tournament creation, and append-only audit logs.

---

## Architecture & Persistence Split

- **Room (`StudyQuestDatabase`)**: Downloaded lessons & questions, offline progress queue (`PendingSyncEntity`), bookmarks (`LocalBookmarkEntity`), and SM-2 spaced-repetition cards (`LocalRevisionEntity`). Synchronized via `SyncWorker` (`WorkManager`).
- **Cloud Firestore (`ai-studio-android-3340c22c-2211-48a0-bcbd-9da93a235842`)**: Authoritative user profiles, friendships, direct/group messaging, matchmaking, live rooms, leaderboards, study plans, notifications, moderation reports, tournaments, and admin configuration (`firestore.rules`).
- **DataStore (`StudyQuestPreferences`)**: Client preferences (languages, theme mode, reduced motion, sound effects, quiet hours).

---

## Testing & Verification

- **Firestore Security Rules (JS Emulator Suite)**: `firestore.test.js` validates owner/admin access, cross-user profile protection, chat privacy, matchmaking integrity, deterministic match submissions, and report handling against the local Firestore Emulator (`127.0.0.1:8085`).
- **Kotlin Repository Integration Tests**: `StudyQuestRepositoriesRuleTest.kt` exercises real repository read/write paths against Firebase Auth (`127.0.0.1:9099`) and Firestore (`127.0.0.1:8085`) emulators via Robolectric.
- **App Check**: Debug builds automatically install `DebugAppCheckProviderFactory`; release builds use `PlayIntegrityAppCheckProviderFactory`.

## Server-Side Deployment Note
- Security rules (`firestore.rules`) govern all client reads/writes directly against the provisioned Cloud Firestore database.
- Server-only infrastructure such as scheduled Cloud Functions for FCM push delivery, automated seasonal rank rollups, and Realtime Database ephemeral heartbeat triggers require server-side Cloud Functions / Blaze deployment from Firebase Console when scaling beyond client-synchronized Firestore listeners.
