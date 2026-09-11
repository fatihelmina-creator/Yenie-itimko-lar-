package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        StudentEntity::class,
        GoalEntity::class,
        SubjectProgressEntity::class,
        TaskEntity::class,
        MockExamEntity::class,
        SwotEntity::class,
        MeetingEntity::class,
        ChatMessageEntity::class,
        ScheduleItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class EduCoachDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun goalDao(): GoalDao
    abstract fun subjectProgressDao(): SubjectProgressDao
    abstract fun taskDao(): TaskDao
    abstract fun mockExamDao(): MockExamDao
    abstract fun swotDao(): SwotDao
    abstract fun meetingDao(): MeetingDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: EduCoachDatabase? = null

        fun getDatabase(context: Context): EduCoachDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EduCoachDatabase::class.java,
                    "educoach_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
