package com.example.madcamp1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CommunityListAdapter(private val players: List<Player>) :
    RecyclerView.Adapter<CommunityListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val photo: ImageView = view.findViewById(R.id.playerPhoto)
        val name: TextView = view.findViewById(R.id.playerName)
        val backNumber: TextView = view.findViewById(R.id.playerNumber)
        val position: TextView = view.findViewById(R.id.playerPosition)
        val tag: TextView = view.findViewById(R.id.playerTag)
//        val availability: TextView = view.findViewById(R.id.playerAvailability)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = players.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val player = players[position]
        holder.name.text = player.name
        holder.backNumber.text = "#${player.number}"
        holder.position.text = player.position
        holder.photo.setImageResource(player.photoResId)
        holder.tag.text = "tag: " + player.tag.joinToString(", ")
//        holder.availability.text = formatAvailability(player.availableSlots)
    }

    private fun formatAvailability(slots: List<String>): String {
        return slots.joinToString(", ")
    }
}