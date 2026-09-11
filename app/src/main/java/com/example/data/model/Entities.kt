package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole(val titleTr: String, val description: String) {
    STUDENT("Öğrenci", "Hedefler, görevler, ders takibi ve AI koç"),
    COACH("Öğretmen / Koç", "Öğrenci koçluğu, SWOT analizi, görüşmeler ve görev atama"),
    PARENT("Veli", "Canlı takip, devam durumu, deneme sonuçları ve koç notları"),
    ADMIN("Okul Yönetimi", "Kurumsal istatistikler, şube analizleri ve lisanslama")
}

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val gradeLevel: String, // Örn: "12. Sınıf Sayısal"
    val targetSchool: String, // Örn: "Hacettepe Tıp Fakültesi"
    val avatarColorHex: String = "#1E3A8A",
    val currentPoints: Int = 1450,
    val level: Int = 4,
    val streakDays: Int = 7,
    val phone: String = "+90 555 123 4567",
    val parentName: String = "Mehmet Yılmaz"
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val title: String,
    val type: String, // "DAILY", "WEEKLY", "MONTHLY"
    val currentAmount: Int,
    val targetAmount: Int,
    val unit: String, // "Soru", "Saat", "Konu", "Deneme"
    val isCompleted: Boolean = false,
    val dateCreated: String = "Bugün"
)

@Entity(tableName = "subject_progress")
data class SubjectProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val subjectName: String,
    val completedTopics: Int,
    val totalTopics: Int,
    val masteryPercentage: Int, // 0 - 100
    val missingTopics: String // Virgülle ayrılmış
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val title: String,
    val description: String,
    val assignedBy: String = "Ahmet Hoca (Koç)",
    val dueDate: String,
    val isCompleted: Boolean = false,
    val completedAt: String? = null
)

@Entity(tableName = "mock_exams")
data class MockExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val examName: String,
    val examDate: String,
    val examType: String = "TYT", // "TYT" veya "AYT"
    val turkceCorrect: Int,
    val turkceWrong: Int,
    val turkceNet: Float,
    val matCorrect: Int,
    val matWrong: Int,
    val matNet: Float,
    val fenCorrect: Int,
    val fenWrong: Int,
    val fenNet: Float,
    val sosyalCorrect: Int,
    val sosyalWrong: Int,
    val sosyalNet: Float,
    val totalNet: Float,
    val score: Float
)

@Entity(tableName = "swot_analyses")
data class SwotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val strengths: String, // Güçlü yönler
    val weaknesses: String, // Zayıf yönler
    val opportunities: String, // Fırsatlar / Gelişim alanları
    val risks: String, // Riskler
    val lastUpdated: String
)

@Entity(tableName = "coaching_meetings")
data class MeetingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val coachName: String,
    val meetingDate: String,
    val notes: String,
    val decisions: String,
    val nextGoals: String
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channelId: String, // "student_coach_1", "parent_coach_1", "group_12a"
    val senderRole: String,
    val senderName: String,
    val message: String,
    val timestamp: String,
    val isAi: Boolean = false
)

@Entity(tableName = "schedule_items")
data class ScheduleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val dayOfWeek: String, // "Pazartesi", "Salı", vb.
    val timeSlot: String, // "08:00 - 09:30"
    val subjectName: String,
    val topicDescription: String,
    val isCompleted: Boolean = false,
    val orderIndex: Int = 0,
    val hasReminder: Boolean = true,
    val reminderMinutesBefore: Int = 15,
    val sessionType: String = "Konu Çalışması" // "Konu Çalışması", "Soru Çözümü", "Deneme Sınavı", "Tekrar", "Pomodoro / Mola"
)

data class BadgeItem(
    val title: String,
    val description: String,
    val iconEmoji: String,
    val isUnlocked: Boolean,
    val unlockCriteria: String
)
