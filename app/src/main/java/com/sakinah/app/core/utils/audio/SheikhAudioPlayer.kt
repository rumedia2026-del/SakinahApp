package com.sakinah.app.core.utils.audio

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SheikhAudioPlayer(context: Context) {

    private val appContext = context.applicationContext
    private var exoPlayer: ExoPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrackIndex = MutableStateFlow<Int?>(null)
    val currentTrackIndex: StateFlow<Int?> = _currentTrackIndex.asStateFlow()

    private var onTrackCompletedCallback: (() -> Unit)? = null

    init {
        initPlayer()
    }

    private fun initPlayer() {
        exoPlayer = ExoPlayer.Builder(appContext).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    _isPlaying.value = playing
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        _isPlaying.value = false
                        onTrackCompletedCallback?.invoke()
                    }
                }
            })
        }
    }

    /**
     * تشغيل تلاوة آية قرآنية بصوت الشيخ مشاري راشد العفاسي من EveryAyah
     * @param surahNum رقم السورة (1-114)
     * @param ayahNumInSurah رقم الآية في السورة
     */
    fun playQuranAyah(surahNum: Int, ayahNumInSurah: Int, onComplete: (() -> Unit)? = null) {
        val s = String.format("%03d", surahNum)
        val a = String.format("%03d", ayahNumInSurah)
        val url = "https://everyayah.com/data/Alafasy_128kbps/$s$a.mp3"
        playUrl(url, onComplete)
    }

    /**
     * تشغيل مقطع صوتي عبر رابط مباشر مع دعم الاستماع التلقائي
     */
    fun playUrl(url: String, onComplete: (() -> Unit)? = null) {
        onTrackCompletedCallback = onComplete
        try {
            exoPlayer?.let { player ->
                player.stop()
                player.clearMediaItems()
                val mediaItem = MediaItem.fromUri(Uri.parse(url))
                player.setMediaItem(mediaItem)
                player.prepare()
                player.play()
                _isPlaying.value = true
            }
        } catch (e: Exception) {
            _isPlaying.value = false
        }
    }

    fun playAyah(surahNum: Int, ayahNumInSurah: Int, onComplete: (() -> Unit)? = null) {
        playQuranAyah(surahNum, ayahNumInSurah, onComplete)
    }

    fun pause() {
        exoPlayer?.pause()
        _isPlaying.value = false
    }

    fun stop() {
        exoPlayer?.stop()
        _isPlaying.value = false
        _currentTrackIndex.value = null
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
        _isPlaying.value = false
    }
}
