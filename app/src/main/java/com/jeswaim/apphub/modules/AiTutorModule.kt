package com.jeswaim.apphub.modules

import com.jeswaim.apphub.llm.LlmBridge

class AiTutorModule(private val llmBridge: LlmBridge) {

    fun answer(question: String): String {
        val prompt = "As an AI Tutor, answer the following question: $question"
        return llmBridge.generateCommentary(prompt)
    }
}
