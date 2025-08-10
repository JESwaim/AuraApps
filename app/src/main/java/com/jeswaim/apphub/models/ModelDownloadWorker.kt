package com.jeswaim.apphub.models

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class ModelDownloadWorker(private val ctx: Context, params: WorkerParameters): CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val url = inputData.getString("url") ?: return Result.failure()
        val filename = inputData.getString("filename") ?: return Result.failure()
        val subdir = inputData.getString("subdir") ?: "misc"

        val client = OkHttpClient()
        val req = Request.Builder().url(url).build()
        val resp = client.newCall(req).execute()
        if (!resp.isSuccessful) return Result.retry()
        val body = resp.body ?: return Result.failure()

        val dir = File(ctx.filesDir, "models/$subdir")
        if (!dir.exists()) dir.mkdirs()
        val outFile = File(dir, filename)
        FileOutputStream(outFile).use { out ->
            body.byteStream().use { inp -> inp.copyTo(out) }
        }
        return Result.success()
    }
}
