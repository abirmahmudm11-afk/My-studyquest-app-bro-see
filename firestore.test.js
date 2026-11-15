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
