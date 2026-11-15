package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.PairingReviewRepository
import com.example.model.buildPairingId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: PairingReviewRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = PairingReviewRepository(db.pairingReviewDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `app_name matches Cedar and Oak`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Cedar & Oak", appName)
    }

    @Test
    fun `submit reviews computes accurate average rating and supports update and delete`() = runBlocking {
        val cigarId = "padron_1964"
        val bevId = "cuban_cafecito"
        val pid = buildPairingId(cigarId, bevId)

        val firstId = repository.submitReview(
            cigarId = cigarId,
            beverageId = bevId,
            rating = 5,
            reviewText = "Velvety cocoa and sweet demerara crema.",
            reviewerName = "Tester A",
        )
        repository.submitReview(
            cigarId = cigarId,
            beverageId = bevId,
            rating = 3,
            reviewText = "Good, slightly bold.",
            reviewerName = "Tester B",
        )

        val summaries = repository.observeRatingSummaries().first()
        val summary = summaries[pid]
        assertNotNull(summary)
        assertEquals(4.0, summary!!.averageRating, 0.01)
        assertEquals(2, summary.reviewCount)

        // Update first review from 5 to 4
        repository.submitReview(
            cigarId = cigarId,
            beverageId = bevId,
            rating = 4,
            reviewText = "Balanced finish.",
            reviewerName = "Tester A",
            existingReviewId = firstId,
        )
        val updatedSummary = repository.observeRatingSummaries().first()[pid]
        assertEquals(3.5, updatedSummary!!.averageRating, 0.01)
        assertEquals(2, updatedSummary.reviewCount)

        // Delete first review -> only rating 3 remains
        repository.deleteReview(firstId)
        val afterDeleteSummary = repository.observeRatingSummaries().first()[pid]
        assertEquals(3.0, afterDeleteSummary!!.averageRating, 0.01)
        assertEquals(1, afterDeleteSummary.reviewCount)

        val otherSummary = repository.observeRatingSummaries().first()["nonexistent__pair"]
        assertNull(otherSummary)
    }
}
