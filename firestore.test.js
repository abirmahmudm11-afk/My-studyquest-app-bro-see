const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_student_1";
const BOB_UID = "bob_student_2";
const CHARLIE_UID = "charlie_outsider_3";
const ADMIN_EMAIL = "abirmahmudm116@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function serverTs() {
  return new Date();
}

test("Unauthenticated user cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("public_profiles").get());
  await assertFails(unauthDb.collection("friend_requests").get());
  await assertFails(unauthDb.collection("conversations").get());
  await assertFails(unauthDb.collection("matches").get());
});

test("Public profile: owner can create/update own non-PII profile, cannot tamper with Bob's profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("public_profiles").doc(ALICE_UID).set({
      userId: ALICE_UID,
      studyQuestId: "SQ-1001",
      username: "alice_bd",
      displayName: "Alice Rahman",
      avatarId: "owl_classic",
      classId: "class_10",
      curriculumVersion: "bangla",
      overallLevel: 1,
      totalXp: 0,
      coins: 50,
      createdAt: serverTs(),
      updatedAt: serverTs(),
    })
  );

  // Bob cannot overwrite Alice's profile
  await assertFails(
    bobDb.collection("public_profiles").doc(ALICE_UID).update({
      totalXp: 9999,
      updatedAt: serverTs(),
    })
  );
});

test("Private PII & Learning records: strict owner isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("private").doc("account").set({
      userId: ALICE_UID,
      email: "alice@example.com",
      accountStatus: "active",
      createdAt: serverTs(),
      updatedAt: serverTs(),
    })
  );

  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("private").doc("account").get()
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("learning_records").doc("rec_1").set({
      userId: ALICE_UID,
      recordType: "practice",
      refId: "math_ch1",
      subjectId: "math",
      idempotencyKey: "practice_math_ch1_1",
      score: 8,
      totalMarks: 10,
      xpAwarded: 26,
      createdAt: serverTs(),
      updatedAt: serverTs(),
    })
  );

  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("learning_records").get()
  );
});

test("Friend requests and friendships: only recipient can accept, non-members cannot read friendship", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("friend_requests").doc("req_1").set({
      fromUid: ALICE_UID,
      toUid: BOB_UID,
      fromDisplayName: "Alice",
      toDisplayName: "Bob",
      status: "pending",
      pairKey: `${ALICE_UID}_${BOB_UID}`,
      createdAt: serverTs(),
      updatedAt: serverTs(),
    })
  );

  // Alice cannot self-accept her own outgoing request
  await assertFails(
    aliceDb.collection("friend_requests").doc("req_1").update({
      status: "accepted",
      updatedAt: serverTs(),
    })
  );

  // Bob accepts the request
  await assertSucceeds(
    bobDb.collection("friend_requests").doc("req_1").update({
      status: "accepted",
      updatedAt: serverTs(),
    })
  );

  await assertSucceeds(
    bobDb.collection("friendships").doc("fr_1").set({
      memberUids: [ALICE_UID, BOB_UID],
      pairKey: `${ALICE_UID}_${BOB_UID}`,
      createdAt: serverTs(),
    })
  );

  await assertFails(charlieDb.collection("friendships").doc("fr_1").get());
});

test("Private messaging & Study Group: non-member cannot read or post messages", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("conversations").doc("conv_1").set({
      type: "group",
      name: "SSC Math Squad",
      ownerId: ALICE_UID,
      memberUids: [ALICE_UID, BOB_UID],
      invitedUids: [],
      createdAt: serverTs(),
      updatedAt: serverTs(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("conversations").doc("conv_1").collection("messages").doc("msg_1").set({
      senderUid: ALICE_UID,
      senderName: "Alice",
      memberUids: [ALICE_UID, BOB_UID],
      text: "Ready for chapter 3 trigonometry?",
      deliveryStatus: "sent",
      readByUids: [ALICE_UID],
      createdAt: serverTs(),
    })
  );

  // Bob can query messages with matching memberUids filter
  await assertSucceeds(
    bobDb.collection("conversations").doc("conv_1").collection("messages")
      .where("memberUids", "array-contains", BOB_UID)
      .get()
  );

  // Charlie (non-member) cannot read conversation or send messages
  await assertFails(charlieDb.collection("conversations").doc("conv_1").get());
  await assertFails(
    charlieDb.collection("conversations").doc("conv_1").collection("messages").doc("msg_2").set({
      senderUid: CHARLIE_UID,
      senderName: "Charlie",
      memberUids: [ALICE_UID, BOB_UID, CHARLIE_UID],
      text: "Intruder!",
      deliveryStatus: "sent",
      createdAt: serverTs(),
    })
  );
});

test("Multiplayer match: terminal state lock prevents altering completed match scores", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("matches").doc("match_1").set({
      mode: "quick_battle",
      hostUid: ALICE_UID,
      classId: "class_10",
      subjectId: "math",
      difficulty: "medium",
      status: "live",
      playerUids: [ALICE_UID, BOB_UID],
      readyUids: [ALICE_UID, BOB_UID],
      questionIds: ["q1", "q2", "q3"],
      questionCount: 3,
      durationSeconds: 90,
      scores: { [ALICE_UID]: 0, [BOB_UID]: 0 },
      createdAt: serverTs(),
      updatedAt: serverTs(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("matches").doc("match_1").collection("submissions").doc(`${ALICE_UID}_q1`).set({
      userId: ALICE_UID,
      matchId: "match_1",
      questionId: "q1",
      selectedIndex: 2,
      isCorrect: true,
      responseTimeMs: 3200,
      pointsAwarded: 10,
      submittedAt: serverTs(),
    })
  );

  // Submissions are append-only: cannot update/tamper after creation
  await assertFails(
    aliceDb.collection("matches").doc("match_1").collection("submissions").doc(`${ALICE_UID}_q1`).update({
      pointsAwarded: 100,
    })
  );

  // Complete match
  await assertSucceeds(
    aliceDb.collection("matches").doc("match_1").update({
      status: "completed",
      scores: { [ALICE_UID]: 30, [BOB_UID]: 20 },
      winnerUid: ALICE_UID,
      updatedAt: serverTs(),
    })
  );

  // Terminal state lock: Bob cannot mutate winner after completion
  await assertFails(
    bobDb.collection("matches").doc("match_1").update({
      winnerUid: BOB_UID,
      updatedAt: serverTs(),
    })
  );
});

test("Admin Studio: email spoofing without email_verified fails, verified owner succeeds", async () => {
  const spoofDb = testEnv.authenticatedContext("spoof_uid", {
    email: ADMIN_EMAIL,
    email_verified: false,
  }).firestore();

  const verifiedAdminDb = testEnv.authenticatedContext("admin_uid", {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();

  await assertFails(
    spoofDb.collection("academic_content").doc("c_1").set({
      contentType: "lesson",
      classId: "class_10",
      subjectId: "math",
      titleEn: "Sets",
      titleBn: "সেট",
      status: "published",
      authorUid: "spoof_uid",
      version: 1,
      createdAt: serverTs(),
      updatedAt: serverTs(),
    })
  );

  await assertSucceeds(
    verifiedAdminDb.collection("academic_content").doc("c_1").set({
      contentType: "lesson",
      classId: "class_10",
      subjectId: "math",
      titleEn: "Sets and Functions",
      titleBn: "সেট ও ফাংশন",
      status: "published",
      authorUid: "admin_uid",
      version: 1,
      createdAt: serverTs(),
      updatedAt: serverTs(),
    })
  );
});

test("Practice modules & leaderboard: allows pomodoro/flashcard/daily_challenge and streakCount, rejects invalid recordType or negative streak", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("public_profiles").doc(ALICE_UID).set({
      userId: ALICE_UID,
      studyQuestId: "SQ-1001",
      username: "alice_bd",
      displayName: "Alice Rahman",
      avatarId: "owl_classic",
      classId: "class_10",
      curriculumVersion: "bangla",
      academicGroup: "science",
      dailyGoalMinutes: 45,
      overallLevel: 2,
      totalXp: 120,
      coins: 65,
      streakCount: 3,
      lastChallengeDateKey: "2026-11-15",
      createdAt: serverTs(),
      updatedAt: serverTs(),
    })
  );

  await assertFails(
    aliceDb.collection("public_profiles").doc(ALICE_UID).update({
      academicGroup: "invalid_group",
      updatedAt: serverTs(),
    })
  );

  await assertFails(
    aliceDb.collection("public_profiles").doc(ALICE_UID).update({
      dailyGoalMinutes: 2,
      updatedAt: serverTs(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("study_plans").doc("onboarding_starter_goal").set({
      userId: ALICE_UID,
      title: "SSC Board Exam Readiness",
      subjectId: "math",
      planType: "exam_countdown",
      targetDateEpochMs: Date.now() + 86400000 * 30,
      minutesPerDay: 45,
      completed: false,
      createdAt: serverTs(),
      updatedAt: serverTs(),
    })
  );

  await assertFails(
    aliceDb.collection("public_profiles").doc(ALICE_UID).update({
      streakCount: -1,
      updatedAt: serverTs(),
    })
  );

  for (const rType of ["pomodoro", "flashcard", "daily_challenge"]) {
    await assertSucceeds(
      aliceDb.collection("users").doc(ALICE_UID).collection("learning_records").doc(`rec_${rType}`).set({
        userId: ALICE_UID,
        recordType: rType,
        refId: `ref_${rType}`,
        subjectId: "math",
        idempotencyKey: `key_${rType}`,
        score: 5,
        totalMarks: 5,
        xpAwarded: 25,
        createdAt: serverTs(),
        updatedAt: serverTs(),
      })
    );
  }

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("learning_records").doc("rec_bad").set({
      userId: ALICE_UID,
      recordType: "unknown_demo",
      refId: "bad",
      subjectId: "math",
      idempotencyKey: "key_bad",
      xpAwarded: 10,
      createdAt: serverTs(),
      updatedAt: serverTs(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("leaderboard_entries").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Rahman",
      studyQuestId: "SQ-1001",
      classId: "class_10",
      seasonId: "season_2026_1",
      weekKey: "2026_w46",
      subjectId: "all",
      weeklyPoints: 120,
      seasonalPoints: 120,
      competitionRating: 1000,
      rankTier: "Bronze",
      streakCount: 3,
      optInNational: true,
      updatedAt: serverTs(),
    })
  );

  // Bob cannot tamper with Alice's leaderboard entry
  await assertFails(
    bobDb.collection("leaderboard_entries").doc(ALICE_UID).update({
      weeklyPoints: 9999,
      updatedAt: serverTs(),
    })
  );
});

test("XP ledger is strictly append-only; badges, goals, and study sessions enforce owner & anti-farming bounds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  // XP Ledger create succeeds for valid positive award
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("xp_ledger").doc("tx_1").set({
      userId: ALICE_UID,
      sourceType: "lesson",
      sourceId: "lesson_trig_basics",
      xpDelta: 20,
      coinDelta: 5,
      description: "Lesson completed",
      idempotencyKey: "lesson_lesson_trig_basics_2026",
      createdAt: serverTs(),
    })
  );

  // XP Ledger is immutable from client (update and delete forbidden)
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("xp_ledger").doc("tx_1").update({ xpDelta: 500 })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("xp_ledger").doc("tx_1").delete()
  );

  // Focus sessions must have >= 60 focusedSeconds and endedAt > startedAt
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("study_sessions").doc("sess_1").set({
      userId: ALICE_UID,
      subjectId: "math",
      mode: "pomodoro",
      startedAtEpochMs: 1000000,
      endedAtEpochMs: 2500000,
      focusedSeconds: 1500,
      xpAwarded: 50,
      status: "completed",
      idempotencyKey: "focus_sess_1",
      createdAt: serverTs(),
    })
  );

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("study_sessions").doc("sess_fake").set({
      userId: ALICE_UID,
      subjectId: "math",
      mode: "pomodoro",
      startedAtEpochMs: 1000000,
      endedAtEpochMs: 1010000,
      focusedSeconds: 10,
      xpAwarded: 50,
      status: "completed",
      idempotencyKey: "focus_sess_fake",
      createdAt: serverTs(),
    })
  );

  // Time study goals must be >= 5 minutes
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("study_goals").doc("goal_1").set({
      userId: ALICE_UID,
      title: "Focus 30 mins in Physics",
      goalType: "time_minutes",
      periodType: "daily",
      subjectId: "physics",
      targetValue: 30,
      currentValue: 0,
      difficulty: "medium",
      xpReward: 40,
      completed: false,
      rewardClaimed: false,
      periodKey: "2026-11-15",
      createdAt: serverTs(),
      updatedAt: serverTs(),
    })
  );

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("study_goals").doc("goal_tiny").set({
      userId: ALICE_UID,
      title: "Fake 1 min goal",
      goalType: "time_minutes",
      periodType: "daily",
      subjectId: "physics",
      targetValue: 1,
      currentValue: 0,
      difficulty: "easy",
      xpReward: 40,
      completed: false,
      rewardClaimed: false,
      periodKey: "2026-11-15",
      createdAt: serverTs(),
      updatedAt: serverTs(),
    })
  );

  // Badges owner-isolated
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("badges").doc("badge_first_study").set({
      userId: ALICE_UID,
      badgeId: "badge_first_study",
      category: "study",
      rarity: "common",
      progress: 1,
      target: 1,
      unlocked: true,
      pinned: true,
      favorite: true,
      animationShown: true,
      updatedAt: serverTs(),
    })
  );
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("badges").get());
});

test("Class community, AI storyboard jobs, learning videos, subscription tier lock & competition archives", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const adminDb = testEnv.authenticatedContext("admin_uid", {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();

  // Alice creates a Class Community post; Bob can upvote/comment, cannot edit Alice's body
  await assertSucceeds(
    aliceDb.collection("community_posts").doc("post_1").set({
      authorUid: ALICE_UID,
      authorName: "Alice",
      authorClassId: "class_10",
      classId: "class_10",
      subjectId: "math",
      postType: "question",
      title: "How to solve quadratic roots?",
      body: "Please explain discriminant > 0 step by step.",
      upvoterUids: [],
      commentCount: 0,
      flagged: false,
      hidden: false,
      createdAt: serverTs(),
      updatedAt: serverTs(),
    })
  );

  await assertSucceeds(
    bobDb.collection("community_posts").doc("post_1").update({
      upvoterUids: [BOB_UID],
      updatedAt: serverTs(),
    })
  );

  await assertFails(
    bobDb.collection("community_posts").doc("post_1").update({
      body: "Hacked post body",
      updatedAt: serverTs(),
    })
  );

  await assertSucceeds(
    bobDb.collection("community_posts").doc("post_1").collection("comments").doc("c_1").set({
      postId: "post_1",
      authorUid: BOB_UID,
      authorName: "Bob",
      classId: "class_10",
      body: "Use b^2 - 4ac > 0 for two distinct real roots!",
      upvoterUids: [],
      isHelpful: false,
      createdAt: serverTs(),
    })
  );

  // Student can initialize free subscription but cannot self-upgrade to premium
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("subscription").doc("status").set({
      userId: ALICE_UID,
      tier: "free",
      billingProvider: "google_play_unconfigured",
      adEnabled: true,
      updatedAt: serverTs(),
    })
  );

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("subscription").doc("status").update({
      tier: "premium",
      updatedAt: serverTs(),
    })
  );

  await assertSucceeds(
    adminDb.collection("assignments").doc("assign_w1").set({
      titleEn: "Weekly Trigonometry Sprint",
      titleBn: "সাপ্তাহিক ত্রিকোণমিতি অনুশীলন",
      assignmentType: "weekly",
      classId: "class_10",
      subjectId: "math",
      difficulty: "medium",
      targetCount: 3,
      xpReward: 75,
      active: true,
      periodKey: "2026_w46",
      createdAt: serverTs(),
      updatedAt: serverTs(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("ai_video_jobs").doc("job_1").set({
      userId: ALICE_UID,
      sourceType: "chapter",
      sourceTitle: "Motion & Equations",
      classId: "class_10",
      subjectId: "physics",
      status: "completed",
      progressPercent: 100,
      externalRenderStatus: "unconfigured",
      createdAt: serverTs(),
      updatedAt: serverTs(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("competition_archives").doc("2026_w45_weekly_xp").set({
      periodKey: "2026_w45_weekly_xp",
      periodType: "weekly",
      category: "xp",
      winners: ["Alice (1200)"],
      archivedAt: serverTs(),
    })
  );
});

test("Quest Plan tiers, AI/Video credit ledgers, AI Lab conversations & learning_videos enforce owner isolation and prevent self-upgrade", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const adminDb = testEnv.authenticatedContext("admin_uid", {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();

  // Owner cannot self-create a paid tier like studyquest_ai
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("subscription").doc("status").set({
      userId: ALICE_UID,
      tier: "studyquest_ai",
      billingProvider: "google_play",
      adEnabled: false,
      monthlyAiLimit: 100,
      aiCreditsUsed: 0,
      monthlyVideoLimit: 2,
      videoCreditsUsed: 0,
      bonusVideoCredits: 0,
      updatedAt: serverTs(),
    })
  );

  // Admin can grant studyquest_ai; Alice can consume credits on it without altering tier/limits
  await assertSucceeds(
    adminDb.collection("users").doc(ALICE_UID).collection("subscription").doc("status").set({
      userId: ALICE_UID,
      tier: "studyquest_ai",
      activePlanName: "StudyQuest AI",
      billingProvider: "admin_grant",
      adEnabled: false,
      monthlyAiLimit: 100,
      aiCreditsUsed: 0,
      monthlyVideoLimit: 2,
      videoCreditsUsed: 0,
      bonusVideoCredits: 0,
      updatedAt: serverTs(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("subscription").doc("status").update({
      aiCreditsUsed: 1,
      updatedAt: serverTs(),
    })
  );

  // Alice cannot raise her own monthlyAiLimit
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("subscription").doc("status").update({
      monthlyAiLimit: 999,
      updatedAt: serverTs(),
    })
  );

  // AI & Video credit ledgers + AI Lab conversations are strictly owner-isolated
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("ai_credit_ledger").doc("res_1").set({
      userId: ALICE_UID,
      delta: -1,
      entryType: "reserve",
      toolId: "QuestTutor",
      modelUsed: "gpt-6-luna",
      createdAt: serverTs(),
    })
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("ai_credit_ledger").get()
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("ai_conversations").doc("conv_ai_1").set({
      userId: ALICE_UID,
      title: "Quadratic Formula",
      toolId: "StepByStepSolver",
      providerMode: "studyquest_ai",
      createdAt: serverTs(),
      updatedAt: serverTs(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("ai_conversations").doc("conv_ai_1")
      .collection("messages").doc("m_1").set({
        conversationId: "conv_ai_1",
        userId: ALICE_UID,
        role: "user",
        text: "Solve x^2 - 5x + 6 = 0",
        createdAt: serverTs(),
      })
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("ai_conversations").doc("conv_ai_1").get()
  );

  // Uploaded / Animated learning videos: public readable by signed-in peers, private readable only by owner
  await assertSucceeds(
    aliceDb.collection("learning_videos").doc("vid_pub_1").set({
      ownerUid: ALICE_UID,
      ownerName: "Alice",
      title: "Quadratic Animated Lesson",
      description: "Clean visual walkthrough",
      language: "en",
      videoSourceType: "animated_lesson",
      classId: "class_10",
      subjectId: "math",
      durationSeconds: 45,
      scenesJson: "[{\"title\":\"Intro\"}]",
      visibility: "public",
      createdAt: serverTs(),
      updatedAt: serverTs(),
    })
  );
  await assertSucceeds(
    bobDb.collection("learning_videos").doc("vid_pub_1").get()
  );

  await assertSucceeds(
    aliceDb.collection("learning_videos").doc("vid_priv_1").set({
      ownerUid: ALICE_UID,
      title: "My Uploaded Algebra Clip",
      videoSourceType: "uploaded_video",
      classId: "class_10",
      subjectId: "math",
      durationSeconds: 60,
      scenesJson: "[{\"title\":\"Clip\"}]",
      visibility: "private",
      createdAt: serverTs(),
      updatedAt: serverTs(),
    })
  );
  await assertFails(
    bobDb.collection("learning_videos").doc("vid_priv_1").get()
  );

  // ai_usage_logs: owner can write & read own logs; Bob cannot read Alice's usage logs
  await assertSucceeds(
    aliceDb.collection("ai_usage_logs").doc("log_1").set({
      userId: ALICE_UID,
      toolId: "QuestTutor",
      modelId: "gpt-6-luna",
      estimatedCostUsd: 0.002,
      creditsDeducted: 1,
      status: "success",
      createdAt: serverTs(),
    })
  );
  await assertFails(
    bobDb.collection("ai_usage_logs").doc("log_1").get()
  );
});


