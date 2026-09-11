package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.components.AuthDialog
import com.example.ui.components.EduCoachHeader
import com.example.ui.components.StudentSelectorDialog
import com.example.ui.viewmodel.EduCoachViewModel

@Composable
fun MainScreen(
    viewModel: EduCoachViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val authLoading by viewModel.authLoading.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val selectedStudent by viewModel.selectedStudent.collectAsStateWithLifecycle()
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val subjectProgress by viewModel.subjectProgress.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val mockExams by viewModel.mockExams.collectAsStateWithLifecycle()
    val swot by viewModel.swot.collectAsStateWithLifecycle()
    val meetings by viewModel.meetings.collectAsStateWithLifecycle()
    val schedule by viewModel.schedule.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val selectedChannelId by viewModel.selectedChannelId.collectAsStateWithLifecycle()
    val aiStudentAdvice by viewModel.aiStudentAdvice.collectAsStateWithLifecycle()
    val aiCoachAdvice by viewModel.aiCoachAdvice.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) }
    var showStudentSelectorDialog by remember { mutableStateOf(false) }
    var showRegisterStudentDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            EduCoachHeader(
                currentRole = currentRole,
                selectedStudent = selectedStudent,
                allStudents = allStudents,
                currentUser = currentUser,
                onOpenAuthDialog = { showAuthDialog = true },
                onRoleSelected = { role -> viewModel.setRole(role) },
                onSelectStudent = { student -> viewModel.selectStudent(student) },
                onRegisterNewStudent = { showStudentSelectorDialog = true }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = when (currentRole) {
                                UserRole.STUDENT -> Icons.Default.School
                                UserRole.COACH -> Icons.Default.CoPresent
                                UserRole.PARENT -> Icons.Default.FamilyRestroom
                                UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                            },
                            contentDescription = "Ana Panel"
                        )
                    },
                    label = { Text(currentRole.titleTr) },
                    modifier = Modifier.testTag("nav_tab_panel")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Deneme Analizi") },
                    label = { Text("Deneme") },
                    modifier = Modifier.testTag("nav_tab_exams")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Koç") },
                    label = { Text("AI Koç") },
                    modifier = Modifier.testTag("nav_tab_ai")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Forum, contentDescription = "İletişim") },
                    label = { Text("İletişim") },
                    modifier = Modifier.testTag("nav_tab_chat")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.CorporateFare, contentDescription = "Yönetim") },
                    label = { Text("Yönetim") },
                    modifier = Modifier.testTag("nav_tab_admin")
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    when (currentRole) {
                        UserRole.STUDENT -> {
                            StudentDashboardScreen(
                                student = selectedStudent,
                                goals = goals,
                                subjectProgress = subjectProgress,
                                tasks = tasks,
                                schedule = schedule,
                                onToggleGoal = { goal -> viewModel.toggleGoal(goal) },
                                onUpdateGoalProgress = { goal, delta -> viewModel.updateGoalProgress(goal, delta) },
                                onAddGoal = { title, type, target, unit -> viewModel.addGoal(title, type, target, unit) },
                                onToggleTask = { task -> viewModel.toggleTask(task) },
                                onToggleSchedule = { item -> viewModel.toggleSchedule(item) },
                                onReorderSchedule = { day, list -> viewModel.reorderSchedule(day, list) },
                                onAddScheduleItem = { day, time, subj, topic, type, hasRem, remMins ->
                                    viewModel.addScheduleItem(day, time, subj, topic, type, hasRem, remMins)
                                },
                                onUpdateScheduleItem = { item -> viewModel.updateScheduleItem(item) },
                                onDeleteScheduleItem = { item -> viewModel.deleteScheduleItem(item) },
                                onToggleReminder = { item -> viewModel.toggleScheduleReminder(item) },
                                onUpdateReminder = { item, hasRem, mins -> viewModel.updateScheduleReminder(item, hasRem, mins) },
                                onTriggerTestPush = { item -> viewModel.triggerInstantPush(item) }
                            )
                        }
                        UserRole.COACH -> {
                            CoachDashboardScreen(
                                student = selectedStudent,
                                swot = swot,
                                meetings = meetings,
                                aiCoachAdvice = aiCoachAdvice,
                                isAiLoading = isAiLoading,
                                onRequestAiAdvice = { viewModel.requestCoachAiAdvice() },
                                onUpdateSwot = { s, w, o, t -> viewModel.updateSwot(s, w, o, t) },
                                onAddMeeting = { notes, decisions, nextGoals -> viewModel.addMeeting(notes, decisions, nextGoals) },
                                onAssignTask = { title, desc, due -> viewModel.addTask(title, desc, due) }
                            )
                        }
                        UserRole.PARENT -> {
                            ParentDashboardScreen(
                                student = selectedStudent,
                                mockExams = mockExams,
                                meetings = meetings,
                                goals = goals,
                                onOpenChatWithCoach = {
                                    viewModel.setChatChannel("parent_coach_1")
                                    selectedTab = 3
                                }
                            )
                        }
                        UserRole.ADMIN -> {
                            AdminScreen(
                                students = allStudents,
                                onSelectStudent = { student -> viewModel.selectStudent(student) }
                            )
                        }
                    }
                }
                1 -> {
                    ExamAnalysisScreen(
                        mockExams = mockExams,
                        onAddExam = { name, type, tD, tY, mD, mY, fD, fY, sD, sY ->
                            viewModel.addMockExam(name, type, tD, tY, mD, mY, fD, fY, sD, sY)
                        }
                    )
                }
                2 -> {
                    AiCoachScreen(
                        student = selectedStudent,
                        aiStudentAdvice = aiStudentAdvice,
                        aiCoachAdvice = aiCoachAdvice,
                        isAiLoading = isAiLoading,
                        onAskStudentCheckIn = { studiedToday, subject, hours, note ->
                            viewModel.askStudentAiCheckIn(studiedToday, subject, hours, note)
                        },
                        onRequestCoachAdvice = {
                            viewModel.requestCoachAiAdvice()
                        }
                    )
                }
                3 -> {
                    ChatAndMeetingScreen(
                        student = selectedStudent,
                        messages = chatMessages,
                        currentChannelId = selectedChannelId,
                        onChannelSelected = { ch -> viewModel.setChatChannel(ch) },
                        onSendMessage = { text -> viewModel.sendChatMessage(text) }
                    )
                }
                4 -> {
                    AdminScreen(
                        students = allStudents,
                        onSelectStudent = { student -> viewModel.selectStudent(student) }
                    )
                }
            }
        }
    }

    if (showStudentSelectorDialog) {
        StudentSelectorDialog(
            allStudents = allStudents,
            selectedStudent = selectedStudent,
            onSelectStudent = { student -> viewModel.selectStudent(student) },
            onAddNewStudent = { showRegisterStudentDialog = true },
            onDismiss = { showStudentSelectorDialog = false }
        )
    }

    if (showRegisterStudentDialog) {
        RegisterStudentDialog(
            onDismiss = { showRegisterStudentDialog = false },
            onConfirm = { name, grade, target, parentName ->
                viewModel.registerNewStudent(name, grade, target, parentName)
                showRegisterStudentDialog = false
            }
        )
    }

    if (showAuthDialog) {
        AuthDialog(
            currentUser = currentUser,
            isLoading = authLoading,
            errorMessage = authError,
            onDismiss = { showAuthDialog = false },
            onSignInEmail = { email, pass, role ->
                viewModel.signInWithEmail(email, pass, role) {
                    showAuthDialog = false
                }
            },
            onSignUpEmail = { name, email, pass, role ->
                viewModel.signUpWithEmail(name, email, pass, role) {
                    showAuthDialog = false
                }
            },
            onSignInGoogle = { activityContext, role ->
                viewModel.signInWithGoogle(activityContext, role) {
                    showAuthDialog = false
                }
            },
            onQuickSignIn = { role, name, email ->
                viewModel.quickSignIn(role, name, email)
                showAuthDialog = false
            },
            onSignOut = {
                viewModel.signOut()
            }
        )
    }
}
