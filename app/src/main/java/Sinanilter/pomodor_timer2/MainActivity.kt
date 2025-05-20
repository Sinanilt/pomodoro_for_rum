package Sinanilter.pomodor_timer2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        val etLesson = findViewById<EditText>(R.id.etLesson)
        val npPomodoroDuration = findViewById<NumberPicker>(R.id.npPomodoroDuration)
        val npShortBreak = findViewById<NumberPicker>(R.id.npShortBreak)
        val npSetCount = findViewById<NumberPicker>(R.id.npSetCount)
        val npLongBreak = findViewById<NumberPicker>(R.id.npLongBreak)

        val tvPomodoroHint = findViewById<TextView>(R.id.tvPomodoroHint)
        val tvShortBreakHint = findViewById<TextView>(R.id.tvShortBreakHint)
        val tvSetCountHint = findViewById<TextView>(R.id.tvSetCountHint)
        val tvLongBreakHint = findViewById<TextView>(R.id.tvLongBreakHint)

        // NumberPicker ayarları
        npPomodoroDuration.minValue = 1
        npPomodoroDuration.maxValue = 60
        npPomodoroDuration.value = 25
        npPomodoroDuration.setOnClickListener { tvPomodoroHint.visibility = View.GONE }
        npPomodoroDuration.setOnValueChangedListener { _, _, newVal ->
            tvPomodoroHint.visibility = if (newVal != 0) View.GONE else View.VISIBLE
        }

        npShortBreak.minValue = 1
        npShortBreak.maxValue = 15
        npShortBreak.value = 5
        npShortBreak.setOnClickListener { tvShortBreakHint.visibility = View.GONE }
        npShortBreak.setOnValueChangedListener { _, _, newVal ->
            tvShortBreakHint.visibility = if (newVal != 0) View.GONE else View.VISIBLE
        }

        npSetCount.minValue = 1
        npSetCount.maxValue = 10
        npSetCount.value = 4
        npSetCount.setOnClickListener { tvSetCountHint.visibility = View.GONE }
        npSetCount.setOnValueChangedListener { _, _, newVal ->
            tvSetCountHint.visibility = if (newVal != 0) View.GONE else View.VISIBLE
        }

        npLongBreak.minValue = 1
        npLongBreak.maxValue = 30
        npLongBreak.value = 15
        npLongBreak.setOnClickListener { tvLongBreakHint.visibility = View.GONE }
        npLongBreak.setOnValueChangedListener { _, _, newVal ->
            tvLongBreakHint.visibility = if (newVal != 0) View.GONE else View.VISIBLE
        }

        val btnStart = findViewById<Button>(R.id.btnStart)
        btnStart.setOnClickListener {
            try {
                if (etLesson.text.toString().trim().isEmpty()) {
                    Toast.makeText(this, "Lütfen ders adını giriniz", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val lesson = etLesson.text.toString()
                val pomoDur = npPomodoroDuration.value
                val shortBreak = npShortBreak.value
                val setCount = npSetCount.value
                val longBreak = npLongBreak.value

                val intent = Intent(this, PomodoroActivity::class.java).apply {
                    putExtra("LESSON", lesson)
                    putExtra("POMODORO", pomoDur)
                    putExtra("SHORT_BREAK", shortBreak)
                    putExtra("SET_COUNT", setCount)
                    putExtra("LONG_BREAK", longBreak)
                }
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Bir hata oluştu: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        val fabStatistics = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabStatistics)
        fabStatistics.setOnClickListener {
            val intent = Intent(this, StatisticsActivity::class.java)
            startActivity(intent)
        }

        val fabTodo = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabTodo)
        fabTodo.setOnClickListener {
            val intent = Intent(this, TodoActivity::class.java)
            startActivity(intent)
        }
    }
}
