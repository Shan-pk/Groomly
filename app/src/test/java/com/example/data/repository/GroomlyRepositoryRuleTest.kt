package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.robolectric.annotation.Config

@Config(sdk = [36])
class GroomlyRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAppointment_validPayload_createsAndEmitsInRealtimeFlow() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = GroomlyRepository(firestore)

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createAppointment(
                providerId = "prov_royal_gents",
                providerName = "Royal Gents Salon",
                vertical = "SALON",
                serviceSummary = "Premium Grooming Package (Haircut + Beard + Facial)",
                staffId = "staff_ahmed",
                staffName = "Ahmed – Senior Barber",
                dateIso = "2026-11-05",
                timeSlot = "11:30 AM",
                durationMinutes = 75,
                totalAmountInr = 500,
                advancePaidInr = 100,
                remainingBalanceInr = 400,
                paymentMethod = "UPI",
                paymentType = "ADVANCE",
                couponCode = "GROOM100",
                isHomeService = false
            )
        }
        assertTrue(result.isSuccess)
        val appointmentId = result.getOrThrow()
        assertTrue(appointmentId.isNotEmpty())

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeAppointments().first { list -> list.any { it.id == appointmentId } }
        }
        assertTrue(emitted.any { it.id == appointmentId && it.advancePaidInr == 100L && it.remainingBalanceInr == 400L })
    }

    @Test
    fun joinLiveQueue_andProgressTicket_succeedsForAuthenticatedOwner() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = GroomlyRepository(firestore)

        val createRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.joinLiveQueue(
                providerId = "prov_royal_gents",
                providerName = "Royal Gents Salon",
                serviceName = "Signature Skin Fade",
                staffName = "Ahmed – Senior Barber",
                queueNumber = 15,
                currentlyServingNumber = 12,
                estimatedWaitMinutes = 24
            )
        }
        assertTrue(createRes.isSuccess)
        val ticketId = createRes.getOrThrow()

        val updateRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.updateQueueTicketState(
                ticketId = ticketId,
                currentlyServingNumber = 14,
                estimatedWaitMinutes = 8,
                newStatus = "APPROACHING"
            )
        }
        assertTrue(updateRes.isSuccess)
    }

    @Test
    fun getAppointmentById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = GroomlyRepository(firestore)
        val appointmentId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createAppointment(
                providerId = "prov_royal_gents",
                providerName = "Royal Gents Salon",
                vertical = "SALON",
                serviceSummary = "Beard Sculpting",
                staffId = "staff_sameer",
                staffName = "Sameer – Beard Specialist",
                dateIso = "2026-11-06",
                timeSlot = "04:00 PM",
                durationMinutes = 30,
                totalAmountInr = 350,
                advancePaidInr = 100,
                remainingBalanceInr = 250,
                paymentMethod = "UPI",
                paymentType = "ADVANCE"
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = GroomlyRepository(firestore)
        val bobFetch = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getAppointmentById(appointmentId)
        }
        assertTrue(bobFetch.isFailure)
        val ex = bobFetch.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(ex)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ex?.code)
    }

    @Test
    fun observeAppointments_unauthenticatedCaller_failsWithError() = runBlocking {
        auth.signOut()
        val repository = GroomlyRepository(firestore)

        var caught: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeAppointments().first()
            }
        } catch (t: Throwable) {
            caught = t
        }
        assertNotNull(caught)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@groomly.test"
        const val BOB_EMAIL = "bob@groomly.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
