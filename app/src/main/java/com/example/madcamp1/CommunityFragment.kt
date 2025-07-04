package com.example.madcamp1

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madcamp1.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CommunityFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CommunityAdapter
    private lateinit var dateButton: Button
    private lateinit var selectedDateText: TextView

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())


    private val allPlayers = listOf(
        Player("Alice", "Forward", 10, listOf("2025-07-05", "2025-07-08")),
        Player("Bob", "Goalkeeper", 1, listOf("2025-07-06", "2025-07-08")),
        Player("Charlie", "Defender", 5, listOf("2025-07-05", "2025-07-10")),
        Player("Diana", "Midfielder", 12, listOf("2025-07-07"))
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_community, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewCommunity)
        dateButton = view.findViewById(R.id.buttonPickDate)
        selectedDateText = view.findViewById(R.id.textSelectedDate)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = CommunityAdapter(emptyList())
        recyclerView.adapter = adapter

        dateButton.setOnClickListener {
            showDatePicker()
        }

        return view
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(),
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                val selectedDate = dateFormat.format(calendar.time)
                selectedDateText.text = "Selected Date: $selectedDate"
                filterPlayersByDate(selectedDate)
            },
            year, month, day
        )

        datePickerDialog.show()
    }

    private fun filterPlayersByDate(date: String) {
        val filteredPlayers = allPlayers.filter { it.availableDates.contains(date) }
        adapter.updateData(filteredPlayers)
    }
}