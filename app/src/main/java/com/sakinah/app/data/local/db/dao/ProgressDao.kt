package com.sakinah.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sakinah.app.data.local.db.entities.DailyProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {

    @Query("SELECT * FROM daily_progress WHERE date = :date LIMIT 1")
    fun getProgressByDate(date: String): Flow<DailyProgressEntity?>

    @Query("SELECT * FROM daily_progress ORDER BY date DESC LIMIT :limit")
    fun getRecentProgress(limit: Int): Flow<List<DailyProgressEntity>>

    @Query("SELECT * FROM daily_progress ORDER BY date ASC")
    fun getAllProgress(): Flow<List<DailyProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: DailyProgressEntity)

    @Query("SELECT SUM(totalTasbihCount) FROM daily_progress")
    fun getTotalLifetimeTasbih(): Flow<Int?>
}
