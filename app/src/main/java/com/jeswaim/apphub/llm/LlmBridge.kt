package com.jeswaim.apphub.llm

import android.content.Context

class LlmBridge(private val context: Context) {
    fun generateCommentary(prompt: String): String {
        // Placeholder: replace with MLC integration
        return "Thanks for the photo! Based on aesthetics and composition, here's a friendly take: $prompt"
    }
}
