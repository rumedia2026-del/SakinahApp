package com.sakinah.app.feature.prayer

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdhanSettingsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val context: Context get() = getApplication()
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private var mediaPlayer: MediaPlayer? = null

    private val _playingMuezzinId = MutableStateFlow<String?>(null)
    val playingMuezzinId: StateFlow<String?> = _playingMuezzinId.asStateFlow()

    private val _isMusicVolumeMuted = MutableStateFlow(false)
    val isMusicVolumeMuted: StateFlow<Boolean> = _isMusicVolumeMuted.asStateFlow()

    fun playPreview(muadhin: MuadhinItem) {
        // إذا كان نفس المؤذن قيد التشغيل، أوقفه
        if (_playingMuezzinId.value == muadhin.id) {
            stopPreview()
            return
        }

        // أوقف وحرر أي مشغل سابق فوراً
        stopPreview()

        try {
            // فحص مستوى صوت الوسائط في الهاتف
            audioManager?.let { am ->
                val currentMusicVol = am.getStreamVolume(AudioManager.STREAM_MUSIC)
                val maxMusicVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                if (currentMusicVol == 0) {
                    _isMusicVolumeMuted.value = true
                    // رفع الصوت تلقائياً لنسبة 70% ليسمع المستخدم الأذان
                    val targetVol = (maxMusicVol * 0.7f).toInt().coerceAtLeast(1)
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, AudioManager.FLAG_SHOW_UI)
                } else {
                    _isMusicVolumeMuted.value = false
                }
            }

            // إنشاء كائن MediaPlayer مباشرة بالمعرف الصريح R.raw
            val mp = MediaPlayer.create(context, muadhin.rawResId)
            if (mp != null) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
                mp.setAudioAttributes(audioAttributes)
                mp.setVolume(1.0f, 1.0f) // أقصى درجة صوت وبدون أي تصفير أو تدريج
                mp.setOnCompletionListener {
                    _playingMuezzinId.value = null
                    it.release()
                    if (mediaPlayer == it) {
                        mediaPlayer = null
                    }
                }
                mp.setOnErrorListener { _, _, _ ->
                    _playingMuezzinId.value = null
                    stopPreview()
                    true
                }
                mp.start()
                mediaPlayer = mp
                _playingMuezzinId.value = muadhin.id
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopPreview()
        }
    }

    fun stopPreview() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
            _playingMuezzinId.value = null
        }
    }

    fun dismissMutedWarning() {
        _isMusicVolumeMuted.value = false
    }

    override fun onCleared() {
        super.onCleared()
        stopPreview()
    }
}
