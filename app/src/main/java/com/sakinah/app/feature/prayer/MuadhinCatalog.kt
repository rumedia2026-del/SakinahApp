package com.sakinah.app.feature.prayer

data class MuadhinItem(
    val id: String,
    val name: String,
    val description: String,
    val previewAudioUrl: String
)

object MuadhinCatalog {
    val muadhins = listOf(
        MuadhinItem(
            id = "abdelbaset",
            name = "الشيخ عبد الباسط عبد الصمد",
            description = "أذان خاشع وتاريخي بنبرة مصرية أصيلة",
            previewAudioUrl = "https://server8.mp3quran.net/afs/001.mp3"
        ),
        MuadhinItem(
            id = "makkah",
            name = "أذان المسجد الحرام بمكة المكرمة",
            description = "النداء الروحاني العذب من الحرم المكي الشريف",
            previewAudioUrl = "https://server8.mp3quran.net/afs/001.mp3"
        ),
        MuadhinItem(
            id = "madinah",
            name = "أذان المسجد النبوي الشريف",
            description = "نداء طمأنينة وسكينة من مدينة رسول الله ﷺ",
            previewAudioUrl = "https://server8.mp3quran.net/afs/001.mp3"
        ),
        MuadhinItem(
            id = "refat",
            name = "الشيخ محمد رفعت",
            description = "صوت رمضان والزمن الجميل والسكينة الخالدة",
            previewAudioUrl = "https://server8.mp3quran.net/afs/001.mp3"
        ),
        MuadhinItem(
            id = "alaqsa",
            name = "أذان المسجد الأقصى المبارك",
            description = "نداء العزة والمهابة من مسرى الحبيب ﷺ",
            previewAudioUrl = "https://server8.mp3quran.net/afs/001.mp3"
        )
    )
}
