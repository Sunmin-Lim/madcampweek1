package com.example.madcamp1

data class CommunitySlot(
    val count: Int = 0,
    val date: String? = null,
    val time: String? = null
)

sealed class GridRow {
    data class TimeRow(val time: String) : GridRow()
    data class HeatmapRow(val cells: List<CommunitySlot>) : GridRow()
}
