package com.example.madcamp1

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment(R.layout.fragment_home) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = MyAdapter(getPlayerList())
    }

    private fun getPlayerList(): List<Player> {
        return listOf(
            Player("Son Heung-min", "Forward", 7),
            Player("Kim Min-jae", "Defender", 4),
            Player("Lee Kang-in", "Midfielder", 18),
            Player("Cho Gue-sung", "Forward", 9)
        )
    }
}