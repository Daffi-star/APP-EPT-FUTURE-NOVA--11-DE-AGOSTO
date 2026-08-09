package com.dafi.ruwayspace

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dafi.ruwayspace.data.ExamEntity

class ExamAdapter(
    private val examList: List<ExamEntity>,
    private val onClick: (ExamEntity) -> Unit
) : RecyclerView.Adapter<ExamAdapter.ExamViewHolder>() {

    class ExamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEmoji: TextView = itemView.findViewById(R.id.tvExamEmoji)
        val tvTitle: TextView = itemView.findViewById(R.id.tvExamTitle)
        val tvDetails: TextView = itemView.findViewById(R.id.tvExamDetails)
        val tvDaysRemaining: TextView = itemView.findViewById(R.id.tvDaysRemaining)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBarExam)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exam, parent, false)
        return ExamViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExamViewHolder, position: Int) {
        val exam = examList[position]

        holder.tvTitle.text = exam.title
        holder.tvDetails.text = "${exam.classroom} • ${exam.date}"
        holder.tvDaysRemaining.text = exam.daysRemaining

        // Asignar el emoji elegido por el usuario
        holder.tvEmoji.text = exam.iconEmoji

        // Lógica de la barra de progreso (Urgencia)
        val daysInt = exam.daysRemaining.filter { it.isDigit() }.toIntOrNull() ?: 0
        val maxDays = 30 // Rango de anticipación: 30 días

        // Calculamos: Si faltan 30 días = 0%, si faltan 0 días = 100%
        val progress = when {
            daysInt <= 0 -> 100 // Ya es hoy o pasó
            daysInt >= maxDays -> 0 // Falta mucho, barra vacía
            else -> ((maxDays - daysInt) * 100) / maxDays
        }

        holder.progressBar.progress = progress

        // Cambiar el color de la barra según el tiempo restante
        val barColor = when {
            daysInt <= 2 -> Color.parseColor("#E53935") // Rojo (Muy poco tiempo / Urgente)
            daysInt <= 7 -> Color.parseColor("#FB8C00") // Naranja (Tiempo moderado)
            else -> Color.parseColor("#43A047")         // Verde (Falta bastante)
        }
        holder.progressBar.progressTintList = ColorStateList.valueOf(barColor)

        // Aplicar el color pastel de fondo seleccionado dinámicamente
        try {
            holder.tvEmoji.backgroundTintList = ColorStateList.valueOf(Color.parseColor(exam.cardColor))
        } catch (e: Exception) {
            holder.tvEmoji.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E3F2FD"))
        }

        holder.itemView.setOnClickListener { onClick(exam) }
    }

    override fun getItemCount(): Int = examList.size
}