package com.example.madcamp1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyAdapter
    private lateinit var playerList: List<Player>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Load sample data
        playerList = getPlayers()
        adapter = MyAdapter(playerList)
        recyclerView.adapter = adapter

        return view
    }

    private fun getPlayers(): List<Player> {
        return listOf(
            Player(
                name = "John Smith",
                position = "Forward",
                number = 9,
                availableDates = listOf("2025-07-05", "2025-07-07"),
                photoResId = R.drawable.playerdefault
            ),
            Player(
                name = "Alex Johnson",
                position = "Midfielder",
                number = 8,
                availableDates = listOf("2025-07-06", "2025-07-07"),
                photoResId = R.drawable.playerdefault
            ),
            Player(
                name = "Emily Davis",
                position = "Defender",
                number = 4,
                availableDates = listOf("2025-07-05", "2025-07-08"),
                photoResId = R.drawable.playerdefault
            ),
            Player(
                name = "Michael Lee",
                position = "Goalkeeper",
                number = 1,
                availableDates = listOf("2025-07-06", "2025-07-08"),
                photoResId = R.drawable.playerdefault
            )
        )
    }
}
