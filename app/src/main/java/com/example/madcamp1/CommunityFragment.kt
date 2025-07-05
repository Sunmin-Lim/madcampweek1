package com.example.madcamp1

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class CommunityFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CommunityAdapter
    private lateinit var buttonPrevWeek: ImageButton
    private lateinit var buttonNextWeek: ImageButton
    private lateinit var textWeekRange: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    private var currentWeekStart: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY)

    private val timeSlots = listOf(
        "08:00", "09:00", "10:00", "11:00",
        "12:00", "13:00", "14:00", "15:00",
        "16:00", "17:00", "18:00", "19:00",
        "20:00", "21:00"
    )

    private val allPlayers = listOf(
        Player("Alice", "Forward", 10, R.drawable.playerdefault,
            listOf("2025-07-07 08:00", "2025-07-07 09:00")),
        Player("Bob", "Goalkeeper", 1, R.drawable.playerdefault,
            listOf("2025-07-08 08:00", "2025-07-08 09:00")),
        Player("Charlie", "Defender", 5, R.drawable.playerdefault,
            listOf("2025-07-07 08:00", "2025-07-09 10:00")),
        Player("Diana", "Midfielder", 12, R.drawable.playerdefault,
            listOf("2025-07-07 09:00"))
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_community, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewCommunity)
        buttonPrevWeek = view.findViewById(R.id.buttonPrevWeek)
        buttonNextWeek = view.findViewById(R.id.buttonNextWeek)
        textWeekRange = view.findViewById(R.id.textWeekRange)

        adapter = CommunityAdapter(emptyList())
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 8)
        recyclerView.adapter = adapter

        buttonPrevWeek.setOnClickListener {
            currentWeekStart = currentWeekStart.minusWeeks(1)
            refreshGrid()
        }

        buttonNextWeek.setOnClickListener {
            currentWeekStart = currentWeekStart.plusWeeks(1)
            refreshGrid()
        }

        refreshGrid()

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun refreshGrid() {
        val weekDates = (0..6).map { currentWeekStart.plusDays(it.toLong()) }
        val formatter = DateTimeFormatter.ofPattern("MM-dd")

        // update week range label
        textWeekRange.text = "${formatter.format(weekDates.first())} ~ ${formatter.format(weekDates.last())}"

        // Build counts map
        val counts = mutableMapOf<Pair<String, String>, Int>()
        for (player in allPlayers) {
            for (slot in player.availableSlots) {
                val parts = slot.split(" ")
                if (parts.size == 2) {
                    val date = parts[0]
                    val time = parts[1]
                    counts[Pair(date, time)] = (counts[Pair(date, time)] ?: 0) + 1
                }
            }
        }

        // Build grid cells
        val cells = mutableListOf<SlotCell>()

        // Header Row
        cells.add(SlotCell(isHeader = true, label = "Time"))
        for (date in weekDates) {
            cells.add(SlotCell(isHeader = true, label = date.format(formatter)))
        }

        // Body
        for (time in timeSlots) {
            // first column = time label
            cells.add(SlotCell(isHeader = true, label = time))

            for (date in weekDates) {
                val dateStr = date.toString()
                val count = counts[Pair(dateStr, time)] ?: 0
                cells.add(SlotCell(label = "$count players", count = count))
            }
        }

        adapter.updateData(cells)
    }
}