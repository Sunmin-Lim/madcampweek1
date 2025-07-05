package com.example.madcamp1

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private val playerList: MutableList<Player>) :
    RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photo: ImageView = itemView.findViewById(R.id.playerPhoto)
        val name: TextView = itemView.findViewById(R.id.playerName)
        val positionNumber: TextView = itemView.findViewById(R.id.playerPositionNumber)
        val availability: TextView = itemView.findViewById(R.id.playerAvailability)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val player = playerList[position]

        holder.name.text = player.name
        holder.positionNumber.text = "${player.position} #${player.number}"
        holder.availability.text = player.availableSlots.joinToString(", ")

        // 사진 표시 로직
        if (player.uri != null) {
            holder.photo.setImageURI(player.uri)
        } else {
            holder.photo.setImageResource(player.photoResId)
        }
    }

    override fun getItemCount(): Int = playerList.size

    fun updateList(newList: List<Player>) {
        playerList.clear()
        playerList.addAll(newList)
        notifyDataSetChanged()
    }
}
