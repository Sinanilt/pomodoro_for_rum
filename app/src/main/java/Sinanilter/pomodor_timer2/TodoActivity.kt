package Sinanilter.pomodor_timer2

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.view.View
import android.widget.Button
import android.widget.TextView
import Sinanilter.pomodor_timer2.R
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.recyclerview.widget.ItemTouchHelper

class TodoActivity : AppCompatActivity() {
    private lateinit var todoAdapter: TodoAdapter
    private lateinit var prefs: SharedPreferences
    private val PREF_KEY = "todo_list"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo)
        supportActionBar?.hide()

        prefs = getSharedPreferences("todo_prefs", MODE_PRIVATE)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewTodos)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddTodo)
        val tvTodayDate = findViewById<TextView>(R.id.tvTodayDate)

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayLabel = SimpleDateFormat("yyyy-MM-dd EEEE", Locale("tr")).format(Date())
        tvTodayDate.text = todayLabel.replaceFirstChar { it.uppercase() }

        val todos = loadTodos().filter { it.date == today }.toMutableList()
        todoAdapter = TodoAdapter(todos) { saveTodos() }
        recyclerView.adapter = todoAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Sola kaydırınca sil
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                todoAdapter.removeTodo(pos)
                saveTodos()
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        fabAdd.setOnClickListener {
            showAddTodoDialog()
        }
    }

    private fun saveTodos() {
        val arr = JSONArray()
        for (item in todoAdapter.getTodos()) {
            val obj = JSONObject()
            obj.put("text", item.text)
            obj.put("done", item.done)
            obj.put("date", item.date)
            arr.put(obj)
        }
        prefs.edit().putString(PREF_KEY, arr.toString()).apply()
    }

    private fun loadTodos(): MutableList<TodoItem> {
        val list = mutableListOf<TodoItem>()
        val str = prefs.getString(PREF_KEY, null)
        if (str != null) {
            val arr = JSONArray(str)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(TodoItem(
                    obj.getString("text"),
                    obj.getBoolean("done"),
                    obj.optString("date")
                ))
            }
        }
        return list
    }

    private fun showAddTodoDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_add_todo, null)
        val editText = view.findViewById<EditText>(R.id.etTodoInput)
        val btnAdd = view.findViewById<Button>(R.id.btnAddTodo)
        btnAdd.setOnClickListener {
            val text = editText.text.toString().trim()
            if (text.isNotEmpty()) {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                todoAdapter.addTodo(TodoItem(text, false, today))
                saveTodos()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Ders adı boş olamaz", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.setContentView(view)
        dialog.show()
    }
}

// Basit veri modeli
data class TodoItem(val text: String, var done: Boolean, val date: String) 