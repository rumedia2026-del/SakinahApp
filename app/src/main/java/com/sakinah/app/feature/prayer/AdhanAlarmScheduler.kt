package com.sakinah.app.feature.prayer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.Prayer
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.util.Calendar
import java.util.Date

object AdhanAlarmScheduler {

    private const val PREFS_NAME = "sakinah_prayer_prefs"
    const val KEY_SELECTED_MUEZZIN = "selected_muezzin_id"
    const val KEY_ADHAN_ENABLED = "adhan_notifications_enabled"
    const val KEY_PRE_ADHAN_ENABLED = "pre_adhan_reminder_enabled"

    fun scheduleAllPrayers(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isAdhanEnabled = prefs.getBoolean(KEY_ADHAN_ENABLED, true)
        val isPreAdhanEnabled = prefs.getBoolean(KEY_PRE_ADHAN_ENABLED, true)
        val selectedMuezzinId = prefs.getString(KEY_SELECTED_MUEZZIN, "makkah") ?: "makkah"

        if (!isAdhanEnabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = System.currentTimeMillis()

        // حساب مواقيت اليوم والغد
        val city = PrayerTimesHelper.SUPPORTED_CITIES.first() // رشيد / القاهرة افتراضياً
        val params = city.calculationMethod.parameters.apply {
            madhab = Madhab.SHAFI
        }

        val datesToSchedule = listOf(0, 1) // اليوم والغد
        for (dayOffset in datesToSchedule) {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }
            val dateComponents = DateComponents.from(cal.time)
            val prayerTimes = PrayerTimes(city.coordinates, dateComponents, params)

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

                // 1. تنبيه الأذان الفعلي
                if (triggerTime > now) {
                    val requestCode = getRequestCode(dayOffset, prayerType, 0)
                    val adhanIntent = Intent(context, AdhanAlarmReceiver::class.java).apply {
                        action = AdhanAlarmReceiver.ACTION_ADHAN
                        putExtra(AdhanAlarmReceiver.EXTRA_PRAYER_NAME, prayerName)
                        putExtra(AdhanAlarmReceiver.EXTRA_MUEZZIN_ID, selectedMuezzinId)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        adhanIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setExactAlarm(alarmManager, triggerTime, pendingIntent)
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
                    setExactAlarm(alarmManager, preAdhanTime, prePendingIntent)
                }

                // 3. تنبيه وقت الإقامة (20 دقيقة بعد الأذان)
                val iqamaTime = triggerTime + (20 * 60 * 1000L)
                if (iqamaTime > now) {
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
                    setExactAlarm(alarmManager, iqamaTime, iqamaPendingIntent)
                }
            }
        }
    }

    private fun setExactAlarm(alarmManager: AlarmManager, triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
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
