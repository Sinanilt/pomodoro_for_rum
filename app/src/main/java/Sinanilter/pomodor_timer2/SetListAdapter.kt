package Sinanilter.pomodor_timer2

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SetListAdapter(
    private val items: List<Boolean>
) : RecyclerView.Adapter<SetListAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTick: TextView = itemView.findViewById(R.id.tvTick)
        val tvSetLabel: TextView = itemView.findViewById(R.id.tvSetLabel)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_set, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val isDone = items[position]
        holder.tvTick.visibility = if (isDone) View.VISIBLE else View.GONE
        holder.tvSetLabel.text = "${position + 1}. set"
        holder.tvStatus.text = if (isDone) "tamamlandı" else "devam ediyor"
        // Tüm yazılar aynı renk, font ve boyutta
        val color = Color.parseColor("#222222")
        holder.tvSetLabel.setTextColor(color)
        holder.tvStatus.setTextColor(color)
        holder.tvSetLabel.textSize = 16f
        holder.tvStatus.textSize = 16f
        holder.tvSetLabel.setTypeface(null, Typeface.NORMAL)
        holder.tvStatus.setTypeface(null, Typeface.NORMAL)
    }

    override fun getItemCount(): Int = items.size
} 