package com.example.madcamp1

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CommunityAdapter(
    private var items: List<GridRow>,
    private val onCellClick: (CommunitySlot) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TIME = 0
        private const val TYPE_HEATMAP = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is GridRow.TimeRow -> TYPE_TIME
            is GridRow.HeatmapRow -> TYPE_HEATMAP
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TIME -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.community_time, parent, false)
                TimeRowViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.community_heatmap, parent, false)
                HeatmapRowViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is GridRow.TimeRow -> {
                (holder as TimeRowViewHolder).textTimeRow.text = item.time
            }
            is GridRow.HeatmapRow -> {
                val h = holder as HeatmapRowViewHolder
                item.cells.forEachIndexed { index, cell : CommunitySlot ->
                    h.cells[index].setBackgroundColor(getHeatmapColor(cell.count))
                    h.cells[index].setOnClickListener {
                        if (cell.count > 0) onCellClick(cell)
                    }
                }
            }
        }
    }

    fun updateData(newItems: List<GridRow>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun getHeatmapColor(count: Int): Int {
        return when (count) {
            0 -> Color.WHITE
            1 -> Color.parseColor("#C8E6C9")
            2 -> Color.parseColor("#81C784")
            3 -> Color.parseColor("#388E3C")
            else -> Color.parseColor("#1B5E20")
        }
    }

    class TimeRowViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textTimeRow: TextView = view.findViewById(R.id.textTimeRow)
    }

    class HeatmapRowViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cells = listOf(
            view.findViewById<View>(R.id.cell1),
            view.findViewById<View>(R.id.cell2),
            view.findViewById<View>(R.id.cell3),
            view.findViewById<View>(R.id.cell4),
            view.findViewById<View>(R.id.cell5),
            view.findViewById<View>(R.id.cell6)
        )
    }
}