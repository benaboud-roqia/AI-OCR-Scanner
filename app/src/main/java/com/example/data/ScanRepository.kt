package com.example.data

import kotlinx.coroutines.flow.Flow

class ScanRepository(private val scanDao: ScanDao) {
    val allScans: Flow<List<ScanDocument>> = scanDao.getAllScans()

    fun getScanById(id: Long): Flow<ScanDocument?> = scanDao.getScanById(id)

    suspend fun insertScan(scan: ScanDocument): Long = scanDao.insertScan(scan)

    suspend fun updateScan(scan: ScanDocument) = scanDao.updateScan(scan)

    suspend fun deleteScan(scan: ScanDocument) = scanDao.deleteScan(scan)

    suspend fun deleteScanById(id: Long) = scanDao.deleteScanById(id)
}
