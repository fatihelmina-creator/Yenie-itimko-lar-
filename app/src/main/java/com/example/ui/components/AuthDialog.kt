package com.example.ui.components

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.auth.EduCoachUser
import com.example.data.model.UserRole
import com.example.ui.theme.*

@Composable
fun AuthDialog(
    currentUser: EduCoachUser?,
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSignInEmail: (email: String, pass: String, role: UserRole) -> Unit,
    onSignUpEmail: (name: String, email: String, pass: String, role: UserRole) -> Unit,
    onSignInGoogle: (activityContext: android.content.Context, role: UserRole) -> Unit,
    onQuickSignIn: (role: UserRole, name: String, email: String) -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    var authMode by remember { mutableStateOf(if (currentUser == null) 0 else 2) } // 0: Giriş, 1: Kayıt, 2: Profil & Hızlı Geçiş
    var selectedRole by remember { mutableStateOf(currentUser?.role ?: UserRole.COACH) }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f)
                .testTag("auth_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF0F172A), NavyPrimary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "EduCoach Güvenli Kimlik",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = NavyPrimary
                            )
                            Text(
                                text = "Firebase Auth & Credential Manager",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = SlateTextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Current Authenticated User Summary Card
                if (currentUser != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(NavyPrimary, IndigoSecondary)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser.displayName.take(1).uppercase(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentUser.displayName,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = NavyPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (currentUser.isFirebaseUser) EmeraldSuccess.copy(alpha = 0.15f) else AmberAccent.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = if (currentUser.isFirebaseUser) "Firebase Aktif" else "Yerel / Demo",
                                            color = if (currentUser.isFirebaseUser) Color(0xFF047857) else Color(0xFFB45309),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = currentUser.email.ifBlank { "UID: ${currentUser.uid.take(12)}..." },
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = SlateTextSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Aktif Rol: ${currentUser.role.titleTr}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = IndigoSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = onSignOut,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                            ) {
                                Text("Çıkış", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Mode Tabs: Giriş / Kayıt / Hızlı Roller
                TabRow(
                    selectedTabIndex = authMode,
                    containerColor = Color(0xFFF1F5F9),
                    contentColor = NavyPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = authMode == 0,
                        onClick = { authMode = 0 },
                        text = { Text("Giriş Yap", style = MaterialTheme.typography.labelMedium) }
                    )
                    Tab(
                        selected = authMode == 1,
                        onClick = { authMode = 1 },
                        text = { Text("Kayıt Ol", style = MaterialTheme.typography.labelMedium) }
                    )
                    Tab(
                        selected = authMode == 2,
                        onClick = { authMode = 2 },
                        text = { Text("Hızlı Roller", style = MaterialTheme.typography.labelMedium) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Error Message banner if any
                AnimatedVisibility(visible = !errorMessage.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEE2E2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF991B1B)
                            )
                        }
                    }
                }

                // Loading Indicator
                AnimatedVisibility(visible = isLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = NavyPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kimlik doğrulanıyor...", style = MaterialTheme.typography.bodySmall, color = SlateTextSecondary)
                    }
                }

                when (authMode) {
                    0, 1 -> {
                        // Role Selection Chip Group
                        Text(
                            text = "Giriş Yapılacak Rol Seçin:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = NavyPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            UserRole.values().forEach { role ->
                                val isSelected = selectedRole == role
                                val roleLabel = when (role) {
                                    UserRole.STUDENT -> "🎓 Öğrenci"
                                    UserRole.COACH -> "👨‍🏫 Koç/Öğretmen"
                                    UserRole.PARENT -> "👨‍👩‍👧 Veli"
                                    UserRole.ADMIN -> "🏫 Yönetim"
                                }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedRole = role },
                                    label = { Text(roleLabel, style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NavyPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Full Name (only in Registration mode)
                        if (authMode == 1) {
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Ad Soyad") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("E-posta Adresi") },
                            placeholder = { Text("ornek@educoachpro.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Şifre") },
                            placeholder = { Text("En az 6 karakter") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Şifreyi Göster/Gizle"
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Email Auth Submit Button
                        Button(
                            onClick = {
                                if (authMode == 0) {
                                    onSignInEmail(email, password, selectedRole)
                                } else {
                                    onSignUpEmail(fullName, email, password, selectedRole)
                                }
                            },
                            enabled = email.isNotBlank() && password.length >= 6 && !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = if (authMode == 0) Icons.Default.Login else Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (authMode == 0) "E-posta ile Güvenli Giriş Yap" else "EduCoach Hesabı Oluştur",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // OR Divider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFCBD5E1))
                            Text(
                                text = " VEYA ",
                                style = MaterialTheme.typography.labelSmall.copy(color = SlateTextSecondary),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFCBD5E1))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Google Sign-In with Credential Manager Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Button(
                                onClick = {
                                    onSignInGoogle(context, selectedRole)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFF1F2937)
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    // Google Multi-color G Icon representation
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF4285F4),
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "G",
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Google ile Hızlı Giriş Yap",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF1F2937)
                                        )
                                        Text(
                                            text = "Android Credential Manager API",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                            color = Color(0xFF6B7280)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // Quick Switch / Preset Demo Accounts for Teachers, Coaches & Students
                        Text(
                            text = "Önceden Tanımlı Kurumsal Hesaplar:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = NavyPrimary
                        )
                        Text(
                            text = "Öğretmen, koç veya öğrenci rolleriyle anında güvenli geçiş yapın:",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = SlateTextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Preset Accounts list
                        val presetUsers = listOf(
                            Triple(UserRole.COACH, "Ayşe Yılmaz (Eğitim Koçu / Öğretmen)", "koc.ayse@educoachpro.com"),
                            Triple(UserRole.STUDENT, "Ali Yılmaz (12. Sınıf Sayısal - Öğrenci)", "ali.yilmaz@educoachpro.com"),
                            Triple(UserRole.STUDENT, "Zeynep Kaya (12. Sınıf EA - Öğrenci)", "zeynep.kaya@educoachpro.com"),
                            Triple(UserRole.PARENT, "Fatma Yılmaz (Veli)", "fatma.veli@educoachpro.com"),
                            Triple(UserRole.ADMIN, "Mehmet Öz (Kurum Müdürü / Yönetici)", "admin@educoachpro.com")
                        )

                        presetUsers.forEach { (role, name, userEmail) ->
                            val isCurrent = currentUser?.email == userEmail
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isCurrent) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                                border = BorderStroke(
                                    width = if (isCurrent) 1.5.dp else 1.dp,
                                    color = if (isCurrent) NavyPrimary else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = NavyPrimary
                                        )
                                        Text(
                                            text = userEmail,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = SlateTextSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            onQuickSignIn(role, name, userEmail)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isCurrent) EmeraldSuccess else NavyPrimary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = if (isCurrent) "Aktif" else "Geçiş Yap",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Google Credential Manager trigger here as well
                        Button(
                            onClick = {
                                onSignInGoogle(context, UserRole.STUDENT)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Google Kimlik Yöneticisi ile Doğrula")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Security & Architecture Certification Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Güvenlik Standartları",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = NavyPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Firebase Authentication (v34.17.0 BoM)\n• Android Credential Manager API (Passkey/Google ID)\n• AES-256 Şifrelenmiş Oturum ve Token Yönetimi",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = SlateTextSecondary
                        )
                    }
                }
            }
        }
    }
}
