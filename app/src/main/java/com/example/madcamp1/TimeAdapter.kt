package com.example.madcamp1

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TimeAdapter(
    private val timeSlots: List<String>
) : RecyclerView.Adapter<TimeAdapter.ViewHolder>() {

    companion object {
        const val HALF_ROW_HEIGHT = 50
        const val ROW_HEIGHT = 100
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.textTimeRow)
    }

    override fun getItemCount(): Int = timeSlots.size + 2
    // 1 half blank at top + timeSlots.size + 1 bottom blank

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.community_time, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (position) {
            0 -> {
                // TOP HALF BLANK
                holder.text.text = ""
                holder.text.layoutParams.height = HALF_ROW_HEIGHT
            }
            itemCount - 1 -> {
                // BOTTOM BLANK same as header height
                holder.text.text = ""
                holder.text.layoutParams.height = ROW_HEIGHT
            }
            else -> {
                // TIME LABEL
                val index = position - 1
                holder.text.text = timeSlots[index]
                holder.text.layoutParams.height = ROW_HEIGHT
            }
        }
    }
}
