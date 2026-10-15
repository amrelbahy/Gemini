package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseSeeder
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Sherkety Legal", appName)
    }

    @Test
    fun `database seeder initializes legal desk records`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getDatabase(context)
        DatabaseSeeder.seedInitialDataIfEmpty(db)

        val tasks = db.taskDao().getAllTasksSnapshot()
        val contracts = db.contractDao().getAllContractsSnapshot()
        val incorporations = db.incorporationDao().getAllIncorporationsSnapshot()

        assertTrue("Tasks should be seeded", tasks.isNotEmpty())
        assertTrue("Contracts should be seeded", contracts.isNotEmpty())
        assertTrue("Incorporations should be seeded", incorporations.isNotEmpty())
    }
}
