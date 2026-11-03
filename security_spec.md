# Groomly Security Specification (Phase 0 TDD)

## 1. Data Invariants
1. **Authentication Mandatory**: No unauthenticated caller may read or write any document in `/users/{userId}`, `/appointments/{appointmentId}`, `/queue_tickets/{ticketId}`, `/provider_businesses/{businessId}`, or `/reviews/{reviewId}`.
2. **Strict Tenant Isolation**: Every document is strictly owned by `userId == request.auth.uid`. List queries MUST filter by `where("userId", "==", request.auth.uid)` and rules enforce `resource.data.userId == request.auth.uid`.
3. **Immutable Ownership & Creation Timestamps**: `userId` and `createdAt` can never be mutated on `update`.
4. **Server Timestamp Bounds**: `createdAt` (on create) and `updatedAt` (on create/update) must be `timestamp` and `<= request.time`.
5. **Terminal State Locking**:
   - Appointments in `'CANCELLED'` or `'REFUNDED'` status cannot be further updated.
   - Queue tickets in `'COMPLETED'` or `'CANCELLED'` status cannot be further updated.
6. **Bounded Arrays & Strings**: All strings enforce `.size()` bounds and arrays (`savedAddresses`, `favouriteProviderIds`) enforce `size() <= 10`.

## 2. The "Dirty Dozen" Payloads
1. **Unauthenticated Read**: `unauthDb.collection("appointments").get()` -> Reject (`PERMISSION_DENIED`).
2. **Cross-User Read (PII / Booking Leak)**: Alice reads Bob's `/appointments/bob_appt` -> Reject.
3. **Unfiltered List Query (Query Trust Test)**: Alice runs `db.collection("appointments").get()` without `.where("userId", "==", ALICE_UID)` -> Reject.
4. **Identity Spoofing on Create**: Alice creates an appointment with `userId: BOB_UID` -> Reject.
5. **Shadow Update (Ghost Field Injection)**: Alice updates her appointment with `{ "isAdmin": true }` -> Reject via `hasOnly()`.
6. **Ownership Transfer on Update**: Alice updates `userId` on her existing appointment to `BOB_UID` -> Reject.
7. **CreatedAt Tampering**: Alice modifies `createdAt` during an appointment update -> Reject.
8. **Terminal State Mutation**: Alice attempts to reschedule an already `'CANCELLED'` appointment -> Reject.
9. **Value Poisoning (Out-of-Bounds Rating)**: Alice creates a review with `rating: 99` or negative payment `totalAmountInr: -500` -> Reject via `isValidReview()` / `isValidAppointment()`.
10. **Bounded Array Overflow**: Alice writes 15 items to `savedAddresses` in `/users/{userId}` -> Reject (`size() <= 10`).
11. **Invalid Enum Injection**: Alice sets appointment `status: 'HACKED'` -> Reject.
12. **Future Timestamp Spoofing**: Client sends `createdAt` set 1 hour in the future (`> request.time`) -> Reject.
