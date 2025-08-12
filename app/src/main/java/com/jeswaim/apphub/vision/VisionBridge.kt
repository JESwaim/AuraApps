package com.jeswaim.apphub.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class VisionBridge(private val context: Context) {
    // Placeholder for aesthetics/NSFW model availability
    val modelAvailable: Boolean by lazy {
        try {
            context.assets.openFd("models/aesthetic.tflite").close()
            true
        } catch (_: Throwable) { false }
    }

    fun ratePhoto(path: String): Pair<Int, Float> {
        if (!modelAvailable) {
            // Dummy heuristic; real implementation would run TFLite
            val score = (path.hashCode().ushr(1) % 10) + 1
            val nsfw = 0.0f
            return score to nsfw
        }
        val tflite = getInterpreter()
        val input = preprocessImage(path)
        val output = Array(1) { FloatArray(2) }
        tflite.run(input, output)
        val score = (output[0][0] * 10).toInt()
        val nsfw = output[0][1]
        return score to nsfw
    }

    private fun getInterpreter(): Interpreter {
        val assetManager = context.assets
        val model = assetManager.openFd("models/aesthetic.tflite")
        val inputStream = FileInputStream(model.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = model.startOffset
        val declaredLength = model.declaredLength
        val mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        return Interpreter(mappedByteBuffer)
    }

    private fun preprocessImage(path: String): ByteBuffer {
        val bitmap = BitmapFactory.decodeFile(path)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(224 * 224)
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)
        var pixel = 0
        for (i in 0 until 224) {
            for (j in 0 until 224) {
                val `val` = intValues[pixel++]
                byteBuffer.putFloat(((`val` shr 16) and 0xFF) / 255.0f)
                byteBuffer.putFloat(((`val` shr 8) and 0xFF) / 255.0f)
                byteBuffer.putFloat((`val` and 0xFF) / 255.0f)
            }
        }
        return byteBuffer
    }
}
