package com.example.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity

object SessionNotificationHelper {
    const val CHANNEL_ID_STUDY_REMINDER = "study_session_reminders_channel"
    const val CHANNEL_NAME_STUDY_REMINDER = "Çalışma Seansı Hatırlatıcıları"
    const val CHANNEL_DESC_STUDY_REMINDER = "Planlanmış ders ve etüt seansları öncesi bildirimler"

    const val CHANNEL_ID_INSTANT = "study_instant_alerts_channel"
    const val CHANNEL_NAME_INSTANT = "Anlık Seans Bildirimleri"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            // Main Reminder Channel
            val reminderChannel = NotificationChannel(
                CHANNEL_ID_STUDY_REMINDER,
                CHANNEL_NAME_STUDY_REMINDER,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC_STUDY_REMINDER
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setSound(defaultSoundUri, audioAttributes)
            }

            // Instant Test / Quick Alert Channel
            val instantChannel = NotificationChannel(
                CHANNEL_ID_INSTANT,
                CHANNEL_NAME_INSTANT,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Hızlı hatırlatıcı ve test bildirimleri"
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(instantChannel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun showSessionNotification(
        context: Context,
        notificationId: Int,
        subjectName: String,
        topicDescription: String,
        timeSlot: String,
        minutesBefore: Int
    ) {
        createNotificationChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission(context)) {
            return
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_TO_SCHEDULE", true)
            putExtra("EXTRA_SESSION_ID", notificationId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "📚 Ders Başlıyor: $subjectName"
        val content = if (minutesBefore > 0) {
            "$minutesBefore dakika sonra: $topicDescription ($timeSlot)"
        } else {
            "Şimdi başlama vakti: $topicDescription ($timeSlot)"
        }

        val bigText = buildString {
            append("⏰ Seans Saati: $timeSlot\n")
            append("📖 Konu: $topicDescription\n")
            append("🎯 Hazırlığını yap, odaklan ve hedeflerine bir adım daha yaklaş!")
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_STUDY_REMINDER)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setColor(0xFF1E3A8A.toInt())
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_media_play,
                "Seansı Aç",
                pendingIntent
            )

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Gracefully ignore if notification permission was revoked
        }
    }

    fun showInstantNotification(
        context: Context,
        notificationId: Int = (System.currentTimeMillis() % 100000).toInt(),
        title: String,
        message: String
    ) {
        createNotificationChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission(context)) {
            return
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_INSTANT)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setColor(0xFF0D9488.toInt())
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Gracefully ignore if notification permission was revoked
        }
    }
}
