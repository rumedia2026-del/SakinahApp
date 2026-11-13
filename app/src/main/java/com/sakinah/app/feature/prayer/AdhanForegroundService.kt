package com.sakinah.app.feature.prayer

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes as AndroidAudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.sakinah.app.MainActivity
import com.sakinah.app.R
import com.sakinah.app.SakinahApplication

class AdhanForegroundService : Service(), SensorEventListener {

    private var exoPlayer: ExoPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var audioManager: AudioManager? = null
    private var vibrator: Vibrator? = null

    private val handler = Handler(Looper.getMainLooper())
    private var crescendoRunnable: Runnable? = null
    private var currentVolume = 0.1f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SakinahApp:AdhanWakeLock").apply {
            acquire(10 * 60 * 1000L /* 10 minutes max */)
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: "الصلاة"
        val muezzinId = intent?.getStringExtra(EXTRA_MUEZZIN_ID) ?: "makkah"

        if (intent?.action == ACTION_STOP_ADHAN) {
            stopAdhan()
            return START_NOT_STICKY
        }

        val prefs = getSharedPreferences("sakinah_prayer_prefs", Context.MODE_PRIVATE)
        val flipToSilenceEnabled = prefs.getBoolean(AdhanAlarmScheduler.KEY_FLIP_TO_SILENCE, true)
        val crescendoEnabled = prefs.getBoolean(AdhanAlarmScheduler.KEY_CRESCENDO_VOLUME, true)
        val deafModeEnabled = prefs.getBoolean(AdhanAlarmScheduler.KEY_DEAF_MODE, false)

        if (flipToSilenceEnabled && accelerometer != null) {
            sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }

        if (deafModeEnabled) {
            triggerDeafModeVibration(prayerName)
        }

        val notification = createNotification(prayerName, muezzinId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        playAdhan(muezzinId, crescendoEnabled)

        return START_NOT_STICKY
    }

    private fun triggerDeafModeVibration(prayerName: String) {
        try {
            // نمط اهتزاز مخصص ومميز لكل صلاة
            val timings = when (prayerName) {
                "الفجر" -> longArrayOf(0, 800, 300, 800)
                "الظهر" -> longArrayOf(0, 300, 200, 300, 200, 300, 200, 300)
                "العصر" -> longArrayOf(0, 400, 200, 400, 200, 400)
                "المغرب" -> longArrayOf(0, 600, 200, 600, 200, 200)
                "العشاء" -> longArrayOf(0, 500, 200, 500, 200, 500, 200, 500)
                else -> longArrayOf(0, 500, 200, 500)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(timings, -1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playAdhan(muezzinId: String, crescendo: Boolean) {
        try {
            requestAudioFocus()

            val rawResId = MuadhinCatalog.getRawResourceId(muezzinId)
            val uri = Uri.parse("android.resource://$packageName/$rawResId")

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .setUsage(C.USAGE_ALARM)
                .build()

            currentVolume = if (crescendo) 0.1f else 1.0f

            exoPlayer = ExoPlayer.Builder(this)
                .setAudioAttributes(audioAttributes, true)
                .build().apply {
                    setMediaItem(MediaItem.fromUri(uri))
                    volume = currentVolume
                    prepare()
                    play()
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if (playbackState == Player.STATE_ENDED) {
                                stopAdhan()
                            }
                        }
                    })
                }

            if (crescendo) {
                startCrescendoVolume()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopAdhan()
        }
    }

    private fun startCrescendoVolume() {
        val totalSteps = 20
        val stepDurationMs = 500L // 20 steps * 500ms = 10 seconds total
        var currentStep = 0

        crescendoRunnable = object : Runnable {
            override fun run() {
                if (currentStep < totalSteps && exoPlayer != null) {
                    currentStep++
                    currentVolume = (currentStep.toFloat() / totalSteps.toFloat()).coerceIn(0.1f, 1.0f)
                    exoPlayer?.volume = currentVolume
                    handler.postDelayed(this, stepDurationMs)
                }
            }
        }
        handler.postDelayed(crescendoRunnable!!, stepDurationMs)
    }

    private fun requestAudioFocus() {
        try {
            val am = audioManager ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val focusAttrs = AndroidAudioAttributes.Builder()
                    .setUsage(AndroidAudioAttributes.USAGE_ALARM)
                    .setContentType(AndroidAudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                    .setAudioAttributes(focusAttrs)
                    .build()
                am.requestAudioFocus(focusRequest)
            } else {
                @Suppress("DEPRECATION")
                am.requestAudioFocus(null, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val zAxis = event.values[2]
            // عندما يوضع الهاتف مقلوباً على وجهه تكون قيمة المحور Z سالبة بقوة (< -7.0)
            if (zAxis < -7.0f) {
                stopAdhan()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun createNotification(prayerName: String, muezzinId: String): Notification {
        val muadhin = MuadhinCatalog.getMuadhinById(muezzinId)

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AdhanForegroundService::class.java).apply {
            action = ACTION_STOP_ADHAN
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, SakinahApplication.CHANNEL_ID_ADHAN)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("حان الآن موعد أذان $prayerName 🕌")
            .setContentText("صوت: ${muadhin.name} • اقلب الهاتف لكتم الصوت أو اضغط إيقاف")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(R.drawable.ic_launcher_foreground, "كتم الأذان ⏹", stopPendingIntent)
            .build()
    }

    private fun stopAdhan() {
        crescendoRunnable?.let { handler.removeCallbacks(it) }
        try {
            sensorManager?.unregisterListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            exoPlayer?.stop()
            exoPlayer?.release()
            exoPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAdhan()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    companion object {
        const val NOTIFICATION_ID = 2001
        const val ACTION_STOP_ADHAN = "com.sakinah.app.ACTION_STOP_ADHAN"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_MUEZZIN_ID = "extra_muezzin_id"
    }
}
