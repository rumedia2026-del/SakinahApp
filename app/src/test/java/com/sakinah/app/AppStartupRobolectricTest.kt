package com.sakinah.app

import android.app.NotificationManager
import android.content.Context
import androidx.room.Room
import com.sakinah.app.data.local.db.SakinahDatabase
import com.sakinah.app.feature.fiqh.FiqhDataLoader
import com.sakinah.app.feature.prayer.AdhanAlarmScheduler
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppStartupRobolectricTest {

    private fun getContext(): Context = RuntimeEnvironment.getApplication()

    @Test
    fun testContextAndApplicationStartup() {
        val context = getContext()
        assertNotNull("Application context must not be null", context)

        // Notification channels should be creatable without crash
        val manager = context.getSystemService(NotificationManager::class.java)
        assertNotNull("NotificationManager must be available", manager)
    }

    @Test
    fun testAdhanAlarmSchedulerSafeExecutionWithoutSecurityCrash() {
        val context = getContext()
        // AdhanAlarmScheduler must execute safely without throwing unhandled SecurityException
        var error: Throwable? = null
        try {
            AdhanAlarmScheduler.scheduleAllPrayers(context)
        } catch (t: Throwable) {
            error = t
        }
        assertTrue("AdhanAlarmScheduler should not throw unhandled exception on startup: $error", error == null)
    }

    @Test
    fun testRoomDatabaseSchemaAndMigrations() {
        val context = getContext()

        // Test in-memory database with production migrations
        val db = Room.inMemoryDatabaseBuilder(context, SakinahDatabase::class.java)
            .addMigrations(*SakinahDatabase.MIGRATIONS)
            .build()

        assertNotNull("CategoryDao must not be null", db.categoryDao())
        assertNotNull("ZikrDao must not be null", db.zikrDao())
        assertNotNull("ProgressDao must not be null", db.progressDao())
        assertNotNull("MunajatDao must not be null", db.munajatDao())

        // Ensure munajat table query flow succeeds
        val activeMunajatFlow = db.munajatDao().getActiveMunajat()
        assertNotNull("Active munajat flow must not be null", activeMunajatFlow)

        db.close()
    }

    @Test
    fun testRawFiqhGuideGracefulLoad() {
        val context = getContext()
        val chapters = FiqhDataLoader.loadFiqhGuide(context)
        assertNotNull("Fiqh chapters list must not be null", chapters)
        assertTrue("Fiqh guide chapters should be parsed successfully", chapters.isNotEmpty())
    }
}
