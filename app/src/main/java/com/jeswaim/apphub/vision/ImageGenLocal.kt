package com.jeswaim.apphub.vision

import android.content.Context
import android.graphics.Bitmap
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.io.Closeable

class ImageGenLocal(private val context: Context) : Closeable {

    companion object {
        private const val MODEL_PATH = "models/sd"
        private const val TEXT_ENCODER_MODEL = "text_encoder/model.onnx"
        private const val UNET_MODEL = "unet/model.onnx"
        private const val VAE_DECODER_MODEL = "vae_decoder/model.onnx"
    }

    private var ortEnv: OrtEnvironment? = null
    private var textEncoderSession: OrtSession? = null
    private var unetSession: OrtSession? = null
    private var vaeDecoderSession: OrtSession? = null

    fun modelExists(): Boolean {
        return try {
            val assetManager = context.assets
            val unetFiles = assetManager.list("$MODEL_PATH/unet")
            val textEncoderFiles = assetManager.list("$MODEL_PATH/text_encoder")
            val vaeDecoderFiles = assetManager.list("$MODEL_PATH/vae_decoder")

            (unetFiles?.contains("model.onnx") == true) &&
            (textEncoderFiles?.contains("model.onnx") == true) &&
            (vaeDecoderFiles?.contains("model.onnx") == true)
        } catch (e: Exception) {
            false
        }
    }

    fun load() {
        if (ortEnv != null) return // Already loaded

        ortEnv = OrtEnvironment.getEnvironment()

        val sessionOptions = OrtSession.SessionOptions()
        // For performance, you can configure options here, e.g.:
        // sessionOptions.setExecutionMode(OrtSession.ExecutionMode.PARALLEL)
        // sessionOptions.setInterOpNumThreads(2)
        // sessionOptions.setIntraOpNumThreads(4)

        textEncoderSession = createSession(TEXT_ENCODER_MODEL, sessionOptions)
        unetSession = createSession(UNET_MODEL, sessionOptions)
        vaeDecoderSession = createSession(VAE_DECODER_MODEL, sessionOptions)
    }

    private fun createSession(modelName: String, options: OrtSession.SessionOptions): OrtSession {
        val modelBytes = context.assets.open("$MODEL_PATH/$modelName").readBytes()
        return ortEnv!!.createSession(modelBytes, options)
    }

    fun generate(prompt: String): Bitmap? {
        // TODO: Implement Stable Diffusion inference pipeline
        if (textEncoderSession == null || unetSession == null || vaeDecoderSession == null) {
            throw IllegalStateException("Models are not loaded. Call load() first.")
        }
        // 1. Tokenize prompt
        // 2. Run text encoder
        // 3. Run UNet loop
        // 4. Run VAE decoder
        // 5. Convert to Bitmap
        return null
    }

    override fun close() {
        textEncoderSession?.close()
        unetSession?.close()
        vaeDecoderSession?.close()
        ortEnv?.close() // This closes the environment for all sessions
    }
}
