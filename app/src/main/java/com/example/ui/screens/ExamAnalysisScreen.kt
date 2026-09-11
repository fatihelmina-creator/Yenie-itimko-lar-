package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MockExamEntity
import com.example.ui.components.SimpleExamNetChart
import com.example.ui.theme.*

@Composable
fun ExamAnalysisScreen(
    mockExams: List<MockExamEntity>,
    onAddExam: (name: String, type: String, tD: Int, tY: Int, mD: Int, mY: Int, fD: Int, fY: Int, sD: Int, sY: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddExamDialog by remember { mutableStateOf(false) }
    var selectedExam by remember { mutableStateOf(mockExams.firstOrNull()) }

    LaunchedEffect(mockExams) {
        if (selectedExam == null && mockExams.isNotEmpty()) {
            selectedExam = mockExams.first()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "📊 Deneme Sınavı Analizi",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Optik net girişleri, haftalık & aylık gelişim",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { showAddExamDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_exam_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Optik Giriş")
                    }
                }
            }
        }

        // Net Gelişim Grafiği (Son Sınavlar)
        item {
            val examNames = mockExams.map { it.examName.take(9) }
            val examScores = mockExams.map { it.totalNet }
            SimpleExamNetChart(examNames = examNames.reversed(), scores = examScores.reversed())
        }

        // Active Exam Selector Chips
        item {
            Column {
                Text(
                    text = "İncelenen Deneme Sınavı:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    mockExams.take(4).forEach { exam ->
                        val isSelected = selectedExam?.id == exam.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedExam = exam },
                            label = {
                                Text(
                                    text = "${exam.examName.take(12)} (${exam.totalNet}N)",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NavyPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Selected Exam Breakdown
        item {
            val current = selectedExam ?: mockExams.firstOrNull()
            if (current != null) {
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
                                    text = current.examName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Sınav Tarihi: ${current.examDate} • ${current.examType}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = NavyPrimary
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${current.totalNet} NET",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                                    )
                                    Text(
                                        text = "Puan: ${current.score.toInt()}",
                                        color = AmberAccent,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Ders Bazında Net Dağılımı:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SubjectNetBadge("Türkçe", current.turkceCorrect, current.turkceWrong, current.turkceNet, modifier = Modifier.weight(1f))
                            SubjectNetBadge("Matematik", current.matCorrect, current.matWrong, current.matNet, modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SubjectNetBadge("Fen Bilimleri", current.fenCorrect, current.fenWrong, current.fenNet, modifier = Modifier.weight(1f))
                            SubjectNetBadge("Sosyal Bilgiler", current.sosyalCorrect, current.sosyalWrong, current.sosyalNet, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Exam History List
        item {
            Text(
                text = "Kayıtlı Tüm Deneme Sınavları",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(mockExams) { exam ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = exam.examName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${exam.examDate} • ${exam.examType} • T:${exam.turkceNet}N | M:${exam.matNet}N | F:${exam.fenNet}N | S:${exam.sosyalNet}N",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFDBEAFE)
                    ) {
                        Text(
                            text = "${exam.totalNet} Net",
                            color = NavyPrimary,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    if (showAddExamDialog) {
        AddExamDialog(
            onDismiss = { showAddExamDialog = false },
            onConfirm = { name, type, tD, tY, mD, mY, fD, fY, sD, sY ->
                onAddExam(name, type, tD, tY, mD, mY, fD, fY, sD, sY)
                showAddExamDialog = false
            }
        )
    }
}

@Composable
fun SubjectNetBadge(
    subject: String,
    correct: Int,
    wrong: Int,
    net: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = subject,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${correct}D ${wrong}Y",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${net} Net",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyPrimary
                    )
                )
            }
        }
    }
}

@Composable
fun AddExamDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String, tD: Int, tY: Int, mD: Int, mY: Int, fD: Int, fY: Int, sD: Int, sY: Int) -> Unit
) {
    var examName by remember { mutableStateOf("Özdebir TYT 4") }
    var examType by remember { mutableStateOf("TYT") }
    var tD by remember { mutableStateOf("32") }
    var tY by remember { mutableStateOf("4") }
    var mD by remember { mutableStateOf("30") }
    var mY by remember { mutableStateOf("3") }
    var fD by remember { mutableStateOf("15") }
    var fY by remember { mutableStateOf("3") }
    var sD by remember { mutableStateOf("16") }
    var sY by remember { mutableStateOf("2") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Optik Deneme Sonuç Girişi") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(
                        value = examName,
                        onValueChange = { examName = it },
                        label = { Text("Deneme Sınavı Adı") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = examType == "TYT",
                            onClick = { examType = "TYT" },
                            label = { Text("TYT (Temel Yeterlilik)") }
                        )
                        FilterChip(
                            selected = examType == "AYT",
                            onClick = { examType = "AYT" },
                            label = { Text("AYT (Alan Yeterlilik)") }
                        )
                    }
                }
                item {
                    Text("Ders Doğru & Yanlış Sayıları:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = tD, onValueChange = { tD = it }, label = { Text("Türkçe D") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = tY, onValueChange = { tY = it }, label = { Text("Türkçe Y") }, modifier = Modifier.weight(1f))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = mD, onValueChange = { mD = it }, label = { Text("Matematik D") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = mY, onValueChange = { mY = it }, label = { Text("Matematik Y") }, modifier = Modifier.weight(1f))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = fD, onValueChange = { fD = it }, label = { Text("Fen D") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = fY, onValueChange = { fY = it }, label = { Text("Fen Y") }, modifier = Modifier.weight(1f))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = sD, onValueChange = { sD = it }, label = { Text("Sosyal D") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = sY, onValueChange = { sY = it }, label = { Text("Sosyal Y") }, modifier = Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        examName,
                        examType,
                        tD.toIntOrNull() ?: 0,
                        tY.toIntOrNull() ?: 0,
                        mD.toIntOrNull() ?: 0,
                        mY.toIntOrNull() ?: 0,
                        fD.toIntOrNull() ?: 0,
                        fY.toIntOrNull() ?: 0,
                        sD.toIntOrNull() ?: 0,
                        sY.toIntOrNull() ?: 0
                    )
                }
            ) {
                Text("Optik Sonucu Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}
