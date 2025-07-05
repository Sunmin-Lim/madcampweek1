package com.example.madcamp1

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

class CommunityFragment : Fragment() {

    private lateinit var recyclerViewTimeLabels: RecyclerView
    private lateinit var recyclerViewGrid: RecyclerView
    private lateinit var buttonPrevWeek: ImageButton
    private lateinit var buttonNextWeek: ImageButton
    private lateinit var textWeekRange: TextView

    private val sharedViewModel: SharedViewModel by activityViewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    private var currentWeekStart: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY)

    private var allPlayers: List<Player> = emptyList()

    // The grid stops at 21:00
    private val heatmapTimeSlots = listOf(
        "08:00", "09:00", "10:00", "11:00",
        "12:00", "13:00", "14:00", "15:00",
        "16:00", "17:00", "18:00", "19:00",
        "20:00", "21:00"
    )

    // The time column includes 22:00
    private val timeColumnSlots = heatmapTimeSlots + "22:00"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_community, container, false)

        // Find views
        recyclerViewTimeLabels = view.findViewById(R.id.recyclerViewTimeLabels)
        recyclerViewGrid = view.findViewById(R.id.recyclerViewGrid)
        buttonPrevWeek = view.findViewById(R.id.buttonPrevWeek)
        buttonNextWeek = view.findViewById(R.id.buttonNextWeek)
        textWeekRange = view.findViewById(R.id.textWeekRange)

        // LayoutManagers
        recyclerViewTimeLabels.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewGrid.layoutManager = LinearLayoutManager(requireContext())

        // Week navigation buttons
        buttonPrevWeek.setOnClickListener {
            currentWeekStart = currentWeekStart.minusWeeks(1)
            refreshUI()
        }

        buttonNextWeek.setOnClickListener {
            currentWeekStart = currentWeekStart.plusWeeks(1)
            refreshUI()
        }

        // Observe players data
        sharedViewModel.players.observe(viewLifecycleOwner) { players ->
            allPlayers = players ?: emptyList()
            refreshUI()
        }

        // Sync scrolling
        setupScrollSync()

        // Initial UI
        refreshUI()

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun refreshUI() {
        // Compute week dates
        val weekDates = (0..6).map { currentWeekStart.plusDays(it.toLong()) }
        val rangeFormatter = DateTimeFormatter.ofPattern("MM-dd")

        // Update week range label
        textWeekRange.text = "${rangeFormatter.format(weekDates.first())} ~ ${rangeFormatter.format(weekDates.last())}"

        // Build counts map from players
        val countsMap = mutableMapOf<Pair<String, String>, Int>()
        for (player in allPlayers) {
            for (slot in player.availableSlots) {
                val parts = slot.split(" ")
                if (parts.size == 2) {
                    val date = parts[0]
                    val time = parts[1]
                    countsMap[Pair(date, time)] = (countsMap[Pair(date, time)] ?: 0) + 1
                }
            }
        }

        // Build heatmap grid data (only for heatmapTimeSlots)
        val gridData = mutableListOf<List<Int>>()
        for (time in heatmapTimeSlots) {
            val row = mutableListOf<Int>()
            for (date in weekDates) {
                val dateStr = date.toString()
                val count = countsMap[Pair(dateStr, time)] ?: 0
                row.add(count)
            }
            gridData.add(row)
        }

        // Build header dates with line breaks
        val dateHeaders = weekDates.map { date ->
            val datePart = date.format(DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH))
            val dayPart = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
            "$datePart\n$dayPart"
        }

        // Set adapters
        recyclerViewTimeLabels.adapter = TimeAdapter(timeColumnSlots)
        recyclerViewGrid.adapter = HeatmapAdapter(dateHeaders, gridData) { row, col ->
            onGridCellClicked(row, col)
        }
    }

    private fun setupScrollSync() {
        var syncing = false
        val listener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (!syncing) {
                    syncing = true
                    if (rv == recyclerViewTimeLabels) {
                        recyclerViewGrid.scrollBy(0, dy)
                    } else {
                        recyclerViewTimeLabels.scrollBy(0, dy)
                    }
                    syncing = false
                }
            }
        }
        recyclerViewTimeLabels.addOnScrollListener(listener)
        recyclerViewGrid.addOnScrollListener(listener)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onGridCellClicked(row: Int, col: Int) {
        val weekDates = (0..6).map { currentWeekStart.plusDays(it.toLong()) }
        val date = weekDates[col].toString()
        val time = heatmapTimeSlots[row]

        val playersForSlot = allPlayers.filter { player ->
            player.availableSlots.contains("$date $time")
        }

        if (playersForSlot.isNotEmpty()) {
            val bottomSheet = CommunityList(playersForSlot)
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }
    }
}