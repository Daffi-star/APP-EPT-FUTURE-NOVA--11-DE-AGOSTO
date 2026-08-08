package com.dafi.ruwayspace

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.dafi.ruwayspace.data.CourseEntity

class CourseAdapter(
    private val courseList: List<CourseEntity>,
    private val onClick: (CourseEntity) -> Unit
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvCourseTitle)
        val tvDesc: TextView = itemView.findViewById(R.id.tvCourseDesc)
        val tvProgressText: TextView = itemView.findViewById(R.id.tvProgressText)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBarCourse)
        val ivIconBg: ImageView = itemView.findViewById(R.id.ivCourseIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courseList[position]
        holder.tvTitle.text = course.title
        holder.tvDesc.text = course.description
        holder.tvProgressText.text = "${course.progress}%"
        holder.progressBar.progress = course.progress

        // Tonos pasteles y suaves estilo Notion
        val colorHex = when (course.difficulty.lowercase().trim()) {
            "fácil", "facil" -> "#E8F5E9"     // Verde suave
            "medio", "moderado" -> "#FFF3E0"  // Naranja suave
            "difícil", "dificil" -> "#FFEBEE" // Rojo suave
            else -> "#E3F2FD"                 // Azul suave por defecto
        }
        holder.ivIconBg.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorHex))

        // Al hacer clic en la tarjeta -> EDITAR
        holder.itemView.setOnClickListener { onClick(course) }

        // Al hacer clic en el icono -> ABRIR PDF
        holder.ivIconBg.setOnClickListener {
            if (course.pdfUris.isNotEmpty()) {
                (holder.itemView.context as? CoursesActivity)?.abrirPdf(course.pdfUris.first())
            } else {
                Toast.makeText(holder.itemView.context, "No hay archivos adjuntos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = courseList.size
}