package com.sakinah.app.core.utils.audio

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.sakinah.app.feature.quran.audio.QuranAudioDownloader
import com.sakinah.app.feature.quran.audio.QuranSheikhManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SheikhAudioPlayer(context: Context) {

    private val appContext = context.applicationContext
    private var exoPlayer: ExoPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _currentlyPlayingSurah = MutableStateFlow<Int?>(null)
    val currentlyPlayingSurah: StateFlow<Int?> = _currentlyPlayingSurah.asStateFlow()

    private val _currentPlayingTitle = MutableStateFlow<String?>(null)
    val currentPlayingTitle: StateFlow<String?> = _currentPlayingTitle.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var onTrackCompletedCallback: (() -> Unit)? = null

    init {
        initPlayer()
    }

    private fun initPlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(30000)
            .setUserAgent("SakinahApp/1.0 (Android)")

        val mediaSourceFactory = DefaultMediaSourceFactory(appContext)
            .setDataSourceFactory(httpDataSourceFactory)

        exoPlayer = ExoPlayer.Builder(appContext)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(audioAttributes, true)
            .build().apply {
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        _isPlaying.value = playing
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        _isBuffering.value = (playbackState == Player.STATE_BUFFERING)
                        if (playbackState == Player.STATE_ENDED) {
                            _isPlaying.value = false
                            _currentlyPlayingSurah.value = null
                            _currentPlayingTitle.value = null
                            onTrackCompletedCallback?.invoke()
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        _isPlaying.value = false
                        _isBuffering.value = false
                        if (!isNetworkAvailable()) {
                            _errorMessage.value = "لا يوجد اتصال بالإنترنت، يرجى الاتصال أو تحميل السورة للاستماع أوفلاين"
                        } else {
                            _errorMessage.value = "تعذر تشغيل الصوت، جاري المحاولة مرة أخرى..."
                        }
                    }
                })
            }
    }

    /**
     * تشغيل سورة كاملة مع فحص الملف المحلي المحمّل أولاً
     */
    fun playSurah(
        sheikhId: String,
        surahNumber: Int,
        surahName: String = "",
        onComplete: (() -> Unit)? = null
    ) {
        _errorMessage.value = null
        onTrackCompletedCallback = onComplete

        val reciter = QuranSheikhManager.getReciterById(sheikhId)
        _currentlyPlayingSurah.value = surahNumber
        _currentPlayingTitle.value = "$surahName بصوت ${reciter.nameAr}"

        val localFile = QuranAudioDownloader.getSurahFile(appContext, sheikhId, surahNumber)
        val mediaItem = if (QuranAudioDownloader.isSurahDownloaded(appContext, sheikhId, surahNumber)) {
            // تشغيل الملف المحلي أوفلاين 100%
            MediaItem.fromUri(Uri.fromFile(localFile))
        } else {
            // تشغيل البث المباشر أونلاين
            val onlineUrl = QuranSheikhManager.getSurahOnlineUrl(sheikhId, surahNumber)
            MediaItem.fromUri(Uri.parse(onlineUrl))
        }

        playMediaItem(mediaItem)
    }

    /**
     * تشغيل آية قرآنية بصوت الشيخ مشاري العفاسي
     */
    fun playQuranAyah(surahNum: Int, ayahNumInSurah: Int, onComplete: (() -> Unit)? = null) {
        val s = String.format("%03d", surahNum)
        val a = String.format("%03d", ayahNumInSurah)
        val url = "https://everyayah.com/data/Alafasy_128kbps/$s$a.mp3"
        playUrl(url, onComplete)
    }

    fun playAyah(surahNum: Int, ayahNumInSurah: Int, onComplete: (() -> Unit)? = null) {
        playQuranAyah(surahNum, ayahNumInSurah, onComplete)
    }

    /**
     * تشغيل مقطع صوتي عبر رابط مباشر
     */
    fun playUrl(url: String, onComplete: (() -> Unit)? = null) {
        playUrls(listOf(url), onComplete)
    }

    /**
     * تشغيل قائمة مقاطع صوتية متتالية (مثل آيات السورة الواحدة أو مجموعة آيات)
     */
    fun playUrls(urls: List<String>, onComplete: (() -> Unit)? = null) {
        if (urls.isEmpty()) return
        _errorMessage.value = null
        if (!isNetworkAvailable() && urls.any { it.startsWith("http") }) {
            _errorMessage.value = "لا يوجد اتصال بالإنترنت لتشغيل المقطع الصوتي"
            return
        }
        onTrackCompletedCallback = onComplete
        try {
            val mediaItems = urls.map { MediaItem.fromUri(Uri.parse(it)) }
            exoPlayer?.let { player ->
                player.stop()
                player.clearMediaItems()
                player.setMediaItems(mediaItems)
                player.prepare()
                player.play()
                _isPlaying.value = true
            }
        } catch (e: Exception) {
            _errorMessage.value = "تعذر تشغيل الرابط الصوتي"
            _isPlaying.value = false
        }
    }

    private fun playMediaItem(mediaItem: MediaItem) {
        try {
            exoPlayer?.let { player ->
                player.stop()
                player.clearMediaItems()
                player.setMediaItem(mediaItem)
                player.prepare()
                player.play()
                _isPlaying.value = true
            }
        } catch (e: Exception) {
            _isPlaying.value = false
        }
    }

    fun pause() {
        exoPlayer?.pause()
        _isPlaying.value = false
    }

    fun stop() {
        exoPlayer?.stop()
        _isPlaying.value = false
        _currentlyPlayingSurah.value = null
        _currentPlayingTitle.value = null
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
        _isPlaying.value = false
        _isBuffering.value = false
        _currentlyPlayingSurah.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
