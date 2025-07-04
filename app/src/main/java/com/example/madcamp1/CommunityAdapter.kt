package com.example.madcamp1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CommunityAdapter(
    private var players: List<Player>
) : RecyclerView.Adapter<CommunityAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.nameText)
        val positionText: TextView = view.findViewById(R.id.positionText)
        val numberText: TextView = view.findViewById(R.id.numberText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = players.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val player = players[position]
        holder.nameText.text = player.name
        holder.positionText.text = "Position: ${player.position}"
        holder.numberText.text = "Number: ${player.number}"
    }

    fun updateData(newPlayers: List<Player>) {
        players = newPlayers
        notifyDataSetChanged()
    }
}
