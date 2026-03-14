package com.example.collagealert

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import androidx.lifecycle.LiveData

@Dao
interface AlertDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(alert: AlertEntity)

    @Update
    suspend fun update(alert: AlertEntity)

    @Delete
    suspend fun delete(alert: AlertEntity)

    @Query("SELECT * FROM alerts WHERE isDeleted = 0 ORDER BY timestamp DESC")
    fun getAllAlerts(): LiveData<List<AlertEntity>>

    @Query("SELECT COUNT(*) FROM alerts WHERE isRead = 0 AND isDeleted = 0")
    fun getUnreadCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM alerts WHERE isDeleted = 0")
    fun getTotalCount(): LiveData<Int>

    @Query("SELECT * FROM alerts WHERE id = :id LIMIT 1")
    suspend fun getAlertById(id: String): AlertEntity?

    @Query("SELECT * FROM alerts WHERE timestamp = :timestamp AND title = :title LIMIT 1")
    suspend fun getAlertByTimestampAndTitle(timestamp: Long, title: String): AlertEntity?

    @Query("UPDATE alerts SET isDeleted = 1 WHERE id = :id")
    suspend fun softDelete(id: String)

    @Query("DELETE FROM alerts")
    suspend fun deleteAll()
}