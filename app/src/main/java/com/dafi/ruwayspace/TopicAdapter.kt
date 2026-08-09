package com.dafi.ruwayspace

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dafi.ruwayspace.data.Topic

class TopicAdapter(private val topicList: MutableList<Topic>, private val onClick: (Topic) -> Unit) :
    RecyclerView.Adapter<TopicAdapter.TopicViewHolder>() {

    fun actualizarLista(nuevaLista: List<Topic>) {
        topicList.clear()
        topicList.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    class TopicViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmoji: TextView = view.findViewById(R.id.tvTopicEmoji)
        val tvTitle: TextView = view.findViewById(R.id.tvTopicTitle)
        val tvCategory: TextView = view.findViewById(R.id.tvTopicCategory)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBarTopic)
        val tvPercentage: TextView = view.findViewById(R.id.tvTopicPercentage)
        val tvStatus: TextView = view.findViewById(R.id.tvTopicStatus)
        val ivAction: ImageView = view.findViewById(R.id.ivTopicAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_topic, parent, false)
        return TopicViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val topic = topicList[position]
        holder.tvEmoji.text = topic.emoji
        holder.tvTitle.text = topic.title
        holder.tvCategory.text = topic.category
        holder.progressBar.progress = topic.progress
        holder.tvPercentage.text = "${topic.progress}%"
        holder.tvStatus.text = topic.status

        // Estilos dinámicos según el estado
        when (topic.status) {
            "Completado" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#059669"))
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_completed)
                holder.ivAction.setImageResource(R.drawable.ic_check_circle) // Opcional un icono de check verde
            }
            "Pendiente" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#DB2777"))
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                holder.ivAction.setImageResource(R.drawable.ic_chevron_right)
            }
            else -> { // En progreso
                holder.tvStatus.setTextColor(Color.parseColor("#D97706"))
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_inprogress)
                holder.ivAction.setImageResource(R.drawable.ic_chevron_right)
            }
        }

        holder.itemView.setOnClickListener { onClick(topic) }
    }

    override fun getItemCount(): Int = topicList.size
}