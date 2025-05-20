package Sinanilter.pomodor_timer2

import Sinanilter.pomodor_timer2.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PomodoroActivity : AppCompatActivity() {
    private lateinit var tvLesson: TextView
    private lateinit var tvTimer: TextView
    private lateinit var btnStartPause: Button
    private lateinit var circularProgressBar: CircularProgressBar
    private lateinit var lesson: String
    private var pomoDur: Int = 25
    private var shortBreak: Int = 5
    private var setCount: Int = 4
    private var longBreak: Int = 15
    private var currentSet = 1
    private var isRunning = false
    private var isBreak = false
    private var timer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0
    private var totalTimeInMillis: Long = 0
    private lateinit var setStatus: MutableList<Boolean>
    private lateinit var setAdapter: SetListAdapter
    private lateinit var rvSetList: RecyclerView
    private var mediaPlayer: MediaPlayer? = null
    private var realWorkMillis: Long = 0L
    private var pomodoroStartTime: Long = 0L
    private var overtimeStart: Long = 0L
    private var overtimeTimer: CountDownTimer? = null
    private var overtimeMillis: Long = 0L

    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "pomodoro_timer_channel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d("Pomodoro", "onCreate başladı")
            setContentView(R.layout.activity_pomodoro)
            supportActionBar?.hide()
            Log.d("Pomodoro", "setContentView tamamlandı")

            // UI elemanlarını bağlayalım
            try {
                tvLesson = findViewById(R.id.tvLesson)
                tvTimer = findViewById(R.id.tvTimer)
                btnStartPause = findViewById(R.id.btnStartPause)
                circularProgressBar = findViewById(R.id.circularProgressBar)
                Log.d("Pomodoro", "UI elemanları başarıyla bağlandı")
            } catch (e: Exception) {
                Log.e("Pomodoro", "UI elemanları bağlanırken hata: ${e.message}", e)
                Toast.makeText(this, "UI elemanları yüklenirken hata oluştu", Toast.LENGTH_LONG).show()
                return
            }

            // Intent kontrolü
            if (intent == null) {
                Log.e("Pomodoro", "Intent null!")
                Toast.makeText(this, "Veri aktarımında hata oluştu", Toast.LENGTH_LONG).show()
                return
            }

            // Verileri alalım
            try {
                lesson = intent.getStringExtra("LESSON") ?: "Ders"
                pomoDur = intent.getIntExtra("POMODORO", 25)
                shortBreak = intent.getIntExtra("SHORT_BREAK", 5)
                setCount = intent.getIntExtra("SET_COUNT", 4)
                longBreak = intent.getIntExtra("LONG_BREAK", 15)

                Log.d("Pomodoro", "Veriler alındı: Ders: $lesson, Süre: $pomoDur, Kısa mola: $shortBreak, Set: $setCount, Uzun mola: $longBreak")

                // UI'ı güncelleyelim
                tvLesson.text = lesson
                tvTimer.text = String.format("%02d:00", pomoDur)
                btnStartPause.text = "Başlat"
                circularProgressBar.apply {
                    progressMax = 100f
                    progressBarWidth = 12f
                    backgroundProgressBarWidth = 12f
                    progressBarColor = Color.WHITE
                    backgroundProgressBarColor = Color.parseColor("#FF8C00")
                    roundBorder = true
                    progress = 100f
                }
                Log.d("Pomodoro", "UI güncellendi")
            } catch (e: Exception) {
                Log.e("Pomodoro", "Veri işlenirken hata: ${e.message}", e)
                Toast.makeText(this, "Veriler işlenirken hata oluştu", Toast.LENGTH_LONG).show()
                return
            }

            // Ders adı ve set sayısı intent'ten alınıyor
            val lessonName = intent.getStringExtra("LESSON") ?: "Ders"
            val tvLessonNameBig = findViewById<TextView>(R.id.tvLessonNameBig)
            tvLessonNameBig.text = lessonName

            // Set kutucukları (hepsi tamamlanmamış başlar)
            rvSetList = findViewById(R.id.rvSetList)
            setStatus = MutableList(setCount) { false }
            setAdapter = SetListAdapter(setStatus)
            rvSetList.adapter = setAdapter
            rvSetList.layoutManager = LinearLayoutManager(this)

            createNotificationChannel()
            initializeMediaPlayer()

            btnStartPause.setOnClickListener {
                if (!isRunning) {
                    if (btnStartPause.text == "Başlat") {
                        currentSet = 1
                        isBreak = false
                        startPomodoro()
                    } else {
                        resumeTimer()
                    }
                } else {
                    pauseTimer()
                }
            }

            val btnReturnMain = findViewById<Button>(R.id.btnReturnMain)
            btnReturnMain.visibility = Button.GONE

        } catch (e: Exception) {
            Log.e("Pomodoro", "Kritik hata: ${e.message}", e)
            Toast.makeText(this, "Uygulama başlatılırken hata oluştu", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer.create(this, R.raw.timer_complete)
    }

    private fun playTimerCompleteSound() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                    it.prepare()
                }
                it.start()
            }
        } catch (e: Exception) {
            Log.e("Pomodoro", "Ses çalma hatası: ${e.message}", e)
        }
    }

    private fun startPomodoro() {
        isRunning = true
        isBreak = false
        tvLesson.text = "$lesson - Set $currentSet/$setCount"
        btnStartPause.text = "Duraklat"
        timeLeftInMillis = pomoDur * 60 * 1000L
        totalTimeInMillis = timeLeftInMillis
        updateProgressBar(1f)
        updateTheme(false)
        pomodoroStartTime = System.currentTimeMillis()
        startTimer(timeLeftInMillis, "Pomodoro") {
            // Set tamamlandığında kutucuğa tik at
            if (currentSet - 1 in setStatus.indices) {
                setStatus[currentSet - 1] = true
                setAdapter.notifyItemChanged(currentSet - 1)
            }
            // Otomatik geçiş yerine buton göster
            startOvertime()
            showTransitionButton("Molaya Geç") {
                stopOvertimeAndAddToWork()
                if (currentSet < setCount) {
                    startShortBreak()
                } else {
                    startLongBreak()
                }
            }
        }
    }

    private fun startShortBreak() {
        isRunning = true
        isBreak = true
        tvLesson.text = "$lesson - Kısa Mola ($currentSet/$setCount)"
        btnStartPause.text = "Duraklat"
        timeLeftInMillis = shortBreak * 60 * 1000L
        totalTimeInMillis = timeLeftInMillis
        updateProgressBar(1f)
        updateTheme(true)
        startTimer(timeLeftInMillis, "Kısa Mola") {
            showTransitionButton("Derse Başla") {
                currentSet++
                startPomodoro()
            }
        }
    }

    private fun startLongBreak() {
        isRunning = true
        isBreak = true
        tvLesson.text = "$lesson - Uzun Mola"
        btnStartPause.text = "Duraklat"
        timeLeftInMillis = longBreak * 60 * 1000L
        totalTimeInMillis = timeLeftInMillis
        updateProgressBar(1f)
        updateTheme(true)
        startTimer(timeLeftInMillis, "Uzun Mola") {
            // Uzun mola bitince CongratulationsActivity'ye geç
            val intent = Intent(this, CongratulationsActivity::class.java)
            intent.putExtra("SET_COUNT", setCount)
            intent.putExtra("POMODORO", pomoDur)
            intent.putExtra("LESSON", lesson)
            intent.putExtra("REAL_WORK_MILLIS", realWorkMillis)
            startActivity(intent)
            finish()
        }
    }

    private fun showTransitionButton(buttonText: String, onButtonClick: () -> Unit) {
        isRunning = false
        btnStartPause.text = buttonText
        btnStartPause.setOnClickListener {
            if (!isRunning) {
                if (btnStartPause.text == "Başlat") {
                    currentSet = 1
                    isBreak = false
                    startPomodoro()
                } else {
                    resumeTimer()
                }
            } else {
                pauseTimer()
            }
            onButtonClick()
        }
    }

    private fun showCongratulations() {
        isRunning = false
        isBreak = false
        // Toplam çalışma süresi (sadece pomodoro süreleri)
        val totalMinutes = pomoDur * setCount
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        val timeString = if (hours > 0) {
            "$hours saat $minutes dakika"
        } else {
            "$minutes dakika"
        }
        val analysis = "Tebrikler!\n\nToplam $setCount set ve $timeString çalıştınız.\nBu motivasyonu koruyarak devam edin!"
        tvLesson.text = analysis
        tvTimer.text = ""
        btnStartPause.text = "Başlat"
        updateProgressBar(0f)
        updateTheme(false)
        cancelNotification()
        // Ana menüye dön butonunu göster
        val btnReturnMain = findViewById<Button>(R.id.btnReturnMain)
        btnReturnMain.visibility = Button.VISIBLE
        btnReturnMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    private fun startTimer(millis: Long, mode: String, onFinish: () -> Unit) {
        timer?.cancel()
        timer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                tvTimer.text = String.format("%02d:%02d", minutes, seconds)
                val progress = millisUntilFinished.toFloat() / totalTimeInMillis
                updateProgressBar(progress)
                showNotification(mode, String.format("%02d:%02d", minutes, seconds))
            }
            override fun onFinish() {
                isRunning = false
                btnStartPause.text = "Başlat"
                updateProgressBar(0f)
                cancelNotification()
                if (!(isBreak && currentSet > setCount)) {
                    playTimerCompleteSound()
                }
                onFinish()
            }
        }.start()
        isRunning = true
        btnStartPause.text = "Duraklat"
    }

    private fun updateProgressBar(progress: Float) {
        circularProgressBar.progress = progress * 100f
    }

    private fun pauseTimer() {
        timer?.cancel()
        isRunning = false
        btnStartPause.text = "Devam Et"
        // Bildirimde kalan süre gösterilmeye devam edebilir
    }

    private fun resumeTimer() {
        startTimer(timeLeftInMillis, if (isBreak) {
            if (currentSet > setCount) "Uzun Mola" else "Kısa Mola"
        } else {
            "Pomodoro"
        }) {
            // Sadece kalan süreden devam et, yeni bir oturum başlatma
            if (!isBreak) {
                if (currentSet < setCount) {
                    showTransitionButton("Molaya Geç") {
                        startShortBreak()
                    }
                } else {
                    showTransitionButton("Molaya Geç") {
                        startLongBreak()
                    }
                }
            } else {
                showTransitionButton("Derse Başla") {
                    currentSet++
                    startPomodoro()
                }
            }
        }
    }

    private fun startOvertime() {
        overtimeStart = System.currentTimeMillis()
        overtimeMillis = 0L
        val tvTimer = findViewById<TextView>(R.id.tvTimer)
        tvTimer.setTextColor(android.graphics.Color.RED)
        overtimeTimer?.cancel()
        overtimeTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                overtimeMillis = System.currentTimeMillis() - overtimeStart
                val seconds = (overtimeMillis / 1000) % 60
                val minutes = (overtimeMillis / 1000) / 60
                tvTimer.text = String.format("+%02d:%02d", minutes, seconds)
            }
            override fun onFinish() {}
        }.start()
    }

    private fun stopOvertimeAndAddToWork() {
        overtimeTimer?.cancel()
        val tvTimer = findViewById<TextView>(R.id.tvTimer)
        tvTimer.setTextColor(if (isBreak) android.graphics.Color.parseColor("#FF8C00") else android.graphics.Color.WHITE)
        val thisPomodoroMillis = (System.currentTimeMillis() - pomodoroStartTime) + overtimeMillis
        realWorkMillis += thisPomodoroMillis
        overtimeMillis = 0L
        // --- Çalışma süresini SharedPreferences ile kaydet ---
        val prefs = getSharedPreferences("work_stats", MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val prev = prefs.getLong(today, 0L)
        prefs.edit().putLong(today, prev + thisPomodoroMillis).apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        overtimeTimer?.cancel()
        cancelNotification()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // Bildirim fonksiyonları
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Pomodoro Timer"
            val descriptionText = "Pomodoro zamanlayıcı bildirimi"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(mode: String, time: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(this, PomodoroActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(mode)
            .setContentText(time)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun cancelNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    override fun onPause() {
        super.onPause()
        // Eğer zamanlayıcı çalışıyorsa, notification'ı güncel tutmaya devam et
        if (isRunning) {
            val minutes = (timeLeftInMillis / 1000) / 60
            val seconds = (timeLeftInMillis / 1000) % 60
            showNotification(
                if (isBreak) {
                    if (currentSet > setCount) "Uzun Mola" else "Kısa Mola"
                } else "Pomodoro",
                String.format("%02d:%02d", minutes, seconds)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // Uygulama öne gelince notification'ı kaldır
        cancelNotification()
    }

    private fun updateTheme(isBreak: Boolean) {
        val mainLayout = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.main)
        val tvLessonNameBig = findViewById<TextView>(R.id.tvLessonNameBig)
        val tvTimer = findViewById<TextView>(R.id.tvTimer)
        val btnStartPause = findViewById<Button>(R.id.btnStartPause)

        if (isBreak) {
            // Mola teması - Beyaz arka plan, turuncu zamanlayıcı
            mainLayout.setBackgroundColor(Color.WHITE)
            tvLessonNameBig.setTextColor(Color.parseColor("#FF8C00"))
            tvTimer.setTextColor(Color.parseColor("#FF8C00"))
            btnStartPause.setBackgroundColor(Color.parseColor("#FF8C00"))
            circularProgressBar.apply {
                progressBarColor = Color.parseColor("#FF8C00")
                backgroundProgressBarColor = Color.parseColor("#FFE4B5")
            }
        } else {
            // Pomodoro teması - Turuncu arka plan, beyaz zamanlayıcı
            mainLayout.setBackgroundColor(Color.parseColor("#FFA500"))
            tvLessonNameBig.setTextColor(Color.WHITE)
            tvTimer.setTextColor(Color.WHITE)
            btnStartPause.setBackgroundColor(Color.parseColor("#FF8C00"))
            circularProgressBar.apply {
                progressBarColor = Color.WHITE
                backgroundProgressBarColor = Color.parseColor("#FF8C00")
            }
        }
    }
}
