package com.sakinah.app.feature.quran.audio

data class SheikhReciter(
    val id: String,
    val nameAr: String,
    val style: String,
    val serverBaseUrl: String
)

object QuranSheikhManager {

    val RECITERS = listOf(
        SheikhReciter("afs", "مشاري راشد العفاسي", "مرتل", "https://server8.mp3quran.net/afs/"),
        SheikhReciter("husr", "محمود خليل الحصري", "مرتل", "https://server13.mp3quran.net/husr/"),
        SheikhReciter("minsh", "محمد صديق المنشاوي", "مرتل", "https://server10.mp3quran.net/minsh/"),
        SheikhReciter("basit", "عبد الباسط عبد الصمد", "مرتل", "https://server7.mp3quran.net/basit/"),
        SheikhReciter("maher", "ماهر المعيقلي", "حرم مكي", "https://server12.mp3quran.net/maher/"),
        SheikhReciter("yasser", "ياسر الدوسري", "حرم مكي", "https://server11.mp3quran.net/yasser/"),
        SheikhReciter("s_gmd", "سعد الغامدي", "مرتل", "https://server7.mp3quran.net/s_gmd/")
    )

    fun getReciterById(id: String): SheikhReciter {
        return RECITERS.find { it.id == id } ?: RECITERS.first()
    }

    fun getSurahOnlineUrl(sheikhId: String, surahNumber: Int): String {
        val reciter = getReciterById(sheikhId)
        val formattedNumber = String.format("%03d", surahNumber)
        return "${reciter.serverBaseUrl}$formattedNumber.mp3"
    }
}
