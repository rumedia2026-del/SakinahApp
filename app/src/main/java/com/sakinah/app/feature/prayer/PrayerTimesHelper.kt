package com.sakinah.app.feature.prayer

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.Prayer
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.Qibla
import com.batoulapps.adhan.data.DateComponents
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CityLocation(
    val name: String,
    val country: String,
    val coordinates: Coordinates,
    val calculationMethod: CalculationMethod = CalculationMethod.EGYPTIAN
)

data class PrayerTimeItem(
    val nameAr: String,
    val prayer: Prayer,
    val formattedTime: String,
    val isNext: Boolean = false
)

data class MonthlyPrayerDay(
    val dayNumber: Int,
    val dayName: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val isToday: Boolean = false
)

data class PrayerScheduleState(
    val cityName: String = "رشيد - البحيرة",
    val countryName: String = "مصر",
    val hijriDateFormatted: String = "",
    val gregorianDateFormatted: String = "",
    val nextPrayerName: String = "",
    val countdownFormatted: String = "",
    val qiblaDirection: Double = 136.0,
    val prayerList: List<PrayerTimeItem> = emptyList()
)

object PrayerTimesHelper {

    val SUPPORTED_CITIES = listOf(
        CityLocation("رشيد - البحيرة", "مصر", Coordinates(31.4045, 30.4164), CalculationMethod.EGYPTIAN),
        CityLocation("القاهرة", "مصر", Coordinates(30.0444, 31.2357), CalculationMethod.EGYPTIAN),
        CityLocation("الإسكندرية", "مصر", Coordinates(31.2001, 29.9187), CalculationMethod.EGYPTIAN),
        CityLocation("الجيزة", "مصر", Coordinates(30.0131, 31.2089), CalculationMethod.EGYPTIAN),
        CityLocation("مكة المكرمة", "السعودية", Coordinates(21.4225, 39.8262), CalculationMethod.UMM_AL_QURA),
        CityLocation("المدينة المنورة", "السعودية", Coordinates(24.5247, 39.5692), CalculationMethod.UMM_AL_QURA),
        CityLocation("الرياض", "السعودية", Coordinates(24.7136, 46.6753), CalculationMethod.UMM_AL_QURA),
        CityLocation("جدة", "السعودية", Coordinates(21.5433, 39.1728), CalculationMethod.UMM_AL_QURA),
        CityLocation("دبي", "الإمارات", Coordinates(25.2048, 55.2708), CalculationMethod.DUBAI),
        CityLocation("الكويت", "الكويت", Coordinates(29.3759, 47.9774), CalculationMethod.KUWAIT)
    )

    fun getCityByName(name: String): CityLocation {
        return SUPPORTED_CITIES.firstOrNull { it.name == name || it.name.contains(name) }
            ?: SUPPORTED_CITIES.first()
    }

    fun calculateTodayPrayers(
        city: CityLocation = SUPPORTED_CITIES.first()
    ): PrayerScheduleState {
        val now = Date()
        val calendar = Calendar.getInstance()
        val dateComponents = DateComponents.from(now)

        val params = city.calculationMethod.parameters.apply {
            madhab = Madhab.SHAFI
        }

        val prayerTimes = PrayerTimes(city.coordinates, dateComponents, params)
        val nextPrayer = prayerTimes.nextPrayer()

        val timeFormat = SimpleDateFormat("hh:mm a", Locale("ar"))
        val gregorianFormat = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar"))

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

        val qibla = Qibla(city.coordinates).direction

        return PrayerScheduleState(
            cityName = city.name,
            countryName = city.country,
            hijriDateFormatted = "اليوم المبارك",
            gregorianDateFormatted = gregorianFormat.format(now),
            nextPrayerName = nextName,
            countdownFormatted = countdown,
            qiblaDirection = qibla,
            prayerList = items
        )
    }

    fun calculateMonthlySchedule(city: CityLocation = SUPPORTED_CITIES.first()): List<MonthlyPrayerDay> {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        val timeFormat = SimpleDateFormat("hh:mm a", Locale("ar"))
        val dayNameFormat = SimpleDateFormat("EEE", Locale("ar"))

        val params = city.calculationMethod.parameters.apply {
            madhab = Madhab.SHAFI
        }

        val result = mutableListOf<MonthlyPrayerDay>()
        for (day in 1..maxDays) {
            calendar.set(year, month, day)
            val dayDate = calendar.time
            val dc = DateComponents(year, month + 1, day)
            val pt = PrayerTimes(city.coordinates, dc, params)

            result.add(
                MonthlyPrayerDay(
                    dayNumber = day,
                    dayName = dayNameFormat.format(dayDate),
                    fajr = timeFormat.format(pt.fajr),
                    sunrise = timeFormat.format(pt.sunrise),
                    dhuhr = timeFormat.format(pt.dhuhr),
                    asr = timeFormat.format(pt.asr),
                    maghrib = timeFormat.format(pt.maghrib),
                    isha = timeFormat.format(pt.isha),
                    isToday = (day == currentDay)
                )
            )
        }
        return result
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
