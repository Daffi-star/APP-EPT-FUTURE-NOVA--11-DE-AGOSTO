package com.dafi.ruwayspace

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ChatMessageAdapter(private val listaMensajes: List<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_BOT = 2
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
    }

    class BotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
    }

    override fun getItemViewType(position: Int): Int {
        return if (listaMensajes[position].isFromUser) {
            VIEW_TYPE_USER
        } else {
            VIEW_TYPE_BOT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_USER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_user, parent, false)
            UserViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_bot, parent, false)
            BotViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mensaje = listaMensajes[position]
        when (holder) {
            is UserViewHolder -> holder.tvMessage.text = mensaje.text
            is BotViewHolder -> holder.tvMessage.text = mensaje.text
        }
    }

    override fun getItemCount(): Int = listaMensajes.size
}