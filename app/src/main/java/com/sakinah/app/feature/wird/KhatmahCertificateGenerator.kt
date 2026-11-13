package com.sakinah.app.feature.wird

import android.content.Context
import android.graphics.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object KhatmahCertificateGenerator {

    fun generateCertificate(
        context: Context,
        userName: String,
        durationDays: Int,
        khatmahNumber: Int = 1
    ): File {
        val width = 1200
        val height = 800
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // خلفية ورقية فاخرة
        canvas.drawColor(android.graphics.Color.parseColor("#FFFDF7"))

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
        }

        // إطار إسلامي ذهبي مزدوج
        paint.color = android.graphics.Color.parseColor("#C8A951")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 14f
        canvas.drawRect(40f, 40f, width - 40f, height - 40f, paint)

        paint.strokeWidth = 4f
        canvas.drawRect(55f, 55f, width - 55f, height - 55f, paint)

        // زخارف الزوايا
        paint.style = Paint.Style.FILL
        paint.textSize = 28f
        paint.color = android.graphics.Color.parseColor("#0F3D2E")
        canvas.drawText("۞", 80f, 90f, paint)
        canvas.drawText("۞", width - 80f, 90f, paint)
        canvas.drawText("۞", 80f, height - 70f, paint)
        canvas.drawText("۞", width - 80f, height - 70f, paint)

        // الترويسة العلوية
        paint.textSize = 34f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.color = android.graphics.Color.parseColor("#0F3D2E")
        canvas.drawText("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", width / 2f, 130f, paint)

        // عنوان الشهادة
        paint.textSize = 52f
        paint.color = android.graphics.Color.parseColor("#C8A951")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("شَهَادَةُ خَتْمِ القُرْآنِ الكَرِيمِ", width / 2f, 220f, paint)

        // الآية الكريمة
        paint.textSize = 26f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        paint.color = android.graphics.Color.parseColor("#2E7D32")
        canvas.drawText("﴿ إِنَّ هَٰذَا الْقُرْآنَ يَهْدِي لِلَّتِي هِيَ أَقْوَمُ وَيُبَشِّرُ الْمُؤْمِنِينَ ﴾", width / 2f, 290f, paint)

        // نص الشهادة
        paint.textSize = 32f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = android.graphics.Color.parseColor("#2C2620")
        canvas.drawText("بفضلٍ من الله وتوفيقه، أتمّ القارئ المبارك:", width / 2f, 380f, paint)

        // اسم القارئ
        paint.textSize = 46f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = android.graphics.Color.parseColor("#0F3D2E")
        val displayName = if (userName.isBlank()) "حامل كتاب الله" else userName
        canvas.drawText(displayName, width / 2f, 450f, paint)

        // تفاصيل الختمة
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = android.graphics.Color.parseColor("#424242")
        canvas.drawText("ختم تلاوة وتدبر القرآن الكريم كاملاً (٦٠٤ صفحة)", width / 2f, 520f, paint)
        canvas.drawText("في مدة مباركة قدرها: $durationDays يوماً • الختمة رقم: $khatmahNumber", width / 2f, 570f, paint)

        // التاريخ والتطبيق
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val dateStr = dateFormat.format(Date())

        paint.textSize = 22f
        paint.color = android.graphics.Color.parseColor("#8D6E63")
        canvas.drawText("تاريخ الإتمام: $dateStr م", 250f, 680f, paint)
        canvas.drawText("تطبيق سَكِينَة • Sakinah App", width - 250f, 680f, paint)

        // حفظ الصورة
        val file = File(context.cacheDir, "khatmah_certificate_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }
}
