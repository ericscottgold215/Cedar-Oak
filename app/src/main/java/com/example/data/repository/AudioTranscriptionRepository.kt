package com.example.data.repository

import com.example.data.local.AudioTranscriptionDao
import com.example.data.local.AudioTranscriptionEntity
import com.example.model.AudioTranscription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AudioTranscriptionRepository(
    private val dao: AudioTranscriptionDao,
) {
    fun observeTranscriptions(): Flow<List<AudioTranscription>> =
        dao.observeAllTranscriptions().map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun saveTranscription(
        title: String,
        sourceLabel: String,
        durationSeconds: Int,
        styleLabel: String,
        transcriptText: String,
    ): Long {
        val cleanTitle = title.trim().ifBlank { "Voice Note" }
        return dao.insertTranscription(
            AudioTranscriptionEntity(
                title = cleanTitle,
                sourceLabel = sourceLabel,
                durationSeconds = durationSeconds,
                styleLabel = styleLabel,
                transcriptText = transcriptText.trim(),
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun deleteTranscription(id: Long) {
        dao.deleteById(id)
    }
}
