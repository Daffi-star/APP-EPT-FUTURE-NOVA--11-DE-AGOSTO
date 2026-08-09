package com.dafi.ruwayspace.data

import androidx.room.TypeConverter
import com.dafi.ruwayspace.model.SubTask
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class Converters {
    // --- Lo que ya tenías para List<String> ---
    @TypeConverter
    fun fromString(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return list.joinToString(separator = ",")
    }

    // --- NUEVO: Lo que necesitas para las subtareas de los Temas ---
    @TypeConverter
    fun fromSubtaskList(value: MutableList<SubTask>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toSubtaskList(value: String): MutableList<SubTask> {
        val type = object : TypeToken<MutableList<SubTask>>() {}.type
        return Gson().fromJson(value, type) ?: mutableListOf()
    }
}