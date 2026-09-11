package com.example

import com.example.notification.SessionReminderScheduler
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun sessionReminderScheduler_parsesDayOfWeekCorrectly() {
    assertEquals(Calendar.MONDAY, SessionReminderScheduler.getDayOfWeekConstant("Pazartesi"))
    assertEquals(Calendar.TUESDAY, SessionReminderScheduler.getDayOfWeekConstant("Salı"))
    assertEquals(Calendar.WEDNESDAY, SessionReminderScheduler.getDayOfWeekConstant("Çarşamba"))
    assertEquals(Calendar.THURSDAY, SessionReminderScheduler.getDayOfWeekConstant("Perşembe"))
    assertEquals(Calendar.FRIDAY, SessionReminderScheduler.getDayOfWeekConstant("Cuma"))
    assertEquals(Calendar.SATURDAY, SessionReminderScheduler.getDayOfWeekConstant("Cumartesi"))
    assertEquals(Calendar.SUNDAY, SessionReminderScheduler.getDayOfWeekConstant("Pazar"))
  }

  @Test
  fun sessionReminderScheduler_parsesStartTimeCorrectly() {
    val (hour1, min1) = SessionReminderScheduler.parseStartTime("08:30 - 10:00")
    assertEquals(8, hour1)
    assertEquals(30, min1)

    val (hour2, min2) = SessionReminderScheduler.parseStartTime("14:15")
    assertEquals(14, hour2)
    assertEquals(15, min2)
  }

  @Test
  fun sessionReminderScheduler_calculatesFutureTriggerTime() {
    val triggerTime = SessionReminderScheduler.calculateNextTriggerTimeMillis(
      dayOfWeek = "Pazartesi",
      timeSlot = "10:00 - 11:30",
      reminderMinutesBefore = 15
    )
    val now = System.currentTimeMillis()
    assertTrue("Trigger time must be in the future", triggerTime > now)
  }

  @Test
  fun eduCoachUser_creationAndRoleIntegrity() {
    val teacherUser = com.example.auth.EduCoachUser(
      uid = "uid_teacher_123",
      email = "ogretmen@educoachpro.com",
      displayName = "Ayşe Yılmaz",
      role = com.example.data.model.UserRole.COACH,
      isFirebaseUser = true
    )
    assertEquals("uid_teacher_123", teacherUser.uid)
    assertEquals("ogretmen@educoachpro.com", teacherUser.email)
    assertEquals(com.example.data.model.UserRole.COACH, teacherUser.role)
    assertTrue(teacherUser.isFirebaseUser)

    val studentUser = com.example.auth.EduCoachUser(
      uid = "uid_student_456",
      email = "ogrenci@educoachpro.com",
      displayName = "Ali Yılmaz",
      role = com.example.data.model.UserRole.STUDENT,
      isFirebaseUser = false
    )
    assertEquals(com.example.data.model.UserRole.STUDENT, studentUser.role)
    assertFalse(studentUser.isFirebaseUser)
  }
}

