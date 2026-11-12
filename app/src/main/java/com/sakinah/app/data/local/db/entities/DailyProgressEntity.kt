package com.sakinah.app.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * الكيان الخاص بتتبع الإنجاز اليومي والمواظبة (Consistency & Daily Progress).
 */
@Entity(tableName = "daily_progress")
data class DailyProgressEntity(
    @PrimaryKey
    val date: String, // صيغة YYYY-MM-DD
    val completedAzkarIds: String, // قائمة المعرفات مفصولة بفواصل
    val totalTasbihCount: Int,
    val completedCategories: String // قائمة الأقسام المكتملة مفصولة بفواصل
)
