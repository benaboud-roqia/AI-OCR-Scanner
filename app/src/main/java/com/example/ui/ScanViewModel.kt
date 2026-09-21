package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ScanDocument
import com.example.data.ScanRepository
import com.example.export.ExportManager
import com.example.ocr.GeminiOcrEngine
import com.example.ocr.OcrResult
import com.example.ocr.SampleDocument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScanRepository
    private val ocrEngine = GeminiOcrEngine()

    val scans: StateFlow<List<ScanDocument>>

    private val _currentScan = MutableStateFlow<ScanDocument?>(null)
    val currentScan: StateFlow<ScanDocument?> = _currentScan.asStateFlow()

    private val _currentBitmap = MutableStateFlow<Bitmap?>(null)
    val currentBitmap: StateFlow<Bitmap?> = _currentBitmap.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ScanRepository(database.scanDao())
        scans = repository.allScans.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun selectScan(scan: ScanDocument) {
        _currentScan.value = scan
        // If imageUri exists and points to a file, load thumbnail
        scan.imageUri?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) {
                    _currentBitmap.value = BitmapFactory.decodeFile(file.absolutePath)
                } else {
                    _currentBitmap.value = null
                }
            } catch (e: Exception) {
                _currentBitmap.value = null
            }
        } ?: run {
            _currentBitmap.value = null
        }
    }

    fun closeActiveScan() {
        _currentScan.value = null
        _currentBitmap.value = null
    }

    /**
     * Process an image (from Camera or Gallery Picker)
     */
    fun processImageUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                _isProcessing.value = true
                _errorMessage.value = null

                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap == null) {
                    _errorMessage.value = "Impossible de charger l'image sélectionnée."
                    _isProcessing.value = false
                    return@launch
                }

                _currentBitmap.value = bitmap
                analyzeBitmap(context, bitmap, defaultTitle = "Scan ${SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date())}")
            } catch (e: Exception) {
                _errorMessage.value = "Erreur de lecture de l'image: ${e.message}"
                _isProcessing.value = false
            }
        }
    }

    /**
     * Process a direct Bitmap (e.g. from Camera TakePicturePreview)
     */
    fun processBitmap(context: Context, bitmap: Bitmap) {
        viewModelScope.launch {
            _currentBitmap.value = bitmap
            val title = "Photo ${SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date())}"
            analyzeBitmap(context, bitmap, defaultTitle = title)
        }
    }

    /**
     * Load an instant interactive sample document
     */
    fun loadSampleDocument(sample: SampleDocument) {
        viewModelScope.launch {
            _isProcessing.value = true
            _errorMessage.value = null

            val bitmap = sample.generateBitmap()
            _currentBitmap.value = bitmap

            // Save bitmap to internal cache file
            val imagePath = saveBitmapLocally(bitmap, sample.id)

            val wordCount = sample.sampleText.split(Regex("\\s+")).count { it.isNotBlank() }
            val charCount = sample.sampleText.length

            val doc = ScanDocument(
                title = sample.title,
                extractedText = sample.sampleText,
                timestamp = System.currentTimeMillis(),
                imageUri = imagePath,
                wordCount = wordCount,
                charCount = charCount,
                category = sample.category
            )

            val insertedId = repository.insertScan(doc)
            val savedDoc = doc.copy(id = insertedId)
            _currentScan.value = savedDoc
            _isProcessing.value = false
        }
    }

    private suspend fun analyzeBitmap(context: Context, bitmap: Bitmap, defaultTitle: String) {
        _isProcessing.value = true
        _errorMessage.value = null

        when (val result = ocrEngine.extractTextFromBitmap(bitmap)) {
            is OcrResult.Success -> {
                val extracted = result.text
                val wordCount = extracted.split(Regex("\\s+")).count { it.isNotBlank() }
                val charCount = extracted.length

                val localImagePath = saveBitmapLocally(bitmap, "scan_${System.currentTimeMillis()}")

                val category = detectCategory(extracted)
                val doc = ScanDocument(
                    title = defaultTitle,
                    extractedText = extracted,
                    timestamp = System.currentTimeMillis(),
                    imageUri = localImagePath,
                    wordCount = wordCount,
                    charCount = charCount,
                    category = category
                )

                val id = repository.insertScan(doc)
                _currentScan.value = doc.copy(id = id)
            }
            is OcrResult.Error -> {
                _errorMessage.value = result.message
            }
        }
        _isProcessing.value = false
    }

    private fun detectCategory(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("facture") || lower.contains("tva") || lower.contains("total ttc") || lower.contains("montant") -> "Facture"
            lower.contains("contrat") || lower.contains("article") || lower.contains("soussign") || lower.contains("accord") -> "Contrat"
            lower.contains("reçu") || lower.contains("ticket") || lower.contains("caisse") -> "Reçu"
            lower.contains("réunion") || lower.contains("compte-rendu") || lower.contains("notes") || lower.contains("ordre du jour") -> "Notes"
            else -> "Document"
        }
    }

    fun updateScanText(scanId: Long, newTitle: String, newText: String, newCategory: String) {
        viewModelScope.launch {
            val current = _currentScan.value
            if (current != null && current.id == scanId) {
                val words = newText.split(Regex("\\s+")).count { it.isNotBlank() }
                val updated = current.copy(
                    title = newTitle,
                    extractedText = newText,
                    category = newCategory,
                    wordCount = words,
                    charCount = newText.length
                )
                repository.updateScan(updated)
                _currentScan.value = updated
            }
        }
    }

    fun deleteScan(scan: ScanDocument) {
        viewModelScope.launch {
            // Delete image file if exists
            scan.imageUri?.let { path ->
                try { File(path).delete() } catch (_: Exception) {}
            }
            repository.deleteScan(scan)
            if (_currentScan.value?.id == scan.id) {
                _currentScan.value = null
                _currentBitmap.value = null
            }
        }
    }

    fun exportAsPdf(context: Context, scan: ScanDocument) {
        val file = ExportManager.exportToPdf(context, scan.title, scan.extractedText)
        if (file != null) {
            ExportManager.shareExportedFile(context, file, "application/pdf", scan.title)
        } else {
            Toast.makeText(context, "Erreur lors de la création du PDF.", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportAsTxt(context: Context, scan: ScanDocument) {
        val file = ExportManager.exportToPlainText(context, scan.title, scan.extractedText)
        if (file != null) {
            ExportManager.shareExportedFile(context, file, "text/plain", scan.title)
        } else {
            Toast.makeText(context, "Erreur lors de la création du fichier TXT.", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(context: Context, scan: ScanDocument) {
        ExportManager.copyToClipboard(context, scan.extractedText, scan.title)
    }

    private fun saveBitmapLocally(bitmap: Bitmap, prefix: String): String? {
        return try {
            val app = getApplication<Application>()
            val dir = File(app.filesDir, "scans")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "${prefix}_${System.currentTimeMillis()}.jpg")
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
            fos.flush()
            fos.close()
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
