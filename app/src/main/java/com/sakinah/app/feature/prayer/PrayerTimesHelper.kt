package com.sakinah.app.feature.prayer

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.Prayer
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class PrayerTimeItem(
    val nameAr: String,
    val prayer: Prayer,
    val formattedTime: String,
    val isNext: Boolean = false
)

data class PrayerScheduleState(
    val cityName: String = "مكة المكرمة",
    val hijriDateFormatted: String = "",
    val gregorianDateFormatted: String = "",
    val nextPrayerName: String = "",
    val countdownFormatted: String = "",
    val prayerList: List<PrayerTimeItem> = emptyList()
)

object PrayerTimesHelper {

    // إحداثيات مكة المكرمة الافتراضية
    val MECCA_COORDINATES = Coordinates(21.4225, 39.8262)

    fun calculateTodayPrayers(
        coordinates: Coordinates = MECCA_COORDINATES,
        cityName: String = "مكة المكرمة"
    ): PrayerScheduleState {
        val now = Date()
        val calendar = Calendar.getInstance()
        val dateComponents = DateComponents.from(now)

        val params = CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters.apply {
            madhab = Madhab.SHAFI
        }

        val prayerTimes = PrayerTimes(coordinates, dateComponents, params)
        val nextPrayer = prayerTimes.nextPrayer()

        val timeFormat = SimpleDateFormat("hh:mm a", Locale("ar"))
        val gregorianFormat = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar"))

        // حساب العد التنازلي للصلاة القادمة
        val (nextName, countdown) = if (nextPrayer != Prayer.NONE) {
            val nextDate = prayerTimes.timeForPrayer(nextPrayer)
            val diffMs = nextDate.time - now.time
            val hours = (diffMs / (1000 * 60 * 60)).coerceAtLeast(0)
            val minutes = ((diffMs / (1000 * 60)) % 60).coerceAtLeast(0)
            val name = getArabicPrayerName(nextPrayer)
            name to "متبقي ${hours}س و ${minutes}د"
        } else {
            "الفجر" to "الصلاة القادمة فجراً"
        }

        val items = listOf(
            PrayerTimeItem("الفجر", Prayer.FAJR, timeFormat.format(prayerTimes.fajr), nextPrayer == Prayer.FAJR),
            PrayerTimeItem("الشروق", Prayer.SUNRISE, timeFormat.format(prayerTimes.sunrise), nextPrayer == Prayer.SUNRISE),
            PrayerTimeItem("الظهر", Prayer.DHUHR, timeFormat.format(prayerTimes.dhuhr), nextPrayer == Prayer.DHUHR),
            PrayerTimeItem("العصر", Prayer.ASR, timeFormat.format(prayerTimes.asr), nextPrayer == Prayer.ASR),
            PrayerTimeItem("المغرب", Prayer.MAGHRIB, timeFormat.format(prayerTimes.maghrib), nextPrayer == Prayer.MAGHRIB),
            PrayerTimeItem("العشاء", Prayer.ISHA, timeFormat.format(prayerTimes.isha), nextPrayer == Prayer.ISHA)
        )

        return PrayerScheduleState(
            cityName = cityName,
            hijriDateFormatted = "اليوم المبارك",
            gregorianDateFormatted = gregorianFormat.format(now),
            nextPrayerName = nextName,
            countdownFormatted = countdown,
            prayerList = items
        )
    }

    private fun getArabicPrayerName(prayer: Prayer): String {
        return when (prayer) {
            Prayer.FAJR -> "الفجر"
            Prayer.SUNRISE -> "الشروق"
            Prayer.DHUHR -> "الظهر"
            Prayer.ASR -> "العصر"
            Prayer.MAGHRIB -> "المغرب"
            Prayer.ISHA -> "العشاء"
            else -> "الفجر"
        }
    }
}
