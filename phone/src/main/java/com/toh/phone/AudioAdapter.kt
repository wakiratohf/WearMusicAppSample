package com.toh.phone

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import com.toh.shared.model.AudioItem

// ------------ Adapter ------------
class AudioAdapter(private val list: List<AudioItem>) : RecyclerView.Adapter<AudioAdapter.ViewHolder>() {
    inner class ViewHolder(val view: android.view.View) : RecyclerView.ViewHolder(view)


    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.view.findViewById<android.widget.TextView>(android.R.id.text1).text = item.title
        holder.view.findViewById<android.widget.TextView>(android.R.id.text2).text =
            "${item.fileName} • ${(item.size / 1024)} KB"
    }


    override fun getItemCount() = list.size
}