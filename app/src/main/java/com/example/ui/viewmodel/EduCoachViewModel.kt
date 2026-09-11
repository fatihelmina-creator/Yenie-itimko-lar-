package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthManager
import com.example.auth.AuthResult
import com.example.auth.EduCoachUser
import com.example.data.ai.GeminiAiHelper
import com.example.data.local.DatabaseSeeder
import com.example.data.local.EduCoachDatabase
import com.example.data.model.*
import com.example.data.repository.EduCoachRepository
import com.example.notification.SessionReminderScheduler
import android.content.Context
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EduCoachViewModel(application: Application) : AndroidViewModel(application) {
    private val database = EduCoachDatabase.getDatabase(application)
    private val repository = EduCoachRepository(database)
    private val authManager = AuthManager(application)

    val currentUser: StateFlow<EduCoachUser?> = authManager.currentUser
    val authLoading: StateFlow<Boolean> = authManager.isLoading
    val authError: StateFlow<String?> = authManager.authError

    private val _currentRole = MutableStateFlow(authManager.currentUser.value?.role ?: UserRole.STUDENT)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    val allStudents: StateFlow<List<StudentEntity>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedStudent = MutableStateFlow<StudentEntity?>(null)
    val selectedStudent: StateFlow<StudentEntity?> = _selectedStudent.asStateFlow()

    private val _selectedChannelId = MutableStateFlow("student_coach_1")
    val selectedChannelId: StateFlow<String> = _selectedChannelId.asStateFlow()

    private val _aiStudentAdvice = MutableStateFlow<String?>(null)
    val aiStudentAdvice: StateFlow<String?> = _aiStudentAdvice.asStateFlow()

    private val _aiCoachAdvice = MutableStateFlow<String?>(null)
    val aiCoachAdvice: StateFlow<String?> = _aiCoachAdvice.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    val goals: StateFlow<List<GoalEntity>> = _selectedStudent
        .filterNotNull()
        .flatMapLatest { student -> repository.getGoalsForStudent(student.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subjectProgress: StateFlow<List<SubjectProgressEntity>> = _selectedStudent
        .filterNotNull()
        .flatMapLatest { student -> repository.getProgressForStudent(student.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> = _selectedStudent
        .filterNotNull()
        .flatMapLatest { student -> repository.getTasksForStudent(student.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mockExams: StateFlow<List<MockExamEntity>> = _selectedStudent
        .filterNotNull()
        .flatMapLatest { student -> repository.getExamsForStudent(student.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val swot: StateFlow<SwotEntity?> = _selectedStudent
        .filterNotNull()
        .flatMapLatest { student -> repository.getSwotForStudent(student.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val meetings: StateFlow<List<MeetingEntity>> = _selectedStudent
        .filterNotNull()
        .flatMapLatest { student -> repository.getMeetingsForStudent(student.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val schedule: StateFlow<List<ScheduleItemEntity>> = _selectedStudent
        .filterNotNull()
        .flatMapLatest { student -> repository.getScheduleForStudent(student.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = _selectedChannelId
        .flatMapLatest { channel -> repository.getChatMessages(channel) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            DatabaseSeeder.seedDatabaseIfEmpty(database)
            allStudents.collectLatest { list ->
                if (_selectedStudent.value == null && list.isNotEmpty()) {
                    _selectedStudent.value = list.first()
                }
            }
        }
    }

    fun setRole(role: UserRole) {
        _currentRole.value = role
        // adjust default chat channel based on role
        when (role) {
            UserRole.STUDENT -> _selectedChannelId.value = "student_coach_1"
            UserRole.COACH -> _selectedChannelId.value = "student_coach_1"
            UserRole.PARENT -> _selectedChannelId.value = "parent_coach_1"
            UserRole.ADMIN -> _selectedChannelId.value = "group_12a"
        }
    }

    fun selectStudent(student: StudentEntity) {
        _selectedStudent.value = student
        _aiStudentAdvice.value = null
        _aiCoachAdvice.value = null
    }

    fun setChatChannel(channelId: String) {
        _selectedChannelId.value = channelId
    }

    fun addGoal(title: String, type: String, targetAmount: Int, unit: String) {
        val student = _selectedStudent.value ?: return
        viewModelScope.launch {
            repository.insertGoal(
                GoalEntity(
                    studentId = student.id,
                    title = title,
                    type = type,
                    currentAmount = 0,
                    targetAmount = targetAmount,
                    unit = unit,
                    isCompleted = false
                )
            )
        }
    }

    fun updateGoalProgress(goal: GoalEntity, delta: Int) {
        viewModelScope.launch {
            val newAmount = (goal.currentAmount + delta).coerceIn(0, goal.targetAmount)
            val isDone = newAmount >= goal.targetAmount
            repository.updateGoal(goal.copy(currentAmount = newAmount, isCompleted = isDone))
        }
    }

    fun toggleGoal(goal: GoalEntity) {
        viewModelScope.launch {
            val newCompleted = !goal.isCompleted
            val newAmount = if (newCompleted) goal.targetAmount else 0
            repository.updateGoal(goal.copy(isCompleted = newCompleted, currentAmount = newAmount))
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            repository.deleteGoal(goalId)
        }
    }

    fun addTask(title: String, description: String, dueDate: String) {
        val student = _selectedStudent.value ?: return
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    studentId = student.id,
                    title = title,
                    description = description,
                    assignedBy = "Ahmet Hoca (Koç)",
                    dueDate = dueDate,
                    isCompleted = false
                )
            )
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            val newCompleted = !task.isCompleted
            val completedTime = if (newCompleted) {
                SimpleDateFormat("dd MMM HH:mm", Locale("tr")).format(Date())
            } else null
            repository.setTaskCompleted(task.id, newCompleted, completedTime)
        }
    }

    fun addMockExam(
        examName: String,
        examType: String,
        tD: Int, tY: Int,
        mD: Int, mY: Int,
        fD: Int, fY: Int,
        sD: Int, sY: Int
    ) {
        val student = _selectedStudent.value ?: return
        viewModelScope.launch {
            val tNet = (tD - (tY * 0.25f)).coerceAtLeast(0f)
            val mNet = (mD - (mY * 0.25f)).coerceAtLeast(0f)
            val fNet = (fD - (fY * 0.25f)).coerceAtLeast(0f)
            val sNet = (sD - (sY * 0.25f)).coerceAtLeast(0f)
            val totalNet = tNet + mNet + fNet + sNet
            // Standard TYT/AYT approx score formula
            val baseScore = if (examType == "TYT") 100f + (tNet * 3.3f) + (mNet * 3.3f) + (fNet * 3.4f) + (sNet * 3.4f)
            else 100f + (mNet * 3.0f) + (fNet * 3.0f)

            val currentDate = SimpleDateFormat("dd MMM yyyy", Locale("tr")).format(Date())
            repository.insertMockExam(
                MockExamEntity(
                    studentId = student.id,
                    examName = examName,
                    examDate = currentDate,
                    examType = examType,
                    turkceCorrect = tD, turkceWrong = tY, turkceNet = tNet,
                    matCorrect = mD, matWrong = mY, matNet = mNet,
                    fenCorrect = fD, fenWrong = fY, fenNet = fNet,
                    sosyalCorrect = sD, sosyalWrong = sY, sosyalNet = sNet,
                    totalNet = totalNet,
                    score = baseScore
                )
            )
        }
    }

    fun updateSwot(strengths: String, weaknesses: String, opportunities: String, risks: String) {
        val student = _selectedStudent.value ?: return
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale("tr")).format(Date())
            repository.updateSwot(
                SwotEntity(
                    studentId = student.id,
                    strengths = strengths,
                    weaknesses = weaknesses,
                    opportunities = opportunities,
                    risks = risks,
                    lastUpdated = dateStr
                )
            )
        }
    }

    fun addMeeting(notes: String, decisions: String, nextGoals: String) {
        val student = _selectedStudent.value ?: return
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("tr")).format(Date())
            repository.insertMeeting(
                MeetingEntity(
                    studentId = student.id,
                    coachName = "Ahmet Hoca",
                    meetingDate = dateStr,
                    notes = notes,
                    decisions = decisions,
                    nextGoals = nextGoals
                )
            )
        }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val channel = _selectedChannelId.value
        val role = _currentRole.value
        val senderName = when (role) {
            UserRole.STUDENT -> _selectedStudent.value?.fullName ?: "Öğrenci"
            UserRole.COACH -> "Ahmet Hoca (Koç)"
            UserRole.PARENT -> "${_selectedStudent.value?.parentName ?: "Mehmet Bey"} (Veli)"
            UserRole.ADMIN -> "Okul Yönetimi"
        }
        val timeStr = SimpleDateFormat("HH:mm", Locale("tr")).format(Date())

        viewModelScope.launch {
            repository.sendChatMessage(
                ChatMessageEntity(
                    channelId = channel,
                    senderRole = role.name,
                    senderName = senderName,
                    message = text.trim(),
                    timestamp = timeStr
                )
            )
        }
    }

    fun toggleSchedule(item: ScheduleItemEntity) {
        viewModelScope.launch {
            repository.updateScheduleStatus(item.id, !item.isCompleted)
        }
    }

    fun reorderSchedule(dayOfWeek: String, reorderedList: List<ScheduleItemEntity>) {
        viewModelScope.launch {
            val updated = reorderedList.mapIndexed { index, item ->
                item.copy(orderIndex = index)
            }
            repository.updateAllSchedules(updated)
        }
    }

    fun addScheduleItem(
        dayOfWeek: String,
        timeSlot: String,
        subjectName: String,
        topicDescription: String,
        sessionType: String = "Konu Çalışması",
        hasReminder: Boolean = true,
        reminderMinutes: Int = 15
    ) {
        val student = _selectedStudent.value ?: return
        viewModelScope.launch {
            val currentList = schedule.value.filter { it.dayOfWeek.equals(dayOfWeek, ignoreCase = true) }
            val nextOrder = currentList.size
            val newItem = ScheduleItemEntity(
                studentId = student.id,
                dayOfWeek = dayOfWeek,
                timeSlot = timeSlot,
                subjectName = subjectName,
                topicDescription = topicDescription,
                isCompleted = false,
                orderIndex = nextOrder,
                hasReminder = hasReminder,
                reminderMinutesBefore = reminderMinutes,
                sessionType = sessionType
            )
            val newId = repository.insertScheduleItem(newItem)
            if (hasReminder) {
                SessionReminderScheduler.scheduleReminder(
                    getApplication(),
                    newItem.copy(id = newId)
                )
            }
        }
    }

    fun updateScheduleItem(item: ScheduleItemEntity) {
        viewModelScope.launch {
            repository.updateSchedule(item)
            if (item.hasReminder) {
                SessionReminderScheduler.scheduleReminder(getApplication(), item)
            } else {
                SessionReminderScheduler.cancelReminder(getApplication(), item.id)
            }
        }
    }

    fun deleteScheduleItem(item: ScheduleItemEntity) {
        viewModelScope.launch {
            repository.deleteSchedule(item)
            SessionReminderScheduler.cancelReminder(getApplication(), item.id)
        }
    }

    fun toggleScheduleReminder(item: ScheduleItemEntity) {
        viewModelScope.launch {
            val newHasReminder = !item.hasReminder
            repository.updateScheduleReminder(item.id, newHasReminder, item.reminderMinutesBefore)
            val updated = item.copy(hasReminder = newHasReminder)
            if (newHasReminder) {
                SessionReminderScheduler.scheduleReminder(getApplication(), updated)
            } else {
                SessionReminderScheduler.cancelReminder(getApplication(), item.id)
            }
        }
    }

    fun updateScheduleReminder(item: ScheduleItemEntity, hasReminder: Boolean, reminderMinutes: Int) {
        viewModelScope.launch {
            repository.updateScheduleReminder(item.id, hasReminder, reminderMinutes)
            val updated = item.copy(hasReminder = hasReminder, reminderMinutesBefore = reminderMinutes)
            if (hasReminder) {
                SessionReminderScheduler.scheduleReminder(getApplication(), updated)
            } else {
                SessionReminderScheduler.cancelReminder(getApplication(), item.id)
            }
        }
    }

    fun triggerInstantPush(item: ScheduleItemEntity) {
        SessionReminderScheduler.triggerInstantTestNotification(getApplication(), item)
    }

    fun registerNewStudent(name: String, grade: String, target: String, parentName: String) {
        viewModelScope.launch {
            val newId = repository.insertStudent(
                StudentEntity(
                    fullName = name,
                    gradeLevel = grade,
                    targetSchool = target,
                    parentName = parentName,
                    currentPoints = 500,
                    level = 1,
                    streakDays = 1
                )
            )
            val student = repository.getStudentById(newId)
            if (student != null) {
                _selectedStudent.value = student
            }
        }
    }

    fun askStudentAiCheckIn(studiedToday: Boolean, difficultSubject: String, hours: String, note: String) {
        val student = _selectedStudent.value ?: return
        viewModelScope.launch {
            _isAiLoading.value = true
            val response = GeminiAiHelper.generateCoachingAdviceForStudent(
                studentName = student.fullName,
                studiedToday = studiedToday,
                difficultSubject = difficultSubject,
                hoursStudied = hours,
                studentNote = note
            )
            _aiStudentAdvice.value = response
            _isAiLoading.value = false
        }
    }

    fun requestCoachAiAdvice() {
        val student = _selectedStudent.value ?: return
        val currentExams = mockExams.value
        val examSummary = if (currentExams.isNotEmpty()) {
            val latest = currentExams.first()
            "${latest.examName} (${latest.examDate}) -> T:${latest.turkceNet}N, M:${latest.matNet}N, F:${latest.fenNet}N, S:${latest.sosyalNet}N. Toplam: ${latest.totalNet} Net"
        } else "Henüz deneme kaydı yok"

        val currentSwot = swot.value
        val swotSummary = if (currentSwot != null) {
            "Güçlü: ${currentSwot.strengths} | Zayıf: ${currentSwot.weaknesses} | Risk: ${currentSwot.risks}"
        } else "Genel koçluk takibinde"

        viewModelScope.launch {
            _isAiLoading.value = true
            val response = GeminiAiHelper.generateCoachRecommendations(
                studentName = student.fullName,
                examSummary = examSummary,
                missedGoals = "Haftalık soru çözüm hedefleri son 3 haftadır %25 eksik kalıyor",
                swotHighlights = swotSummary
            )
            _aiCoachAdvice.value = response
            _isAiLoading.value = false
        }
    }

    // --- Authentication & Credential Manager methods ---
    fun signInWithEmail(email: String, pass: String, role: UserRole, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = authManager.signInWithEmail(email, pass, role)
            if (result is AuthResult.Success) {
                _currentRole.value = role
                onSuccess()
            }
        }
    }

    fun signUpWithEmail(name: String, email: String, pass: String, role: UserRole, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = authManager.signUpWithEmail(name, email, pass, role)
            if (result is AuthResult.Success) {
                _currentRole.value = role
                onSuccess()
            }
        }
    }

    fun signInWithGoogle(activityContext: Context, role: UserRole, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = authManager.signInWithGoogleCredentialManager(activityContext, role = role)
            if (result is AuthResult.Success) {
                _currentRole.value = role
                onSuccess()
            }
        }
    }

    fun quickSignIn(role: UserRole, name: String, email: String) {
        authManager.quickSignInAs(role, name, email)
        _currentRole.value = role
    }

    fun signOut() {
        authManager.signOut()
    }
}
