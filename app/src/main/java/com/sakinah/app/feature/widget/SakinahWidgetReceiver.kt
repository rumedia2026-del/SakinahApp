package com.sakinah.app.feature.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

// ═══════════════════════════════════════════════════════════════════
//  SAKINAH GLANCE WIDGET RECEIVER
//  Placeholder — سيُكتمل في المرحلة الرابعة
// ═══════════════════════════════════════════════════════════════════

class SakinahWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = SakinahWidget()
}
