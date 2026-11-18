package com.example.ai

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

enum class TranscriptionStyle(
    val label: String,
    val description: String,
) {
    VERBATIM(
        label = "Verbatim",
        description = "Clean word-for-word transcript with accurate punctuation.",
    ),
    TASTING_MEMO(
        label = "Tasting Memo",
        description = "Full transcript plus cigar, drink, and flavor takeaways.",
    ),
    CONCISE_SUMMARY(
        label = "Summary",
        description = "Key bullet-point highlights and pairing impressions.",
    ),
}

class AudioTranscriptionService {

    private val model by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    fun transcribeAudioStream(
        audioBytes: ByteArray,
        mimeType: String,
        style: TranscriptionStyle,
    ): Flow<String> = flow {
        require(audioBytes.isNotEmpty()) {
            "The selected audio clip is empty."
        }
        require(audioBytes.size <= MAX_INLINE_AUDIO_BYTES) {
            "Audio file exceeds the 19 MB limit (${formatSizeMb(audioBytes.size)}). Please trim or choose a smaller clip."
        }

        val prompt = buildPromptForStyle(style)
        val inputContent = content {
            inlineData(audioBytes, mimeType)
            text(prompt)
        }

        val accumulated = StringBuilder()
        withTimeout(TRANSCRIPTION_TIMEOUT_MS) {
            model.generateContentStream(inputContent).collect { chunk ->
                val chunkText = chunk.text.orEmpty()
                if (chunkText.isNotEmpty()) {
                    accumulated.append(chunkText)
                    emit(accumulated.toString())
                }
            }
        }

        if (accumulated.isBlank()) {
            throw IllegalStateException("No spoken audio could be transcribed from this clip.")
        }
    }

    fun polishDictatedTextStream(
        rawDictation: String,
        style: TranscriptionStyle,
    ): Flow<String> = flow {
        require(rawDictation.isNotBlank()) {
            "Dictate some speech first before polishing."
        }

        val prompt = when (style) {
            TranscriptionStyle.VERBATIM ->
                "Clean up punctuation, capitalization, and natural paragraph breaks in this dictated audio transcript without inventing words or changing meaning:\n\n$rawDictation"
            TranscriptionStyle.TASTING_MEMO ->
                "Format this dictated voice note cleanly with proper punctuation, then add a brief 'Tasting & Pairing Highlights' section listing any cigar, beverage, and flavor descriptors mentioned (if any):\n\n$rawDictation"
            TranscriptionStyle.CONCISE_SUMMARY ->
                "Summarize this dictated voice note into crisp, scannable bullet points preserving every key observation:\n\n$rawDictation"
        }

        val accumulated = StringBuilder()
        withTimeout(TRANSCRIPTION_TIMEOUT_MS) {
            model.generateContentStream(prompt).collect { chunk ->
                val chunkText = chunk.text.orEmpty()
                if (chunkText.isNotEmpty()) {
                    accumulated.append(chunkText)
                    emit(accumulated.toString())
                }
            }
        }

        if (accumulated.isBlank()) {
            throw IllegalStateException("Model returned an empty transcript.")
        }
    }

    private fun buildPromptForStyle(style: TranscriptionStyle): String = when (style) {
        TranscriptionStyle.VERBATIM ->
            "Transcribe all spoken words in this audio accurately, preserving the speaker's exact wording with clean punctuation, capitalization, and natural paragraph or speaker breaks. If multiple speakers are audible, label them Speaker 1, Speaker 2, etc. Do not add commentary if only clean transcription is needed."
        TranscriptionStyle.TASTING_MEMO ->
            "First, provide an accurate verbatim transcript of the spoken audio under a 'Transcript' heading. Second, under a 'Key Notes' heading, summarize the main points—and if cigars, spirits, beverages, or tasting flavors are mentioned, highlight them concisely."
        TranscriptionStyle.CONCISE_SUMMARY ->
            "Listen carefully to the audio and produce: 1) a concise 2–4 sentence overview of what was said, 2) key bullet-point takeaways, and 3) any notable names, cigars, drinks, or action items mentioned."
    }

    companion object {
        const val MAX_INLINE_AUDIO_BYTES = 19 * 1024 * 1024 // < 20 MB Firebase AI inline limit
        private const val TRANSCRIPTION_TIMEOUT_MS = 60_000L

        fun formatSizeMb(bytes: Int): String {
            val mb = bytes / (1024.0 * 1024.0)
            return String.format(java.util.Locale.US, "%.1f MB", mb)
        }
    }
}
