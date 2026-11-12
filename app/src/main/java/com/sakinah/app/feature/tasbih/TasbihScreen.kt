package com.sakinah.app.feature.tasbih

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakinah.app.core.ui.theme.SakinahTheme
import com.sakinah.app.core.ui.theme.SakinahThemeMode
import com.sakinah.app.core.utils.rememberHapticFeedback

@Composable
fun TasbihScreen(
    viewModel: TasbihViewModel = hiltViewModel()
) {
    val currentPhraseIndex by viewModel.currentPhraseIndex.collectAsState()
    val count by viewModel.currentCount.collectAsState()
    val totalCount by viewModel.totalSessionCount.collectAsState()

    val phrase = viewModel.phrases[currentPhraseIndex]
    val haptic = rememberHapticFeedback()

    TasbihContent(
        phrase = phrase,
        count = count,
        totalCount = totalCount,
        onTap = {
            val newCount = viewModel.onScreenTapped()
            when (newCount) {
                33, 66 -> haptic.milestone()
                phrase.target -> haptic.completion()
                else -> haptic.tasbihTap()
            }
        },
        onSwipeNext = {
            viewModel.nextPhrase()
            haptic.swipe()
        },
        onSwipePrev = {
            viewModel.prevPhrase()
            haptic.swipe()
        },
        onReset = viewModel::resetCount
    )
}

@Composable
fun TasbihContent(
    phrase: TasbihPhrase,
    count: Int,
    totalCount: Int,
    onTap: () -> Unit,
    onSwipeNext: () -> Unit,
    onSwipePrev: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressFraction = (count.toFloat() / phrase.target.toFloat()).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (totalDrag > 80f) {
                            onSwipeNext()
                        } else if (totalDrag < -80f) {
                            onSwipePrev()
                        }
                        totalDrag = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        totalDrag += dragAmount
                    }
                )
            }
            .pointerInput(phrase.target) {
                detectTapGestures(
                    onTap = { onTap() }
                )
            }
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "إجمالي الجلسة: $totalCount",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            IconButton(onClick = onReset) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "إعادة ضبط",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = phrase,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "PhraseTransition"
            ) { targetPhrase ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = targetPhrase.arabicText,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = targetPhrase.meaning,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier.size(240.dp),
                    strokeWidth = 10.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "الهدف: ${phrase.target}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = " اسحب يميناً أو يساراً للتبديل • المس في أي مكان للعدّ ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  COMPOSE PREVIEWS — الوضع النهاري والوضع الليلي
// ═══════════════════════════════════════════════════════════════════

@Preview(name = "Tasbih Screen - Light Warm Paper", showBackground = true)
@Composable
private fun TasbihScreenLightPreview() {
    SakinahTheme(themeMode = SakinahThemeMode.WARM_PAPER) {
        TasbihContent(
            phrase = TasbihPhrase(1, "سُبْحَانَ اللَّهِ", "تنزيه الله تعالى عن كل نقص وعيب", 33),
            count = 14,
            totalCount = 47,
            onTap = {},
            onSwipeNext = {},
            onSwipePrev = {},
            onReset = {}
        )
    }
}

@Preview(name = "Tasbih Screen - OLED Black", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun TasbihScreenDarkPreview() {
    SakinahTheme(themeMode = SakinahThemeMode.PURE_OLED_BLACK) {
        TasbihContent(
            phrase = TasbihPhrase(2, "الْحَمْدُ لِلَّهِ", "الثناء والشكر الكامل للمنعم", 33),
            count = 33,
            totalCount = 99,
            onTap = {},
            onSwipeNext = {},
            onSwipePrev = {},
            onReset = {}
        )
    }
}
