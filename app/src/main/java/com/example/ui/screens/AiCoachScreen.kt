package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentEntity
import com.example.ui.theme.*

@Composable
fun AiCoachScreen(
    student: StudentEntity?,
    aiStudentAdvice: String?,
    aiCoachAdvice: String?,
    isAiLoading: Boolean,
    onAskStudentCheckIn: (studiedToday: Boolean, subject: String, hours: String, note: String) -> Unit,
    onRequestCoachAdvice: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (student == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var studiedToday by remember { mutableStateOf(true) }
    var selectedSubject by remember { mutableStateOf("Matematik") }
    var hoursStudied by remember { mutableStateOf("4") }
    var studentNote by remember { mutableStateOf("Trigonometride uzun sorularda süre yetiştiremiyorum.") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Hero Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(NavyPrimary, Color(0xFF4338CA), Color(0xFF6D28D9))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(AmberAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = Color(0xFF78350F),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "EduCoach AI Koç",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Gemini Destekli Pedagojik Analiz & Koçluk Motoru",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE0E7FF))
                            )
                        }
                    }
                }
            }
        }

        // ÖĞRENCİ İÇİN GÜNLÜK CHECK-IN FORMU
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🎯 Günlük Öğrenci Koçluk Değerlendirmesi",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "AI Koçuna bugünkü durumunu anlat, anında kişisel çalışma stratejisi al.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Soru 1: Bugün Çalıştın mı?
                    Text(
                        text = "1. Bugün ders çalıştın mı?",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = studiedToday,
                            onClick = { studiedToday = true },
                            label = { Text("Evet, Odaklandım ✅") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldSuccess, selectedLabelColor = Color.White),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !studiedToday,
                            onClick = { studiedToday = false },
                            label = { Text("Mola / Dinlenme 💤") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AmberAccent, selectedLabelColor = Color.White),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Soru 2: En Zorlandığın Ders Hangisi?
                    Text(
                        text = "2. En çok zorlandığın ders hangisi oldu?",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Matematik", "Geometri", "Fizik", "Türkçe").forEach { subject ->
                            FilterChip(
                                selected = selectedSubject == subject,
                                onClick = { selectedSubject = subject },
                                label = { Text(subject, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Soru 3: Çalışma Süresi
                    Text(
                        text = "3. Bugün toplam kaç saat çalıştın?",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("2 Saat", "3 Saat", "4 Saat", "5+ Saat").forEach { h ->
                            val cleanH = h.split(" ").first()
                            FilterChip(
                                selected = hoursStudied == cleanH,
                                onClick = { hoursStudied = cleanH },
                                label = { Text(h, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Soru 4: Öğrencinin hissi / notu
                    Text(
                        text = "4. Bugün koçuna sormak veya belirtmek istediğin bir durum var mı?",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = studentNote,
                        onValueChange = { studentNote = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Örn: Formülleri anlıyorum ama denemede zaman yetmiyor...") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onAskStudentCheckIn(studiedToday, selectedSubject, hoursStudied, studentNote)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_ai_checkin_button"),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isAiLoading
                    ) {
                        if (isAiLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Koç Analiz Ediyor...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Koçuma Danış ve Taktik Al")
                        }
                    }
                }
            }
        }

        // AI ÖĞRENCİ GERİ BİLDİRİM KARTI
        if (aiStudentAdvice != null) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PsychologyAlt, contentDescription = null, tint = IndigoSecondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Koç Kişisel Taktik Raporu",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF0FDF4),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = aiStudentAdvice,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = Color(0xFF14532D),
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // KOÇ İÇİN SİSTEM ÖNERİLERİ KARTI
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
                                text = "👨‍🏫 Koç & Öğretmen Strateji Panosu",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Öğrencinin 3 haftalık trendi ve sistem tavsiyeleri",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = onRequestCoachAdvice,
                            modifier = Modifier.testTag("request_coach_ai_button")
                        ) {
                            Text("Rapor Üret")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val coachText = aiCoachAdvice ?: """
                        📌 Sistem Önerisi Örneği:
                        Öğrenci son 3 haftadır matematik hedeflerini tamamlayamadı.
                        
                        Sistem Stratejisi:
                        • Çalışma süresini azalt (Odak kaybını önle).
                        • Günlük konu sayısını düşür (Derinleşmeye odaklan).
                        • Soru çözüm oranını artır (Analizli soru çözümü).
                    """.trimIndent()

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF3C7),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = coachText,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = Color(0xFF78350F),
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }
    }
}
