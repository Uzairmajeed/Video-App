package com.example.videoapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ShowCategoryAdapter(
    private val items: List<String>,
    private val onItemSelected: (String) -> Unit
) : RecyclerView.Adapter<ShowCategoryAdapter.CategoryViewHolder>() {

    private var selectedPosition = 0

    inner class CategoryViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.chipTextView)

        init {
            view.setOnClickListener {
                val previous = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previous)
                notifyItemChanged(selectedPosition)
                onItemSelected(items[selectedPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_chip, parent, false)
        return CategoryViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.textView.text = items[position]
        val isSelected = position == selectedPosition

        holder.textView.setBackgroundResource(
            if (isSelected) R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected
        )
        holder.textView.setTextColor(
            ContextCompat.getColor(holder.textView.context,
                if (isSelected) android.R.color.black else android.R.color.white)
        )
    }
}
