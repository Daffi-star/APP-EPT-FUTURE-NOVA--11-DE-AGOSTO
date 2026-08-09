package com.dafi.ruwayspace.data

import androidx.room.*
import com.dafi.ruwayspace.data.Topic

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics")
    suspend fun getAllTopics(): List<Topic>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: Topic): Long

    @Update
    suspend fun updateTopic(topic: Topic)

    @Delete
    suspend fun deleteTopic(topic: Topic)
}