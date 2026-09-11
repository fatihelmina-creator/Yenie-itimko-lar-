package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY id ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: Long): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity): Long

    @Update
    suspend fun updateStudent(student: StudentEntity)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE studentId = :studentId ORDER BY id DESC")
    fun getGoalsForStudent(studentId: Long): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoal(id: Long)
}

@Dao
interface SubjectProgressDao {
    @Query("SELECT * FROM subject_progress WHERE studentId = :studentId")
    fun getProgressForStudent(studentId: Long): Flow<List<SubjectProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: SubjectProgressEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(progressList: List<SubjectProgressEntity>)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE studentId = :studentId ORDER BY isCompleted ASC, id DESC")
    fun getTasksForStudent(studentId: Long): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
    suspend fun setTaskCompleted(id: Long, isCompleted: Boolean, completedAt: String?)
}

@Dao
interface MockExamDao {
    @Query("SELECT * FROM mock_exams WHERE studentId = :studentId ORDER BY id DESC")
    fun getExamsForStudent(studentId: Long): Flow<List<MockExamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: MockExamEntity): Long
}

@Dao
interface SwotDao {
    @Query("SELECT * FROM swot_analyses WHERE studentId = :studentId LIMIT 1")
    fun getSwotForStudent(studentId: Long): Flow<SwotEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSwot(swot: SwotEntity): Long
}

@Dao
interface MeetingDao {
    @Query("SELECT * FROM coaching_meetings WHERE studentId = :studentId ORDER BY id DESC")
    fun getMeetingsForStudent(studentId: Long): Flow<List<MeetingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeeting(meeting: MeetingEntity): Long
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE channelId = :channelId ORDER BY id ASC")
    fun getMessagesForChannel(channelId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule_items WHERE studentId = :studentId ORDER BY orderIndex ASC, id ASC")
    fun getScheduleForStudent(studentId: Long): Flow<List<ScheduleItemEntity>>

    @Query("SELECT * FROM schedule_items WHERE studentId = :studentId AND dayOfWeek = :dayOfWeek ORDER BY orderIndex ASC, id ASC")
    fun getScheduleForStudentAndDay(studentId: Long, dayOfWeek: String): Flow<List<ScheduleItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(item: ScheduleItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSchedules(items: List<ScheduleItemEntity>)

    @Update
    suspend fun updateSchedule(item: ScheduleItemEntity)

    @Update
    suspend fun updateAllSchedules(items: List<ScheduleItemEntity>)

    @Query("UPDATE schedule_items SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateScheduleStatus(id: Long, isCompleted: Boolean)

    @Query("UPDATE schedule_items SET hasReminder = :hasReminder, reminderMinutesBefore = :reminderMinutes WHERE id = :id")
    suspend fun updateScheduleReminder(id: Long, hasReminder: Boolean, reminderMinutes: Int)

    @Delete
    suspend fun deleteSchedule(item: ScheduleItemEntity)

    @Query("DELETE FROM schedule_items WHERE id = :id")
    suspend fun deleteScheduleById(id: Long)
}
