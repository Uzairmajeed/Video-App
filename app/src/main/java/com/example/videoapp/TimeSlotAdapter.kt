package com.example.videoapp

import MediaItemList
import TimePeriod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TimeSlotAdapter(
    private val timePeriods: List<TimePeriod>,
    private val listener: VideoSelectionListenerForHome,
    private var selectedItem: MediaItemList? // ✅ Not videoUrl
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    fun updateSelectedItem(item: MediaItemList) {
        selectedItem = item
        notifyDataSetChanged()
    }

    class TimeSlotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeTextView: TextView = view.findViewById(R.id.timeTextView)
        val mediaRecycler: RecyclerView = view.findViewById(R.id.mediaItemRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun getItemCount(): Int = timePeriods.size

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val period = timePeriods[position]
        holder.timeTextView.text = period.time

        val adapter = MediaItemAdapter(period.shows, listener, selectedItem,period.time)
        holder.mediaRecycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            this.adapter = adapter
            setHasFixedSize(true)
        }
    }
}

