package com.jeswaim.apphub.models

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

class ModelDownloadWorker(private val ctx: Context, params: WorkerParameters): CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val url = inputData.getString("url") ?: return Result.failure()
        val filename = inputData.getString("filename") ?: return Result.failure()
        val subdir = inputData.getString("subdir") ?: "misc"
        val unzip = inputData.getBoolean("unzip", false)

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

        if (unzip) {
            try {
                unzip(outFile, dir)
                outFile.delete() // Clean up the zip file
            } catch (e: Exception) {
                // If unzip fails, we should probably fail the worker
                return Result.failure()
            }
        }

        return Result.success()
    }

    @Throws(Exception::class)
    private fun unzip(zipFile: File, targetDirectory: File) {
        ZipInputStream(zipFile.inputStream()).use { zipInputStream ->
            var entry = zipInputStream.nextEntry
            while (entry != null) {
                val newFile = File(targetDirectory, entry.name)
                if (entry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    // Ensure parent directory exists
                    File(newFile.parent!!).mkdirs()
                    FileOutputStream(newFile).use { fos ->
                        zipInputStream.copyTo(fos)
                    }
                }
                zipInputStream.closeEntry()
                entry = zipInputStream.nextEntry
            }
        }
    }
}
