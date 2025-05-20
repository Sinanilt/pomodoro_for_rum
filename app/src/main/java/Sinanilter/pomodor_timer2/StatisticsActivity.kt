package Sinanilter.pomodor_timer2

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import Sinanilter.pomodor_timer2.R

class StatisticsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)
        supportActionBar?.hide()

        val barChart = findViewById<BarChart>(R.id.barChart)

        val prefs = getSharedPreferences("work_stats", MODE_PRIVATE)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEE", Locale("tr"))
        val calendar = Calendar.getInstance()
        val workMinutes = mutableListOf<Float>()
        val days = mutableListOf<String>()
        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dateKey = dateFormat.format(calendar.time)
            val millis = prefs.getLong(dateKey, 0L)
            workMinutes.add(millis / 1000f / 60f) // dakikaya çevir
            days.add(dayFormat.format(calendar.time).replaceFirstChar { it.uppercase() })
        }

        val entries = workMinutes.mapIndexed { index, value -> BarEntry(index.toFloat(), value) }
        val dataSet = BarDataSet(entries, "Çalışma Süresi (dakika)")
        dataSet.color = Color.parseColor("#FF8C00")
        val barData = BarData(dataSet)
        barData.barWidth = 0.6f

        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.axisRight.isEnabled = false
        barChart.axisLeft.axisMinimum = 0f
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(days)
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.granularity = 1f
        barChart.xAxis.setDrawGridLines(false)
        barChart.legend.isEnabled = true
        barChart.setFitBars(true)
        barChart.invalidate()
    }
} 