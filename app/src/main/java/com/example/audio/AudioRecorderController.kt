package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import java.io.File

class AudioRecorderController(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var player: MediaPlayer? = null

    fun startRecording(): File {
        stopRecording()
        stopPlayback()

        val target = File(context.cacheDir, "voice_memo_${System.currentTimeMillis()}.m4a")
        val instance = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        instance.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(44100)
            setAudioEncodingBitRate(96000)
            setOutputFile(target.absolutePath)
            prepare()
            start()
        }

        recorder = instance
        outputFile = target
        return target
    }

    fun readAmplitudeNormalized(): Float {
        val current = recorder ?: return 0f
        return try {
            (current.maxAmplitude / 32767f).coerceIn(0f, 1f)
        } catch (_: Exception) {
            0f
        }
    }

    fun stopRecording(): File? {
        val finished = outputFile
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {
            recorder?.release()
        } finally {
            recorder = null
        }
        return finished
    }

    fun startPlayback(file: File, onFinished: () -> Unit) {
        stopPlayback()
        val mediaPlayer = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            setOnCompletionListener { onFinished() }
            prepare()
            start()
        }
        player = mediaPlayer
    }

    fun startPlayback(uri: Uri, onFinished: () -> Unit) {
        stopPlayback()
        val mediaPlayer = MediaPlayer().apply {
            setDataSource(context, uri)
            setOnCompletionListener { onFinished() }
            prepare()
            start()
        }
        player = mediaPlayer
    }

    fun stopPlayback() {
        try {
            player?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (_: Exception) {
            player?.release()
        } finally {
            player = null
        }
    }

    fun release() {
        stopRecording()
        stopPlayback()
    }
}
