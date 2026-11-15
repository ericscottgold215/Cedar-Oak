package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PairingReviewDao {

    @Query("SELECT * FROM pairing_reviews ORDER BY createdAt DESC")
    fun observeAllReviews(): Flow<List<PairingReviewEntity>>

    @Query("SELECT * FROM pairing_reviews WHERE pairingId = :pairingId ORDER BY createdAt DESC")
    fun observeReviewsForPairing(pairingId: String): Flow<List<PairingReviewEntity>>

    @Query("SELECT * FROM pairing_reviews WHERE isUserOwn = 1 ORDER BY createdAt DESC")
    fun observeUserReviews(): Flow<List<PairingReviewEntity>>

    @Query(
        """
        SELECT pairingId,
               AVG(CAST(rating AS REAL)) AS averageRating,
               COUNT(*) AS reviewCount
        FROM pairing_reviews
        GROUP BY pairingId
        """
    )
    fun observeRatingAggregates(): Flow<List<PairingRatingAggregate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReview(review: PairingReviewEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(reviews: List<PairingReviewEntity>)

    @Query("DELETE FROM pairing_reviews WHERE id = :reviewId")
    suspend fun deleteReviewById(reviewId: Long)

    @Query("SELECT COUNT(*) FROM pairing_reviews")
    suspend fun getReviewCount(): Int
}
