package com.example.videoapp

import MediaItem
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MediaItemAdapter(
    private val mediaItems: List<MediaItem>,
    private val listener: VideoSelectionListener,
    private var selectedVideoUrl: String? // <== ADD THIS
) : RecyclerView.Adapter<MediaItemAdapter.MediaViewHolder>() {

    fun updateSelectedVideoUrl(url: String) {
        selectedVideoUrl = url
        notifyDataSetChanged()
    }

    class MediaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.titleTextView)
        val channel: TextView = view.findViewById(R.id.channelTextView)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        val timeRemaining: TextView = view.findViewById(R.id.timeRemainingTextView)
        val container: View = view.findViewById(R.id.mediaItemContainer) // <- Root layout in item_media.xml
        val thumbnail: View = view.findViewById(R.id.thumbnailView) // <- ImageView or shape thumbnail background
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

        // Highlight selected item
        if (item.videoUrl == selectedVideoUrl) {
            holder.container.setBackgroundResource(R.drawable.selected_item_background)
            holder.thumbnail.setBackgroundColor(Color.RED) // Or use red drawable if needed
        } else {
            holder.container.setBackgroundResource(R.drawable.default_item_background)
            holder.thumbnail.setBackgroundColor(Color.DKGRAY)
        }

        holder.itemView.setOnClickListener {
            listener.onVideoSelected(item.videoUrl)
        }
    }
}

