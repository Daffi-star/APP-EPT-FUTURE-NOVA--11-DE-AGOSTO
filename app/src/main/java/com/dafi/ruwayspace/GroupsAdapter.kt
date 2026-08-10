package com.dafi.ruwayspace

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dafi.ruwayspace.data.GroupItem

class GroupsAdapter(
    private val groups: List<GroupItem>,
    private val onClick: (GroupItem) -> Unit
) : RecyclerView.Adapter<GroupsAdapter.GroupViewHolder>() {

    class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGroupName: TextView = view.findViewById(R.id.tvGroupName)
        val tvGroupCode: TextView = view.findViewById(R.id.tvGroupCode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group_card, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.tvGroupName.text = group.groupName
        holder.tvGroupCode.text = "Código: ${group.roomCode}"

        holder.itemView.setOnClickListener {
            onClick(group)
        }
    }

    override fun getItemCount() = groups.size
}