package com.example.madcamp1

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HeatmapAdapter(
    private val dates: List<String>,
    private val gridData: List<List<Int>>,
    private val onCellClick: (row: Int, col: Int) -> Unit
) : RecyclerView.Adapter<HeatmapAdapter.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_GRID_ROW = 1

        const val HALF_ROW_HEIGHT = 50
        const val ROW_HEIGHT = 100
    }

    // Total items = 1 header + N grid rows
    override fun getItemCount(): Int = gridData.size + 1

    // Decide which type of row this position is
    override fun getItemViewType(position: Int): Int {

        return if (position == 0) {
            VIEW_TYPE_HEADER
        } else {
            VIEW_TYPE_GRID_ROW
        }
    }

    // Inflate SAME layout for all rows
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.community_heatmap, parent, false)
        return ViewHolder(view)
    }

    // Bind data for HEADER, GRID ROW, or HALF BLANK
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Set row height
        holder.itemView.layoutParams.height = ROW_HEIGHT

        when (position) {
            0 -> {
                // HEADER ROW: show date labels
                holder.cells.forEachIndexed { i, cell ->
                    if (i < dates.size) {
                        cell.text = dates[i]
                        cell.setBackgroundColor(Color.DKGRAY)
                        cell.setTextColor(Color.WHITE)
                    } else {
                        cell.text = ""
                        cell.setBackgroundColor(Color.DKGRAY)
                    }
                    cell.setOnClickListener(null)
                }
            }

            else -> {
                // HEATMAP GRID ROW
                val dataIndex = position - 1
                val rowData = gridData[dataIndex]
                rowData.forEachIndexed { i, count ->
                    holder.cells[i].text = "" // no text in heatmap
                    holder.cells[i].setBackgroundColor(getHeatmapColor(count))
                    holder.cells[i].setOnClickListener {
                        if (count > 0) onCellClick(dataIndex, i)
                    }
                }
            }
        }
    }

    // ViewHolder holds references to the 7 cells in the row
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cells: List<TextView> = listOf(
            view.findViewById(R.id.cell0),
            view.findViewById(R.id.cell1),
            view.findViewById(R.id.cell2),
            view.findViewById(R.id.cell3),
            view.findViewById(R.id.cell4),
            view.findViewById(R.id.cell5),
            view.findViewById(R.id.cell6)
        )
    }

    // Heatmap coloring function
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