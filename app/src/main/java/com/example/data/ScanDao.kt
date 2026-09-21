package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    @Query("SELECT * FROM scans ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<ScanDocument>>

    @Query("SELECT * FROM scans WHERE id = :id LIMIT 1")
    fun getScanById(id: Long): Flow<ScanDocument?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanDocument): Long

    @Update
    suspend fun updateScan(scan: ScanDocument)

    @Delete
    suspend fun deleteScan(scan: ScanDocument)

    @Query("DELETE FROM scans WHERE id = :id")
    suspend fun deleteScanById(id: Long)
}
