package com.example.madcamp1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.CalendarView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class CommunityFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CommunityAdapter
    private lateinit var calendarView: CalendarView
    private lateinit var selectedDateText: TextView

    private val allPlayers = listOf(
        Player("Alice", "Forward", 10, R.drawable.playerdefault, listOf("2025-07-05", "2025-07-08")),
        Player("Bob", "Goalkeeper", 1, R.drawable.playerdefault, listOf("2025-07-06", "2025-07-08")),
        Player("Charlie", "Defender", 5, R.drawable.playerdefault, listOf("2025-07-05", "2025-07-10")),
        Player("Diana", "Midfielder", 12, R.drawable.playerdefault, listOf("2025-07-07"))
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_community, container, false)

        calendarView = view.findViewById(R.id.calendarView)
        selectedDateText = view.findViewById(R.id.textSelectedDate)
        recyclerView = view.findViewById(R.id.recyclerViewCommunity)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = CommunityAdapter(emptyList())
        recyclerView.adapter = adapter

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            selectedDateText.text = "Selected Date: $selectedDate"
            filterPlayersByDate(selectedDate)
        }

        return view
    }

    private fun filterPlayersByDate(date: String) {
        val filteredPlayers = allPlayers.filter { it.availableDates.contains(date) }
        adapter.updateData(filteredPlayers)
    }
}