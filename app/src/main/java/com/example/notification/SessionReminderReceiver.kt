package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.EduCoachDatabase
import com.example.data.model.ScheduleItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SessionReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        Log.d(TAG, "onReceive triggered with action: $action")

        when (action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // Device rebooted, reschedule all active reminders from Room DB
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = EduCoachDatabase.getDatabase(context)
                        val allStudents = db.studentDao().getAllStudents().firstOrNull() ?: emptyList()
                        allStudents.forEach { student ->
                            val scheduleItems = db.scheduleDao().getScheduleForStudent(student.id).firstOrNull() ?: emptyList()
                            scheduleItems.filter { it.hasReminder }.forEach { item ->
                                SessionReminderScheduler.scheduleReminder(context, item)
                            }
                        }
                        Log.d(TAG, "Successfully restored all reminders after boot")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error restoring reminders after boot", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            SessionReminderScheduler.ACTION_SESSION_REMINDER -> {
                val sessionId = intent.getLongExtra(SessionReminderScheduler.EXTRA_SESSION_ID, -1L)
                val subjectName = intent.getStringExtra(SessionReminderScheduler.EXTRA_SUBJECT_NAME) ?: "Ders Çalışma"
                val topicDesc = intent.getStringExtra(SessionReminderScheduler.EXTRA_TOPIC_DESC) ?: "Planlanan konu çalışması"
                val timeSlot = intent.getStringExtra(SessionReminderScheduler.EXTRA_TIME_SLOT) ?: "09:00"
                val dayOfWeek = intent.getStringExtra(SessionReminderScheduler.EXTRA_DAY_OF_WEEK) ?: "Pazartesi"
                val minutesBefore = intent.getIntExtra(SessionReminderScheduler.EXTRA_MINUTES_BEFORE, 15)

                Log.d(TAG, "Triggering notification for session $sessionId: $subjectName ($timeSlot)")

                // Display local push notification
                SessionNotificationHelper.showSessionNotification(
                    context = context,
                    notificationId = if (sessionId > 0) sessionId.toInt() else (System.currentTimeMillis() % 10000).toInt(),
                    subjectName = subjectName,
                    topicDescription = topicDesc,
                    timeSlot = timeSlot,
                    minutesBefore = minutesBefore
                )

                // Reschedule for next week recurring session
                if (sessionId > 0) {
                    val entity = ScheduleItemEntity(
                        id = sessionId,
                        studentId = 0,
                        dayOfWeek = dayOfWeek,
                        timeSlot = timeSlot,
                        subjectName = subjectName,
                        topicDescription = topicDesc,
                        isCompleted = false,
                        hasReminder = true,
                        reminderMinutesBefore = minutesBefore
                    )
                    SessionReminderScheduler.scheduleReminder(context, entity)
                }
            }
        }
    }

    companion object {
        private const val TAG = "SessionReminderReceiver"
    }
}
