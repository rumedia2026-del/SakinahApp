package com.sakinah.app.data.repository

import com.sakinah.app.data.local.db.dao.CategoryDao
import com.sakinah.app.data.local.db.dao.ZikrDao
import com.sakinah.app.data.local.db.entities.CategoryEntity
import com.sakinah.app.data.local.db.entities.ZikrEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AzkarRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val zikrDao: ZikrDao
) {

    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun getCategoryById(id: Int): CategoryEntity? = categoryDao.getCategoryById(id)

    fun getAzkarByCategory(categoryId: Int): Flow<List<ZikrEntity>> =
        zikrDao.getAzkarByCategory(categoryId)

    fun getZikrById(id: Int): Flow<ZikrEntity?> = zikrDao.getZikrById(id)

    fun getFavoriteAzkar(): Flow<List<ZikrEntity>> = zikrDao.getFavoriteAzkar()

    fun getAzkarByEmotionalTag(tag: String): Flow<List<ZikrEntity>> =
        zikrDao.getAzkarByEmotionalTag(tag)

    fun searchAzkar(query: String): Flow<List<ZikrEntity>> = zikrDao.searchAzkar(query)

    suspend fun toggleFavorite(id: Int, currentFavoriteStatus: Boolean) {
        zikrDao.updateFavoriteStatus(id, !currentFavoriteStatus)
    }

    suspend fun addCustomZikr(zikr: ZikrEntity) {
        zikrDao.insertZikr(zikr.copy(isUserCustom = true))
    }

    suspend fun deleteCustomZikr(id: Int) {
        zikrDao.deleteCustomZikr(id)
    }

    suspend fun isDatabaseEmpty(): Boolean = categoryDao.getCount() == 0
}
