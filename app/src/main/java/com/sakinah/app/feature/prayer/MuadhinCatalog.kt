package com.sakinah.app.feature.prayer

import android.content.Context
import androidx.annotation.RawRes
import com.sakinah.app.R

data class MuadhinItem(
    val id: String,
    val name: String,
    val description: String,
    @RawRes val rawResId: Int
)

object MuadhinCatalog {
    val muadhins = listOf(
        MuadhinItem(
            id = "makkah",
            name = "أذان المسجد الحرام بمكة المكرمة",
            description = "النداء الروحاني العذب الخالد - الشيخ علي بن أحمد ملا",
            rawResId = R.raw.adhan_makkah
        ),
        MuadhinItem(
            id = "madinah",
            name = "أذان المسجد النبوي الشريف",
            description = "نداء طمأنينة ومهابة وسكينة - الشيخ عصام بخاري",
            rawResId = R.raw.adhan_madinah
        ),
        MuadhinItem(
            id = "alafasy",
            name = "الشيخ مشاري راشد العفاسي",
            description = "أذان خاشع ونقي عالي الوضوح بروحانية فريدة",
            rawResId = R.raw.adhan_afasy
        ),
        MuadhinItem(
            id = "abdelbaset",
            name = "الشيخ عبد الباسط عبد الصمد",
            description = "أذان تاريخي شجي ومهيب بنبرة مصرية أصيلة",
            rawResId = R.raw.adhan_abdulbasit
        ),
        MuadhinItem(
            id = "qatami",
            name = "الشيخ ناصر القطامي",
            description = "أذان خاشع ومؤثر بروحانية عالية وأداء متقن",
            rawResId = R.raw.adhan_qatami
        ),
        MuadhinItem(
            id = "takbeerat",
            name = "تكبيرات الأذان فقط",
            description = "الله أكبر الله أكبر، تنبيه مبارك ومختصر لوقت الفريضة",
            rawResId = R.raw.adhan_takbeer
        )
    )

    fun getMuadhinById(id: String): MuadhinItem {
        return muadhins.firstOrNull { it.id == id } ?: muadhins.first()
    }

    fun getAudioUri(context: Context, id: String): String {
        val item = getMuadhinById(id)
        return "android.resource://${context.packageName}/${item.rawResId}"
    }

    fun getRawResourceId(id: String): Int {
        return getMuadhinById(id).rawResId
    }
}
