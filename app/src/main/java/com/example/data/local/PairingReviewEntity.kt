package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pairing_reviews")
data class PairingReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val pairingId: String,
    val cigarId: String,
    val beverageId: String,
    val rating: Int,
    val reviewText: String,
    val reviewerName: String,
    val isUserOwn: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
)

data class PairingRatingAggregate(
    val pairingId: String,
    val averageRating: Double,
    val reviewCount: Int,
)
