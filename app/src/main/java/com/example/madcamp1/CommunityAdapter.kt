package com.example.madcamp1

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class SlotCell(
    val isHeader: Boolean = false,
    val label: String = "",
    val count: Int = 0
)

class CommunityAdapter(
    private var slots: List<SlotCell>
) : RecyclerView.Adapter<CommunityAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textCell: TextView = view.findViewById(R.id.textCell)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = slots.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cell = slots[position]

        holder.textCell.text = cell.label

        if (cell.isHeader) {
            holder.textCell.setBackgroundColor(Color.DKGRAY)
        } else {
            holder.textCell.setBackgroundColor(getHeatmapColor(cell.count))
        }
    }

    fun updateData(newSlots: List<SlotCell>) {
        slots = newSlots
        notifyDataSetChanged()
    }

    private fun getHeatmapColor(count: Int): Int {
        return when (count) {
            0 -> Color.WHITE
            1 -> Color.parseColor("#C8E6C9")  // Light green
            2 -> Color.parseColor("#81C784")  // Medium green
            3 -> Color.parseColor("#388E3C")  // Darker green
            else -> Color.parseColor("#1B5E20")  // Very dark green
        }
    }
}