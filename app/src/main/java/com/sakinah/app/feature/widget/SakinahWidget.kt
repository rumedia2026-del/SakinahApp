package com.sakinah.app.feature.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import java.util.Calendar

val KEY_WIDGET_TASBIH_COUNT = intPreferencesKey("widget_tasbih_count")

class SakinahWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                val count = currentState(key = KEY_WIDGET_TASBIH_COUNT) ?: 0
                val (phrase, target) = getContextualPhrase()

                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.surface)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سكينة • تسبيح الشاشة الرئيسية",
                            style = TextStyle(
                                color = GlanceTheme.colors.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = GlanceModifier.height(8.dp))

                        Text(
                            text = phrase,
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        )

                        Spacer(modifier = GlanceModifier.height(10.dp))

                        // زر التسبيح والعدّ المباشر من الشاشة الرئيسية
                        Row(
                            modifier = GlanceModifier
                                .background(GlanceTheme.colors.primary)
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                                .clickable(actionRunCallback<WidgetTasbihActionCallback>()),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تسبيح  |  $count",
                                style = TextStyle(
                                    color = GlanceTheme.colors.onPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getContextualPhrase(): Pair<String, Int> {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 4..11 -> "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ" to 33
            in 15..19 -> "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ" to 33
            in 20..23, in 0..3 -> "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ" to 33
            else -> "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ" to 100
        }
    }
}

class WidgetTasbihActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, glanceId) { prefs ->
            val current = prefs[KEY_WIDGET_TASBIH_COUNT] ?: 0
            prefs[KEY_WIDGET_TASBIH_COUNT] = current + 1
        }
        SakinahWidget().update(context, glanceId)
    }
}
