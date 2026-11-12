package com.sakinah.app.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * الكيان الخاص بأقسام وتصنيفات الأذكار.
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: Int,
    val titleAr: String,
    val iconName: String,
    val recommendedTimeStart: String,
    val recommendedTimeEnd: String,
    val orderIndex: Int
)
