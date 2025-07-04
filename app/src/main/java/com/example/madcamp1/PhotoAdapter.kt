package com.example.madcamp1

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class PhotoAdapter(private val photos: List<Photo>) :
    RecyclerView.Adapter<PhotoAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val photo = photos[position]

        if (photo.uri != null) {
            holder.imageView.setImageURI(photo.uri)
        } else {
            holder.imageView.setImageResource(photo.resourceId)
        }

        // Add this click listener
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            AlertDialog.Builder(context)
                .setTitle("Photo Details")
                .setMessage("Date: ${photo.date}\nDescription: ${photo.description}")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    override fun getItemCount(): Int = photos.size
}