package com.example.fittrack.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ChatMsg(val fromUser: Boolean, val text: String)

/**
 * Minimal Gemini REST client (no extra dependencies). To swap providers later, only this file changes.
 * Model names change often: if the first one isn't found (HTTP 404) the next one is tried.
 */
object AiClient {
    private val MODELS = listOf("gemini-3.8-flash", "gemini-2.5-flash")

    suspend fun chat(apiKey: String, system: String, history: List<ChatMsg>): Result<String> =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) return@withContext Result.failure(IllegalStateException("Add your Gemini API key first."))
            var lastError = "Something went wrong. Please try again."
            for (model in MODELS) {
                try {
                    val contents = JSONArray()
                    history.forEach { m ->
                        contents.put(
                            JSONObject()
                                .put("role", if (m.fromUser) "user" else "model")
                                .put("parts", JSONArray().put(JSONObject().put("text", m.text)))
                        )
                    }
                    val body = JSONObject()
                        .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", system))))
                        .put("contents", contents)
                        .put("generationConfig", JSONObject().put("temperature", 0.7).put("maxOutputTokens", 2048))

                    val conn = (URL("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent")
                        .openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        connectTimeout = 15_000
                        readTimeout = 45_000
                        doOutput = true
                        setRequestProperty("Content-Type", "application/json")
                        setRequestProperty("x-goog-api-key", apiKey)
                    }
                    conn.outputStream.use { it.write(body.toString().toByteArray()) }
                    val code = conn.responseCode
                    val raw = (if (code in 200..299) conn.inputStream else conn.errorStream)
                        ?.bufferedReader()?.use { it.readText() } ?: ""

                    if (code in 200..299) {
                        val parts = JSONObject(raw).getJSONArray("candidates").getJSONObject(0)
                            .getJSONObject("content").getJSONArray("parts")
                        val sb = StringBuilder()
                        for (i in 0 until parts.length()) sb.append(parts.getJSONObject(i).optString("text", ""))
                        val reply = clean(sb.toString())
                        return@withContext if (reply.isBlank()) Result.failure(IllegalStateException("The coach sent an empty reply. Try rephrasing."))
                        else Result.success(reply)
                    }

                    val msg = runCatching { JSONObject(raw).getJSONObject("error").getString("message") }.getOrDefault("HTTP $code")
                    lastError = when {
                        code == 429 -> "Rate limit reached. Wait a moment and try again."
                        code == 403 || msg.contains("API key", ignoreCase = true) -> "That API key was rejected. Check it and try again."
                        else -> msg
                    }
                    if (code != 404) break // only a missing model is worth retrying with the next one
                } catch (e: Exception) {
                    lastError = "Couldn't reach the coach. Check your internet connection."
                    break
                }
            }
            Result.failure(IllegalStateException(lastError))
        }

    /** The UI shows plain text, so turn common markdown into simple bullets. */
    private fun clean(s: String): String =
        s.replace("**", "").replace("##", "").replace(Regex("(?m)^\\s*[*-]\\s+"), "• ").trim()
}
