package com.sakinah.app.feature.prayer

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.sakinah.app.MainActivity
import com.sakinah.app.R
import com.sakinah.app.SakinahApplication

class AdhanForegroundService : Service() {

    private var exoPlayer: ExoPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SakinahApp:AdhanWakeLock").apply {
            acquire(10 * 60 * 1000L /* 10 minutes max */)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: "الصلاة"
        val muezzinId = intent?.getStringExtra(EXTRA_MUEZZIN_ID) ?: "makkah"

        if (intent?.action == ACTION_STOP_ADHAN) {
            stopAdhan()
            return START_NOT_STICKY
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

        playAdhan(muezzinId)

        return START_NOT_STICKY
    }

    private fun playAdhan(muezzinId: String) {
        try {
            val rawResId = MuadhinCatalog.getRawResourceId(muezzinId)
            val uri = Uri.parse("android.resource://$packageName/$rawResId")

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .setUsage(C.USAGE_ALARM)
                .build()

            exoPlayer = ExoPlayer.Builder(this)
                .setAudioAttributes(audioAttributes, true)
                .build().apply {
                    setMediaItem(MediaItem.fromUri(uri))
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
        } catch (e: Exception) {
            e.printStackTrace()
            stopAdhan()
        }
    }

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
            .setContentText("صوت: ${muadhin.name} • حيّ على الصلاة، حيّ على الفلاح")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(R.drawable.ic_launcher_foreground, "كتم الأذان ⏹", stopPendingIntent)
            .build()
    }

    private fun stopAdhan() {
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
        try {
            exoPlayer?.release()
            exoPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
