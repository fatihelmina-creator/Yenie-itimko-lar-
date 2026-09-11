package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.StatCard
import com.example.ui.components.StudySchedule
import com.example.ui.theme.*

@Composable
fun StudentDashboardScreen(
    student: StudentEntity?,
    goals: List<GoalEntity>,
    subjectProgress: List<SubjectProgressEntity>,
    tasks: List<TaskEntity>,
    schedule: List<ScheduleItemEntity>,
    onToggleGoal: (GoalEntity) -> Unit,
    onUpdateGoalProgress: (GoalEntity, Int) -> Unit,
    onAddGoal: (String, String, Int, String) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onToggleSchedule: (ScheduleItemEntity) -> Unit,
    onReorderSchedule: (String, List<ScheduleItemEntity>) -> Unit = { _, _ -> },
    onAddScheduleItem: (String, String, String, String, String, Boolean, Int) -> Unit = { _, _, _, _, _, _, _ -> },
    onUpdateScheduleItem: (ScheduleItemEntity) -> Unit = {},
    onDeleteScheduleItem: (ScheduleItemEntity) -> Unit = {},
    onToggleReminder: (ScheduleItemEntity) -> Unit = {},
    onUpdateReminder: (ScheduleItemEntity, Boolean, Int) -> Unit = { _, _, _ -> },
    onTriggerTestPush: (ScheduleItemEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (student == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var selectedGoalFilter by remember { mutableStateOf("DAILY") }
    var showAddGoalDialog by remember { mutableStateOf(false) }

    val filteredGoals = goals.filter { it.type == selectedGoalFilter }
    val totalGoalsCount = filteredGoals.size
    val completedGoalsCount = filteredGoals.count { it.isCompleted }
    val goalCompletionPercentage = if (totalGoalsCount > 0) {
        (completedGoalsCount * 100) / totalGoalsCount
    } else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero / Gamification Header Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(NavyPrimary, IndigoSecondary, Color(0xFF312E81))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = student.gradeLevel,
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Merhaba, ${student.fullName}!",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "Hedef: ${student.targetSchool}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFFE0E7FF)
                                    )
                                )
                            }

                            // Level Badge
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(AmberAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "LVL",
                                        color = Color(0xFF78350F),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    )
                                    Text(
                                        text = "${student.level}",
                                        color = Color(0xFF78350F),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress to next level
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Gelişim Seviyesi: Bilge Adayı",
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${student.currentPoints} / 2000 EXP",
                                color = AmberAccent,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { student.currentPoints / 2000f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = AmberAccent,
                            trackColor = Color.White.copy(alpha = 0.2f),
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Gamification Badges Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BadgePill("🥇 Hedef Avcısı", true)
                            BadgePill("🔥 ${student.streakDays} Gün Seri", true)
                            BadgePill("📐 Mat Ustası", true)
                            BadgePill("🏆 1000 Soru", true)
                        }
                    }
                }
            }
        }

        // Daily Motivation Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "💡", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Günün Koçluk İlhamı",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        )
                        Text(
                            text = "“Zirveye çıkanlar, her gün küçük adımları hiç aksatmadan atanlardır. Bugün çözdüğün her soru sınavda seni öne geçirecek!”",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF78350F)
                            )
                        )
                    }
                }
            }
        }

        // Quick Stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Bugün Çözülen",
                    value = "230",
                    subtitle = "Hedef: 250",
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess) },
                    accentColor = EmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Deneme Neti",
                    value = "96.0",
                    subtitle = "Kurum 3.sü",
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = null, tint = NavyPrimary) },
                    accentColor = NavyPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Ödev Görevleri",
                    value = "${tasks.count { !it.isCompleted }}",
                    subtitle = "Bekleyen",
                    icon = { Icon(Icons.Default.Assignment, contentDescription = null, tint = IndigoSecondary) },
                    accentColor = IndigoSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // HEDEF TAKİBİ BÖLÜMÜ
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🎯 Hedef Takip Sistemi",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Tamamlanma Oranı: %$goalCompletionPercentage ($completedGoalsCount/$totalGoalsCount)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { showAddGoalDialog = true },
                            modifier = Modifier.testTag("add_goal_button")
                        ) {
                            Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Yeni Hedef Ekle", tint = NavyPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Segmented Filter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("DAILY" to "Günlük", "WEEKLY" to "Haftalık", "MONTHLY" to "Aylık").forEach { (type, label) ->
                            val isSelected = selectedGoalFilter == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedGoalFilter = type },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (filteredGoals.isEmpty()) {
                        Text(
                            text = "Bu zaman diliminde henüz hedef eklenmemiş. Yukarıdaki + butonuna basarak yeni hedef ekleyebilirsiniz.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        filteredGoals.forEach { goal ->
                            GoalRowItem(
                                goal = goal,
                                onToggle = { onToggleGoal(goal) },
                                onIncrement = { onUpdateGoalProgress(goal, 10) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // DERS & KONU TAKİBİ BÖLÜMÜ
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📚 Ders Takibi & Kazanım Analizi",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Konu bazlı ilerleme ve tespit edilen eksikler",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    subjectProgress.forEach { progress ->
                        SubjectProgressRow(progress = progress)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        // GÖREV SİSTEMİ BÖLÜMÜ (ÖĞRETMENİN VERDİĞİ GÖREVLER)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "📝 Koçun Verdiği Görevler",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Son teslim tarihine göre sıralanmış ödevler",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (tasks.isEmpty()) {
                        Text(
                            text = "Şu anda atanmış aktif görev bulunmuyor.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        tasks.forEach { task ->
                            TaskRowItem(task = task, onToggle = { onToggleTask(task) })
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // DERS PROGRAMI (HAFTALIK / GÜNLÜK AKIŞ VE SÜRÜKLE-BIRAK SEANS DÜZENLEYİCİ)
        item {
            StudySchedule(
                student = student,
                scheduleItems = schedule,
                onToggleCompleted = onToggleSchedule,
                onReorderSchedule = onReorderSchedule,
                onAddScheduleItem = onAddScheduleItem,
                onUpdateScheduleItem = onUpdateScheduleItem,
                onDeleteScheduleItem = onDeleteScheduleItem,
                onToggleReminder = onToggleReminder,
                onUpdateReminder = onUpdateReminder,
                onTriggerTestPush = onTriggerTestPush
            )
        }
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { title, type, target, unit ->
                onAddGoal(title, type, target, unit)
                showAddGoalDialog = false
            }
        )
    }
}

@Composable
fun BadgePill(text: String, isUnlocked: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isUnlocked) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f)
    ) {
        Text(
            text = text,
            color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.4f),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun GoalRowItem(
    goal: GoalEntity,
    onToggle: () -> Unit,
    onIncrement: () -> Unit
) {
    val progressRatio = if (goal.targetAmount > 0) {
        (goal.currentAmount.toFloat() / goal.targetAmount.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (goal.isCompleted) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = goal.isCompleted,
                onCheckedChange = { onToggle() },
                modifier = Modifier.testTag("goal_checkbox_${goal.id}")
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (goal.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (goal.isCompleted) SlateTextSecondary else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (goal.isCompleted) EmeraldSuccess else NavyPrimary,
                        trackColor = Color.LightGray.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${goal.currentAmount} / ${goal.targetAmount} ${goal.unit}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (!goal.isCompleted) {
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = onIncrement,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("+10", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun SubjectProgressRow(progress: SubjectProgressEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = progress.subjectName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "%${progress.masteryPercentage} Kazanım",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (progress.masteryPercentage >= 75) EmeraldSuccess else AmberAccent
                )
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress.masteryPercentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (progress.masteryPercentage >= 75) EmeraldSuccess else AmberAccent,
            trackColor = Color.LightGray.copy(alpha = 0.3f)
        )
        if (progress.missingTopics.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Eksik Konular: ",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = RoseRisk
                )
                Text(
                    text = progress.missingTopics,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TaskRowItem(task: TaskEntity, onToggle: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (task.isCompleted) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                modifier = Modifier.testTag("task_checkbox_${task.id}")
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (task.isCompleted) SlateTextSecondary else MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Koç: ${task.assignedBy}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (task.isCompleted) EmeraldSuccess.copy(alpha = 0.15f) else RoseRisk.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (task.isCompleted) "Tamamlandı" else "Son Tarih: ${task.dueDate}",
                            color = if (task.isCompleted) EmeraldSuccess else RoseRisk,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleRowItem(item: ScheduleItemEntity, onToggle: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (item.isCompleted) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = NavyPrimary.copy(alpha = 0.15f)
            ) {
                Text(
                    text = item.timeSlot,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = NavyPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.subjectName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = item.topicDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onToggle() },
                modifier = Modifier.testTag("schedule_checkbox_${item.id}")
            )
        }
    }
}

@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, type: String, target: Int, unit: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("DAILY") }
    var targetStr by remember { mutableStateOf("100") }
    var unitStr by remember { mutableStateOf("Soru") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Yeni Hedef Belirle") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Hedef Başlığı (Örn: Paragraf 40 Soru)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Hedef Periyodu:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("DAILY" to "Günlük", "WEEKLY" to "Haftalık", "MONTHLY" to "Aylık").forEach { (type, label) ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = targetStr,
                        onValueChange = { targetStr = it },
                        label = { Text("Hedef Miktar") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unitStr,
                        onValueChange = { unitStr = it },
                        label = { Text("Birim (Soru, Saat)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val targetInt = targetStr.toIntOrNull() ?: 100
                    if (title.isNotBlank()) {
                        onConfirm(title, selectedType, targetInt, unitStr)
                    }
                }
            ) {
                Text("Hedefi Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç")
            }
        }
    )
}

@Composable
fun RegisterStudentDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, grade: String, target: String, parentName: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("12. Sınıf Sayısal") }
    var target by remember { mutableStateOf("Tıp / Mühendislik") }
    var parentName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Yeni Öğrenci Kaydı Aç") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Öğrenci Adı Soyadı") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = grade,
                    onValueChange = { grade = it },
                    label = { Text("Sınıf ve Alan (Örn: 12. Sınıf Sayısal)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Hedef Üniversite / Bölüm") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = parentName,
                    onValueChange = { parentName = it },
                    label = { Text("Veli Adı Soyadı") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, grade, target, if (parentName.isBlank()) "$name Velisi" else parentName)
                    }
                }
            ) {
                Text("Öğrenciyi Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
