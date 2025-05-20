package Sinanilter.pomodor_timer2

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CongratulationsActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_congratulations)
        supportActionBar?.hide()

        val setCount = intent.getIntExtra("SET_COUNT", 0)
        val pomoDur = intent.getIntExtra("POMODORO", 0)
        val lesson = intent.getStringExtra("LESSON") ?: "Ders"

        val totalMinutes = pomoDur * setCount
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        val timeString = if (hours > 0) {
            "$hours saat $minutes dakika"
        } else {
            "$minutes dakika"
        }

        val realWorkMillis = intent.getLongExtra("REAL_WORK_MILLIS", 0L)
        val realMinutes = (realWorkMillis / 1000 / 60).toInt()
        val realHours = realMinutes / 60
        val realRemainMinutes = realMinutes % 60
        val realTimeString = if (realHours > 0) {
            "$realHours saat $realRemainMinutes dakika"
        } else {
            "$realRemainMinutes dakika"
        }

        val analysis = if (realWorkMillis > 0L) {
            "🏆 Toplam $setCount set ve $realTimeString çalıştınız.\n(Bekleme dahil gerçek süre)\nBu motivasyonu koruyarak devam edin!"
        } else {
            "🏆 Toplam $setCount set ve $timeString çalıştınız.\nBu motivasyonu koruyarak devam edin!"
        }

        val tvCongrats = findViewById<TextView>(R.id.tvCongrats)
        tvCongrats.text = "Tebrikler!"

        val tvCongratsDetail = findViewById<TextView>(R.id.tvCongratsDetail)
        tvCongratsDetail.text = analysis

        val btnReturnMain = findViewById<Button>(R.id.btnReturnMain)
        btnReturnMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        // Play sound
        mediaPlayer = MediaPlayer.create(this, R.raw.timer_complete)
        mediaPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
} 