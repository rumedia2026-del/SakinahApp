package com.sakinah.app.core.utils.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.sakinah.app.R
import com.sakinah.app.data.local.db.entities.ZikrEntity
import java.io.File
import java.io.FileOutputStream

object ZikrCardGenerator {

    /**
     * توليد بطاقة تصميمية عالية الدقة للذكر ومشاركتها مباشرة
     */
    fun shareZikrAsImage(context: Context, zikr: ZikrEntity, showTashkeel: Boolean = true) {
        val bitmap = createZikrShareBitmap(context, zikr, showTashkeel)
        val uri = saveBitmapToCache(context, bitmap)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "${zikr.textPlain}\n\nتطبيق سكينة للأذكار والأدعية 🕌")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "مشاركة الذكر كبطاقة تصميمية"))
    }

    /**
     * دالة متوافقة مع الاستدعاء القديم
     */
    fun createZikrBitmap(context: Context, zikr: ZikrEntity, showTashkeel: Boolean = true): Bitmap {
        return createZikrShareBitmap(context, zikr, showTashkeel)
    }

    /**
     * توليد بطاقة الذكر بدقة متناهية، توسيط عربي RTL، وارتفاع ديناميكي يمنع قص النصوص نهائياً
     */
    fun createZikrShareBitmap(context: Context, zikr: ZikrEntity, showTashkeel: Boolean = true): Bitmap {
        val width = 1080
        val sideMargin = 80f
        val textWidth = (width - (sideMargin * 2)).toInt() // 920px منطقة آمنة للنص

        val textToRender = if (showTashkeel) zikr.textWithTashkeel else zikr.textPlain

        // تحديد حجم الخط ديناميكياً حسب طول النص
        val baseTextSize = when {
            textToRender.length > 600 -> 32f
            textToRender.length > 350 -> 36f
            textToRender.length > 200 -> 40f
            textToRender.length > 100 -> 44f
            else -> 48f
        }

        val textPaint = TextPaint().apply {
            color = Color.parseColor("#0F3D2E") // أخضر زمردي عميق
            textSize = baseTextSize
            isAntiAlias = true
        }

        // إنشاء المخطط النصي بتوسيط دقيق وتوجيه RTL لمنع خروج الحروف خارج الإطار
        val textLayout = StaticLayout.Builder.obtain(
            textToRender,
            0,
            textToRender.length,
            textPaint,
            textWidth
        )
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_RTL)
            .setLineSpacing(22f, 1f)
            .setIncludePad(true)
            .build()

        // حساب الارتفاع الديناميكي للبطاقة لمنع قص النصوص الطويلة
        val headerHeight = 330f
        val footerHeight = 280f
        val safeVerticalPadding = 120f
        val neededHeight = (headerHeight + textLayout.height + footerHeight + safeVerticalPadding).toInt()
        val height = maxOf(1350, neededHeight) // 1350 كحد أدنى للمحافظة على أبعاد المشاركة الفاخرة

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. خلفية كريمية دافئة فاخرة (#FDFBF7)
        canvas.drawColor(Color.parseColor("#FDFBF7"))

        // 2. إطار مزدوج: خارجي أخضر زمردي (#0F3D2E) وداخلي ذهبي (#C8A951)
        val outerBorderPaint = Paint().apply {
            color = Color.parseColor("#0F3D2E")
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(32f, 32f, (width - 32).toFloat(), (height - 32).toFloat()), 36f, 36f, outerBorderPaint)

        val innerBorderPaint = Paint().apply {
            color = Color.parseColor("#C8A951")
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(46f, 46f, (width - 46).toFloat(), (height - 46).toFloat()), 26f, 26f, innerBorderPaint)

        // 3. الترويسة: شعار تطبيق "سكينة" واسم التطبيق
        val logoBitmap = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.sakinah_logo)
        } catch (_: Exception) {
            null
        }

        var headerY = 75f
        if (logoBitmap != null) {
            val logoSize = 100
            val scaledLogo = Bitmap.createScaledBitmap(logoBitmap, logoSize, logoSize, true)
            canvas.drawBitmap(scaledLogo, (width - logoSize) / 2f, headerY, null)
            headerY += logoSize + 20f
        } else {
            headerY += 30f
        }

        val titlePaint = TextPaint().apply {
            color = Color.parseColor("#0F3D2E")
            textSize = 50f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("سـَـكـِـيـنـَـة", width / 2f, headerY + 45f, titlePaint)

        val subtitlePaint = TextPaint().apply {
            color = Color.parseColor("#C8A951")
            textSize = 28f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("ألا بذكر الله تطمئن القلوب", width / 2f, headerY + 95f, subtitlePaint)

        // خط فاصل زخرفي علوي ذهبي
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#C8A951")
            strokeWidth = 2.5f
            isAntiAlias = true
        }
        canvas.drawLine(180f, headerY + 130f, (width - 180).toFloat(), headerY + 130f, dividerPaint)

        // 4. رسم نص الذكر في المنتصف
        // تمركز النص رأسياً داخل المساحة المخصصة له
        val textStartY = headerY + 170f
        val availableTextSpace = height - textStartY - footerHeight
        val verticalOffset = ((availableTextSpace - textLayout.height) / 2f).coerceAtLeast(0f)
        val textY = textStartY + verticalOffset

        canvas.save()
        canvas.translate(sideMargin, textY)
        textLayout.draw(canvas)
        canvas.restore()

        // 5. خط فاصل زخرفي سفلي ذهبي
        val footerStartY = height - footerHeight + 40f
        canvas.drawLine(180f, footerStartY, (width - 180).toFloat(), footerStartY, dividerPaint)

        // 6. التذييل: المصدر والفضل وتطبيق سكينة
        val source = zikr.sourceReference ?: "صحيح الأذكار"
        val footerPaint = TextPaint().apply {
            color = Color.parseColor("#0F3D2E")
            textSize = 32f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("• المصدر: $source •", width / 2f, footerStartY + 55f, footerPaint)

        if (!zikr.virtueText.isNullOrBlank()) {
            val virtuePaint = TextPaint().apply {
                color = Color.parseColor("#5A6472")
                textSize = 26f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            val virtue = if (zikr.virtueText.length > 70) zikr.virtueText.take(67) + "..." else zikr.virtueText
            canvas.drawText(virtue, width / 2f, footerStartY + 105f, virtuePaint)
        }

        val appTagPaint = TextPaint().apply {
            color = Color.parseColor("#C8A951")
            textSize = 26f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("تطبيق سكينة للأذكار والمصحف الشريف 🕌", width / 2f, footerStartY + 160f, appTagPaint)

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
