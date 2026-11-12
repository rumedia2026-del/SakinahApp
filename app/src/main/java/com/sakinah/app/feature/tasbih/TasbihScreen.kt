package com.sakinah.app.feature.tasbih

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.HearingDisabled
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TasbihScreen(
    viewModel: TasbihViewModel = hiltViewModel()
) {
    val currentPhraseIndex by viewModel.currentPhraseIndex.collectAsState()
    val count by viewModel.currentCount.collectAsState()
    val totalCount by viewModel.totalSessionCount.collectAsState()

    val phrase = viewModel.phrases[currentPhraseIndex]
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()

    var isDeafMode by remember { mutableStateOf(false) }
    var flashState by remember { mutableStateOf(false) }

    val flashAlpha by animateFloatAsState(
        targetValue = if (flashState) 0.35f else 0f,
        animationSpec = tween(120),
        label = "TasbihFlash"
    )

    val goldAccent = Color(0xFFC8A951)

    Box(modifier = Modifier.fillMaxSize()) {
        TasbihContent(
            phrase = phrase,
            count = count,
            totalCount = totalCount,
            isDeafMode = isDeafMode,
            onToggleDeafMode = {
                isDeafMode = !isDeafMode
                if (isDeafMode) haptic.milestone()
            },
            onTap = {
                val newCount = viewModel.onScreenTapped()

                if (isDeafMode) {
                    scope.launch {
                        flashState = true
                        delay(120)
                        flashState = false
                    }
                }

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

        // وميض الشاشة لفاقدي السمع
        if (flashAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(goldAccent.copy(alpha = flashAlpha))
            )
        }
    }
}

@Composable
fun TasbihContent(
    phrase: TasbihPhrase,
    count: Int,
    totalCount: Int,
    isDeafMode: Boolean,
    onToggleDeafMode: () -> Unit,
    onTap: () -> Unit,
    onSwipeNext: () -> Unit,
    onSwipePrev: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressFraction = (count.toFloat() / phrase.target.toFloat()).coerceIn(0f, 1f)
    val goldAccent = Color(0xFFC8A951)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        totalDrag += dragAmount
                    },
                    onDragEnd = {
                        if (totalDrag > 80f) {
                            onSwipePrev()
                        } else if (totalDrag < -80f) {
                            onSwipeNext()
                        }
                        totalDrag = 0f
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onTap() })
            }
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "إجمالي الجلسة: $totalCount",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = isDeafMode,
                    onClick = onToggleDeafMode,
                    label = { Text(if (isDeafMode) "فاقدي السمع ✓" else "فاقدي السمع", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Icon(Icons.Default.HearingDisabled, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = goldAccent.copy(alpha = 0.2f),
                        selectedLabelColor = goldAccent
                    )
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(onClick = onReset) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "إعادة ضبط",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 14.dp
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "من ${phrase.target}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "المس في أي مكان للتسبيح • اسحب أفقياً لتغيير الذكر",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onSwipePrev) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "السابق",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${phrase.target} تسبيحة",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            IconButton(onClick = onSwipeNext) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "التالي",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
