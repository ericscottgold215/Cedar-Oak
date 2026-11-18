package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioTranscriptionDao {

    @Query("SELECT * FROM audio_transcriptions ORDER BY createdAt DESC")
    fun observeAllTranscriptions(): Flow<List<AudioTranscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranscription(entity: AudioTranscriptionEntity): Long

    @Query("DELETE FROM audio_transcriptions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
