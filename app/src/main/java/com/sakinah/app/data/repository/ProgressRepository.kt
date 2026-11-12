package com.sakinah.app.data.repository

import com.sakinah.app.data.local.db.dao.ProgressDao
import com.sakinah.app.data.local.db.entities.DailyProgressEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepository @Inject constructor(
    private val progressDao: ProgressDao
) {

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun getTodayProgress(): Flow<DailyProgressEntity?> {
        return progressDao.getProgressByDate(getTodayDateString())
    }

    fun getRecentProgress(limit: Int = 30): Flow<List<DailyProgressEntity>> {
        return progressDao.getRecentProgress(limit)
    }

    fun getTotalLifetimeTasbih(): Flow<Int?> {
        return progressDao.getTotalLifetimeTasbih()
    }

    suspend fun recordTasbih(count: Int) {
        val today = getTodayDateString()
        // ملاحظة: يُفضل استخدام استعلام تحديث مباشر أو جمع القيمة الحالية
        progressDao.insertOrUpdateProgress(
            DailyProgressEntity(
                date = today,
                completedAzkarIds = "",
                totalTasbihCount = count,
                completedCategories = ""
            )
        )
    }

    suspend fun saveDailyProgress(progress: DailyProgressEntity) {
        progressDao.insertOrUpdateProgress(progress)
    }
}
