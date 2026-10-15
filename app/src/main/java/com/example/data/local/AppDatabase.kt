package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        ContactEntity::class,
        TaskEntity::class,
        SubTaskEntity::class,
        IncorporationEntity::class,
        IncorporationPartyEntity::class,
        IncorporationChecklistEntity::class,
        ContractEntity::class,
        ContractObligationEntity::class,
        ManagementRequestEntity::class,
        FinanceFollowupEntity::class,
        OpportunityEntity::class,
        MeetingEntity::class,
        DocumentEntity::class,
        ActivityLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun taskDao(): TaskDao
    abstract fun incorporationDao(): IncorporationDao
    abstract fun contractDao(): ContractDao
    abstract fun managementRequestDao(): ManagementRequestDao
    abstract fun financeDao(): FinanceDao
    abstract fun opportunityDao(): OpportunityDao
    abstract fun meetingDao(): MeetingDao
    abstract fun documentDao(): DocumentDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sherkety_legal_desk.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
