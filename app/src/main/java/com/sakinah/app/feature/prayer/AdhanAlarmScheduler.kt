package com.sakinah.app.feature.prayer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.Prayer
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.sakinah.app.MainActivity
import java.util.Calendar

object AdhanAlarmScheduler {

    private const val PREFS_NAME = "sakinah_prayer_prefs"
    const val KEY_SELECTED_MUEZZIN = "selected_muezzin_id"
    const val KEY_FAJR_MUEZZIN = "selected_fajr_muezzin_id"
    const val KEY_ADHAN_ENABLED = "adhan_notifications_enabled"
    const val KEY_PRE_ADHAN_ENABLED = "pre_adhan_reminder_enabled"
    const val KEY_IQAMA_ENABLED = "iqama_reminder_enabled"
    const val KEY_CRESCENDO_VOLUME = "adhan_crescendo_volume"
    const val KEY_FLIP_TO_SILENCE = "adhan_flip_to_silence"
    const val KEY_DEAF_MODE = "adhan_deaf_mode"
    const val KEY_MOSQUE_SILENT = "adhan_mosque_silent"
    const val KEY_ADHAN_VOLUME = "adhan_volume_level" // 0..100
    const val KEY_SELECTED_CITY = "selected_city_name"

    fun scheduleAllPrayers(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isAdhanEnabled = prefs.getBoolean(KEY_ADHAN_ENABLED, true)
        val isPreAdhanEnabled = prefs.getBoolean(KEY_PRE_ADHAN_ENABLED, true)
        val isIqamaEnabled = prefs.getBoolean(KEY_IQAMA_ENABLED, true)
        val defaultMuezzinId = prefs.getString(KEY_SELECTED_MUEZZIN, "makkah") ?: "makkah"
        val fajrMuezzinId = prefs.getString(KEY_FAJR_MUEZZIN, defaultMuezzinId) ?: defaultMuezzinId
        val savedCityName = prefs.getString(KEY_SELECTED_CITY, "القاهرة") ?: "القاهرة"

        if (!isAdhanEnabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = System.currentTimeMillis()

        // تحديد الموقع: آخر موقع GPS معروف أو المدينة المختارة/الافتراضية
        val (coordinates, calculationMethod) = getEffectiveCoordinatesAndMethod(context, savedCityName)
        val params = calculationMethod.parameters.apply {
            madhab = Madhab.SHAFI
        }

        val datesToSchedule = listOf(0, 1) // اليوم والغد
        for (dayOffset in datesToSchedule) {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }
            val dateComponents = DateComponents.from(cal.time)
            val prayerTimes = PrayerTimes(coordinates, dateComponents, params)

            val prayerEntries = listOf(
                Triple("الفجر", Prayer.FAJR, prayerTimes.fajr),
                Triple("الظهر", Prayer.DHUHR, prayerTimes.dhuhr),
                Triple("العصر", Prayer.ASR, prayerTimes.asr),
                Triple("المغرب", Prayer.MAGHRIB, prayerTimes.maghrib),
                Triple("العشاء", Prayer.ISHA, prayerTimes.isha)
            )

            for (entry in prayerEntries) {
                val prayerName = entry.first
                val prayerType = entry.second
                val prayerDate = entry.third
                val triggerTime = prayerDate.time

                val effectiveMuezzin = if (prayerType == Prayer.FAJR) fajrMuezzinId else defaultMuezzinId

                // 1. تنبيه الأذان الفعلي عبر setAlarmClock
                if (triggerTime > now) {
                    val requestCode = getRequestCode(dayOffset, prayerType, 0)
                    val adhanIntent = Intent(context, AdhanAlarmReceiver::class.java).apply {
                        action = AdhanAlarmReceiver.ACTION_ADHAN
                        putExtra(AdhanAlarmReceiver.EXTRA_PRAYER_NAME, prayerName)
                        putExtra(AdhanAlarmReceiver.EXTRA_MUEZZIN_ID, effectiveMuezzin)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        adhanIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setAlarmClock(alarmManager, context, triggerTime, pendingIntent)
                }

                // 2. تنبيه اقتراب الصلاة (15 دقيقة قبل الأذان)
                val preAdhanTime = triggerTime - (15 * 60 * 1000L)
                if (isPreAdhanEnabled && preAdhanTime > now) {
                    val preRequestCode = getRequestCode(dayOffset, prayerType, 1)
                    val preIntent = Intent(context, AdhanAlarmReceiver::class.java).apply {
                        action = AdhanAlarmReceiver.ACTION_PRE_ADHAN
                        putExtra(AdhanAlarmReceiver.EXTRA_PRAYER_NAME, prayerName)
                    }
                    val prePendingIntent = PendingIntent.getBroadcast(
                        context,
                        preRequestCode,
                        preIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setAlarmClock(alarmManager, context, preAdhanTime, prePendingIntent)
                }

                // 3. تنبيه وقت الإقامة (20 دقيقة بعد الأذان)
                val iqamaTime = triggerTime + (20 * 60 * 1000L)
                if (isIqamaEnabled && iqamaTime > now) {
                    val iqamaRequestCode = getRequestCode(dayOffset, prayerType, 2)
                    val iqamaIntent = Intent(context, AdhanAlarmReceiver::class.java).apply {
                        action = AdhanAlarmReceiver.ACTION_IQAMA
                        putExtra(AdhanAlarmReceiver.EXTRA_PRAYER_NAME, prayerName)
                    }
                    val iqamaPendingIntent = PendingIntent.getBroadcast(
                        context,
                        iqamaRequestCode,
                        iqamaIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setAlarmClock(alarmManager, context, iqamaTime, iqamaPendingIntent)
                }
            }
        }
    }

    /**
     * جدولة اختبار حقيقي بعد ثوانٍ محددة
     */
    fun scheduleTestAdhan(context: Context, delaySeconds: Int = 10, muezzinId: String = "makkah") {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerTime = System.currentTimeMillis() + (delaySeconds * 1000L)

        val adhanIntent = Intent(context, AdhanAlarmReceiver::class.java).apply {
            action = AdhanAlarmReceiver.ACTION_ADHAN
            putExtra(AdhanAlarmReceiver.EXTRA_PRAYER_NAME, "اختبار الأذان")
            putExtra(AdhanAlarmReceiver.EXTRA_MUEZZIN_ID, muezzinId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9999,
            adhanIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setAlarmClock(alarmManager, context, triggerTime, pendingIntent)
    }

    private fun setAlarmClock(
        alarmManager: AlarmManager,
        context: Context,
        triggerAtMillis: Long,
        operation: PendingIntent
    ) {
        val showIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            0,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, operation)
    }

    private fun getEffectiveCoordinatesAndMethod(
        context: Context,
        savedCityName: String
    ): Pair<Coordinates, com.batoulapps.adhan.CalculationMethod> {
        val city = PrayerTimesHelper.getCityByName(savedCityName)
        // فحص آخر موقع GPS معروف إن توفرت الصلاحية
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val lastGps = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (lastGps != null) {
                return Coordinates(lastGps.latitude, lastGps.longitude) to city.calculationMethod
            }
        } catch (_: SecurityException) {
            // الصلاحية غير ممنوحة، استخدام المدينة المختارة
        } catch (_: Exception) {
            // تجاوز أي خطأ آخر
        }
        return city.coordinates to city.calculationMethod
    }

    private fun getRequestCode(dayOffset: Int, prayer: Prayer, alertType: Int): Int {
        val prayerIndex = when (prayer) {
            Prayer.FAJR -> 1
            Prayer.DHUHR -> 2
            Prayer.ASR -> 3
            Prayer.MAGHRIB -> 4
            Prayer.ISHA -> 5
            else -> 0
        }
        return (dayOffset * 100) + (prayerIndex * 10) + alertType
    }
}
