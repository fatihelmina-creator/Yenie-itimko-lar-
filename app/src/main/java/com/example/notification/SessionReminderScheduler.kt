package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.ScheduleItemEntity
import java.util.Calendar

object SessionReminderScheduler {
    const val ACTION_SESSION_REMINDER = "com.example.ACTION_SESSION_REMINDER"
    const val EXTRA_SESSION_ID = "extra_session_id"
    const val EXTRA_SUBJECT_NAME = "extra_subject_name"
    const val EXTRA_TOPIC_DESC = "extra_topic_desc"
    const val EXTRA_TIME_SLOT = "extra_time_slot"
    const val EXTRA_DAY_OF_WEEK = "extra_day_of_week"
    const val EXTRA_MINUTES_BEFORE = "extra_minutes_before"

    private const val TAG = "SessionReminderSched"

    fun getDayOfWeekConstant(dayOfWeekName: String): Int {
        return when (dayOfWeekName.trim().lowercase()) {
            "pazar" -> Calendar.SUNDAY
            "pazartesi" -> Calendar.MONDAY
            "salı", "sali" -> Calendar.TUESDAY
            "çarşamba", "carsamba" -> Calendar.WEDNESDAY
            "perşembe", "persembe" -> Calendar.THURSDAY
            "cuma" -> Calendar.FRIDAY
            "cumartesi" -> Calendar.SATURDAY
            else -> Calendar.MONDAY
        }
    }

    fun parseStartTime(timeSlot: String): Pair<Int, Int> {
        return try {
            val startPart = timeSlot.split("-")[0].trim()
            val parts = startPart.split(":")
            val hour = parts[0].trim().toInt()
            val minute = if (parts.size > 1) parts[1].trim().toInt() else 0
            Pair(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
        } catch (_: Exception) {
            Pair(9, 0)
        }
    }

    fun calculateNextTriggerTimeMillis(
        dayOfWeek: String,
        timeSlot: String,
        reminderMinutesBefore: Int
    ): Long {
        val (startHour, startMinute) = parseStartTime(timeSlot)
        val targetDay = getDayOfWeekConstant(dayOfWeek)

        val now = Calendar.getInstance()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, targetDay)
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, -reminderMinutesBefore)
        }

        // If time already passed this week, schedule for next week
        if (calendar.timeInMillis <= now.timeInMillis) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }

    fun scheduleReminder(context: Context, item: ScheduleItemEntity) {
        if (!item.hasReminder) {
            cancelReminder(context, item.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerTimeMillis = calculateNextTriggerTimeMillis(
            item.dayOfWeek,
            item.timeSlot,
            item.reminderMinutesBefore
        )

        val intent = Intent(context, SessionReminderReceiver::class.java).apply {
            action = ACTION_SESSION_REMINDER
            putExtra(EXTRA_SESSION_ID, item.id)
            putExtra(EXTRA_SUBJECT_NAME, item.subjectName)
            putExtra(EXTRA_TOPIC_DESC, item.topicDescription)
            putExtra(EXTRA_TIME_SLOT, item.timeSlot)
            putExtra(EXTRA_DAY_OF_WEEK, item.dayOfWeek)
            putExtra(EXTRA_MINUTES_BEFORE, item.reminderMinutesBefore)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled reminder for ${item.subjectName} (ID: ${item.id}) at $triggerTimeMillis")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm for session ${item.id}", e)
        }
    }

    fun cancelReminder(context: Context, sessionId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, SessionReminderReceiver::class.java).apply {
            action = ACTION_SESSION_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            sessionId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Cancelled reminder for session ID $sessionId")
    }

    fun rescheduleAll(context: Context, items: List<ScheduleItemEntity>) {
        items.forEach { item ->
            if (item.hasReminder) {
                scheduleReminder(context, item)
            }
        }
    }

    fun triggerInstantTestNotification(context: Context, item: ScheduleItemEntity) {
        SessionNotificationHelper.showSessionNotification(
            context = context,
            notificationId = item.id.toInt(),
            subjectName = item.subjectName,
            topicDescription = item.topicDescription,
            timeSlot = item.timeSlot,
            minutesBefore = item.reminderMinutesBefore
        )
    }
}
