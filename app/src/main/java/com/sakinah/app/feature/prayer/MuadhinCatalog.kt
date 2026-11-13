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
            id = "alafasy",
            name = "الشيخ مشاري راشد العفاسي",
            description = "أذان خاشع ونقي عالي الوضوح بروحانية فريدة",
            previewAudioUrl = "https://server8.mp3quran.net/afs/001.mp3"
        ),
        MuadhinItem(
            id = "makkah",
            name = "أذان المسجد الحرام بمكة المكرمة",
            description = "النداء الروحاني العذب الخالد من الحرم المكي الشريف",
            previewAudioUrl = "https://server8.mp3quran.net/afs/002.mp3"
        ),
        MuadhinItem(
            id = "madinah",
            name = "أذان المسجد النبوي الشريف",
            description = "نداء طمأنينة ومهابة وسكينة من مدينة رسول الله ﷺ",
            previewAudioUrl = "https://server8.mp3quran.net/afs/003.mp3"
        ),
        MuadhinItem(
            id = "abdelbaset",
            name = "الشيخ عبد الباسط عبد الصمد",
            description = "أذان تاريخي شجي ومهيب بنبرة مصرية أصيلة",
            previewAudioUrl = "https://server8.mp3quran.net/afs/004.mp3"
        ),
        MuadhinItem(
            id = "minshawi",
            name = "الشيخ محمد صديق المنشاوي",
            description = "الصوت الباكي الخاشع الذي يمس القلوب والوجدان",
            previewAudioUrl = "https://server8.mp3quran.net/afs/005.mp3"
        ),
        MuadhinItem(
            id = "takbeerat",
            name = "تكبيرات الأذان والتلبية المختصرة",
            description = "الله أكبر الله أكبر، تنبيه مبارك ومختصر لوقت الفريضة",
            previewAudioUrl = "https://server8.mp3quran.net/afs/006.mp3"
        )
    )

    fun getMuadhinById(id: String): MuadhinItem {
        return muadhins.firstOrNull { it.id == id } ?: muadhins.first()
    }
}
