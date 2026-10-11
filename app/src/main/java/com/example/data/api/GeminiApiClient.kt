package com.example.data.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "GeminiApiClient"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"
    }

    suspend fun generateContent(
        apiKey: String,
        prompt: String,
        systemInstruction: String,
        chatHistory: List<Pair<String, String>> = emptyList() // (role, text)
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("Gemini API Key missing. Please provide a valid key in Settings.")
            )
        }

        try {
            val rootJson = JSONObject()

            // System Instruction
            if (systemInstruction.isNotBlank()) {
                val sysInstObj = JSONObject()
                val sysParts = JSONArray()
                val sysPart = JSONObject().put("text", systemInstruction)
                sysParts.put(sysPart)
                sysInstObj.put("parts", sysParts)
                rootJson.put("systemInstruction", sysInstObj)
            }

            // Generation config
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.4)
            rootJson.put("generationConfig", genConfig)

            // Contents array
            val contentsArray = JSONArray()

            // Prior chat history
            for ((role, text) in chatHistory) {
                val item = JSONObject()
                item.put("role", if (role == "user") "user" else "model")
                val parts = JSONArray()
                parts.put(JSONObject().put("text", text))
                item.put("parts", parts)
                contentsArray.put(item)
            }

            // Current user message
            val currentMsg = JSONObject()
            currentMsg.put("role", "user")
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", prompt))
            currentMsg.put("parts", currentParts)
            contentsArray.put(currentMsg)

            rootJson.put("contents", contentsArray)

            val url = "$BASE_URL?key=$apiKey"
            val requestBody = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error: ${response.code} $responseBody")
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    errJson.optJSONObject("error")?.optString("message") ?: "API Error ${response.code}"
                } catch (_: Exception) {
                    "API Error ${response.code}: $responseBody"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val resJson = JSONObject(responseBody)
            val candidates = resJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text")
                    return@withContext Result.success(text)
                }
            }

            Result.failure(Exception("No content returned from Gemini API"))
        } catch (e: Exception) {
            Log.e(TAG, "Request failed", e)
            Result.failure(e)
        }
    }
}
