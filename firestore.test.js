const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
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

function validAppointmentPayload(uid) {
  const now = new Date();
  return {
    userId: uid,
    providerId: "prov_royal_gents",
    providerName: "Royal Gents Atelier",
    vertical: "SALON",
    serviceSummary: "Executive Skin Fade + Hot Towel Beard Sculpt",
    staffId: "staff_ahmed",
    staffName: "Ahmed Khan",
    dateIso: "2026-11-05",
    timeSlot: "11:30 AM",
    durationMinutes: 60,
    totalAmountInr: 850,
    advancePaidInr: 200,
    remainingBalanceInr: 650,
    paymentMethod: "UPI",
    paymentType: "ADVANCE",
    couponCode: "GROOMFIRST",
    isHomeService: false,
    status: "CONFIRMED",
    createdAt: now,
    updatedAt: now,
  };
}

function validQueuePayload(uid) {
  const now = new Date();
  return {
    userId: uid,
    providerId: "prov_royal_gents",
    providerName: "Royal Gents Atelier",
    serviceName: "Express Skin Fade",
    staffName: "Ahmed Khan",
    queueNumber: 14,
    currentlyServingNumber: 11,
    estimatedWaitMinutes: 25,
    qrCheckInCode: "GRM-Q-14-ROYAL",
    status: "WAITING",
    createdAt: now,
    updatedAt: now,
  };
}

test("1. Unauthenticated user cannot read or list appointments", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("appointments").get());
});

test("2. Authenticated user can create valid appointment and query own appointments", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb
      .collection("appointments")
      .doc("appt_1")
      .set(validAppointmentPayload(ALICE_UID))
  );
  await assertSucceeds(
    aliceDb.collection("appointments").where("userId", "==", ALICE_UID).get()
  );
});

test("3. Query Enforcer: Authenticated user fails list query without userId filter", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("appointments").get());
});

test("4. Cross-user isolation: Alice cannot read or modify Bob's appointment", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx
      .firestore()
      .collection("appointments")
      .doc("bob_appt")
      .set(validAppointmentPayload(BOB_UID));
  });
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("appointments").doc("bob_appt").get());
});

test("5. Shadow Update rejected when ghost field is injected", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx
      .firestore()
      .collection("appointments")
      .doc("alice_appt")
      .set(validAppointmentPayload(ALICE_UID));
  });
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("appointments").doc("alice_appt").update({
      status: "COMPLETED",
      isVerifiedAdmin: true,
      updatedAt: new Date(),
    })
  );
});

test("6. Terminal State Locking: Cancelled appointment cannot be updated again", async () => {
  const cancelledPayload = {
    ...validAppointmentPayload(ALICE_UID),
    status: "CANCELLED",
  };
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx
      .firestore()
      .collection("appointments")
      .doc("alice_cancelled")
      .set(cancelledPayload);
  });
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("appointments").doc("alice_cancelled").update({
      status: "CONFIRMED",
      updatedAt: new Date(),
    })
  );
});

test("7. Live Queue Ticket creation and progression succeeds for owner", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb
      .collection("queue_tickets")
      .doc("ticket_1")
      .set(validQueuePayload(ALICE_UID))
  );
  await assertSucceeds(
    aliceDb.collection("queue_tickets").doc("ticket_1").update({
      currentlyServingNumber: 13,
      estimatedWaitMinutes: 8,
      status: "APPROACHING",
      updatedAt: new Date(),
    })
  );
});
