package com.cristina.tfg_android_indoor_app.ui.userlist

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cristina.tfg_android_indoor_app.R
import com.cristina.tfg_android_indoor_app.data.model.UserListItem
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import androidx.core.graphics.toColorInt

class UserAdapter(
    private val users: List<UserListItem>,
    private val onDelete: (Int) -> Unit,
    private val onResetPassword: (Int) -> Unit,
    private val onChangeRole: (Int) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ShapeableImageView = itemView.findViewById(R.id.ivAvatar)
        val tvName: MaterialTextView = itemView.findViewById(R.id.tvName)
        val tvEmail: MaterialTextView = itemView.findViewById(R.id.tvEmail)
        val chipRole: Chip = itemView.findViewById(R.id.chipRole)
        val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)
        val btnReset: MaterialButton = itemView.findViewById(R.id.btnResetPassword)
        val btnRole: MaterialButton = itemView.findViewById(R.id.btnChangeRole)
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

        holder.chipRole.text = user.role.uppercase()
        val color = if (user.role == "admin") "#DC2626" else "#2563EB"
        holder.chipRole.chipBackgroundColor = ColorStateList.valueOf(color.toColorInt())
        holder.chipRole.chipStrokeColor = ColorStateList.valueOf(color.toColorInt())
        holder.chipRole.chipStrokeWidth = 1f

        holder.btnDelete.setOnClickListener { onDelete(user.id) }
        holder.btnReset.setOnClickListener { onResetPassword(user.id) }
        holder.btnRole.setOnClickListener { onChangeRole(user.id) }
    }

    override fun getItemCount(): Int = users.size
}