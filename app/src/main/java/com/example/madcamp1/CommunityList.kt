package com.example.madcamp1

import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CommunityList(
    private val players: List<Player>
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_communitylist, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewCommunityList)
        val buttonClose: ImageButton = view.findViewById(R.id.buttonClose)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = CommunityListAdapter(players)

        buttonClose.setOnClickListener {
            dismiss()
        }

        return view
    }
}
