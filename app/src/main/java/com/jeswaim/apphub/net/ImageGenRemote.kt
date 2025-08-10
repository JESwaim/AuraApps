package com.jeswaim.apphub.net

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ImageGenRemote {
    companion object {
        const val PREF_IMG_REMOTE_URL = "img_remote_url"
    }
    private val client = OkHttpClient()

    fun testGenerate(url: String, prompt: String = "a small blue bird on a branch"): Pair<Bitmap?, String> {
        return try {
            val payload = JSONObject(mapOf("prompt" to prompt)).toString()
            val req = Request.Builder().url(url).post(payload.toRequestBody()).build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null to "HTTP ${resp.code}"
                val body = resp.body ?: return null to "empty body"
                // naive: assume endpoint returns image bytes
                val bytes = body.bytes()
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                bmp to "ok"
            }
        } catch (t: Throwable) {
            null to (t.message ?: "error")
        }
    }
}
