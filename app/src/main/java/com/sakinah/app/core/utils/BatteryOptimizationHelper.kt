package com.sakinah.app.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

data class ManufacturerGuide(
    val brandName: String,
    val title: String,
    val steps: List<String>,
    val actionIntent: Intent?
)

object BatteryOptimizationHelper {

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true
    }

    fun requestIgnoreBatteryOptimizations(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback إلى صفحة تفاصيل التطبيق
            openAppDetailsSettings(context)
        }
    }

    fun openAppDetailsSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun getDeviceSpecificGuide(context: Context): ManufacturerGuide {
        val manufacturer = Build.MANUFACTURER.lowercase()

        return when {
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") -> {
                ManufacturerGuide(
                    brandName = "Xiaomi / HyperOS / MIUI",
                    title = "ضبط هواتف شاومي لضمان وصول التنبيهات",
                    steps = listOf(
                        "1. تفعيل 'التشغيل التلقائي' (Autostart) لتطبيق سكينة.",
                        "2. ضبط موفر البطارية (Battery saver) على 'لا توجد قيود' (No restrictions).",
                        "3. قفل التطبيق في قائمة التطبيقات المفتوحة (Recent Apps)."
                    ),
                    actionIntent = Intent().apply {
                        setClassName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
            }
            manufacturer.contains("samsung") -> {
                ManufacturerGuide(
                    brandName = "Samsung Galaxy",
                    title = "ضبط هواتف سامسونج لعدم إيقاف الأذكار",
                    steps = listOf(
                        "1. الدخول إلى 'البطارية والعناية بالجهاز'.",
                        "2. اختيار 'حدود استخدام الخلفية' (Background usage limits).",
                        "3. إضافة سكينة إلى 'التطبيقات التي لن يتم وضعها في وضع السكون مطلقاً' (Never sleeping apps)."
                    ),
                    actionIntent = Intent().apply {
                        setClassName("com.samsung.android.lool", "com.samsung.android.sm.battery.ui.BatteryActivity")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
            }
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> {
                ManufacturerGuide(
                    brandName = "Huawei / Honor",
                    title = "ضبط هواتف هواوي وهونر",
                    steps = listOf(
                        "1. الدخول إلى إعدادات 'البطارية' ثم 'تشغيل التطبيقات' (App launch).",
                        "2. إيقاف الإدارة التلقائية لتطبيق سكينة وتفعيل: التشغيل التلقائي، التشغيل الثانوي، والتشغيل في الخلفية."
                    ),
                    actionIntent = Intent().apply {
                        setClassName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
            }
            manufacturer.contains("oppo") || manufacturer.contains("realme") || manufacturer.contains("oneplus") -> {
                ManufacturerGuide(
                    brandName = "Oppo / Realme / OnePlus",
                    title = "ضبط هواتف أوبو وريلمي وون بلس",
                    steps = listOf(
                        "1. تمكين خيار 'السماح بالنشاط في الخلفية' (Allow background activity).",
                        "2. إيقاف 'التحسين التلقائي' (Auto-optimize) للبطارية لتطبيق سكينة."
                    ),
                    actionIntent = Intent().apply {
                        setClassName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
            }
            else -> {
                ManufacturerGuide(
                    brandName = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
                    title = "استثناء التطبيق من تحسين البطارية",
                    steps = listOf(
                        "1. الضغط على زر الاستثناء بالأسفل.",
                        "2. اختيار 'السماح' لتطبيق سكينة بالعمل في الخلفية دون قيود."
                    ),
                    actionIntent = null
                )
            }
        }
    }
}
