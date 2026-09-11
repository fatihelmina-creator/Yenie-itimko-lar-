package com.example.data.repository

import com.example.data.local.EduCoachDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class EduCoachRepository(private val db: EduCoachDatabase) {
    val allStudents: Flow<List<StudentEntity>> = db.studentDao().getAllStudents()

    suspend fun getStudentById(id: Long): StudentEntity? = db.studentDao().getStudentById(id)

    suspend fun insertStudent(student: StudentEntity): Long = db.studentDao().insertStudent(student)

    fun getGoalsForStudent(studentId: Long): Flow<List<GoalEntity>> = db.goalDao().getGoalsForStudent(studentId)

    suspend fun insertGoal(goal: GoalEntity): Long = db.goalDao().insertGoal(goal)

    suspend fun updateGoal(goal: GoalEntity) = db.goalDao().updateGoal(goal)

    suspend fun deleteGoal(id: Long) = db.goalDao().deleteGoal(id)

    fun getProgressForStudent(studentId: Long): Flow<List<SubjectProgressEntity>> =
        db.subjectProgressDao().getProgressForStudent(studentId)

    suspend fun updateSubjectProgress(progress: SubjectProgressEntity) =
        db.subjectProgressDao().insertOrUpdate(progress)

    fun getTasksForStudent(studentId: Long): Flow<List<TaskEntity>> =
        db.taskDao().getTasksForStudent(studentId)

    suspend fun insertTask(task: TaskEntity): Long = db.taskDao().insertTask(task)

    suspend fun setTaskCompleted(taskId: Long, isCompleted: Boolean, completedAt: String?) =
        db.taskDao().setTaskCompleted(taskId, isCompleted, completedAt)

    fun getExamsForStudent(studentId: Long): Flow<List<MockExamEntity>> =
        db.mockExamDao().getExamsForStudent(studentId)

    suspend fun insertMockExam(exam: MockExamEntity): Long = db.mockExamDao().insertExam(exam)

    fun getSwotForStudent(studentId: Long): Flow<SwotEntity?> =
        db.swotDao().getSwotForStudent(studentId)

    suspend fun updateSwot(swot: SwotEntity): Long = db.swotDao().insertOrUpdateSwot(swot)

    fun getMeetingsForStudent(studentId: Long): Flow<List<MeetingEntity>> =
        db.meetingDao().getMeetingsForStudent(studentId)

    suspend fun insertMeeting(meeting: MeetingEntity): Long = db.meetingDao().insertMeeting(meeting)

    fun getChatMessages(channelId: String): Flow<List<ChatMessageEntity>> =
        db.chatMessageDao().getMessagesForChannel(channelId)

    suspend fun sendChatMessage(message: ChatMessageEntity): Long =
        db.chatMessageDao().insertMessage(message)

    fun getScheduleForStudent(studentId: Long): Flow<List<ScheduleItemEntity>> =
        db.scheduleDao().getScheduleForStudent(studentId)

    fun getScheduleForStudentAndDay(studentId: Long, dayOfWeek: String): Flow<List<ScheduleItemEntity>> =
        db.scheduleDao().getScheduleForStudentAndDay(studentId, dayOfWeek)

    suspend fun insertScheduleItem(item: ScheduleItemEntity): Long =
        db.scheduleDao().insertSchedule(item)

    suspend fun updateSchedule(item: ScheduleItemEntity) =
        db.scheduleDao().updateSchedule(item)

    suspend fun updateAllSchedules(items: List<ScheduleItemEntity>) =
        db.scheduleDao().updateAllSchedules(items)

    suspend fun updateScheduleStatus(id: Long, isCompleted: Boolean) =
        db.scheduleDao().updateScheduleStatus(id, isCompleted)

    suspend fun updateScheduleReminder(id: Long, hasReminder: Boolean, reminderMinutes: Int) =
        db.scheduleDao().updateScheduleReminder(id, hasReminder, reminderMinutes)

    suspend fun deleteSchedule(item: ScheduleItemEntity) =
        db.scheduleDao().deleteSchedule(item)

    suspend fun deleteScheduleById(id: Long) =
        db.scheduleDao().deleteScheduleById(id)
}
