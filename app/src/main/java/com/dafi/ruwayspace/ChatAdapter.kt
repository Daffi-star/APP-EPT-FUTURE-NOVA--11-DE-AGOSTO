package com.dafi.ruwayspace

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dafi.ruwayspace.data.ChatMessage
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rootLayout: LinearLayout = view as LinearLayout
        val layoutBubble: LinearLayout = view.findViewById(R.id.layoutBubble)
        val tvSender: TextView = view.findViewById(R.id.tvSender)
        val tvMessage: TextView = view.findViewById(R.id.tvMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = messages[position]
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        holder.tvSender.text = chat.senderName
        holder.tvMessage.text = chat.message

        val params = holder.layoutBubble.layoutParams as LinearLayout.LayoutParams

        if (chat.senderId == currentUserId) {
            // Mensaje propio (Alineado a la derecha, color lila suave)
            params.gravity = Gravity.END
            holder.layoutBubble.setBackgroundResource(R.drawable.bg_chat_bubble_user)
            holder.tvSender.visibility = View.GONE // Opcional: ocultar tu propio nombre para limpiar
            holder.tvMessage.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
        } else {
            // Mensaje de otra persona (Alineado a la izquierda, blanco)
            params.gravity = Gravity.START
            holder.layoutBubble.setBackgroundResource(R.drawable.bg_chat_background)
            holder.tvSender.visibility = View.VISIBLE
            holder.tvMessage.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.black))
        }
        holder.layoutBubble.layoutParams = params
    }

    override fun getItemCount() = messages.size
}