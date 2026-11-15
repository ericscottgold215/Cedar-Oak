package com.example.data.repository

import com.example.data.local.PairingReviewDao
import com.example.data.local.PairingReviewEntity
import com.example.data.local.SeedReviews
import com.example.model.PairingRatingSummary
import com.example.model.PairingReview
import com.example.model.buildPairingId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PairingReviewRepository(
    private val dao: PairingReviewDao,
    private val prefs: UserPreferencesRepository? = null,
) {
    private val seedMutex = Mutex()

    fun observeRatingSummaries(): Flow<Map<String, PairingRatingSummary>> =
        dao.observeRatingAggregates().map { rows ->
            rows.associate { row ->
                row.pairingId to PairingRatingSummary(
                    pairingId = row.pairingId,
                    averageRating = row.averageRating,
                    reviewCount = row.reviewCount,
                )
            }
        }

    fun observeReviewsForPairing(pairingId: String): Flow<List<PairingReview>> =
        dao.observeReviewsForPairing(pairingId).map { list ->
            list.map { it.toDomain() }
        }

    fun observeAllReviews(): Flow<List<PairingReview>> =
        dao.observeAllReviews().map { list ->
            list.map { it.toDomain() }
        }

    fun observeUserReviews(): Flow<List<PairingReview>> =
        dao.observeUserReviews().map { list ->
            list.map { it.toDomain() }
        }

    suspend fun ensureSeeded() {
        seedMutex.withLock {
            val alreadySeeded = prefs?.hasSeededReviews() ?: false
            if (alreadySeeded) return
            if (dao.getReviewCount() == 0) {
                dao.insertAll(SeedReviews.initialReviews())
            }
            prefs?.markReviewsSeeded()
        }
    }

    suspend fun submitReview(
        cigarId: String,
        beverageId: String,
        rating: Int,
        reviewText: String,
        reviewerName: String,
        existingReviewId: Long = 0L,
    ): Long {
        val clampedRating = rating.coerceIn(1, 5)
        val cleanName = reviewerName.trim().ifBlank { "Aficionado" }
        val cleanText = reviewText.trim()
        prefs?.setLastReviewerName(cleanName)
        return dao.upsertReview(
            PairingReviewEntity(
                id = existingReviewId,
                pairingId = buildPairingId(cigarId, beverageId),
                cigarId = cigarId,
                beverageId = beverageId,
                rating = clampedRating,
                reviewText = cleanText,
                reviewerName = cleanName,
                isUserOwn = true,
                createdAt = System.currentTimeMillis(),
            )
        )
    }

    suspend fun deleteReview(reviewId: Long) {
        dao.deleteReviewById(reviewId)
    }

    private fun PairingReviewEntity.toDomain(): PairingReview = PairingReview(
        id = id,
        pairingId = pairingId,
        cigarId = cigarId,
        beverageId = beverageId,
        rating = rating,
        reviewText = reviewText,
        reviewerName = reviewerName,
        isUserOwn = isUserOwn,
        createdAt = createdAt,
    )
}
