package com.jeswaim.apphub.modules

import com.jeswaim.apphub.vision.VisionBridge
import com.jeswaim.apphub.llm.LlmBridge
import com.jeswaim.apphub.models.PhotoRatingResult

class HotOrNotModule(
    private val vision: VisionBridge,
    private val llm: LlmBridge
) {
    fun rate(path: String): PhotoRatingResult {
        val (score, _) = vision.ratePhoto(path)
        val commentary = llm.generateCommentary("This photo gets a $score/10. Say something kind and constructive.")
        return PhotoRatingResult(score, commentary)
    }
}
