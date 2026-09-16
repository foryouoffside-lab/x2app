package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiCoachService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun getCoachInsightExplanation(
        visualSpeedMs: Long,
        choiceSpeedMs: Long
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Your simple visual reflex is $visualSpeedMs ms, while your 2-choice decision reflex is $choiceSpeedMs ms (a +${choiceSpeedMs - visualSpeedMs} ms delta). In athletic and gaming neuroscience, simple sensory conduction is near-instant, but the cerebral cortex introduces cognitive latency during motor selection. Choice-reaction drills train motor cortex inhibition, which is what closes this gap."
        }

        val prompt = "You are an elite sports neuro-performance coach for 'Reaction'. The user has a visual simple reaction time of $visualSpeedMs ms and a 2-choice reaction time of $choiceSpeedMs ms. In 2 concise paragraphs, explain why choice speed trails visual speed neurologically and give 2 specific neuromuscular tips to improve choice latency. Keep the tone sharp, clinical, and encouraging."

        try {
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().put(
                    JSONObject().put("parts", JSONArray().put(
                        JSONObject().put("text", prompt)
                    ))
                ))
                put("generationConfig", JSONObject().apply {
                    put("thinkingConfig", JSONObject().apply {
                        put("thinkingLevel", "HIGH")
                    })
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-pro-preview:generateContent?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val respBody = response.body?.string() ?: ""
            if (response.isSuccessful && respBody.isNotEmpty()) {
                val json = JSONObject(respBody)
                val candidates = json.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text")
                if (!text.isNullOrBlank()) {
                    return@withContext text.trim()
                }
            }
        } catch (e: Exception) {
            // Graceful fallback to expert sports science response
        }

        "Your simple visual reflex is $visualSpeedMs ms, while your 2-choice decision reflex is $choiceSpeedMs ms (a +${choiceSpeedMs - visualSpeedMs} ms delta). In athletic and gaming neuroscience, simple sensory conduction is near-instant, but the cerebral cortex introduces cognitive latency during motor selection. Choice-reaction drills train motor cortex inhibition, which is what closes this gap."
    }

    suspend fun analyzeDrillRun(
        drillTitle: String,
        medianMs: Long,
        accuracyPercent: Int,
        consistencyMs: Long
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Offline fallback: describe what was actually measured. Never rank the run
            // against a population or claim a standard this app cannot check.
            return@withContext when {
                accuracyPercent >= 95 && consistencyMs <= 20 ->
                    "Median ${medianMs}ms at ${accuracyPercent}% accuracy, with only ${consistencyMs}ms variance between trials. Your timing held steady across the set."
                accuracyPercent < 90 ->
                    "Median ${medianMs}ms, but accuracy dropped to ${accuracyPercent}%. That usually means responding before the stimulus is confirmed - wait for the cue."
                else ->
                    "Median ${medianMs}ms at ${accuracyPercent}% accuracy, ${consistencyMs}ms variance between trials. Run it again to see whether that holds."
            }
        }

        val prompt = "You are a reaction sports coach. The user just completed a '$drillTitle' session: Median ${medianMs}ms, Accuracy ${accuracyPercent}%, Consistency variance ${consistencyMs}ms. In 1 or 2 punchy, professional sentences, evaluate their motor stability and prescribe the exact next training action."

        try {
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().put(
                    JSONObject().put("parts", JSONArray().put(
                        JSONObject().put("text", prompt)
                    ))
                ))
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val respBody = response.body?.string() ?: ""
            if (response.isSuccessful && respBody.isNotEmpty()) {
                val json = JSONObject(respBody)
                val text = json.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")
                if (!text.isNullOrBlank()) {
                    return@withContext text.trim()
                }
            }
        } catch (e: Exception) {
            // Fallback
        }

        "Median ${medianMs}ms at ${accuracyPercent}% accuracy, ${consistencyMs}ms variance between trials."
    }
}
