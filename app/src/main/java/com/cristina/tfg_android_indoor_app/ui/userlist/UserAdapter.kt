package com.cristina.tfg_android_indoor_app.ui.userlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cristina.tfg_android_indoor_app.R
import com.cristina.tfg_android_indoor_app.data.model.UserListItem

class UserAdapter(
    private val users: List<UserListItem>,
    private val onDelete: (Int) -> Unit,
    private val onResetPassword: (Int) -> Unit,
    private val onChangeRole: (Int) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvEmail: TextView = view.findViewById(R.id.tvEmail)
        val tvRole: TextView = view.findViewById(R.id.tvRole)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
        val btnReset: Button = view.findViewById(R.id.btnResetPassword)
        val btnRole: Button = view.findViewById(R.id.btnChangeRole)
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
        holder.tvRole.text = user.role

        holder.btnDelete.setOnClickListener { onDelete(user.id) }
        holder.btnReset.setOnClickListener { onResetPassword(user.id) }
        holder.btnRole.setOnClickListener { onChangeRole(user.id) }
    }

    override fun getItemCount(): Int = users.size
}
