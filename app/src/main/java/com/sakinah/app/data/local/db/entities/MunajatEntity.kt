package com.sakinah.app.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "munajat")
data class MunajatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val emotionTag: String? = null,
    val isAnswered: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val answeredAt: Long? = null
)
