package com.sakinah.app.feature.azkar.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakinah.app.core.utils.HapticFeedback
import com.sakinah.app.core.utils.share.ZikrCardGenerator
import com.sakinah.app.data.local.db.entities.ZikrEntity

@Composable
fun ZikrCardItem(
    zikr: ZikrEntity,
    currentCount: Int,
    showTashkeel: Boolean,
    fontSizeMultiplier: Float,
    haptic: HapticFeedback,
    deafMode: Boolean = false,
    isPlayingAudio: Boolean = false,
    onTap: () -> Unit,
    onToggleFavorite: () -> Unit,
    onPlayAudio: (() -> Unit)? = null,
    onSpeak: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val effectivePlayAudio = onPlayAudio ?: onSpeak
    var isExpanded by remember { mutableStateOf(false) }
    val isCompleted = currentCount >= zikr.repeatCount

    val displayText = if (showTashkeel) zikr.textWithTashkeel else zikr.textPlain
    val targetProgress = (currentCount.toFloat() / zikr.repeatCount.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = targetProgress, label = "ZikrRingProgress")

    val goldAccent = Color(0xFFC8A951)
    val emeraldPrimary = Color(0xFF0F3D2E)

    // تصميم هادئ ونظيف: عند الاكتمال تصبح الخلفية خضراء زمردية شفافة بنسبة 5% فقط مع إطار ذهبي رفيع 1dp وبدون أي تظليل رمادي
    val cardBackground = if (isCompleted) {
        Color(0xFF0F3D2E).copy(alpha = 0.05f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val cardBorderColor = if (isCompleted) {
        goldAccent.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable {
                if (!isCompleted) {
                    onTap()
                    if (currentCount + 1 == zikr.repeatCount) {
                        haptic.completion(deafMode)
                    } else {
                        haptic.tasbihTap(deafMode)
                    }
                }
            },
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, cardBorderColor),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            draggedElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // شريط الإجراءات العلوي
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // شارة عدد التكرار أو شارة الإتمام الذهبية
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(
                            if (isCompleted) goldAccent.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isCompleted) "✓ تم بحمد الله" else "التكرار: $currentCount / ${zikr.repeatCount}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) goldAccent else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // زر التلاوة الصوتية للشيخ
                    if (effectivePlayAudio != null) {
                        IconButton(
                            onClick = effectivePlayAudio,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlayingAudio) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "استماع بصوت الشيخ",
                                tint = if (isPlayingAudio) goldAccent else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // زر مشاركة كصورة
                    IconButton(
                        onClick = {
                            ZikrCardGenerator.shareZikrAsImage(context, zikr, showTashkeel)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة كبطاقة",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (zikr.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "المفضلة",
                            tint = if (zikr.isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // نص الذكر الشريف (تخفيف الشفافية قليلاً عند الاكتمال إلى 0.75f)
            Text(
                text = displayText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = (21 * fontSizeMultiplier).sp,
                    lineHeight = (38 * fontSizeMultiplier).sp
                ),
                color = if (isCompleted) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // أسفل البطاقة: الفضل والعداد الدائري المتحرك
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!zikr.virtueText.isNullOrBlank() || !zikr.sourceReference.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .clickable { isExpanded = !isExpanded }
                            .padding(vertical = 6.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isExpanded) "إخفاء التفاصيل" else "فضل الذكر وتخريجه",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // حلقة التقدم الدائرية الأنيقة بدون إطارات سميكة مشوهة
                Box(
                    modifier = Modifier.size(54.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(54.dp),
                        color = if (isCompleted) goldAccent else emeraldPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        strokeWidth = 3.5.dp
                    )
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCompleted) goldAccent.copy(alpha = 0.2f)
                                else emeraldPrimary
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "اكتمل",
                                tint = goldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "${zikr.repeatCount - currentCount}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            // التفاصيل المطوية
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (!zikr.virtueText.isNullOrBlank()) {
                        Text(
                            text = "فضل هذا الذكر:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = goldAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = zikr.virtueText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!zikr.sourceReference.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "المصدر: ${zikr.sourceReference}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}
