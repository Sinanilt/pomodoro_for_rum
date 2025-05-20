package Sinanilter.pomodor_timer2

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import Sinanilter.pomodor_timer2.R
import androidx.core.content.ContextCompat

class TodoAdapter(private val items: MutableList<TodoItem>, val onChanged: (() -> Unit)? = null) : RecyclerView.Adapter<TodoAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: CardView = itemView.findViewById(R.id.cardTodo)
        val text: TextView = itemView.findViewById(R.id.tvTodoText)
        val check: CheckBox = itemView.findViewById(R.id.cbTodoDone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.text.text = item.text
        holder.check.isChecked = item.done
        holder.text.paintFlags = if (item.done) holder.text.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG else holder.text.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        holder.check.setOnCheckedChangeListener { _, isChecked ->
            item.done = isChecked
            notifyItemChanged(position)
            onChanged?.invoke()
        }
    }

    override fun getItemCount(): Int = items.size

    fun addTodo(item: TodoItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
        onChanged?.invoke()
    }

    fun getTodos(): List<TodoItem> = items

    fun removeTodo(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
        onChanged?.invoke()
    }
} 