package Sinanilter.pomodor_timer2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import Sinanilter.pomodor_timer2.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()

        // 2 saniye bekle, sonra ana ekrana geç
        android.os.Handler(mainLooper).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)
    }
} 