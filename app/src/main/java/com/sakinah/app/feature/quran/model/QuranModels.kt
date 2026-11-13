package com.sakinah.app.feature.quran.model

data class SurahHeader(
    val number: Int,
    val name: String,
    val englishName: String,
    val revelationType: String,
    val numberOfAyahs: Int,
    val startPage: Int = 1
)

data class JuzItem(
    val number: Int,
    val name: String,
    val startSurahName: String,
    val startPage: Int
)

data class AyahItem(
    val number: Int,             // رقم الآية العام في المصحف (1-6236)
    val numberInSurah: Int,      // رقم الآية في السورة
    val text: String,            // النص بالرسم العثماني
    val surahNumber: Int,
    val surahName: String,
    val juz: Int,
    val page: Int,
    var tafsir: String? = null,
    var jalalaynTafsir: String? = null
)

data class Bookmark(
    val surahNumber: Int,
    val surahName: String,
    val ayahNumberInSurah: Int,
    val ayahGlobalNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class KhatmahPlan(
    val totalDays: Int = 30,
    val currentDay: Int = 1,
    val pagesPerDay: Int = 20,
    val completedPages: Int = 0
)
