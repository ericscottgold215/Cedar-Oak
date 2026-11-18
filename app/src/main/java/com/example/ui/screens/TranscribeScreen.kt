package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ai.AudioTranscriptionService
import com.example.ai.TranscriptionStyle
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.SavedTranscriptionCard
import com.example.ui.components.TranscriptionResultCard
import com.example.ui.viewmodel.AudioInputMode
import com.example.ui.viewmodel.TranscribeViewModel
import com.example.ui.viewmodel.TranscriptionUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TranscribeScreen(
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TranscribeViewModel = viewModel(),
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val inputMode by viewModel.inputMode.collectAsStateWithLifecycle()
    val style by viewModel.style.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val recordingSeconds by viewModel.recordingSeconds.collectAsStateWithLifecycle()
    val amplitudeBars by viewModel.amplitudeBars.collectAsStateWithLifecycle()
    val recordedFile by viewModel.recordedFile.collectAsStateWithLifecycle()
    val recordedDuration by viewModel.recordedDurationSeconds.collectAsStateWithLifecycle()
    val importedPayload by viewModel.importedPayload.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val livePartial by viewModel.livePartialText.collectAsStateWithLifecycle()
    val dictatedDraft by viewModel.dictatedDraft.collectAsStateWithLifecycle()
    val isPlayingPreview by viewModel.isPlayingPreview.collectAsStateWithLifecycle()
    val savedTranscriptions by viewModel.savedTranscriptions.collectAsStateWithLifecycle()

    var micPermissionDenied by remember { mutableStateOf(false) }
    var pendingMicAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        micPermissionDenied = !granted
        if (granted) {
            pendingMicAction?.invoke()
        }
        pendingMicAction = null
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            viewModel.loadAudioFile(uri)
        }
    }

    fun runWithMicPermission(action: () -> Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            micPermissionDenied = false
            action()
        } else {
            pendingMicAction = action
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val isBusy = uiState is TranscriptionUiState.Loading || uiState is TranscriptionUiState.Streaming

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.GraphicEq,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = stringResource(R.string.transcribe_title),
                        style = MaterialTheme.typography.headlineLarge,
                    )
                }
                Text(
                    text = stringResource(R.string.transcribe_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                AudioInputMode.entries.forEachIndexed { idx, mode ->
                    SegmentedButton(
                        selected = inputMode == mode,
                        onClick = { viewModel.setInputMode(mode) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = idx,
                            count = AudioInputMode.entries.size,
                        ),
                        modifier = Modifier.testTag("audio_mode_${mode.name}"),
                    ) {
                        Text(
                            text = mode.label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Output Format",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    TranscriptionStyle.entries.forEach { option ->
                        FilterChip(
                            selected = style == option,
                            onClick = { viewModel.setStyle(option) },
                            label = { Text(option.label) },
                            modifier = Modifier.testTag("transcription_style_${option.name}"),
                        )
                    }
                }
                Text(
                    text = style.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (micPermissionDenied && inputMode != AudioInputMode.FILE) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.transcribe_permission_rationale),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            onClick = { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                            modifier = Modifier.minimumInteractiveComponentSize(),
                        ) {
                            Text(stringResource(R.string.transcribe_grant_permission))
                        }
                    }
                }
            }
        }

        item {
            when (inputMode) {
                AudioInputMode.RECORD -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = if (isRecording) "Recording Live Audio" else "Voice Memo Studio",
                                    style = MaterialTheme.typography.titleLarge,
                                )
                                Text(
                                    text = TranscribeViewModel.formatDuration(
                                        if (isRecording) recordingSeconds else recordedDuration,
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRecording) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            AudioWaveformVisualizer(
                                amplitudes = amplitudeBars,
                                isActive = isRecording,
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Button(
                                    onClick = {
                                        if (isRecording) {
                                            viewModel.stopRecording()
                                        } else {
                                            runWithMicPermission { viewModel.startRecording() }
                                        }
                                    },
                                    enabled = !isBusy,
                                    colors = if (isRecording) {
                                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    } else {
                                        ButtonDefaults.buttonColors()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("record_toggle_button"),
                                ) {
                                    Icon(
                                        imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        stringResource(
                                            if (isRecording) R.string.transcribe_action_stop_record
                                            else R.string.transcribe_action_record,
                                        ),
                                    )
                                }

                                if (recordedFile != null && !isRecording) {
                                    IconButton(
                                        onClick = { viewModel.toggleRecordingPlayback() },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("play_recording_button"),
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingPreview) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                            contentDescription = stringResource(
                                                if (isPlayingPreview) R.string.cd_pause_audio else R.string.cd_play_audio,
                                            ),
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.discardRecording() },
                                        enabled = !isBusy,
                                        modifier = Modifier.minimumInteractiveComponentSize(),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.DeleteOutline,
                                            contentDescription = "Discard recording",
                                            tint = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                }
                            }

                            if (recordedFile != null && !isRecording) {
                                Button(
                                    onClick = { viewModel.transcribeRecordedAudio() },
                                    enabled = !isBusy,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("transcribe_recorded_button"),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.transcribe_action_transcribe))
                                }
                            }
                        }
                    }
                }

                AudioInputMode.FILE -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = "Import Audio File",
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                text = "Supports MP3, M4A, WAV, AAC, OGG, and FLAC clips up to 19 MB.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            OutlinedButton(
                                onClick = { audioPickerLauncher.launch(arrayOf("audio/*")) },
                                enabled = !isBusy,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("pick_audio_file_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.UploadFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.transcribe_action_pick_file))
                            }

                            importedPayload?.let { payload ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    ),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AudioFile,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = payload.displayName,
                                                style = MaterialTheme.typography.titleSmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                text = "${payload.mimeType} · ${AudioTranscriptionService.formatSizeMb(payload.byteSize)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.toggleImportedPlayback() },
                                            modifier = Modifier.minimumInteractiveComponentSize(),
                                        ) {
                                            Icon(
                                                imageVector = if (isPlayingPreview) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                                contentDescription = stringResource(
                                                    if (isPlayingPreview) R.string.cd_pause_audio else R.string.cd_play_audio,
                                                ),
                                            )
                                        }
                                    }
                                }

                                Button(
                                    onClick = { viewModel.transcribeImportedFile() },
                                    enabled = !isBusy,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("transcribe_file_button"),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.transcribe_action_transcribe))
                                }
                            }
                        }
                    }
                }

                AudioInputMode.LIVE_SPEECH -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = "Live Speech-to-Text",
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                text = if (viewModel.isSpeechRecognitionAvailable) {
                                    "Dictate tasting notes hands-free with real-time on-device speech recognition."
                                } else {
                                    "On-device speech recognition is unavailable here. Use Record Memo or Audio File instead."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            OutlinedTextField(
                                value = if (livePartial.isNotEmpty()) {
                                    if (dictatedDraft.isBlank()) livePartial else "$dictatedDraft $livePartial"
                                } else {
                                    dictatedDraft
                                },
                                onValueChange = { viewModel.updateDictatedDraft(it) },
                                label = { Text("Dictated Transcript") },
                                placeholder = { Text("Tap Dictate Note and start speaking...") },
                                minLines = 3,
                                maxLines = 6,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("live_dictation_input"),
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Button(
                                    onClick = {
                                        if (isListening) {
                                            viewModel.stopLiveDictation()
                                        } else {
                                            runWithMicPermission { viewModel.startLiveDictation() }
                                        }
                                    },
                                    enabled = !isBusy,
                                    colors = if (isListening) {
                                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    } else {
                                        ButtonDefaults.buttonColors()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("dictate_toggle_button"),
                                ) {
                                    Icon(
                                        imageVector = if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        stringResource(
                                            if (isListening) R.string.transcribe_action_stop_dictate
                                            else R.string.transcribe_action_dictate,
                                        ),
                                    )
                                }

                                if (dictatedDraft.isNotBlank()) {
                                    OutlinedButton(
                                        onClick = { viewModel.clearDictation() },
                                        modifier = Modifier.height(48.dp),
                                    ) {
                                        Text("Clear")
                                    }
                                }
                            }

                            if (dictatedDraft.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.promoteDictationAsTranscript() },
                                        enabled = !isBusy,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                    ) {
                                        Text("Use As Is")
                                    }
                                    Button(
                                        onClick = { viewModel.polishDictationWithAi() },
                                        enabled = !isBusy,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("polish_dictation_button"),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AutoAwesome,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Format")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            TranscriptionResultCard(
                uiState = uiState,
                onCopyText = { text ->
                    clipboard.setText(AnnotatedString(text))
                    onShowMessage("Transcript copied to clipboard")
                },
                onSaveTranscript = { title ->
                    viewModel.saveTranscriptToLibrary(title)
                    onShowMessage("Transcript saved to library")
                },
                onDismissResult = { viewModel.resetResultState() },
            )
        }

        item {
            Text(
                text = "Saved Transcripts (${savedTranscriptions.size})",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        if (savedTranscriptions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    Text(
                        text = stringResource(R.string.transcribe_empty_history),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(18.dp),
                    )
                }
            }
        } else {
            items(savedTranscriptions, key = { it.id }) { item ->
                SavedTranscriptionCard(
                    item = item,
                    onCopy = {
                        clipboard.setText(AnnotatedString(item.transcriptText))
                        onShowMessage("Transcript copied to clipboard")
                    },
                    onDelete = {
                        viewModel.deleteSavedTranscription(item.id)
                        onShowMessage("Transcript deleted")
                    },
                )
            }
        }
    }
}
