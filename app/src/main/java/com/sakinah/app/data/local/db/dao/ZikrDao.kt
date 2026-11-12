package com.sakinah.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sakinah.app.data.local.db.entities.ZikrEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ZikrDao {

    @Query("SELECT * FROM azkar WHERE categoryId = :categoryId ORDER BY id ASC")
    fun getAzkarByCategory(categoryId: Int): Flow<List<ZikrEntity>>

    @Query("SELECT * FROM azkar WHERE id = :id LIMIT 1")
    fun getZikrById(id: Int): Flow<ZikrEntity?>

    @Query("SELECT * FROM azkar WHERE isFavorite = 1 ORDER BY id ASC")
    fun getFavoriteAzkar(): Flow<List<ZikrEntity>>

    @Query("SELECT * FROM azkar WHERE emotionalTags LIKE '%' || :tag || '%'")
    fun getAzkarByEmotionalTag(tag: String): Flow<List<ZikrEntity>>

    @Query("SELECT * FROM azkar WHERE textWithTashkeel LIKE '%' || :query || '%' OR textPlain LIKE '%' || :query || '%'")
    fun searchAzkar(query: String): Flow<List<ZikrEntity>>

    @Query("UPDATE azkar SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAzkar(azkar: List<ZikrEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZikr(zikr: ZikrEntity)

    @Update
    suspend fun updateZikr(zikr: ZikrEntity)

    @Query("DELETE FROM azkar WHERE id = :id AND isUserCustom = 1")
    suspend fun deleteCustomZikr(id: Int)

    @Query("SELECT COUNT(*) FROM azkar")
    suspend fun getCount(): Int
}
