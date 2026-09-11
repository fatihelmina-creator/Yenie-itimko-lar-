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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.SimpleExamNetChart
import com.example.ui.components.StatCard
import com.example.ui.theme.*

@Composable
fun ParentDashboardScreen(
    student: StudentEntity?,
    mockExams: List<MockExamEntity>,
    meetings: List<MeetingEntity>,
    goals: List<GoalEntity>,
    onOpenChatWithCoach: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (student == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val latestExam = mockExams.firstOrNull()
    val completedGoals = goals.count { it.isCompleted }
    val totalGoals = goals.size
    val completionPercentage = if (totalGoals > 0) (completedGoals * 100) / totalGoals else 84

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Parent Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(IndigoSecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sayın ${student.parentName}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${student.fullName} öğrencimizin canlı koçluk takip paneli",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onOpenChatWithCoach) {
                        Icon(Icons.Default.Chat, contentDescription = "Koçla Mesajlaş", tint = NavyPrimary)
                    }
                }
            }
        }

        // Live Tracking Stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Devam Durumu",
                    value = "%100",
                    subtitle = "0 Devamsızlık",
                    icon = { Icon(Icons.Default.EventAvailable, contentDescription = null, tint = EmeraldSuccess) },
                    accentColor = EmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Hedef Başarısı",
                    value = "%$completionPercentage",
                    subtitle = "Yüksek",
                    icon = { Icon(Icons.Default.TrackChanges, contentDescription = null, tint = NavyPrimary) },
                    accentColor = NavyPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Son Deneme",
                    value = "${latestExam?.totalNet ?: 96.0}",
                    subtitle = "96.0 Net",
                    icon = { Icon(Icons.Default.Scoreboard, contentDescription = null, tint = AmberAccent) },
                    accentColor = AmberAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // CANLI BİLDİRİMLER (NOTIFICATIONS)
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
                        Text(
                            text = "🔔 Anlık Bildirimler",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldSuccess.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Canlı Aktif",
                                color = EmeraldSuccess,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    NotificationItem(
                        icon = Icons.Default.CheckCircle,
                        iconTint = EmeraldSuccess,
                        title = "Öğrenci Hedefi Tamamladı",
                        description = "${student.fullName} bugünkü 'Paragraf Hız Testi (40 Soru)' hedefini başarıyla tamamladı.",
                        time = "14:45"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NotificationItem(
                        icon = Icons.Default.Groups,
                        iconTint = NavyPrimary,
                        title = "Koçluk Görüşmesi Yapıldı",
                        description = "Ahmet Hoca ile haftalık deneme analizi ve çalışma takvimi görüşmesi tamamlandı.",
                        time = "Dün 15:30"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NotificationItem(
                        icon = Icons.Default.Analytics,
                        iconTint = IndigoSecondary,
                        title = "Deneme Sınavı Sonucu Eklendi",
                        description = "${latestExam?.examName ?: "Özdebir TYT 3"} optik sonucu sisteme işlendi: ${latestExam?.totalNet ?: 96.0} Net.",
                        time = "05 Eki"
                    )
                }
            }
        }

        // ÖĞRETMEN & KOÇ NOTLARI (VELİYE ÖZEL)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "👩‍🏫 Koçun Veli Bilgilendirme Notları",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Eğitim koçunun periyodik gözlem ve tavsiyeleri",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0FDF4),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Ahmet Hoca (Eğitim Koçu)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF166534)
                                )
                                Text(
                                    text = "08 Ekim 2026",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF166534)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Mehmet Bey merhaba; Can bu ay çalışma disiplini açısından çok iyi bir ivme yakaladı. Özdebir sınavında 96 net yaparak tıp hedefi sıralamasına girdi. Ev ortamında özellikle akşam 22:00'den sonra dinlenmesini ve uyku düzenini korumasını desteklemeniz çok faydalı olacaktır.",
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                                color = Color(0xFF14532D)
                            )
                        }
                    }
                }
            }
        }

        // EXAM NET CHART
        item {
            val examNames = mockExams.map { it.examName.take(10) }
            val examScores = mockExams.map { it.totalNet }
            SimpleExamNetChart(examNames = examNames.reversed(), scores = examScores.reversed())
        }
    }
}

@Composable
fun NotificationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    description: String,
    time: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    Text(text = time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
