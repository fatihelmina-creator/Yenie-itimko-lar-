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
import com.example.ui.components.StatCard
import com.example.ui.theme.*

@Composable
fun AdminScreen(
    students: List<StudentEntity>,
    onSelectStudent: (StudentEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Admin Header
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
                            .background(NavyPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Okul & Kurum Yönetim Paneli",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Başarı Eğitim Kurumları • 2026-2027 Eğitim Dönemi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Segment Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Kurumsal Metrikler", style = MaterialTheme.typography.labelSmall) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Lisans & Gelir Modeli", style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        if (selectedTab == 0) {
            // Metrics Overview
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Toplam Öğrenci",
                        value = "420",
                        subtitle = "16 Şube",
                        icon = { Icon(Icons.Default.School, contentDescription = null, tint = NavyPrimary) },
                        accentColor = NavyPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Eğitim Koçu",
                        value = "32",
                        subtitle = "%94 Aktif",
                        icon = { Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = IndigoSecondary) },
                        accentColor = IndigoSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Hedef Başarısı",
                        value = "%88",
                        subtitle = "+%12 Yıllık",
                        icon = { Icon(Icons.Default.TrendingUp, contentDescription = null, tint = EmeraldSuccess) },
                        accentColor = EmeraldSuccess,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // RİSKLİ ÖĞRENCİ TESPİT PANELİ (AI Powered)
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
                                    text = "⚠️ Riskli Öğrenci & Sapma Tespiti",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Son 3 haftada hedeflerin gerisinde kalan veya denemesi düşenler",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = RoseRisk.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "3 Kritik Risk",
                                    color = RoseRisk,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        RiskItem(
                            studentName = "Can Yılmaz (12-A)",
                            issue = "Matematik haftalık soru hedeflerinde 3 haftadır %25 sapma var.",
                            action = "Koça 'Pomodoro + Günlük konu sayısını azalt' tavsiyesi iletildi."
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        RiskItem(
                            studentName = "Zeynep Kaya (11-B)",
                            issue = "Son 2 haftalık koçluk görüşme periyodu gecikti.",
                            action = "Danışman koçuna otomatik hatırlatma gönderildi."
                        )
                    }
                }
            }

            // KOÇLUK GÖRÜŞME SIKLIĞI VE ÖĞRETMEN PERFORMANSI
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "👨‍🏫 Öğretmen Koçluk Performansı",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Aylık görüşme tamamlama ve veli memnuniyet indeksleri",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        CoachPerformanceRow("Ahmet Hoca (Matematik)", "14 Öğrenci", 48, 98)
                        Spacer(modifier = Modifier.height(8.dp))
                        CoachPerformanceRow("Selin Hoca (Fizik)", "12 Öğrenci", 42, 95)
                        Spacer(modifier = Modifier.height(8.dp))
                        CoachPerformanceRow("Murat Hoca (Türkçe)", "15 Öğrenci", 51, 96)
                    }
                }
            }
        } else {
            // GELİR MODELİ & LİSANSLAMA EKRANI
            item {
                Text(
                    text = "💼 EduCoach Pro Lisanslama & Gelir Modelleri",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Bireysel koçluktan yüzlerce şubeli zincir eğitim kurumlarına kadar ölçeklenebilir paketler.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Paket 1: Bireysel Abonelik
            item {
                LicenseCard(
                    title = "Bireysel Öğrenci / Veli Aboneliği",
                    badge = "Bireysel",
                    price = "249 ₺",
                    period = "/ Ay",
                    features = listOf(
                        "Bireysel Hedef & Soru Takip Modülü",
                        "Kişisel Gemini AI Koç Değerlendirmeleri",
                        "Sınırsız Deneme Sınavı Net Analizi",
                        "Veli Canlı Takip & Bildirim Paneli",
                        "Günlük Ders & Pomodoro Programı"
                    ),
                    buttonText = "Mevcut Plan (Öğrenci)",
                    accentColor = IndigoSecondary
                )
            }

            // Paket 2: Okul Lisansı
            item {
                LicenseCard(
                    title = "Kurumsal Okul Lisansı (K-12 & Kolejler)",
                    badge = "En Popüler",
                    price = "45 ₺",
                    period = "/ Öğrenci Başına Aylık",
                    features = listOf(
                        "Tüm Öğretmen ve Koç Hesapları Dahil",
                        "SWOT Analizi & Birebir Görüşme Takibi",
                        "Riskli Öğrenci Otomatik AI Erken Uyarı Sistemi",
                        "Okul Yönetimi Raporlama & İstatistik Masası",
                        "Sınırsız HD Online Video Görüşme Odası"
                    ),
                    buttonText = "Kurum Lisansı Aktif",
                    accentColor = NavyPrimary
                )
            }

            // Paket 3: Dershane / Kurs Lisansı
            item {
                LicenseCard(
                    title = "Dershane & Kurs Çoklu Şube Lisansı",
                    badge = "Kurumsal Pro",
                    price = "Özel Fiyat",
                    period = "Zincir Kurumlar İçin",
                    features = listOf(
                        "Çoklu Şube & Kampüs Yönetim Mimarisi",
                        "Optik Okuyucu & Sınav Yazılımı API Entegrasyonu",
                        "Otomatik Veli Bilgilendirme SMS & Push Sistemi",
                        "Öğretmen Performans Prim Analitiği",
                        "Kuruma Özel Logo, Renk & Marka Arayüzü (White-label)"
                    ),
                    buttonText = "Teklif İste & Bilgi Al",
                    accentColor = AmberAccent
                )
            }
        }
    }
}

@Composable
fun RiskItem(studentName: String, issue: String, action: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFEF2F2),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = studentName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF991B1B)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Durum: $issue",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Öneri: $action",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFB91C1C)
            )
        }
    }
}

@Composable
fun CoachPerformanceRow(name: String, students: String, meetings: Int, satisfaction: Int) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                Text(text = "$students • Aylık $meetings Görüşme", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = EmeraldSuccess.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "%$satisfaction Memnuniyet",
                    color = EmeraldSuccess,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun LicenseCard(
    title: String,
    badge: String,
    price: String,
    period: String,
    features: List<String>,
    buttonText: String,
    accentColor: Color
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badge,
                        color = accentColor,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = price,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = period,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            features.forEach { feature ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* License Action */ },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(buttonText)
            }
        }
    }
}
