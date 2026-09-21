package com.example.ocr

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

sealed class OcrResult {
    data class Success(val text: String, val isAiPowered: Boolean) : OcrResult()
    data class Error(val message: String) : OcrResult()
}

class GeminiOcrEngine {

    companion object {
        private const val TAG = "GeminiOcrEngine"
        private const val MODEL_NAME = "gemini-3.5-flash"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun extractTextFromBitmap(bitmap: Bitmap, promptHint: String? = null): OcrResult =
        withContext(Dispatchers.IO) {
            val apiKey = BuildConfig.GEMINI_API_KEY.trim()

            // If API key is missing, warn cleanly and allow mock/fallback extraction
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                Log.w(TAG, "Gemini API key is not configured in BuildConfig.")
                return@withContext OcrResult.Error(
                    "Clé API Gemini non configurée.\n" +
                    "Veuillez définir GEMINI_API_KEY dans le panneau Secrets d'AI Studio pour l'extraction IA en direct. Vous pouvez aussi tester instantanément avec nos 'Exemples de documents' !"
                )
            }

            try {
                // Resize if too large to conserve bandwidth & speed up inference
                val resizedBitmap = resizeBitmapIfNeeded(bitmap, maxDimension = 1280)
                val base64Image = bitmapToBase64(resizedBitmap)

                val prompt = promptHint ?: "Tu es un OCR intelligent d'une extrême précision. Extrais tout le texte de ce document. " +
                        "Conserve la structure exacte (titres, paragraphes, listes, montants, numéros et alignements). " +
                        "Ne rajoute aucun commentaire introductif ni balises markdown entourant l'ensemble, renvoie directement le contenu extrait."

                // Build Gemini REST JSON payload
                val requestJson = JSONObject().apply {
                    val contentsArray = JSONArray().apply {
                        val contentObj = JSONObject().apply {
                            val partsArray = JSONArray().apply {
                                // Text prompt part
                                put(JSONObject().apply {
                                    put("text", prompt)
                                })
                                // Image part
                                put(JSONObject().apply {
                                    put("inlineData", JSONObject().apply {
                                        put("mimeType", "image/jpeg")
                                        put("data", base64Image)
                                    })
                                })
                            }
                            put("parts", partsArray)
                        }
                        put(contentObj)
                    }
                    put("contents", contentsArray)

                    val generationConfig = JSONObject().apply {
                        put("temperature", 0.1)
                    }
                    put("generationConfig", generationConfig)
                }

                val endpoint = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"
                val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(endpoint)
                    .post(body)
                    .build()

                val response = httpClient.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    Log.e(TAG, "API Error: ${response.code} - $responseBody")
                    val errorMsg = try {
                        val json = JSONObject(responseBody)
                        json.optJSONObject("error")?.optString("message") ?: "Erreur HTTP ${response.code}"
                    } catch (e: Exception) {
                        "Erreur HTTP ${response.code}"
                    }
                    return@withContext OcrResult.Error("Échec de l'extraction IA: $errorMsg")
                }

                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text", "")
                        if (text.isNotBlank()) {
                            return@withContext OcrResult.Success(text = text.trim(), isAiPowered = true)
                        }
                    }
                }

                return@withContext OcrResult.Error("Aucun texte détecté dans cette image.")
            } catch (e: Exception) {
                Log.e(TAG, "OCR exception", e)
                return@withContext OcrResult.Error("Erreur lors de l'analyse OCR: ${e.localizedMessage ?: e.message}")
            }
        }

    private fun resizeBitmapIfNeeded(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) {
            return bitmap
        }
        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (width > height) {
            newWidth = maxDimension
            newHeight = (maxDimension / ratio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (maxDimension * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
