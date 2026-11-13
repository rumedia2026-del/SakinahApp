package com.sakinah.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sakinah.app.data.local.db.entities.MunajatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MunajatDao {

    @Query("SELECT * FROM munajat WHERE isAnswered = 0 ORDER BY createdAt DESC")
    fun getActiveMunajat(): Flow<List<MunajatEntity>>

    @Query("SELECT * FROM munajat WHERE isAnswered = 1 ORDER BY answeredAt DESC")
    fun getAnsweredMunajat(): Flow<List<MunajatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(munajat: MunajatEntity): Long

    @Update
    suspend fun update(munajat: MunajatEntity)

    @Delete
    suspend fun delete(munajat: MunajatEntity)

    @Query("UPDATE munajat SET isAnswered = 1, answeredAt = :timestamp WHERE id = :id")
    suspend fun markAsAnswered(id: Long, timestamp: Long = System.currentTimeMillis(), notes: String? = null)
}
