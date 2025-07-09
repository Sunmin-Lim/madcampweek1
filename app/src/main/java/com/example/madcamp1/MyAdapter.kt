package com.example.madcamp1

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(
    private val playerList: MutableList<Player>,
    private val onItemClick: (Player, Int) -> Unit
) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photo: ImageView = itemView.findViewById(R.id.playerPhoto)
        val name: TextView = itemView.findViewById(R.id.playerName)
        val backNumber: TextView = itemView.findViewById(R.id.playerNumber)
        val position: TextView = itemView.findViewById(R.id.playerPosition)
        val tagContainer: ViewGroup = itemView.findViewById(R.id.tagContainer)
//        val availability: TextView = itemView.findViewById(R.id.playerAvailability)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, index: Int) {
        val player = playerList[index]

        holder.name.text = player.name
        holder.backNumber.text = "#${player.number}"
        holder.position.text = player.position
        holder.tagContainer.removeAllViews()
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

        // 사진 표시 로직
        if (player.uri != null) {
            holder.photo.setImageURI(player.uri)
        } else {
            holder.photo.setImageResource(player.photoResId)
        }

        holder.itemView.setOnClickListener {
            onItemClick(player, index)
        }
    }

    override fun getItemCount(): Int = playerList.size

    fun updateList(newList: List<Player>) {
        playerList.clear()
        playerList.addAll(newList)
        notifyDataSetChanged()
    }


}
