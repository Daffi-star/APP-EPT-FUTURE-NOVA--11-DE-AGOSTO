package com.dafi.ruwayspace.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dafi.ruwayspace.data.ExamEntity

@Dao
interface ExamDao {
    @Insert
    suspend fun insertExam(exam: ExamEntity)

    @Query("SELECT * FROM exams_table")
    suspend fun getAllExams(): List<ExamEntity>

    @Update
    suspend fun updateExam(exam: ExamEntity) // <--- Añade esto

    @Delete
    suspend fun deleteExam(exam: ExamEntity)
}