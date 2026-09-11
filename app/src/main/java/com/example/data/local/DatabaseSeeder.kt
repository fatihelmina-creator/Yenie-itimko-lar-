package com.example.data.local

import com.example.data.model.*
import kotlinx.coroutines.flow.first

object DatabaseSeeder {
    suspend fun seedDatabaseIfEmpty(db: EduCoachDatabase) {
        val existingStudents = db.studentDao().getAllStudents().first()
        if (existingStudents.isNotEmpty()) return

        // Insert Students
        val student1Id = db.studentDao().insertStudent(
            StudentEntity(
                fullName = "Can Yılmaz",
                gradeLevel = "12. Sınıf Sayısal",
                targetSchool = "Hacettepe Tıp Fakültesi",
                avatarColorHex = "#1E3A8A",
                currentPoints = 1450,
                level = 4,
                streakDays = 7,
                parentName = "Mehmet Yılmaz"
            )
        )

        val student2Id = db.studentDao().insertStudent(
            StudentEntity(
                fullName = "Zeynep Kaya",
                gradeLevel = "11. Sınıf Eşit Ağırlık",
                targetSchool = "Ankara Hukuk Fakültesi",
                avatarColorHex = "#4F46E5",
                currentPoints = 980,
                level = 3,
                streakDays = 4,
                parentName = "Ayşe Kaya"
            )
        )

        val student3Id = db.studentDao().insertStudent(
            StudentEntity(
                fullName = "Burak Demir",
                gradeLevel = "12. Sınıf Sayısal",
                targetSchool = "İTÜ Bilgisayar Mühendisliği",
                avatarColorHex = "#0D9488",
                currentPoints = 1820,
                level = 5,
                streakDays = 12,
                parentName = "Kemal Demir"
            )
        )

        // Seed Goals for Student 1
        db.goalDao().insertGoal(
            GoalEntity(
                studentId = student1Id,
                title = "Günlük 250 Soru Çözümü (Mat & Fen)",
                type = "DAILY",
                currentAmount = 190,
                targetAmount = 250,
                unit = "Soru",
                isCompleted = false
            )
        )
        db.goalDao().insertGoal(
            GoalEntity(
                studentId = student1Id,
                title = "Paragraf Hız Testi (40 Soru)",
                type = "DAILY",
                currentAmount = 40,
                targetAmount = 40,
                unit = "Soru",
                isCompleted = true
            )
        )
        db.goalDao().insertGoal(
            GoalEntity(
                studentId = student1Id,
                title = "Trigonometri II Formül & Not Tekrarı",
                type = "DAILY",
                currentAmount = 1,
                targetAmount = 1,
                unit = "Konu",
                isCompleted = true
            )
        )
        db.goalDao().insertGoal(
            GoalEntity(
                studentId = student1Id,
                title = "Haftalık 1500 Soru Hedefi",
                type = "WEEKLY",
                currentAmount = 1180,
                targetAmount = 1500,
                unit = "Soru",
                isCompleted = false
            )
        )
        db.goalDao().insertGoal(
            GoalEntity(
                studentId = student1Id,
                title = "2 Adet TYT Deneme Sınavı",
                type = "WEEKLY",
                currentAmount = 1,
                targetAmount = 2,
                unit = "Deneme",
                isCompleted = false
            )
        )
        db.goalDao().insertGoal(
            GoalEntity(
                studentId = student1Id,
                title = "TYT Net Ortalamasını 95+ Yap",
                type = "MONTHLY",
                currentAmount = 88,
                targetAmount = 95,
                unit = "Net",
                isCompleted = false
            )
        )
        db.goalDao().insertGoal(
            GoalEntity(
                studentId = student1Id,
                title = "Aylık 6000 Soru Çözüm Barajı",
                type = "MONTHLY",
                currentAmount = 4450,
                targetAmount = 6000,
                unit = "Soru",
                isCompleted = false
            )
        )

        // Seed Subject Progress
        val progressList = listOf(
            SubjectProgressEntity(studentId = student1Id, subjectName = "Matematik", completedTopics = 22, totalTopics = 28, masteryPercentage = 78, missingTopics = "İntegral Hacim, Olasılık & Kombinasyon"),
            SubjectProgressEntity(studentId = student1Id, subjectName = "Fizik", completedTopics = 16, totalTopics = 24, masteryPercentage = 66, missingTopics = "Manyetizma, Dalga Mekaniği, Modern Fizik"),
            SubjectProgressEntity(studentId = student1Id, subjectName = "Kimya", completedTopics = 20, totalTopics = 23, masteryPercentage = 86, missingTopics = "Elektrokimya & Piller"),
            SubjectProgressEntity(studentId = student1Id, subjectName = "Biyoloji", completedTopics = 18, totalTopics = 22, masteryPercentage = 81, missingTopics = "Bitki Biyolojisi"),
            SubjectProgressEntity(studentId = student1Id, subjectName = "Türkçe", completedTopics = 27, totalTopics = 30, masteryPercentage = 90, missingTopics = "Yazım Kuralları İstisnaları"),
            SubjectProgressEntity(studentId = student1Id, subjectName = "Geometri", completedTopics = 14, totalTopics = 20, masteryPercentage = 70, missingTopics = "Çember Analitiği, Uzay Geometri")
        )
        db.subjectProgressDao().insertAll(progressList)

        // Seed Tasks
        db.taskDao().insertTask(
            TaskEntity(
                studentId = student1Id,
                title = "AYT Matematik Çıkmış Sorular (2020-2024)",
                description = "Son 4 yılın AYT matematik çıkmış sorularını süre tutarak çöz ve yanlışları analiz defterine kaydet.",
                assignedBy = "Ahmet Hoca (Koç)",
                dueDate = "Yarın 18:00",
                isCompleted = false
            )
        )
        db.taskDao().insertTask(
            TaskEntity(
                studentId = student1Id,
                title = "Fizik Newton Yasaları & Sürtünme Kuvveti",
                description = "Apotemi fasikülünden Test 4, 5 ve 6 çözülecek. Takıldığın soruları etüt saatinde getir.",
                assignedBy = "Ahmet Hoca (Koç)",
                dueDate = "14 Ekim 2026",
                isCompleted = false
            )
        )
        db.taskDao().insertTask(
            TaskEntity(
                studentId = student1Id,
                title = "Haftalık Koçluk Değerlendirme Formunu Doldur",
                description = "Bu haftaki uyku, beslenme ve çalışma verimliliğini puanla.",
                assignedBy = "Ahmet Hoca (Koç)",
                dueDate = "09 Ekim 2026",
                isCompleted = true,
                completedAt = "09 Ekim 20:15"
            )
        )

        // Seed Mock Exams
        db.mockExamDao().insertExam(
            MockExamEntity(
                studentId = student1Id,
                examName = "Özdebir TYT Türkiye Geneli 3",
                examDate = "05 Eki 2026",
                examType = "TYT",
                turkceCorrect = 34, turkceWrong = 4, turkceNet = 33.0f,
                matCorrect = 33, matWrong = 3, matNet = 32.25f,
                fenCorrect = 16, fenWrong = 3, fenNet = 15.25f,
                sosyalCorrect = 16, sosyalWrong = 2, sosyalNet = 15.5f,
                totalNet = 96.0f,
                score = 438.5f
            )
        )
        db.mockExamDao().insertExam(
            MockExamEntity(
                studentId = student1Id,
                examName = "Limit TYT Kurumsal 2",
                examDate = "28 Eyl 2026",
                examType = "TYT",
                turkceCorrect = 32, turkceWrong = 5, turkceNet = 30.75f,
                matCorrect = 30, matWrong = 5, matNet = 28.75f,
                fenCorrect = 14, fenWrong = 4, fenNet = 13.0f,
                sosyalCorrect = 15, sosyalWrong = 3, sosyalNet = 14.25f,
                totalNet = 86.75f,
                score = 412.0f
            )
        )
        db.mockExamDao().insertExam(
            MockExamEntity(
                studentId = student1Id,
                examName = "Töder TYT Deneme 1",
                examDate = "19 Eyl 2026",
                examType = "TYT",
                turkceCorrect = 30, turkceWrong = 6, turkceNet = 28.5f,
                matCorrect = 28, matWrong = 6, matNet = 26.5f,
                fenCorrect = 12, fenWrong = 5, fenNet = 10.75f,
                sosyalCorrect = 14, sosyalWrong = 4, sosyalNet = 13.0f,
                totalNet = 78.75f,
                score = 385.0f
            )
        )

        // Seed SWOT
        db.swotDao().insertOrUpdateSwot(
            SwotEntity(
                studentId = student1Id,
                strengths = "• Türkçe paragraf okuma ve anlama hızı çok yüksek\n• Kimya ve Biyoloji derslerinde ezber değil mantık kurabiliyor\n• Kütüphane çalışma disiplini ve odaklanma süresi yüksek (4-5 saat)",
                weaknesses = "• AYT Geometri sorularına zaman ayıramıyor\n• Matematik problemlerinde işlem hatası oranı yüksek\n• Sınavın son 30 dakikasında dikkat dağınıklığı yaşıyor",
                opportunities = "• Günlük 40 dakikalık Geometri rutiniyle +7-8 net artış potansiyeli\n• Zaman yönetimi turlama taktiği ile süreyi 15 dk erken bitirebilme imkanı\n• Akıllı soru bankası soru çözüm analizleri",
                risks = "• Son 3 haftadır matematik haftalık soru hedeflerinin %25 altında kalıyor\n• Uykusuzluk ve sınav stresi nedeniyle deneme dalgalanmaları",
                lastUpdated = "10 Ekim 2026"
            )
        )

        // Seed Meetings
        db.meetingDao().insertMeeting(
            MeetingEntity(
                studentId = student1Id,
                coachName = "Ahmet Hoca",
                meetingDate = "08 Ekim 2026",
                notes = "Özdebir TYT 3 sınavı sonuçları ayrıntılı incelendi. Türkçe 33 net harika, Matematik 32.25 net hedef doğrultusunda. Geometride boş bırakılan 4 soru üzerine konuşuldu.",
                decisions = "1. Her sabah 08:00 - 08:45 arası 20 Geometri + 20 Paragraf sorusu rutini oluşturuldu.\n2. Denemede 'Turlama Taktiği' uygulanacak; 1 dakikadan fazla takılınan soru işaretlenip geçilecek.",
                nextGoals = "Bir sonraki denemede Geometriden en az 6 net çıkarmak ve toplam neti 98'e ulaştırmak."
            )
        )
        db.meetingDao().insertMeeting(
            MeetingEntity(
                studentId = student1Id,
                coachName = "Ahmet Hoca",
                meetingDate = "01 Ekim 2026",
                notes = "Eylül ayı soru çözüm grafikleri değerlendirildi. 4200 soru çözüldü, hedef 5000 idi. Fen netlerindeki artış tatmin edici.",
                decisions = "1. Pomodoro çalışma tekniği (50 dk çalışma + 10 dk mola) uygulanmaya başlanacak.\n2. Fizik manyetizma konusu için video konu anlatımı izlenecek.",
                nextGoals = "Haftalık 1200 soru barajını eksiksiz tamamlamak."
            )
        )

        // Seed Schedule
        db.scheduleDao().insertSchedule(ScheduleItemEntity(studentId = student1Id, dayOfWeek = "Pazartesi", timeSlot = "08:00 - 09:30", subjectName = "Matematik", topicDescription = "Trigonometri II & Formül Uygulamaları", isCompleted = true, orderIndex = 0, hasReminder = true, reminderMinutesBefore = 15, sessionType = "Konu Çalışması"))
        db.scheduleDao().insertSchedule(ScheduleItemEntity(studentId = student1Id, dayOfWeek = "Pazartesi", timeSlot = "09:45 - 11:15", subjectName = "Türkçe", topicDescription = "Paragraf Hız Denemesi & Analiz", isCompleted = true, orderIndex = 1, hasReminder = true, reminderMinutesBefore = 10, sessionType = "Soru Çözümü"))
        db.scheduleDao().insertSchedule(ScheduleItemEntity(studentId = student1Id, dayOfWeek = "Pazartesi", timeSlot = "11:30 - 13:00", subjectName = "Fizik", topicDescription = "Optik & Merceklerde Kırılma", isCompleted = false, orderIndex = 2, hasReminder = true, reminderMinutesBefore = 15, sessionType = "Konu Çalışması"))
        db.scheduleDao().insertSchedule(ScheduleItemEntity(studentId = student1Id, dayOfWeek = "Pazartesi", timeSlot = "14:00 - 15:30", subjectName = "Kimya", topicDescription = "Modern Atom Teorisi Soru Bankası", isCompleted = false, orderIndex = 3, hasReminder = false, reminderMinutesBefore = 15, sessionType = "Soru Çözümü"))
        db.scheduleDao().insertSchedule(ScheduleItemEntity(studentId = student1Id, dayOfWeek = "Pazartesi", timeSlot = "16:00 - 17:30", subjectName = "Biyoloji", topicDescription = "Bitki Biyolojisi Özet Not Çıkarma", isCompleted = false, orderIndex = 4, hasReminder = true, reminderMinutesBefore = 30, sessionType = "Tekrar"))
        
        // Salı
        db.scheduleDao().insertSchedule(ScheduleItemEntity(studentId = student1Id, dayOfWeek = "Salı", timeSlot = "08:30 - 10:00", subjectName = "Geometri", topicDescription = "Çemberde Açılar & Uzunluk Testleri", isCompleted = false, orderIndex = 0, hasReminder = true, reminderMinutesBefore = 15, sessionType = "Soru Çözümü"))
        db.scheduleDao().insertSchedule(ScheduleItemEntity(studentId = student1Id, dayOfWeek = "Salı", timeSlot = "10:30 - 12:00", subjectName = "Matematik", topicDescription = "Logaritma & Diziler Pekiştirme", isCompleted = false, orderIndex = 1, hasReminder = true, reminderMinutesBefore = 15, sessionType = "Konu Çalışması"))
        db.scheduleDao().insertSchedule(ScheduleItemEntity(studentId = student1Id, dayOfWeek = "Salı", timeSlot = "13:30 - 15:00", subjectName = "Fizik", topicDescription = "Manyetizma & İndüksiyon Akımı", isCompleted = false, orderIndex = 2, hasReminder = true, reminderMinutesBefore = 20, sessionType = "Soru Çözümü"))
        db.scheduleDao().insertSchedule(ScheduleItemEntity(studentId = student1Id, dayOfWeek = "Salı", timeSlot = "15:30 - 17:00", subjectName = "Türkçe", topicDescription = "Dil Bilgisi Karma Deneme Çözümü", isCompleted = false, orderIndex = 3, hasReminder = false, reminderMinutesBefore = 15, sessionType = "Deneme Sınavı"))

        // Seed Chat Messages
        db.chatMessageDao().insertMessage(
            ChatMessageEntity(channelId = "student_coach_1", senderRole = "COACH", senderName = "Ahmet Hoca (Koç)", message = "Can tebrik ederim! Özdebir TYT sınavında 96 net yaparak kurum 3.sü oldun 👏", timestamp = "Dün 14:20")
        )
        db.chatMessageDao().insertMessage(
            ChatMessageEntity(channelId = "student_coach_1", senderRole = "STUDENT", senderName = "Can Yılmaz", message = "Çok teşekkürler hocam! Sizin tavsiye ettiğiniz turlama tekniği sayesinde zamanı çok iyi yettirdim.", timestamp = "Dün 14:25")
        )
        db.chatMessageDao().insertMessage(
            ChatMessageEntity(channelId = "student_coach_1", senderRole = "COACH", senderName = "Ahmet Hoca (Koç)", message = "Harika! Yarınki koçluk görüşmemizde Geometrideki 4 boş soruya odaklanacağız.", timestamp = "Dün 14:30")
        )

        db.chatMessageDao().insertMessage(
            ChatMessageEntity(channelId = "parent_coach_1", senderRole = "COACH", senderName = "Ahmet Hoca (Koç)", message = "Mehmet Bey merhaba, Can'ın son deneme sonuçları açıklandı. 96 net ile tıp hedefi çizgisinde ilerliyor.", timestamp = "05 Eki 16:00")
        )
        db.chatMessageDao().insertMessage(
            ChatMessageEntity(channelId = "parent_coach_1", senderRole = "PARENT", senderName = "Mehmet Yılmaz (Veli)", message = "Ahmet Hocam emeğinize sağlık, evde de çok daha düzenli ve moralli çalışıyor.", timestamp = "05 Eki 16:45")
        )

        db.chatMessageDao().insertMessage(
            ChatMessageEntity(channelId = "group_12a", senderRole = "COACH", senderName = "Ahmet Hoca (Koç)", message = "📢 Sevgili 12-A öğrencileri; Cumartesi günü saat 09:30'da AYT Genel Deneme sınavımız vardır. Herkesin zamanında salonda olması rica olunur.", timestamp = "08 Eki 11:00")
        )
    }
}
