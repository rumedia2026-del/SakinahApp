package com.sakinah.app.data.local.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * الكيان الخاص بنصوص الأذكار وتفاصيلها.
 */
@Entity(
    tableName = "azkar",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["isFavorite"])
    ]
)
data class ZikrEntity(
    @PrimaryKey
    val id: Int,
    val categoryId: Int,
    val textWithTashkeel: String,
    val textPlain: String,
    val repeatCount: Int,
    val virtueText: String?,
    val sourceReference: String?,
    val authenticityGrade: String?,
    val audioUrl: String?,
    val emotionalTags: String?,
    val isUserCustom: Boolean = false,
    val isFavorite: Boolean = false
)
