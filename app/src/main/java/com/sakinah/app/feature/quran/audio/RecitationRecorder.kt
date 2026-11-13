package com.sakinah.app.feature.quran.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class RecitationRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentOutputFile: File? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPlayingRecording = MutableStateFlow(false)
    val isPlayingRecording: StateFlow<Boolean> = _isPlayingRecording.asStateFlow()

    private val _hasRecording = MutableStateFlow(false)
    val hasRecording: StateFlow<Boolean> = _hasRecording.asStateFlow()

    fun startRecording(surahNum: Int, ayahNum: Int): Boolean {
        stopPlaying()
        stopRecording()

        return try {
            val file = File(context.cacheDir, "recitation_${surahNum}_${ayahNum}.m4a")
            if (file.exists()) file.delete()
            currentOutputFile = file

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            _isRecording.value = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _isRecording.value = false
            false
        }
    }

    fun stopRecording() {
        if (_isRecording.value) {
            try {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                mediaRecorder = null
                _isRecording.value = false
                _hasRecording.value = currentOutputFile?.exists() == true
            }
        }
    }

    fun playRecording(onComplete: (() -> Unit)? = null) {
        val file = currentOutputFile ?: return
        if (!file.exists()) return

        stopPlaying()

        try {
            val player = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                setOnCompletionListener {
                    _isPlayingRecording.value = false
                    onComplete?.invoke()
                }
                start()
            }
            mediaPlayer = player
            _isPlayingRecording.value = true
        } catch (e: Exception) {
            e.printStackTrace()
            _isPlayingRecording.value = false
        }
    }

    fun stopPlaying() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
            _isPlayingRecording.value = false
        }
    }

    fun release() {
        stopRecording()
        stopPlaying()
    }
}
