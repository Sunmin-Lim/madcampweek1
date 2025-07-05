package com.example.madcamp1

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyAdapter
    private lateinit var playerList: List<Player>

    private val sharedViewModel: SharedViewModel by activityViewModels()

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
//        playerList = getPlayers()
//        adapter = MyAdapter(playerList)
//        recyclerView.adapter = adapter

        // playerList 초기화 후 ViewModel에 전달
        if (sharedViewModel.players.value.isNullOrEmpty()) {
            sharedViewModel.setPlayers(getPlayers())  // 기존에 하던 getPlayers() 함수 재사용
        }

        // FAB 참조 및 클릭 리스너
        val fab = view.findViewById<FloatingActionButton>(R.id.addProfileButton)
        fab.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_player, null)

            val editName = dialogView.findViewById<EditText>(R.id.editPlayerName)
            val editPosition = dialogView.findViewById<EditText>(R.id.editPlayerPosition)
            val editNumber = dialogView.findViewById<EditText>(R.id.editPlayerNumber)

            AlertDialog.Builder(requireContext())
                .setTitle("선수 추가")
                .setView(dialogView)
                .setPositiveButton("추가") { _, _ ->
                    val name = editName.text.toString().trim()
                    val position = editPosition.text.toString().trim()
                    val numberText = editNumber.text.toString().trim()

                    if (name.isNotEmpty() && position.isNotEmpty() && numberText.isNotEmpty()) {
                        val number = numberText.toIntOrNull() ?: 0
                        val currentList = sharedViewModel.players.value ?: emptyList()

                        val newPlayer = Player(
                            name = name,
                            position = position,
                            number = number,
                            availableSlots = listOf("2025-07-09 08:00"), // 예시
                            photoResId = R.drawable.playerdefault
                        )
                        sharedViewModel.setPlayers(currentList + newPlayer)
                    } else {
                        Toast.makeText(requireContext(), "모든 항목을 입력하세요", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("취소", null)
                .show()
        }

        // ViewModel의 데이터로 recylerview 갱신
        sharedViewModel.players.observe(viewLifecycleOwner) { players ->
            adapter = MyAdapter(players)
            recyclerView.adapter = adapter
        }

        return view
    }

    private fun getPlayers(): List<Player> {
        return listOf(
            Player(
                name = "John Smith",
                position = "Forward",
                number = 9,
                availableSlots = listOf("2025-07-05 08:00", "2025-07-07 09:00"),
                photoResId = R.drawable.playerdefault
            ),
            Player(
                name = "Alex Johnson",
                position = "Midfielder",
                number = 8,
                availableSlots = listOf("2025-07-06 08:00", "2025-07-07 08:00"),
                photoResId = R.drawable.playerdefault
            ),
            Player(
                name = "Emily Davis",
                position = "Defender",
                number = 4,
                availableSlots = listOf("2025-07-05 08:00", "2025-07-08 09:00"),
                photoResId = R.drawable.playerdefault
            ),
            Player(
                name = "Michael Lee",
                position = "Goalkeeper",
                number = 1,
                availableSlots = listOf("2025-07-06 09:00", "2025-07-08 09:00"),
                photoResId = R.drawable.playerdefault
            )
        )
    }
}
