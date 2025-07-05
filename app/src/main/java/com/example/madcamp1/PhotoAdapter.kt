package com.example.madcamp1

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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

        val params = holder.imageView.layoutParams
        params.height = (300..600).random()  // Random height between 300dp and 600dp
        holder.imageView.layoutParams = params

        // Add this click listener
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val dialogView = LayoutInflater.from(context).inflate(R.layout.photo_details, null)

            val imageView = dialogView.findViewById<ImageView>(R.id.dialogImage)
            val descriptionView = dialogView.findViewById<TextView>(R.id.dialogDescription)
            val tagsView = dialogView.findViewById<TextView>(R.id.dialogTags)

            if (photo.uri != null) {
                imageView.setImageURI(photo.uri)
            } else {
                imageView.setImageResource(photo.resourceId)
            }

            descriptionView.text = photo.description.ifEmpty { "No description provided." }
            tagsView.text = photo.tags.ifEmpty { "No tags" }

            AlertDialog.Builder(context)
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show()
        }
    }

    override fun getItemCount(): Int = photos.size
}