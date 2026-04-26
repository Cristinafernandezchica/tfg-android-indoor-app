package com.cristina.tfg_android_indoor_app.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cristina.tfg_android_indoor_app.R
import com.cristina.tfg_android_indoor_app.data.model.dto.RoomDto
import com.google.android.material.button.MaterialButton

class RoomEditAdapter(
    private var rooms: List<RoomDto>,
    private val nonEditableRooms: Set<String> = emptySet(),
    private val onEditClick: (RoomDto) -> Unit
) : RecyclerView.Adapter<RoomEditAdapter.RoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_room_edit, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(rooms[position], nonEditableRooms, onEditClick)
    }

    override fun getItemCount(): Int = rooms.size

    fun updateRooms(newRooms: List<RoomDto>) {
        rooms = newRooms
        notifyDataSetChanged()
    }

    class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRoomName: TextView = itemView.findViewById(R.id.tvRoomName)
        private val tvRoomId: TextView = itemView.findViewById(R.id.tvRoomId)
        private val tvDescriptionPreview: TextView = itemView.findViewById(R.id.tvDescriptionPreview)
        private val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEdit)

        fun bind(room: RoomDto, nonEditableRooms: Set<String>, onEditClick: (RoomDto) -> Unit) {
            val realId = room.getRealId()

            tvRoomName.text = room.name ?: "Sin nombre"
            tvRoomId.text = realId ?: "ID desconocido"
            tvDescriptionPreview.text = room.description ?: "Sin descripción"

            val isEditable = realId != null && !nonEditableRooms.contains(realId)

            btnEdit.isEnabled = isEditable
            btnEdit.alpha = if (isEditable) 1.0f else 0.5f

            if (isEditable) {
                btnEdit.setOnClickListener {
                    onEditClick(room)
                }
            } else {
                btnEdit.setOnClickListener(null)
                btnEdit.text = "No editable"
            }
        }
    }
}