package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.data.model.ScheduleItemEntity
import com.example.data.model.StudentEntity
import com.example.notification.SessionNotificationHelper
import com.example.notification.SessionReminderScheduler
import com.example.ui.theme.*
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val DAYS_OF_WEEK = listOf("Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar")

val AVAILABLE_SUBJECTS = listOf(
    "Matematik" to Color(0xFF1E3A8A),
    "Geometri" to Color(0xFFBE185D),
    "Fizik" to Color(0xFF6D28D9),
    "Kimya" to Color(0xFF0284C7),
    "Biyoloji" to Color(0xFF059669),
    "Türkçe" to Color(0xFFD97706),
    "Tarih" to Color(0xFF78350F),
    "Coğrafya" to Color(0xFF047857),
    "Pomodoro / Mola" to Color(0xFF10B981)
)

val SESSION_TYPES = listOf(
    "Konu Çalışması",
    "Soru Çözümü",
    "Deneme Sınavı",
    "Hızlı Tekrar",
    "Pomodoro Molası"
)

fun getSubjectColor(subjectName: String): Color {
    return AVAILABLE_SUBJECTS.firstOrNull { it.first.equals(subjectName, ignoreCase = true) }?.second
        ?: NavyPrimary
}

@Composable
fun StudySchedule(
    student: StudentEntity?,
    scheduleItems: List<ScheduleItemEntity>,
    onToggleCompleted: (ScheduleItemEntity) -> Unit,
    onReorderSchedule: (dayOfWeek: String, List<ScheduleItemEntity>) -> Unit,
    onAddScheduleItem: (dayOfWeek: String, timeSlot: String, subject: String, topic: String, sessionType: String, hasReminder: Boolean, reminderMinutes: Int) -> Unit,
    onUpdateScheduleItem: (ScheduleItemEntity) -> Unit,
    onDeleteScheduleItem: (ScheduleItemEntity) -> Unit,
    onToggleReminder: (ScheduleItemEntity) -> Unit,
    onUpdateReminder: (ScheduleItemEntity, Boolean, Int) -> Unit,
    onTriggerTestPush: (ScheduleItemEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasNotificationPermission by remember {
        mutableStateOf(SessionNotificationHelper.hasNotificationPermission(context))
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    var selectedDay by remember { mutableStateOf("Pazartesi") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ScheduleItemEntity?>(null) }
    var reminderItemToEdit by remember { mutableStateOf<ScheduleItemEntity?>(null) }

    // In-app alert notification simulator
    var activeTestNotification by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Filter items for selected day, sorted by orderIndex
    val currentDayItems = remember(scheduleItems, selectedDay) {
        scheduleItems
            .filter { it.dayOfWeek.equals(selectedDay, ignoreCase = true) }
            .sortedBy { it.orderIndex }
    }

    // Local mutable list for live drag reordering
    var localDayItems by remember(currentDayItems) {
        mutableStateOf(currentDayItems)
    }

    // Drag-and-drop state
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }

    // Calculate metrics
    val totalSessions = localDayItems.size
    val completedSessions = localDayItems.count { it.isCompleted }
    val activeRemindersCount = localDayItems.count { it.hasReminder }
    val completionPercent = if (totalSessions > 0) (completedSessions * 100) / totalSessions else 0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("study_schedule_component")
    ) {
        // Notification Permission Request Card (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = Color(0xFFB45309),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🔔 Bildirim İzni Gerekli",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF92400E)
                        )
                        Text(
                            text = "Seans saatlerinde cihazına sesli ve titreşimli push bildirim gelebilmesi için izin vermelisin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF78350F)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("İzin Ver", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        // Active notification banner if tested
        AnimatedVisibility(
            visible = activeTestNotification != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .shadow(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AmberAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🔔 EduCoach Canlı Seans Hatırlatıcısı",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = activeTestNotification ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    IconButton(onClick = { activeTestNotification = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.White)
                    }
                }
            }
        }

        // Header Card with Day Selector and Quick Stats
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Title and Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = NavyPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ders Çalışma Programı",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            text = "Sürükle-bırak ile seans sırasını düzenle & alarmları yönet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = { showAddDialog = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = NavyPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("add_schedule_session_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Yeni Seans", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Days of week selector tabs
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(DAYS_OF_WEEK.size) { index ->
                        val day = DAYS_OF_WEEK[index]
                        val isSelected = day == selectedDay
                        val dayCount = scheduleItems.count { it.dayOfWeek.equals(day, ignoreCase = true) }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) NavyPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { selectedDay = day }
                                .testTag("day_tab_$day")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = day,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                                if (dayCount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) Color.White.copy(alpha = 0.25f) else NavyPrimary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "$dayCount",
                                            color = if (isSelected) Color.White else NavyPrimary,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Daily Metrics Summary Bar
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$totalSessions Seans Planı",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$completedSessions/$totalSessions Tamamlandı (%$completionPercent)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$activeRemindersCount Alarm",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Local Push Notifications Scheduling Status Row
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (hasNotificationPermission) Color(0xFFECFDF5) else Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (hasNotificationPermission) Icons.Default.CheckCircle else Icons.Default.AccessAlarms,
                                contentDescription = null,
                                tint = if (hasNotificationPermission) EmeraldSuccess else NavyPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (hasNotificationPermission) "AlarmManager Push Zamanlayıcı Aktif" else "Yerel Seans Hatırlatıcısı Hazır",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (hasNotificationPermission) Color(0xFF065F46) else NavyPrimary
                            )
                        }
                        TextButton(
                            onClick = {
                                SessionNotificationHelper.showInstantNotification(
                                    context = context,
                                    title = "📚 EduCoach Seans Alarmı",
                                    message = "Tebrikler! Local push notification başarıyla tetiklendi. Seans vakitlerinde otomatik çalacak."
                                )
                                activeTestNotification = "Yerel push bildirim başarıyla cihazına iletildi! Seans saatlerinde otomatik tetiklenecek."
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Push Gönder", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Preset Templates Shelf (Drag/Tap to add quickly)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⚡ Hızlı Seans Şablonları (1-Tıkla Ekle)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Güne Ekle",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = IndigoSecondary)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        PresetChip(
                            title = "20 Paragraf Rutini",
                            time = "08:00 - 08:45",
                            subject = "Türkçe",
                            onClick = {
                                onAddScheduleItem(selectedDay, "08:00 - 08:45", "Türkçe", "Günlük 20 Paragraf Hız Çözümü", "Soru Çözümü", true, 10)
                            }
                        )
                    }
                    item {
                        PresetChip(
                            title = "TYT Mat Soru Çözümü",
                            time = "09:00 - 10:30",
                            subject = "Matematik",
                            onClick = {
                                onAddScheduleItem(selectedDay, "09:00 - 10:30", "Matematik", "Fonksiyonlar & Polinom Soru Kampı", "Soru Çözümü", true, 15)
                            }
                        )
                    }
                    item {
                        PresetChip(
                            title = "Fizik Formül Tekrarı",
                            time = "14:00 - 15:00",
                            subject = "Fizik",
                            onClick = {
                                onAddScheduleItem(selectedDay, "14:00 - 15:00", "Fizik", "Mekanik Formül Kartları & Çözümlü Örnekler", "Hızlı Tekrar", true, 15)
                            }
                        )
                    }
                    item {
                        PresetChip(
                            title = "Pomodoro Mola (15 dk)",
                            time = "15:00 - 15:15",
                            subject = "Pomodoro / Mola",
                            onClick = {
                                onAddScheduleItem(selectedDay, "15:00 - 15:15", "Pomodoro / Mola", "Göz dinlendirme, su içme & nefes egzersizi", "Pomodoro Molası", false, 0)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Drag and Drop Instructions & Status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DragIndicator,
                    contentDescription = null,
                    tint = IndigoSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Seansı tut ve yukarı/aşağı sürükle",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (draggedIndex != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AmberAccent.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "📍 Sıralanıyor...",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFB45309)),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // List of sessions with interactive drag-and-drop & fallback reorder
        if (localDayItems.isEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "$selectedDay Günü İçin Plan Yok",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Yukarıdaki şablonlardan seçebilir veya 'Yeni Seans' butonuna basarak ders programı oluşturabilirsin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("$selectedDay Gününe Seans Ekle")
                    }
                }
            }
        } else {
            val density = LocalDensity.current
            val itemHeightThresholdPx = with(density) { 90.dp.toPx() }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                localDayItems.forEachIndexed { index, item ->
                    val isBeingDragged = draggedIndex == index

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(if (isBeingDragged) 10f else 1f)
                    ) {
                        DraggableScheduleCard(
                            item = item,
                            index = index,
                            totalCount = localDayItems.size,
                            isBeingDragged = isBeingDragged,
                            onToggleCompleted = { onToggleCompleted(item) },
                            onToggleReminder = { onToggleReminder(item) },
                            onOpenReminderSettings = { reminderItemToEdit = item },
                            onEdit = { editingItem = item },
                            onDelete = { onDeleteScheduleItem(item) },
                            onMoveUp = {
                                if (index > 0) {
                                    val newList = localDayItems.toMutableList()
                                    val temp = newList[index]
                                    newList[index] = newList[index - 1]
                                    newList[index - 1] = temp
                                    localDayItems = newList
                                    onReorderSchedule(selectedDay, newList)
                                }
                            },
                            onMoveDown = {
                                if (index < localDayItems.size - 1) {
                                    val newList = localDayItems.toMutableList()
                                    val temp = newList[index]
                                    newList[index] = newList[index + 1]
                                    newList[index + 1] = temp
                                    localDayItems = newList
                                    onReorderSchedule(selectedDay, newList)
                                }
                            },
                            onDragStart = {
                                draggedIndex = index
                                dragOffsetPx = 0f
                            },
                            onDrag = { dragAmount ->
                                dragOffsetPx += dragAmount
                                val currentIndex = draggedIndex ?: return@DraggableScheduleCard
                                if (dragOffsetPx > itemHeightThresholdPx && currentIndex < localDayItems.size - 1) {
                                    // Move down
                                    val newList = localDayItems.toMutableList()
                                    val target = currentIndex + 1
                                    val temp = newList[currentIndex]
                                    newList[currentIndex] = newList[target]
                                    newList[target] = temp
                                    localDayItems = newList
                                    draggedIndex = target
                                    dragOffsetPx = 0f
                                } else if (dragOffsetPx < -itemHeightThresholdPx && currentIndex > 0) {
                                    // Move up
                                    val newList = localDayItems.toMutableList()
                                    val target = currentIndex - 1
                                    val temp = newList[currentIndex]
                                    newList[currentIndex] = newList[target]
                                    newList[target] = temp
                                    localDayItems = newList
                                    draggedIndex = target
                                    dragOffsetPx = 0f
                                }
                            },
                            onDragEnd = {
                                draggedIndex = null
                                dragOffsetPx = 0f
                                onReorderSchedule(selectedDay, localDayItems)
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Session Dialog
    if (showAddDialog) {
        AddEditSessionDialog(
            initialDay = selectedDay,
            initialItem = null,
            onDismiss = { showAddDialog = false },
            onSave = { day, time, subject, topic, type, hasRem, remMins ->
                onAddScheduleItem(day, time, subject, topic, type, hasRem, remMins)
                showAddDialog = false
            }
        )
    }

    // Edit Session Dialog
    editingItem?.let { itemToEdit ->
        AddEditSessionDialog(
            initialDay = itemToEdit.dayOfWeek,
            initialItem = itemToEdit,
            onDismiss = { editingItem = null },
            onSave = { day, time, subject, topic, type, hasRem, remMins ->
                onUpdateScheduleItem(
                    itemToEdit.copy(
                        dayOfWeek = day,
                        timeSlot = time,
                        subjectName = subject,
                        topicDescription = topic,
                        sessionType = type,
                        hasReminder = hasRem,
                        reminderMinutesBefore = remMins
                    )
                )
                editingItem = null
            }
        )
    }

    // Reminder Settings Dialog
    reminderItemToEdit?.let { remItem ->
        ReminderSettingsDialog(
            item = remItem,
            studentName = student?.fullName ?: "Öğrenci",
            onDismiss = { reminderItemToEdit = null },
            onSave = { hasReminder, minutesBefore ->
                onUpdateReminder(remItem, hasReminder, minutesBefore)
                reminderItemToEdit = null
            },
            onTestReminder = { testMessage ->
                activeTestNotification = testMessage
                reminderItemToEdit = null
                coroutineScope.launch {
                    delay(5000)
                    if (activeTestNotification == testMessage) {
                        activeTestNotification = null
                    }
                }
            }
        )
    }
}

@Composable
fun DraggableScheduleCard(
    item: ScheduleItemEntity,
    index: Int,
    totalCount: Int,
    isBeingDragged: Boolean,
    onToggleCompleted: () -> Unit,
    onToggleReminder: () -> Unit,
    onOpenReminderSettings: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val subjectColor = getSubjectColor(item.subjectName)
    val cardElevation = if (isBeingDragged) 12.dp else 1.dp
    val scale = if (isBeingDragged) 1.02f else 1.0f

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBeingDragged) {
                Color(0xFFEFF6FF)
            } else if (item.isCompleted) {
                Color(0xFFF0FDF4)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .border(
                width = if (isBeingDragged) 2.dp else 1.dp,
                color = if (isBeingDragged) NavyPrimary else if (item.isCompleted) EmeraldSuccess.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("schedule_card_${item.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Time, Subject Pill, Drag Handle & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Time & Subject Badges
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = subjectColor.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(subjectColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.subjectName,
                                color = subjectColor,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.timeSlot,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Drag Handle and Move Arrows
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Move Up/Down accessibility buttons
                    if (index > 0) {
                        IconButton(
                            onClick = onMoveUp,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Yukarı Taşı",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (index < totalCount - 1) {
                        IconButton(
                            onClick = onMoveDown,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Aşağı Taşı",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Drag Handle with Pointer Input
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isBeingDragged) NavyPrimary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .pointerInput(item.id) {
                                detectDragGestures(
                                    onDragStart = { onDragStart() },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        onDrag(dragAmount.y)
                                    },
                                    onDragEnd = { onDragEnd() },
                                    onDragCancel = { onDragEnd() }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = "Sürükle",
                            tint = if (isBeingDragged) NavyPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Middle Row: Checkbox, Topic Description & Session Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = item.isCompleted,
                    onCheckedChange = { onToggleCompleted() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = EmeraldSuccess,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("schedule_checkbox_${item.id}")
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.topicDescription,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (item.isCompleted) SlateTextSecondary else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = item.sessionType,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (item.isCompleted) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldSuccess.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "✓ Tamamlandı",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                    color = EmeraldSuccess,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Row: Session Reminders Chip & Action Buttons (Edit / Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reminder Chip (Clickable to customize, toggle button attached)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (item.hasReminder) AmberAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clickable { onOpenReminderSettings() }
                        .testTag("reminder_chip_${item.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (item.hasReminder) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = "Hatırlatıcı",
                            tint = if (item.hasReminder) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (item.hasReminder) "${item.reminderMinutesBefore} dk önce alarm" else "Hatırlatıcı kapalı",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (item.hasReminder) FontWeight.Bold else FontWeight.Normal,
                                color = if (item.hasReminder) Color(0xFF92400E) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Ayarla",
                            tint = if (item.hasReminder) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                // Edit and Delete Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Düzenle",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Sil",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PresetChip(
    title: String,
    time: String,
    subject: String,
    onClick: () -> Unit
) {
    val color = getSubjectColor(subject)
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = color)
                Text(text = "$subject • $time", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSessionDialog(
    initialDay: String,
    initialItem: ScheduleItemEntity?,
    onDismiss: () -> Unit,
    onSave: (day: String, time: String, subject: String, topic: String, sessionType: String, hasReminder: Boolean, reminderMinutes: Int) -> Unit
) {
    var dayOfWeek by remember { mutableStateOf(initialItem?.dayOfWeek ?: initialDay) }
    var timeSlot by remember { mutableStateOf(initialItem?.timeSlot ?: "08:00 - 09:30") }
    var subjectName by remember { mutableStateOf(initialItem?.subjectName ?: "Matematik") }
    var topicDescription by remember { mutableStateOf(initialItem?.topicDescription ?: "") }
    var sessionType by remember { mutableStateOf(initialItem?.sessionType ?: "Konu Çalışması") }
    var hasReminder by remember { mutableStateOf(initialItem?.hasReminder ?: true) }
    var reminderMinutes by remember { mutableIntStateOf(initialItem?.reminderMinutesBefore ?: 15) }

    val presetTimeSlots = listOf(
        "08:00 - 09:30",
        "09:45 - 11:15",
        "11:30 - 13:00",
        "14:00 - 15:30",
        "16:00 - 17:30",
        "18:00 - 19:30",
        "20:00 - 21:30"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialItem != null) "Seansı Düzenle" else "Yeni Çalışma Seansı Planla",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Day of week selector
                Text("Gün:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(DAYS_OF_WEEK.size) { idx ->
                        val d = DAYS_OF_WEEK[idx]
                        FilterChip(
                            selected = dayOfWeek == d,
                            onClick = { dayOfWeek = d },
                            label = { Text(d, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                // Subject Picker
                Text("Ders:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(AVAILABLE_SUBJECTS.size) { idx ->
                        val (subj, col) = AVAILABLE_SUBJECTS[idx]
                        FilterChip(
                            selected = subjectName == subj,
                            onClick = { subjectName = subj },
                            label = { Text(subj, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(col)
                                )
                            }
                        )
                    }
                }

                // Time Slot
                OutlinedTextField(
                    value = timeSlot,
                    onValueChange = { timeSlot = it },
                    label = { Text("Saat Aralığı (Örn: 08:00 - 09:30)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick time presets
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(presetTimeSlots.size) { idx ->
                        val slot = presetTimeSlots[idx]
                        SuggestionChip(
                            onClick = { timeSlot = slot },
                            label = { Text(slot, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)) }
                        )
                    }
                }

                // Topic / Task details
                OutlinedTextField(
                    value = topicDescription,
                    onValueChange = { topicDescription = it },
                    label = { Text("Konu & Hedef Açıklaması") },
                    placeholder = { Text("Örn: Trigonometri 50 Soru + Formül Tekrarı") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Session Type
                Text("Çalışma Türü:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(SESSION_TYPES.size) { idx ->
                        val type = SESSION_TYPES[idx]
                        FilterChip(
                            selected = sessionType == type,
                            onClick = { sessionType = type },
                            label = { Text(type, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                // Reminder switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("🔔 Seans Hatırlatıcısı", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text(
                            text = if (hasReminder) "$reminderMinutes dakika önce bildirim gönder" else "Hatırlatıcı kapalı",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = hasReminder,
                        onCheckedChange = { hasReminder = it }
                    )
                }

                if (hasReminder) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(5, 10, 15, 30).forEach { mins ->
                            FilterChip(
                                selected = reminderMinutes == mins,
                                onClick = { reminderMinutes = mins },
                                label = { Text("$mins dk", style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (topicDescription.isNotBlank()) {
                        onSave(dayOfWeek, timeSlot, subjectName, topicDescription.trim(), sessionType, hasReminder, reminderMinutes)
                    }
                },
                enabled = topicDescription.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text(if (initialItem != null) "Güncelle" else "Programa Ekle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
fun ReminderSettingsDialog(
    item: ScheduleItemEntity,
    studentName: String,
    onDismiss: () -> Unit,
    onSave: (hasReminder: Boolean, minutesBefore: Int) -> Unit,
    onTestReminder: (String) -> Unit
) {
    var hasReminder by remember { mutableStateOf(item.hasReminder) }
    var minutesBefore by remember { mutableIntStateOf(item.reminderMinutesBefore) }

    val presetOptions = listOf(5, 10, 15, 20, 30, 60)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = AmberAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Seans Hatırlatıcısı Ayarları",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Session info summary
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "${item.dayOfWeek} • ${item.timeSlot}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = NavyPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${item.subjectName} - ${item.topicDescription}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                // Master switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Hatırlatıcıyı Etkinleştir",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Seans saatinden önce sesli ve görsel bildirim al",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = hasReminder,
                        onCheckedChange = { hasReminder = it }
                    )
                }

                if (hasReminder) {
                    Text(
                        text = "Ne kadar önce bildirim gelsin?",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetOptions.take(3).forEach { mins ->
                            FilterChip(
                                selected = minutesBefore == mins,
                                onClick = { minutesBefore = mins },
                                label = { Text("$mins dk önce", style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetOptions.drop(3).forEach { mins ->
                            FilterChip(
                                selected = minutesBefore == mins,
                                onClick = { minutesBefore = mins },
                                label = { Text("$mins dk önce", style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Scheduling info card
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessAlarms,
                                contentDescription = null,
                                tint = NavyPrimary,
                                modifier = Modifier.size(18.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Otomatik Yerel Push Bildirim Mekanizması",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = NavyPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Bu seans her ${item.dayOfWeek} günü saat ${item.timeSlot}'den $minutesBefore dk önce AlarmManager (RTC_WAKEUP) ile cihazına sesli/titreşimli bildirim gönderir. Cihaz yeniden başlatılsa bile alarmlar otomatik korunur.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = SlateTextSecondary
                                )
                            }
                        }
                    }

                    // Direct System Push Notification Test Button
                    val dialogContext = LocalContext.current
                    Button(
                        onClick = {
                            SessionNotificationHelper.showSessionNotification(
                                context = dialogContext,
                                notificationId = item.id.toInt(),
                                subjectName = item.subjectName,
                                topicDescription = item.topicDescription,
                                timeSlot = item.timeSlot,
                                minutesBefore = minutesBefore
                            )
                            val msg = "Push bildirim cihazına iletildi: ${item.subjectName} seansına $minutesBefore dk kaldı!"
                            onTestReminder(msg)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cihaza Push Bildirim Gönder (Canlı Test)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }

                    // In-app Alert Preview Button
                    OutlinedButton(
                        onClick = {
                            val msg = "$studentName, ${item.timeSlot}'de ${item.subjectName} (${item.topicDescription}) seansın $minutesBefore dakika içinde başlıyor! Masana geç ve odaklanma modunu aç."
                            onTestReminder(msg)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Uygulama İçi Önizleme", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(hasReminder, minutesBefore) },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç")
            }
        }
    )
}
