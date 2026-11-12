package com.sakinah.app.feature.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 1. حلقات العبادة اليومية الثلاث (Daily Worship Rings)
 * - أذكار (أخضر زمردي)
 * - ورد القرآن (ذهبي)
 * - التسبيح (أزرق سماوي)
 */
@Composable
fun DailyWorshipRingsCard(
    azkarProgress: Float = 0.75f,
    quranProgress: Float = 0.50f,
    tasbihProgress: Float = 0.85f,
    onAzkarClick: () -> Unit = {},
    onQuranClick: () -> Unit = {},
    onTasbihClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val emeraldColor = Color(0xFF2E7D32)
    val goldColor = Color(0xFFC8A951)
    val sapphireColor = Color(0xFF1976D2)

    val animAzkar by animateFloatAsState(targetValue = azkarProgress.coerceIn(0f, 1f), animationSpec = tween(800), label = "AzkarRing")
    val animQuran by animateFloatAsState(targetValue = quranProgress.coerceIn(0f, 1f), animationSpec = tween(800), label = "QuranRing")
    val animTasbih by animateFloatAsState(targetValue = tasbihProgress.coerceIn(0f, 1f), animationSpec = tween(800), label = "TasbihRing")

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "حلقات العبادة اليومية",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "هدف اليوم المبارك",
                    style = MaterialTheme.typography.labelSmall,
                    color = goldColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // الحلقات الدائرية المتداخلة
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(105.dp)) {
                        val strokeWidth = 8.dp.toPx()
                        val centerOffset = Offset(size.width / 2f, size.height / 2f)

                        // الحلقة الخارجية: الأذكار
                        val radiusOuter = 46.dp.toPx()
                        drawCircle(
                            color = emeraldColor.copy(alpha = 0.15f),
                            radius = radiusOuter,
                            style = Stroke(strokeWidth)
                        )
                        drawArc(
                            color = emeraldColor,
                            startAngle = -90f,
                            sweepAngle = animAzkar * 360f,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round),
                            topLeft = Offset(centerOffset.x - radiusOuter, centerOffset.y - radiusOuter),
                            size = Size(radiusOuter * 2, radiusOuter * 2)
                        )

                        // الحلقة الوسطى: القرآن
                        val radiusMid = 34.dp.toPx()
                        drawCircle(
                            color = goldColor.copy(alpha = 0.15f),
                            radius = radiusMid,
                            style = Stroke(strokeWidth)
                        )
                        drawArc(
                            color = goldColor,
                            startAngle = -90f,
                            sweepAngle = animQuran * 360f,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round),
                            topLeft = Offset(centerOffset.x - radiusMid, centerOffset.y - radiusMid),
                            size = Size(radiusMid * 2, radiusMid * 2)
                        )

                        // الحلقة الداخلية: التسبيح
                        val radiusInner = 22.dp.toPx()
                        drawCircle(
                            color = sapphireColor.copy(alpha = 0.15f),
                            radius = radiusInner,
                            style = Stroke(strokeWidth)
                        )
                        drawArc(
                            color = sapphireColor,
                            startAngle = -90f,
                            sweepAngle = animTasbih * 360f,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round),
                            topLeft = Offset(centerOffset.x - radiusInner, centerOffset.y - radiusInner),
                            size = Size(radiusInner * 2, radiusInner * 2)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = emeraldColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // قائمة المقاييس الثلاثة
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // أذكار
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .clickable { onAzkarClick() }
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(emeraldColor))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("الأذكار اليومية", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        }
                        Text("${(animAzkar * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = emeraldColor)
                    }

                    // ورد القرآن
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .clickable { onQuranClick() }
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(goldColor))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ورد القرآن", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        }
                        Text("${(animQuran * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = goldColor)
                    }

                    // التسبيح
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .clickable { onTasbihClick() }
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(sapphireColor))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("التسبيح والذكر", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        }
                        Text("${(animTasbih * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = sapphireColor)
                    }
                }
            }
        }
    }
}

/**
 * 2. بطاقة "آية وتدبر اليوم" (Daily Ayah & Tadabbur)
 */
@Composable
fun DailyAyahTadabburCard(
    modifier: Modifier = Modifier,
    onTadabburClick: () -> Unit = {}
) {
    val goldColor = Color(0xFFC8A951)
    val deepEmerald = Color(0xFF0F3D2E)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .border(1.dp, goldColor.copy(alpha = 0.35f), MaterialTheme.shapes.large)
            .clickable { onTadabburClick() },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = null,
                        tint = goldColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "آية وتدبر اليوم",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = goldColor
                    )
                }
                Text(
                    text = "سورة الشرح • آية 6",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "﴿ إِنَّ مَعَ الْعُسْرِ يُسْرًا ﴾",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "💡 وقفة تدبرية: لم يقل بعدها بل قال (معه) تأكيداً على أن لطف الله وفرجه يصاحب البلاء منذ أول لحظة، فلن يغلب عسر يسرين.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

/**
 * 3. بطاقة "أسماء الله الحسنى" (99 Names of Allah)
 */
@Composable
fun NamesOfAllahCard(
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val goldColor = Color(0xFFC8A951)

    val sampleNames = listOf(
        Pair("الرَّحْمَٰنُ", "الواسع الرحمة الذي وسعت رحمته كل شيء في خلقه"),
        Pair("الرَّحِيمُ", "الخاص برحمته بالمؤمنين هداية وتوفيقاً في الدنيا وفوزاً في الآخرة"),
        Pair("السَّلَامُ", "السالم من كل عيب ونقص، وواهب السكينة والطمأنينة لعباده"),
        Pair("الْمُؤْمِنُ", "الذي أمن خلقه من عذابه وظلمه، وصدق رسله بآياته"),
        Pair("الْوَهَّابُ", "الكثير الجود والعطاء بلا عوض ولا سؤال"),
        Pair("الرَّزَّاقُ", "المتكفل بأرزاق العباد جميعاً الصغير والكبير"),
        Pair("الْفَتَّاحُ", "الذي يفتح أبواب الرحمة والرزق والفرج من حيث لا يحتسب العبد"),
        Pair("الْحَلِيمُ", "الذي لا يعجل بالعقوبة بل يعفو ويصفح ويرحم"),
        Pair("الْوَدُودُ", "المحب لأوليائه والمتحبب إلى خلقه بنعمه"),
        Pair("النُّورُ", "الذي استنارت السماوات والأرض بنوره، وهادي القلوب بهداه")
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = goldColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "أسماء الله الحسنى وتأملاتها",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = goldColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "«إِنَّ لِلَّهِ تِسْعَةً وَتِسْعِينَ اسْمًا، مِائَةً إِلَّا وَاحِدًا، مَنْ أَحْصَاهَا دَخَلَ الْجَنَّةَ»",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // شريط أفقي للأسماء
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(sampleNames) { (name, meaning) ->
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .background(goldColor.copy(alpha = 0.10f))
                            .border(1.dp, goldColor.copy(alpha = 0.35f), MaterialTheme.shapes.medium)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = goldColor
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    HorizontalDivider(color = goldColor.copy(alpha = 0.25f))
                    Spacer(modifier = Modifier.height(10.dp))
                    sampleNames.forEach { (name, meaning) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "$name:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = goldColor,
                                modifier = Modifier.width(70.dp)
                            )
                            Text(
                                text = meaning,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
