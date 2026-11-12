package com.sakinah.app.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sakinah.app.R
import com.sakinah.app.data.local.db.dao.CategoryDao
import com.sakinah.app.data.local.db.dao.ProgressDao
import com.sakinah.app.data.local.db.dao.ZikrDao
import com.sakinah.app.data.local.db.entities.CategoryEntity
import com.sakinah.app.data.local.db.entities.DailyProgressEntity
import com.sakinah.app.data.local.db.entities.ZikrEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

@Database(
    entities = [
        CategoryEntity::class,
        ZikrEntity::class,
        DailyProgressEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SakinahDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun zikrDao(): ZikrDao
    abstract fun progressDao(): ProgressDao

    companion object {
        const val DATABASE_NAME = "sakinah_db"

        @Volatile
        private var INSTANCE: SakinahDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope): SakinahDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SakinahDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(SakinahDatabaseCallback(context.applicationContext, scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * مُعالج التحميل المسبق (Pre-population Callback)
     * يقرأ ملف azkar_seed.json ويملأ الجداول لأول مرة تلقائياً
     */
    private class SakinahDatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(context, database)
                }
            }
        }

        private suspend fun populateInitialData(context: Context, database: SakinahDatabase) {
            try {
                val inputStream = context.resources.openRawResource(R.raw.azkar_seed)
                val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
                val jsonString = reader.use { it.readText() }

                val rootObject = JSONObject(jsonString)

                // 1. استخراج وإدراج الأقسام
                val categoriesArray = rootObject.getJSONArray("categories")
                val categories = mutableListOf<CategoryEntity>()
                for (i in 0 until categoriesArray.length()) {
                    val obj = categoriesArray.getJSONObject(i)
                    categories.add(
                        CategoryEntity(
                            id = obj.getInt("id"),
                            titleAr = obj.getString("titleAr"),
                            iconName = obj.getString("iconName"),
                            recommendedTimeStart = obj.getString("recommendedTimeStart"),
                            recommendedTimeEnd = obj.getString("recommendedTimeEnd"),
                            orderIndex = obj.getInt("orderIndex")
                        )
                    )
                }
                database.categoryDao().insertCategories(categories)

                // 2. استخراج وإدراج الأذكار
                val azkarArray = rootObject.getJSONArray("azkar")
                val azkar = mutableListOf<ZikrEntity>()
                for (i in 0 until azkarArray.length()) {
                    val obj = azkarArray.getJSONObject(i)
                    azkar.add(
                        ZikrEntity(
                            id = obj.getInt("id"),
                            categoryId = obj.getInt("categoryId"),
                            textWithTashkeel = obj.getString("textWithTashkeel"),
                            textPlain = obj.getString("textPlain"),
                            repeatCount = obj.getInt("repeatCount"),
                            virtueText = if (obj.isNull("virtueText")) null else obj.getString("virtueText"),
                            sourceReference = if (obj.isNull("sourceReference")) null else obj.getString("sourceReference"),
                            authenticityGrade = if (obj.isNull("authenticityGrade")) null else obj.getString("authenticityGrade"),
                            audioUrl = if (obj.isNull("audioUrl")) null else obj.getString("audioUrl"),
                            emotionalTags = if (obj.isNull("emotionalTags")) null else obj.getString("emotionalTags"),
                            isUserCustom = obj.optBoolean("isUserCustom", false),
                            isFavorite = obj.optBoolean("isFavorite", false)
                        )
                    )
                }
                database.zikrDao().insertAzkar(azkar)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
