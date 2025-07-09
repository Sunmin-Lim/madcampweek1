package com.example.madcamp1

import android.view.LayoutInflater
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class CommunityListAdapter(private val players: List<Player>) :
    RecyclerView.Adapter<CommunityListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val photo: ImageView = view.findViewById(R.id.playerPhoto)
        val name: TextView = view.findViewById(R.id.playerName)
        val backNumber: TextView = view.findViewById(R.id.playerNumber)
        val position: TextView = view.findViewById(R.id.playerPosition)
        val tagContainer: ViewGroup = view.findViewById(R.id.tagContainer)
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
        holder.tagContainer.removeAllViews() // Clear existing tags

        for (tagText in player.tag) {
            val tagView = TextView(holder.itemView.context).apply {
                text = tagText
                setPadding(24, 12, 24, 12)
                background = ContextCompat.getDrawable(context, R.drawable.player_tag)
                setTextColor(Color.BLACK)
                textSize = 10f
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 0, 8, 0)
                }
            }
            holder.tagContainer.addView(tagView)
        }

//        holder.availability.text = formatAvailability(player.availableSlots)
    }

    private fun formatAvailability(slots: List<String>): String {
        return slots.joinToString(", ")
    }
}