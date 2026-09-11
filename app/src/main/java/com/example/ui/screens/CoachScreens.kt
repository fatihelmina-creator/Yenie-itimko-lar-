package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun CoachDashboardScreen(
    student: StudentEntity?,
    swot: SwotEntity?,
    meetings: List<MeetingEntity>,
    aiCoachAdvice: String?,
    isAiLoading: Boolean,
    onRequestAiAdvice: () -> Unit,
    onUpdateSwot: (String, String, String, String) -> Unit,
    onAddMeeting: (String, String, String) -> Unit,
    onAssignTask: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (student == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var showSwotEditDialog by remember { mutableStateOf(false) }
    var showAddMeetingDialog by remember { mutableStateOf(false) }
    var showAssignTaskDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Coach Header / Selected Student Overview
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(NavyPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.CoPresent, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = student.fullName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFDBEAFE)
                                ) {
                                    Text(
                                        text = "Koçluk Danışanı",
                                        color = NavyPrimary,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${student.gradeLevel} • Hedef: ${student.targetSchool}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showAddMeetingDialog = true },
                            modifier = Modifier.weight(1f).testTag("add_meeting_button"),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.EventNote, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Görüşme Ekle", style = MaterialTheme.typography.labelSmall)
                        }
                        FilledTonalButton(
                            onClick = { showAssignTaskDialog = true },
                            modifier = Modifier.weight(1f).testTag("assign_task_button"),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.AddTask, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Görev Ata", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // YAPAY ZEKA DESTEKLİ KOÇLUK ÖNERİLERİ
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6366F1)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Yapay Zeka Destekli Koçluk",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Performans ve hedef sapma analiz motoru",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isAiLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            TextButton(
                                onClick = onRequestAiAdvice,
                                modifier = Modifier.testTag("ai_coach_analyze_button")
                            ) {
                                Text("Analiz Et")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val displayAdvice = aiCoachAdvice ?: """
                        📊 ${student.fullName} Koçluk Özet Analizi:
                        Öğrenci son 3 haftadır matematik haftalık soru hedeflerini tamamlayamadı (%25 eksik).
                        
                        Sistem Önerisi:
                        • Blok çalışma süresini 50 dakikaya düşür (Pomodoro döngüsü).
                        • Günlük konu sayısını azaltıp ders odağını daralt.
                        • Soru çözüm oranını ve analiz defteri uygulamasını artır.
                    """.trimIndent()

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF5F3FF),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = displayAdvice,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = Color(0xFF3B0764)
                            )
                        }
                    }
                }
            }
        }

        // SWOT ANALİZİ BÖLÜMÜ
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
                                text = "🧭 SWOT Analizi",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Son Güncelleme: ${swot?.lastUpdated ?: "10 Ekim 2026"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { showSwotEditDialog = true },
                            modifier = Modifier.testTag("edit_swot_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "SWOT Düzenle", tint = NavyPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4-Quadrant SWOT View
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SwotQuadrant(
                            title = "💪 Güçlü Yönler (Strengths)",
                            content = swot?.strengths ?: "• Paragraf okuma hızı yüksek\n• Düzenli kütüphane disiplini",
                            backgroundColor = Color(0xFFECFDF5),
                            titleColor = Color(0xFF047857)
                        )
                        SwotQuadrant(
                            title = "⚠️ Zayıf Yönler (Weaknesses)",
                            content = swot?.weaknesses ?: "• AYT Geometride süre problemi\n• Matematik işlem hatası oranı",
                            backgroundColor = Color(0xFFFFFBEB),
                            titleColor = Color(0xFFB45309)
                        )
                        SwotQuadrant(
                            title = "🚀 Fırsatlar & Gelişim Alanları",
                            content = swot?.opportunities ?: "• Günlük 40 dk Geometri ile +8 net potansiyeli\n• Turlama taktiği",
                            backgroundColor = Color(0xFFEFF6FF),
                            titleColor = Color(0xFF1D4ED8)
                        )
                        SwotQuadrant(
                            title = "🚨 Riskler (Threats)",
                            content = swot?.risks ?: "• Son 3 haftadır hedeflerin %25 altında kalması\n• Sınav anı kaygısı",
                            backgroundColor = Color(0xFFFEF2F2),
                            titleColor = Color(0xFFB91C1C)
                        )
                    }
                }
            }
        }

        // GÖRÜŞME TAKİBİ BÖLÜMÜ
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
                                text = "📋 Koçluk Görüşme Geçmişi",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Birebir koçluk görüşmelerinde alınan kararlar ve hedefler",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (meetings.isEmpty()) {
                        Text(
                            text = "Henüz kaydedilmiş görüşme bulunmuyor.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        meetings.forEach { meeting ->
                            MeetingCardItem(meeting = meeting)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }

    if (showSwotEditDialog) {
        EditSwotDialog(
            currentSwot = swot,
            onDismiss = { showSwotEditDialog = false },
            onConfirm = { s, w, o, t ->
                onUpdateSwot(s, w, o, t)
                showSwotEditDialog = false
            }
        )
    }

    if (showAddMeetingDialog) {
        AddMeetingDialog(
            studentName = student.fullName,
            onDismiss = { showAddMeetingDialog = false },
            onConfirm = { notes, decisions, nextGoals ->
                onAddMeeting(notes, decisions, nextGoals)
                showAddMeetingDialog = false
            }
        )
    }

    if (showAssignTaskDialog) {
        AssignTaskDialog(
            studentName = student.fullName,
            onDismiss = { showAssignTaskDialog = false },
            onConfirm = { title, desc, due ->
                onAssignTask(title, desc, due)
                showAssignTaskDialog = false
            }
        )
    }
}

@Composable
fun SwotQuadrant(
    title: String,
    content: String,
    backgroundColor: Color,
    titleColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = titleColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun MeetingCardItem(meeting: MeetingEntity) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = NavyPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = meeting.meetingDate,
                        color = NavyPrimary,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "Koç: ${meeting.coachName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "📝 Notlar:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = meeting.notes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "✅ Alınan Kararlar:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldSuccess)
            )
            Text(
                text = meeting.decisions,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "🎯 Sonraki Görüşme Hedefleri:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = IndigoSecondary)
            )
            Text(
                text = meeting.nextGoals,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun EditSwotDialog(
    currentSwot: SwotEntity?,
    onDismiss: () -> Unit,
    onConfirm: (strengths: String, weaknesses: String, opportunities: String, risks: String) -> Unit
) {
    var strengths by remember { mutableStateOf(currentSwot?.strengths ?: "") }
    var weaknesses by remember { mutableStateOf(currentSwot?.weaknesses ?: "") }
    var opportunities by remember { mutableStateOf(currentSwot?.opportunities ?: "") }
    var risks by remember { mutableStateOf(currentSwot?.risks ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("SWOT Analizini Güncelle") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(
                        value = strengths,
                        onValueChange = { strengths = it },
                        label = { Text("Güçlü Yönler (Strengths)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = weaknesses,
                        onValueChange = { weaknesses = it },
                        label = { Text("Zayıf Yönler (Weaknesses)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = opportunities,
                        onValueChange = { opportunities = it },
                        label = { Text("Fırsatlar & Gelişim Alanları") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = risks,
                        onValueChange = { risks = it },
                        label = { Text("Riskler & Tehditler") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(strengths, weaknesses, opportunities, risks) }) {
                Text("Güncelle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Vazgeç") }
        }
    )
}

@Composable
fun AddMeetingDialog(
    studentName: String,
    onDismiss: () -> Unit,
    onConfirm: (notes: String, decisions: String, nextGoals: String) -> Unit
) {
    var notes by remember { mutableStateOf("") }
    var decisions by remember { mutableStateOf("") }
    var nextGoals by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$studentName ile Görüşme Kaydı") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Görüşme Notları (Deneme analizi, durum)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                OutlinedTextField(
                    value = decisions,
                    onValueChange = { decisions = it },
                    label = { Text("Alınan Kararlar") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                OutlinedTextField(
                    value = nextGoals,
                    onValueChange = { nextGoals = it },
                    label = { Text("Sonraki Görüşme Hedefleri") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (notes.isNotBlank() || decisions.isNotBlank()) {
                        onConfirm(notes, decisions, nextGoals)
                    }
                }
            ) {
                Text("Görüşmeyi Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

@Composable
fun AssignTaskDialog(
    studentName: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, dueDate: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("15 Ekim 2026") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$studentName İçin Görev Ata") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Görev Başlığı (Örn: AYT Fizik Testi)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Görev Açıklaması") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Son Teslim Tarihi") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, description, dueDate)
                    }
                }
            ) {
                Text("Görevi Ata")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}
