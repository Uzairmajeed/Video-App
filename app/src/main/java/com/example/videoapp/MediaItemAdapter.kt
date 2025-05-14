package com.example.videoapp

import MediaItem
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MediaItemAdapter(private val mediaItems: List<MediaItem>) :
    RecyclerView.Adapter<MediaItemAdapter.MediaViewHolder>() {

    class MediaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.titleTextView)
        val channel: TextView = view.findViewById(R.id.channelTextView)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        val timeRemaining: TextView = view.findViewById(R.id.timeRemainingTextView)
        val logo: ImageView = view.findViewById(R.id.logoImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun getItemCount(): Int = mediaItems.size

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val item = mediaItems[position]
        holder.title.text = item.title
        holder.channel.text = item.channel
        holder.timeRemaining.text = item.timeRemaining
        holder.progressBar.progress = (item.progress * 100).toInt()

        // TODO: Load channelLogo here if needed using Glide/Picasso
        holder.logo.setImageResource(R.drawable.ic_launcher_foreground)
    }
}
