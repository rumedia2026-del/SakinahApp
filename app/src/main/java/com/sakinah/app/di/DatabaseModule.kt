package com.sakinah.app.di

import android.content.Context
import com.sakinah.app.data.local.db.SakinahDatabase
import com.sakinah.app.data.local.db.dao.CategoryDao
import com.sakinah.app.data.local.db.dao.ProgressDao
import com.sakinah.app.data.local.db.dao.ZikrDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Provides
    @Singleton
    fun provideSakinahDatabase(
        @ApplicationContext context: Context,
        scope: CoroutineScope
    ): SakinahDatabase {
        return SakinahDatabase.getInstance(context, scope)
    }

    @Provides
    fun provideCategoryDao(database: SakinahDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun provideZikrDao(database: SakinahDatabase): ZikrDao {
        return database.zikrDao()
    }

    @Provides
    fun provideProgressDao(database: SakinahDatabase): ProgressDao {
        return database.progressDao()
    }
}
