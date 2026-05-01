package com.cristina.tfg_android_indoor_app.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cristina.tfg_android_indoor_app.R

class VisitAdapter(
    private val visits: List<VisitItem>
) : RecyclerView.Adapter<VisitAdapter.ViewHolder>() {

    class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val tvRoomName: TextView = itemView.findViewById(R.id.tvRoomName)
        val tvRoomId: TextView = itemView.findViewById(R.id.tvRoomId)
        val tvVisitsCount: TextView = itemView.findViewById(R.id.tvVisitsCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_visit, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = visits[position]
        holder.tvRoomName.text = item.roomName
        holder.tvRoomId.text = "ID: ${item.roomId}"
        holder.tvVisitsCount.text = item.visits.toString()
    }

    override fun getItemCount(): Int = visits.size
}