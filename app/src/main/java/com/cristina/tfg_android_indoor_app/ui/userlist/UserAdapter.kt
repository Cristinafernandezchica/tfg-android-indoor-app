package com.cristina.tfg_android_indoor_app.ui.userlist

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cristina.tfg_android_indoor_app.R
import com.cristina.tfg_android_indoor_app.data.model.UserListItem
import com.google.android.material.chip.Chip
import androidx.core.graphics.toColorInt

class UserAdapter(
    private val users: List<UserListItem>,
    private val onDelete: (Int) -> Unit,
    private val onResetPassword: (Int) -> Unit,
    private val onChangeRole: (Int) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvEmail: TextView = view.findViewById(R.id.tvEmail)
        val chipRole: Chip = view.findViewById(R.id.chipRole)

        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        val btnReset: ImageButton = view.findViewById(R.id.btnResetPassword)
        val btnRole: ImageButton = view.findViewById(R.id.btnChangeRole)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]

        holder.tvName.text = user.name
        holder.tvEmail.text = user.email

        holder.chipRole.text = user.role
        val color = if (user.role == "admin") "#DC2626" else "#2563EB"
        holder.chipRole.chipBackgroundColor = ColorStateList.valueOf(color.toColorInt())

        holder.btnDelete.setOnClickListener { onDelete(user.id) }
        holder.btnReset.setOnClickListener { onResetPassword(user.id) }
        holder.btnRole.setOnClickListener { onChangeRole(user.id) }
    }

    override fun getItemCount(): Int = users.size
}
