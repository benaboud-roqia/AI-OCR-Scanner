package com.example.export

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportManager {

    private fun getExportsDir(context: Context): File {
        val dir = File(context.cacheDir, "exports")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun sanitizeFilename(title: String): String {
        val clean = title.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        return if (clean.isBlank()) "Document_OCR" else clean.take(40)
    }

    /**
     * Exports text content to a professional multi-page PDF document.
     */
    fun exportToPdf(context: Context, title: String, content: String): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 width in points (72 dpi)
            val pageHeight = 842 // A4 height in points
            val margin = 45f
            val contentWidth = pageWidth - (margin * 2)

            val titlePaint = Paint().apply {
                color = Color.rgb(30, 41, 59) // Slate 800
                textSize = 18f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val metaPaint = Paint().apply {
                color = Color.rgb(100, 116, 139) // Slate 500
                textSize = 10f
                isAntiAlias = true
            }

            val linePaint = Paint().apply {
                color = Color.rgb(226, 232, 240) // Slate 200
                strokeWidth = 1f
                isAntiAlias = true
            }

            val bodyPaint = Paint().apply {
                color = Color.rgb(15, 23, 42) // Slate 900
                textSize = 11.5f
                isAntiAlias = true
            }

            val footerPaint = Paint().apply {
                color = Color.rgb(148, 163, 184) // Slate 400
                textSize = 9f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }

            val lineHeight = 17f
            val bottomMargin = 50f
            val maxContentY = pageHeight - bottomMargin

            // Split content into wrapped lines
            val lines = mutableListOf<String>()
            val paragraphs = content.split("\n")
            for (para in paragraphs) {
                if (para.isBlank()) {
                    lines.add("")
                    continue
                }
                val words = para.split(Regex("\\s+"))
                var currentLine = StringBuilder()
                for (word in words) {
                    val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                    val measure = bodyPaint.measureText(testLine)
                    if (measure > contentWidth) {
                        if (currentLine.isNotEmpty()) {
                            lines.add(currentLine.toString())
                            currentLine = StringBuilder(word)
                        } else {
                            // Word itself is wider than content width
                            lines.add(word)
                            currentLine = StringBuilder()
                        }
                    } else {
                        currentLine = StringBuilder(testLine)
                    }
                }
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                }
            }

            var lineIndex = 0
            var pageNumber = 1

            val totalEstimatedPages = maxOf(1, ((lines.size * lineHeight) / (pageHeight - 120)).toInt() + 1)

            while (lineIndex < lines.size || pageNumber == 1) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                var y = margin

                // Header on page 1
                if (pageNumber == 1) {
                    canvas.drawText(title.ifBlank { "Document Scanné" }, margin, y + 15f, titlePaint)
                    y += 24f
                    val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.FRENCH)
                    val dateStr = "Généré le ${dateFormat.format(Date())} • AI OCR Scanner"
                    canvas.drawText(dateStr, margin, y + 10f, metaPaint)
                    y += 20f
                    canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
                    y += 22f
                } else {
                    // Running header on subsequent pages
                    canvas.drawText(title.take(35), margin, y + 10f, metaPaint)
                    y += 18f
                    canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
                    y += 20f
                }

                // Draw lines for current page
                while (lineIndex < lines.size && y + lineHeight <= maxContentY) {
                    val lineText = lines[lineIndex]
                    if (lineText.isNotEmpty()) {
                        canvas.drawText(lineText, margin, y + 11f, bodyPaint)
                    }
                    y += lineHeight
                    lineIndex++
                }

                // Footer
                val footerY = pageHeight - 25f
                canvas.drawText("Page $pageNumber", pageWidth / 2f, footerY, footerPaint)

                pdfDocument.finishPage(page)
                pageNumber++

                if (lineIndex >= lines.size) break
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(getExportsDir(context), "${sanitizeFilename(title)}_$timestamp.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Exports text content to a clean UTF-8 .txt file.
     */
    fun exportToPlainText(context: Context, title: String, content: String): File? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(getExportsDir(context), "${sanitizeFilename(title)}_$timestamp.txt")
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH)
            val header = "========================================\n" +
                    "$title\n" +
                    "Extrait le : ${dateFormat.format(Date())}\n" +
                    "Source : AI OCR Scanner\n" +
                    "========================================\n\n"
            file.writeText(header + content, Charsets.UTF_8)
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareExportedFile(context: Context, file: File, mimeType: String, title: String) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Exporter $title via...")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erreur d'exportation: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openExportedFile(context: Context, file: File, mimeType: String) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // If no dedicated viewer, fallback to share chooser
            shareExportedFile(context, file, mimeType, file.nameWithoutExtension)
        }
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Texte OCR") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copié dans le presse-papiers !", Toast.LENGTH_SHORT).show()
    }
}
