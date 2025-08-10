package com.jeswaim.apphub.guard

import android.content.Context

data class GuardResult(val allowed: Boolean, val reason: String? = null, val nsfwScore: Float = 0f)

class GuardService(private val context: Context) {
    companion object {
        const val PREF_NSFW_THRESHOLD = "nsfw_threshold"
        const val DEFAULT_THRESHOLD = 0.35f
    }

    fun check(path: String): GuardResult {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val thresh = prefs.getFloat(PREF_NSFW_THRESHOLD, DEFAULT_THRESHOLD)
        // Placeholder nsfwScore; real impl would run NSFW model
        val nsfwScore = 0.0f
        if (nsfwScore >= thresh) return GuardResult(false, "Blocked by NSFW guard", nsfwScore)
        return GuardResult(true, null, nsfwScore)
    }
}
