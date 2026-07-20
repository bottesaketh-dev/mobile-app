package com.example.ui.viewmodel

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object GitHubModelsClient {
    private const val TAG = "GitHubModelsClient"
    private const val ENDPOINT = "https://models.inference.ai.azure.com/chat/completions"

    suspend fun getAiResponse(systemPrompt: String, userMessage: String): String = withContext(Dispatchers.IO) {
        val token = BuildConfig.GITHUB_TOKEN
        if (token.isBlank()) {
            Log.w(TAG, "GITHUB_TOKEN is not configured — AI assistant unavailable")
            return@withContext "AI Assistant is not configured. Please add GITHUB_TOKEN to your .env file."
        }

        try {
            val url = URL(ENDPOINT)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.connectTimeout = 15_000  // 15 seconds
            conn.readTimeout = 30_000     // 30 seconds for AI response generation
            conn.doOutput = true

            val jsonBody = JSONObject().apply {
                put("model", "gpt-4o-mini")
                put("temperature", 0.5)
                put("max_tokens", 1024)

                val messagesArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", userMessage)
                    })
                }
                put("messages", messagesArray)
            }

            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(jsonBody.toString())
            writer.flush()
            writer.close()

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val jsonResponse = JSONObject(response.toString())
                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() > 0) {
                    val firstChoice = choices.getJSONObject(0)
                    val message = firstChoice.getJSONObject("message")
                    return@withContext message.getString("content")
                }
                return@withContext "Received an empty response from AI service."
            } else {
                val errorBody = try {
                    conn.errorStream?.let { stream ->
                        BufferedReader(InputStreamReader(stream)).use { it.readText() }
                    } ?: "No error body"
                } catch (e: Exception) { "Could not read error stream" }

                Log.e(TAG, "HTTP $responseCode from AI endpoint: $errorBody")
                return@withContext when (responseCode) {
                    401 -> "AI authentication failed — check your GITHUB_TOKEN."
                    429 -> "AI rate limit reached — please wait a moment before asking again."
                    else -> "AI service returned HTTP $responseCode. Please try again later."
                }
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "AI request timed out: ${e.message}")
            return@withContext "Request timed out — the AI service is taking too long. Please try again."
        } catch (e: Exception) {
            Log.e(TAG, "Exception during AI call: ${e.javaClass.simpleName}: ${e.message}", e)
            return@withContext "Network error while reaching BlueFox AI Center: ${e.localizedMessage}"
        }
    }
}
