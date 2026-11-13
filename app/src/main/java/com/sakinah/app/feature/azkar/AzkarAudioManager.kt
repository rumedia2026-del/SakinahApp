package com.sakinah.app.feature.azkar

import android.content.Context
import com.sakinah.app.core.utils.audio.SheikhAudioPlayer
import com.sakinah.app.data.local.db.entities.ZikrEntity

object AzkarAudioManager {

    fun getAudioUrlsForZikr(zikr: ZikrEntity): List<String> {
        val text = zikr.textPlain

        return when {
            // آية الكرسي
            text.contains("الله لا اله الا هو الحي القيوم") || text.contains("اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ") ->
                listOf("https://everyayah.com/data/Alafasy_128kbps/002255.mp3")

            // سورة الإخلاص كاملة (٤ آيات)
            text.contains("قل هو الله احد") || text.contains("قُلْ هُوَ اللَّهُ أَحَدٌ") ->
                (1..4).map { String.format(java.util.Locale.US, "https://everyayah.com/data/Alafasy_128kbps/112%03d.mp3", it) }

            // سورة الفلق كاملة (٥ آيات)
            text.contains("قل اعوذ برب الفلق") || text.contains("قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ") ->
                (1..5).map { String.format(java.util.Locale.US, "https://everyayah.com/data/Alafasy_128kbps/113%03d.mp3", it) }

            // سورة الناس كاملة (٦ آيات)
            text.contains("قل اعوذ برب الناس") || text.contains("قُلْ أَعُوذُ بِرَبِّ النَّاسِ") ->
                (1..6).map { String.format(java.util.Locale.US, "https://everyayah.com/data/Alafasy_128kbps/114%03d.mp3", it) }

            // سورة الفاتحة كاملة (٧ آيات)
            text.contains("الحمد لله رب العالمين") && (text.contains("مالك يوم الدين") || text.contains("الفاتحة")) ->
                (1..7).map { String.format(java.util.Locale.US, "https://everyayah.com/data/Alafasy_128kbps/001%03d.mp3", it) }

            // خواتيم البقرة (الآيتان 285 و 286)
            text.contains("امن الرسول") || text.contains("آمَنَ الرَّسُولُ") ->
                listOf(
                    "https://everyayah.com/data/Alafasy_128kbps/002285.mp3",
                    "https://everyayah.com/data/Alafasy_128kbps/002286.mp3"
                )

            // أول البقرة (1-5)
            text.contains("الم ذلك الكتاب") || text.contains("الم ذَلِكَ الْكِتَابُ") ->
                (1..5).map { String.format(java.util.Locale.US, "https://everyayah.com/data/Alafasy_128kbps/002%03d.mp3", it) }

            // سورة الكافرون (1-6)
            text.contains("قل يا ايها الكافرون") || text.contains("قُلْ يَا أَيُّهَا الْكَافِرُونَ") ->
                (1..6).map { String.format(java.util.Locale.US, "https://everyayah.com/data/Alafasy_128kbps/109%03d.mp3", it) }

            // رابط الصوت المباشر المسجل في الذكر
            !zikr.audioUrl.isNullOrBlank() ->
                listOf(zikr.audioUrl)

            else ->
                emptyList()
        }
    }

    fun playZikr(
        player: SheikhAudioPlayer,
        zikr: ZikrEntity,
        onComplete: (() -> Unit)? = null
    ) {
        val urls = getAudioUrlsForZikr(zikr)
        if (urls.isNotEmpty()) {
            player.playUrls(urls, onComplete)
        }
    }
}
