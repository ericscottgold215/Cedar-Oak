package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.AudioTranscription

@Entity(tableName = "audio_transcriptions")
data class AudioTranscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val sourceLabel: String,
    val durationSeconds: Int,
    val styleLabel: String,
    val transcriptText: String,
    val createdAt: Long,
) {
    fun toDomain(): AudioTranscription = AudioTranscription(
        id = id,
        title = title,
        sourceLabel = sourceLabel,
        durationSeconds = durationSeconds,
        styleLabel = styleLabel,
        transcriptText = transcriptText,
        createdAt = createdAt,
    )
}
