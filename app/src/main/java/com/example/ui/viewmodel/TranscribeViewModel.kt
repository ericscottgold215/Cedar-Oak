package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AudioTranscriptionService
import com.example.ai.TranscriptionStyle
import com.example.audio.AudioFilePayload
import com.example.audio.AudioFileReader
import com.example.audio.AudioRecorderController
import com.example.audio.LiveSpeechHelper
import com.example.data.local.AppDatabase
import com.example.data.repository.AudioTranscriptionRepository
import com.example.model.AudioTranscription
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "TranscribeViewModel"

enum class AudioInputMode(val label: String) {
    RECORD("Record Memo"),
    FILE("Audio File"),
    LIVE_SPEECH("Live Dictation"),
}

sealed interface TranscriptionUiState {
    data object Idle : TranscriptionUiState
    data object Loading : TranscriptionUiState
    data class Streaming(
        val text: String,
        val sourceLabel: String,
    ) : TranscriptionUiState

    data class Success(
        val text: String,
        val sourceLabel: String,
        val durationSeconds: Int,
        val style: TranscriptionStyle,
        val savedId: Long? = null,
    ) : TranscriptionUiState

    data class Error(val message: String) : TranscriptionUiState
}

class TranscribeViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val aiService = AudioTranscriptionService()
    private val recorder = AudioRecorderController(appContext)
    private val repository = AudioTranscriptionRepository(
        AppDatabase.getInstance(appContext).audioTranscriptionDao(),
    )

    private val speechHelper = LiveSpeechHelper(
        context = appContext,
        onPartialText = { partial ->
            _livePartialText.value = partial
        },
        onFinalText = { finalChunk ->
            val current = _dictatedDraft.value
            _dictatedDraft.value = if (current.isBlank()) finalChunk else "$current $finalChunk"
            _livePartialText.value = ""
        },
        onErrorMessage = { msg ->
            _uiState.value = TranscriptionUiState.Error(msg)
        },
        onStateChanged = { listening ->
            _isListening.value = listening
        },
    )

    val savedTranscriptions: StateFlow<List<AudioTranscription>> =
        repository.observeTranscriptions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _inputMode = MutableStateFlow(AudioInputMode.RECORD)
    val inputMode: StateFlow<AudioInputMode> = _inputMode.asStateFlow()

    private val _style = MutableStateFlow(TranscriptionStyle.VERBATIM)
    val style: StateFlow<TranscriptionStyle> = _style.asStateFlow()

    private val _uiState = MutableStateFlow<TranscriptionUiState>(TranscriptionUiState.Idle)
    val uiState: StateFlow<TranscriptionUiState> = _uiState.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingSeconds = MutableStateFlow(0)
    val recordingSeconds: StateFlow<Int> = _recordingSeconds.asStateFlow()

    private val _amplitudeBars = MutableStateFlow<List<Float>>(List(28) { 0.08f })
    val amplitudeBars: StateFlow<List<Float>> = _amplitudeBars.asStateFlow()

    private val _recordedFile = MutableStateFlow<File?>(null)
    val recordedFile: StateFlow<File?> = _recordedFile.asStateFlow()

    private val _recordedDurationSeconds = MutableStateFlow(0)
    val recordedDurationSeconds: StateFlow<Int> = _recordedDurationSeconds.asStateFlow()

    private val _importedPayload = MutableStateFlow<AudioFilePayload?>(null)
    val importedPayload: StateFlow<AudioFilePayload?> = _importedPayload.asStateFlow()

    private val _importedUri = MutableStateFlow<Uri?>(null)

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _livePartialText = MutableStateFlow("")
    val livePartialText: StateFlow<String> = _livePartialText.asStateFlow()

    private val _dictatedDraft = MutableStateFlow("")
    val dictatedDraft: StateFlow<String> = _dictatedDraft.asStateFlow()

    private val _isPlayingPreview = MutableStateFlow(false)
    val isPlayingPreview: StateFlow<Boolean> = _isPlayingPreview.asStateFlow()

    val isSpeechRecognitionAvailable: Boolean
        get() = speechHelper.isAvailable()

    private var timerJob: Job? = null
    private var transcriptionJob: Job? = null

    fun setInputMode(mode: AudioInputMode) {
        if (_isRecording.value) stopRecording()
        if (_isListening.value) stopLiveDictation()
        stopPreviewPlayback()
        _inputMode.value = mode
    }

    fun setStyle(style: TranscriptionStyle) {
        _style.value = style
    }

    fun startRecording() {
        stopPreviewPlayback()
        _uiState.value = TranscriptionUiState.Idle
        try {
            val file = recorder.startRecording()
            _recordedFile.value = file
            _isRecording.value = true
            _recordingSeconds.value = 0
            _amplitudeBars.value = List(28) { 0.08f }

            timerJob?.cancel()
            timerJob = viewModelScope.launch {
                var ticks = 0
                while (_isRecording.value) {
                    delay(120L)
                    ticks++
                    if (ticks % 8 == 0) {
                        _recordingSeconds.value = _recordingSeconds.value + 1
                    }
                    val level = recorder.readAmplitudeNormalized().coerceAtLeast(0.08f)
                    _amplitudeBars.value = (_amplitudeBars.value.drop(1) + level)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio recording", e)
            _isRecording.value = false
            _uiState.value = TranscriptionUiState.Error(
                e.localizedMessage ?: "Unable to start microphone recording on this device."
            )
        }
    }

    fun stopRecording() {
        if (!_isRecording.value) return
        timerJob?.cancel()
        _isRecording.value = false
        val finished = recorder.stopRecording()
        _recordedFile.value = finished
        _recordedDurationSeconds.value = _recordingSeconds.value.coerceAtLeast(1)
    }

    fun discardRecording() {
        stopPreviewPlayback()
        _recordedFile.value?.delete()
        _recordedFile.value = null
        _recordedDurationSeconds.value = 0
        _recordingSeconds.value = 0
        _amplitudeBars.value = List(28) { 0.08f }
        if (_uiState.value is TranscriptionUiState.Error) {
            _uiState.value = TranscriptionUiState.Idle
        }
    }

    fun toggleRecordingPlayback() {
        val file = _recordedFile.value ?: return
        if (_isPlayingPreview.value) {
            stopPreviewPlayback()
        } else {
            try {
                _isPlayingPreview.value = true
                recorder.startPlayback(file) {
                    _isPlayingPreview.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Playback error", e)
                _isPlayingPreview.value = false
                _uiState.value = TranscriptionUiState.Error("Could not play back recorded audio.")
            }
        }
    }

    fun toggleImportedPlayback() {
        val uri = _importedUri.value ?: return
        if (_isPlayingPreview.value) {
            stopPreviewPlayback()
        } else {
            try {
                _isPlayingPreview.value = true
                recorder.startPlayback(uri) {
                    _isPlayingPreview.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "File playback error", e)
                _isPlayingPreview.value = false
                _uiState.value = TranscriptionUiState.Error("Could not play back selected audio file.")
            }
        }
    }

    fun stopPreviewPlayback() {
        recorder.stopPlayback()
        _isPlayingPreview.value = false
    }

    fun loadAudioFile(uri: Uri) {
        stopPreviewPlayback()
        viewModelScope.launch {
            _uiState.value = TranscriptionUiState.Loading
            try {
                val payload = withContext(Dispatchers.IO) {
                    AudioFileReader.readUri(appContext, uri)
                }
                _importedUri.value = uri
                _importedPayload.value = payload
                _uiState.value = TranscriptionUiState.Idle
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load selected audio file", e)
                _importedPayload.value = null
                _importedUri.value = null
                _uiState.value = TranscriptionUiState.Error(
                    e.localizedMessage ?: "Failed to read the selected audio file."
                )
            }
        }
    }

    fun transcribeRecordedAudio() {
        val file = _recordedFile.value ?: run {
            _uiState.value = TranscriptionUiState.Error("Record a voice memo first.")
            return
        }
        val duration = _recordedDurationSeconds.value
        val sourceLabel = "Voice Memo (${formatDuration(duration)})"
        val currentStyle = _style.value

        transcriptionJob?.cancel()
        transcriptionJob = viewModelScope.launch {
            _uiState.value = TranscriptionUiState.Loading
            try {
                val bytes = withContext(Dispatchers.IO) { file.readBytes() }
                runStreamingTranscription(
                    bytes = bytes,
                    mimeType = "audio/mp4",
                    sourceLabel = sourceLabel,
                    durationSeconds = duration,
                    style = currentStyle,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read recorded file", e)
                _uiState.value = TranscriptionUiState.Error(
                    e.localizedMessage ?: "Unable to read recorded audio."
                )
            }
        }
    }

    fun transcribeImportedFile() {
        val payload = _importedPayload.value ?: run {
            _uiState.value = TranscriptionUiState.Error("Select an audio file first.")
            return
        }
        val sourceLabel = "File · ${payload.displayName}"
        val currentStyle = _style.value

        transcriptionJob?.cancel()
        transcriptionJob = viewModelScope.launch {
            _uiState.value = TranscriptionUiState.Loading
            runStreamingTranscription(
                bytes = payload.bytes,
                mimeType = payload.mimeType,
                sourceLabel = sourceLabel,
                durationSeconds = 0,
                style = currentStyle,
            )
        }
    }

    private suspend fun runStreamingTranscription(
        bytes: ByteArray,
        mimeType: String,
        sourceLabel: String,
        durationSeconds: Int,
        style: TranscriptionStyle,
    ) {
        var latest = ""
        try {
            aiService.transcribeAudioStream(
                audioBytes = bytes,
                mimeType = mimeType,
                style = style,
            ).collect { partial ->
                latest = partial
                _uiState.value = TranscriptionUiState.Streaming(
                    text = partial,
                    sourceLabel = sourceLabel,
                )
            }
            if (latest.isNotBlank()) {
                _uiState.value = TranscriptionUiState.Success(
                    text = latest.trim(),
                    sourceLabel = sourceLabel,
                    durationSeconds = durationSeconds,
                    style = style,
                )
            } else {
                _uiState.value = TranscriptionUiState.Error("No spoken words were detected in the audio.")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Audio transcription timed out", e)
            _uiState.value = TranscriptionUiState.Error(
                "Transcription timed out after 60 seconds. Try a shorter audio clip."
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Audio transcription failed", e)
            _uiState.value = TranscriptionUiState.Error(
                e.localizedMessage ?: "Audio transcription failed. Check connection and try again."
            )
        }
    }

    fun startLiveDictation() {
        if (_uiState.value is TranscriptionUiState.Error) {
            _uiState.value = TranscriptionUiState.Idle
        }
        speechHelper.startListening()
    }

    fun stopLiveDictation() {
        speechHelper.stopListening()
    }

    fun updateDictatedDraft(newText: String) {
        _dictatedDraft.value = newText
    }

    fun clearDictation() {
        _dictatedDraft.value = ""
        _livePartialText.value = ""
    }

    fun polishDictationWithAi() {
        val raw = _dictatedDraft.value.trim()
        if (raw.isEmpty()) {
            _uiState.value = TranscriptionUiState.Error("Speak or enter some text first.")
            return
        }
        val currentStyle = _style.value
        val sourceLabel = "Live Speech Dictation"

        transcriptionJob?.cancel()
        transcriptionJob = viewModelScope.launch {
            _uiState.value = TranscriptionUiState.Loading
            var latest = ""
            try {
                aiService.polishDictatedTextStream(raw, currentStyle).collect { partial ->
                    latest = partial
                    _uiState.value = TranscriptionUiState.Streaming(partial, sourceLabel)
                }
                _uiState.value = TranscriptionUiState.Success(
                    text = latest.trim(),
                    sourceLabel = sourceLabel,
                    durationSeconds = 0,
                    style = currentStyle,
                )
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Dictation polish timed out", e)
                _uiState.value = TranscriptionUiState.Error("Request timed out after 60 seconds.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Dictation polish failed", e)
                _uiState.value = TranscriptionUiState.Error(
                    e.localizedMessage ?: "Could not format dictation."
                )
            }
        }
    }

    fun promoteDictationAsTranscript() {
        val raw = _dictatedDraft.value.trim()
        if (raw.isEmpty()) return
        _uiState.value = TranscriptionUiState.Success(
            text = raw,
            sourceLabel = "Live Speech Dictation",
            durationSeconds = 0,
            style = TranscriptionStyle.VERBATIM,
        )
    }

    fun saveTranscriptToLibrary(title: String) {
        val state = _uiState.value as? TranscriptionUiState.Success ?: return
        viewModelScope.launch {
            val id = repository.saveTranscription(
                title = title,
                sourceLabel = state.sourceLabel,
                durationSeconds = state.durationSeconds,
                styleLabel = state.style.label,
                transcriptText = state.text,
            )
            _uiState.value = state.copy(savedId = id)
        }
    }

    fun deleteSavedTranscription(id: Long) {
        viewModelScope.launch {
            repository.deleteTranscription(id)
        }
    }

    fun resetResultState() {
        _uiState.value = TranscriptionUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        transcriptionJob?.cancel()
        recorder.release()
        speechHelper.destroy()
    }

    companion object {
        fun formatDuration(totalSeconds: Int): String {
            val mins = totalSeconds / 60
            val secs = totalSeconds % 60
            return String.format(java.util.Locale.US, "%02d:%02d", mins, secs)
        }
    }
}
