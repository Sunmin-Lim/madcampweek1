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
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_GRID_ROW = 1

        private const val HEADER_HEIGHT = 100   // header row는 1시간 높이
        private const val ROW_HEIGHT = 50       // 30분 단위 row 높이
    }

    override fun getItemCount(): Int = gridData.size + 1
    // 1 header + N grid rows

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_HEADER else VIEW_TYPE_GRID_ROW
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.community_heatmap, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) {
            // HEADER ROW
            holder.itemView.layoutParams.height = HEADER_HEIGHT

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

        } else {
            // GRID ROW
            holder.itemView.layoutParams.height = ROW_HEIGHT

            val rowIndex = position - 1
            val rowData = gridData[rowIndex]
            rowData.forEachIndexed { i, count ->
                holder.cells[i].text = ""
                holder.cells[i].setBackgroundColor(getHeatmapColor(count))
                holder.cells[i].setOnClickListener {
                    if (count > 0) onCellClick(rowIndex, i)
                }
            }
        }
    }

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

    private fun getHeatmapColor(count: Int): Int {
        return when (count) {
            0 -> Color.WHITE
            1 -> Color.parseColor("#C8E6C9")
            2 -> Color.parseColor("#81C784")
            3 -> Color.parseColor("#388E3C")
            else -> Color.parseColor("#1B5E20")
        }
    }
}