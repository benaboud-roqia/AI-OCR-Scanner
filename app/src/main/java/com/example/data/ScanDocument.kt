package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scans")
data class ScanDocument(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val extractedText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null,
    val wordCount: Int = 0,
    val charCount: Int = 0,
    val category: String = "Document"
)
