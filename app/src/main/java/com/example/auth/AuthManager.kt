package com.example.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.data.model.UserRole
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class EduCoachUser(
    val uid: String,
    val email: String,
    val displayName: String,
    val role: UserRole,
    val isFirebaseUser: Boolean = true,
    val photoUrl: String? = null
)

sealed class AuthResult {
    data class Success(val user: EduCoachUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthManager(private val context: Context) {

    private val _currentUser = MutableStateFlow<EduCoachUser?>(null)
    val currentUser: StateFlow<EduCoachUser?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val credentialManager = CredentialManager.create(context)

    init {
        // Safe check if Firebase has been initialized
        val firebaseUser = getFirebaseAuth()?.currentUser
        if (firebaseUser != null) {
            _currentUser.value = EduCoachUser(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: "Kullanıcı",
                role = UserRole.STUDENT,
                isFirebaseUser = true,
                photoUrl = firebaseUser.photoUrl?.toString()
            )
        } else {
            // Default demo authenticated user for coach/student instant access
            _currentUser.value = EduCoachUser(
                uid = "demo_coach_1",
                email = "koc.ayse@educoachpro.com",
                displayName = "Ayşe Yılmaz (Eğitim Koçu & Öğretmen)",
                role = UserRole.COACH,
                isFirebaseUser = false
            )
        }
    }

    fun getFirebaseAuth(): FirebaseAuth? {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseApp not initialized or google-services.json absent: ${e.message}")
            null
        }
    }

    suspend fun signInWithEmail(
        email: String,
        pass: String,
        role: UserRole
    ): AuthResult = withContext(Dispatchers.IO) {
        _isLoading.value = true
        _authError.value = null
        try {
            val auth = getFirebaseAuth()
            if (auth != null) {
                val result = auth.signInWithEmailAndPassword(email.trim(), pass.trim()).await()
                val firebaseUser = result.user
                if (firebaseUser != null) {
                    val user = EduCoachUser(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: email,
                        displayName = firebaseUser.displayName ?: email.substringBefore("@"),
                        role = role,
                        isFirebaseUser = true
                    )
                    _currentUser.value = user
                    return@withContext AuthResult.Success(user)
                }
            }

            // Fallback for demo / offline environment
            val fallbackUser = EduCoachUser(
                uid = "local_${System.currentTimeMillis()}",
                email = email,
                displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                role = role,
                isFirebaseUser = false
            )
            _currentUser.value = fallbackUser
            AuthResult.Success(fallbackUser)
        } catch (e: Exception) {
            val message = mapFirebaseError(e)
            _authError.value = message
            AuthResult.Error(message)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun signUpWithEmail(
        name: String,
        email: String,
        pass: String,
        role: UserRole
    ): AuthResult = withContext(Dispatchers.IO) {
        _isLoading.value = true
        _authError.value = null
        try {
            val auth = getFirebaseAuth()
            if (auth != null) {
                val result = auth.createUserWithEmailAndPassword(email.trim(), pass.trim()).await()
                val firebaseUser = result.user
                if (firebaseUser != null) {
                    try {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name.trim())
                            .build()
                        firebaseUser.updateProfile(profileUpdates).await()
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not update user display name: ${e.message}")
                    }

                    val user = EduCoachUser(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: email,
                        displayName = name.ifBlank { email.substringBefore("@") },
                        role = role,
                        isFirebaseUser = true
                    )
                    _currentUser.value = user
                    return@withContext AuthResult.Success(user)
                }
            }

            // Fallback for demo / offline environment
            val fallbackUser = EduCoachUser(
                uid = "local_${System.currentTimeMillis()}",
                email = email,
                displayName = name.ifBlank { email.substringBefore("@") },
                role = role,
                isFirebaseUser = false
            )
            _currentUser.value = fallbackUser
            AuthResult.Success(fallbackUser)
        } catch (e: Exception) {
            val message = mapFirebaseError(e)
            _authError.value = message
            AuthResult.Error(message)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun signInWithGoogleCredentialManager(
        activityContext: Context,
        webClientId: String? = null,
        role: UserRole = UserRole.STUDENT
    ): AuthResult = withContext(Dispatchers.Main) {
        _isLoading.value = true
        _authError.value = null
        try {
            // Configure Google ID option
            val googleIdOptionBuilder = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)

            if (!webClientId.isNullOrBlank()) {
                googleIdOptionBuilder.setServerClientId(webClientId)
            } else {
                // Generic fallback web client ID or project ID placeholder
                googleIdOptionBuilder.setServerClientId("educoachpro-default.apps.googleusercontent.com")
            }

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOptionBuilder.build())
                .build()

            val result = credentialManager.getCredential(
                context = activityContext,
                request = request
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val auth = getFirebaseAuth()
                if (auth != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = auth.signInWithCredential(firebaseCredential).await()
                    val firebaseUser = authResult.user
                    if (firebaseUser != null) {
                        val user = EduCoachUser(
                            uid = firebaseUser.uid,
                            email = firebaseUser.email ?: googleIdTokenCredential.id,
                            displayName = firebaseUser.displayName ?: googleIdTokenCredential.displayName ?: "Google Kullanıcısı",
                            role = role,
                            isFirebaseUser = true,
                            photoUrl = firebaseUser.photoUrl?.toString() ?: googleIdTokenCredential.profilePictureUri?.toString()
                        )
                        _currentUser.value = user
                        return@withContext AuthResult.Success(user)
                    }
                }

                // If Firebase Auth not connected, still accept Google Id Token Profile
                val user = EduCoachUser(
                    uid = googleIdTokenCredential.id,
                    email = googleIdTokenCredential.id,
                    displayName = googleIdTokenCredential.displayName ?: "Google Kullanıcısı",
                    role = role,
                    isFirebaseUser = false,
                    photoUrl = googleIdTokenCredential.profilePictureUri?.toString()
                )
                _currentUser.value = user
                return@withContext AuthResult.Success(user)
            }

            AuthResult.Error("Google kimlik bilgisi alınamadı.")
        } catch (e: GetCredentialCancellationException) {
            _authError.value = "Google girişi iptal edildi."
            AuthResult.Error("Google girişi iptal edildi.")
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Credential Manager error: ${e.message}")
            // Provide informative message
            val msg = if (e.message?.contains("16:") == true || e.message?.contains("developer_error") == true) {
                "Google Sign-In API ayarı (Web Client ID) bekleniyor. Test kullanıcısı ile devam edebilirsiniz."
            } else {
                "Google kimlik yöneticisi hatası: ${e.localizedMessage}"
            }
            _authError.value = msg
            AuthResult.Error(msg)
        } catch (e: Exception) {
            Log.e(TAG, "Sign in with Google error", e)
            val msg = e.localizedMessage ?: "Google ile giriş sırasında bir hata oluştu."
            _authError.value = msg
            AuthResult.Error(msg)
        } finally {
            _isLoading.value = false
        }
    }

    fun quickSignInAs(role: UserRole, name: String, email: String) {
        _currentUser.value = EduCoachUser(
            uid = "quick_${role.name.lowercase()}_${System.currentTimeMillis() % 1000}",
            email = email,
            displayName = name,
            role = role,
            isFirebaseUser = false
        )
        _authError.value = null
    }

    fun signOut() {
        try {
            getFirebaseAuth()?.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Error during signOut: ${e.message}")
        }
        _currentUser.value = null
    }

    private fun mapFirebaseError(e: Exception): String {
        val msg = e.message ?: ""
        return when {
            msg.contains("password", ignoreCase = true) && msg.contains("invalid", ignoreCase = true) ->
                "Hatalı şifre girdiniz. Lütfen tekrar deneyin."
            msg.contains("user-not-found", ignoreCase = true) ->
                "Bu e-posta adresine kayıtlı kullanıcı bulunamadı."
            msg.contains("email-already-in-use", ignoreCase = true) ->
                "Bu e-posta adresi zaten kullanımda."
            msg.contains("invalid-email", ignoreCase = true) ->
                "Geçersiz e-posta formatı."
            msg.contains("weak-password", ignoreCase = true) ->
                "Şifre en az 6 karakter olmalıdır."
            else -> e.localizedMessage ?: "Kimlik doğrulama işlemi gerçekleştirilemedi."
        }
    }

    companion object {
        private const val TAG = "EduCoachAuthManager"
    }
}
