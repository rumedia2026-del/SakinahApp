package com.sakinah.app.core.utils.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.sakinah.app.data.local.db.entities.ZikrEntity
import java.io.File
import java.io.FileOutputStream

object ZikrCardGenerator {

    /**
     * توليد بطاقة تصميمية عالية الدقة للذكر ومشاركتها مباشرة
     */
    fun shareZikrAsImage(context: Context, zikr: ZikrEntity, showTashkeel: Boolean = true) {
        val bitmap = createZikrBitmap(context, zikr, showTashkeel)
        val uri = saveBitmapToCache(context, bitmap)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "${zikr.textPlain}\n\nتطبيق سكينة للأذكار والأدعية 🕌")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "مشاركة الذكر كبطاقة تصميمية"))
    }

    private fun createZikrBitmap(context: Context, zikr: ZikrEntity, showTashkeel: Boolean): Bitmap {
        val width = 1080
        val height = 1350 // نسبة 4:5 المثالية للمشاركة بالواتساب والمنصات

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. الخلفية الدافئة المستوحاة من ثيم Warm Paper (#FAF8F5)
        canvas.drawColor(Color.parseColor("#FAF8F5"))

        // 2. إطار زخرفي هادئ
        val borderPaint = Paint().apply {
            color = Color.parseColor("#D4EDDA")
            style = Paint.Style.STROKE
            strokeWidth = 14f
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(40f, 40f, (width - 40).toFloat(), (height - 40).toFloat()), 32f, 32f, borderPaint)

        // 3. الترويسة العليا (اسم التطبيق والأيقونة)
        val headerPaint = TextPaint().apply {
            color = Color.parseColor("#1B5E20")
            textSize = 52f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("سـَـكـِـيـنـَـة", width / 2f, 160f, headerPaint)

        val subtitlePaint = TextPaint().apply {
            color = Color.parseColor("#5A6472")
            textSize = 28f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("ألا بذكر الله تطمئن القلوب", width / 2f, 215f, subtitlePaint)

        // 4. خط فاصل هادئ
        val linePaint = Paint().apply {
            color = Color.parseColor("#BFB8AE")
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawLine(200f, 260f, (width - 200).toFloat(), 260f, linePaint)

        // 5. نص الذكر في المنتصف
        val textPaint = TextPaint().apply {
            color = Color.parseColor("#1E242B")
            textSize = 50f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val textToRender = if (showTashkeel) zikr.textWithTashkeel else zikr.textPlain
        val textWidth = width - 200

        val textLayout = StaticLayout.Builder.obtain(
            textToRender,
            0,
            textToRender.length,
            textPaint,
            textWidth
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(20f, 1f)
            .setIncludePad(true)
            .build()

        canvas.save()
        val textY = 340f + ((700f - textLayout.height) / 2f).coerceAtLeast(0f)
        canvas.translate(width / 2f, textY)
        textLayout.draw(canvas)
        canvas.restore()

        // 6. المصدر والتخريج بالأسفل
        val source = zikr.sourceReference ?: "صحيح الأذكار"
        val footerPaint = TextPaint().apply {
            color = Color.parseColor("#4A7C59")
            textSize = 34f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("• المصدر: $source •", width / 2f, 1150f, footerPaint)

        if (!zikr.virtueText.isNullOrBlank()) {
            val virtuePaint = TextPaint().apply {
                color = Color.parseColor("#5A6472")
                textSize = 28f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(zikr.virtueText.take(50) + "...", width / 2f, 1205f, virtuePaint)
        }

        val appTagPaint = TextPaint().apply {
            color = Color.parseColor("#1B5E20")
            textSize = 26f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("تطبيق سكينة للأذكار والأدعية", width / 2f, 1280f, appTagPaint)

        return bitmap
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
        val imagesFolder = File(context.cacheDir, "shared_images").apply { mkdirs() }
        val file = File(imagesFolder, "sakinah_zikr_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
