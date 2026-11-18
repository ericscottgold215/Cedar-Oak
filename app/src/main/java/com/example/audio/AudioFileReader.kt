package com.example.audio

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.ai.AudioTranscriptionService
import java.io.ByteArrayOutputStream

data class AudioFilePayload(
    val displayName: String,
    val mimeType: String,
    val byteSize: Int,
    val bytes: ByteArray,
)

object AudioFileReader {

    fun readUri(context: Context, uri: Uri): AudioFilePayload {
        val resolver = context.contentResolver
        val rawName = queryDisplayName(context, uri) ?: "audio_clip"
        val rawMime = resolver.getType(uri).orEmpty()
        val mimeType = normalizeAudioMime(rawMime, rawName)

        val buffer = ByteArray(16 * 1024)
        val out = ByteArrayOutputStream()
        var total = 0

        resolver.openInputStream(uri)?.use { stream ->
            while (true) {
                val count = stream.read(buffer)
                if (count <= 0) break
                total += count
                if (total > AudioTranscriptionService.MAX_INLINE_AUDIO_BYTES) {
                    throw IllegalArgumentException(
                        "Selected audio file exceeds the 19 MB inline transcription limit."
                    )
                }
                out.write(buffer, 0, count)
            }
        } ?: throw IllegalStateException("Could not open the selected audio file.")

        val bytes = out.toByteArray()
        if (bytes.isEmpty()) {
            throw IllegalStateException("The selected audio file appears to be empty.")
        }

        return AudioFilePayload(
            displayName = rawName,
            mimeType = mimeType,
            byteSize = bytes.size,
            bytes = bytes,
        )
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        return context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
        }
    }

    fun normalizeAudioMime(rawMime: String, fileName: String): String {
        if (rawMime.startsWith("audio/")) return rawMime
        val lower = fileName.lowercase()
        return when {
            lower.endsWith(".mp3") -> "audio/mpeg"
            lower.endsWith(".wav") -> "audio/wav"
            lower.endsWith(".ogg") -> "audio/ogg"
            lower.endsWith(".flac") -> "audio/flac"
            lower.endsWith(".aac") -> "audio/aac"
            lower.endsWith(".webm") -> "audio/webm"
            else -> "audio/mp4"
        }
    }
}
