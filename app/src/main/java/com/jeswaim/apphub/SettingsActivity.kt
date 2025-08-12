package com.jeswaim.apphub

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import java.util.concurrent.TimeUnit

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val switchMlc = findViewById<Switch>(R.id.switchMlc)
        val seekNsfw = findViewById<SeekBar>(R.id.seekNsfw)
        val txtNsfwVal = findViewById<TextView>(R.id.txtNsfwVal)

        val editImgRemoteUrl = findViewById<EditText>(R.id.editImgRemoteUrl)
        val btnTestImgRemote = findViewById<Button>(R.id.btnTestImgRemote)
        val imgTestPreview = findViewById<ImageView>(R.id.imgTestPreview)
        val txtImgTestStatus = findViewById<TextView>(R.id.txtImgTestStatus)
        val progImgTest = findViewById<ProgressBar>(R.id.progImgTest)

        val editUrlAesth = findViewById<EditText>(R.id.editUrlAesth)
        val editUrlNsfw = findViewById<EditText>(R.id.editUrlNsfw)
        val editUrlEmbed = findViewById<EditText>(R.id.editUrlEmbed)
        val editUrlSd = findViewById<EditText>(R.id.editUrlSd)
        val btnDownloadAesth = findViewById<Button>(R.id.btnDownloadAesth)
        val btnDownloadNsfw = findViewById<Button>(R.id.btnDownloadNsfw)
        val btnDownloadEmbed = findViewById<Button>(R.id.btnDownloadEmbed)
        val btnDownloadSd = findViewById<Button>(R.id.btnDownloadSd)
        val btnClearMem = findViewById<Button>(R.id.btnClearMemory)

        switchMlc.isChecked = prefs.getBoolean("use_mlc", false)
        switchMlc.setOnCheckedChangeListener { _, b -> prefs.edit().putBoolean("use_mlc", b).apply() }

        val cur = prefs.getFloat(com.jeswaim.apphub.guard.GuardService.PREF_NSFW_THRESHOLD, com.jeswaim.apphub.guard.GuardService.DEFAULT_THRESHOLD)
        seekNsfw.progress = (cur * 100).toInt()
        txtNsfwVal.text = String.format("%.2f", cur)
        seekNsfw.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val v = (progress / 100f).coerceIn(0f, 1f)
                txtNsfwVal.text = String.format("%.2f", v)
                prefs.edit().putFloat(com.jeswaim.apphub.guard.GuardService.PREF_NSFW_THRESHOLD, v).apply()
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        val savedUrl = prefs.getString(com.jeswaim.apphub.net.ImageGenRemote.PREF_IMG_REMOTE_URL, "")
        editImgRemoteUrl.setText(savedUrl)
        editImgRemoteUrl.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                prefs.edit().putString(com.jeswaim.apphub.net.ImageGenRemote.PREF_IMG_REMOTE_URL, editImgRemoteUrl.text.toString().trim()).apply()
            }
        }

        btnTestImgRemote.setOnClickListener {
            val url = prefs.getString(com.jeswaim.apphub.net.ImageGenRemote.PREF_IMG_REMOTE_URL, null)
            if (url.isNullOrBlank()) {
                Toast.makeText(this, "Set Remote Image Gen URL first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            progImgTest.visibility = android.view.View.VISIBLE
            txtImgTestStatus.text = "Testing..."
            Thread {
                val (bmp, status) = com.jeswaim.apphub.net.ImageGenRemote().testGenerate(url)
                runOnUiThread {
                    progImgTest.visibility = android.view.View.GONE
                    if (bmp != null) {
                        imgTestPreview.setImageBitmap(bmp)
                        txtImgTestStatus.text = "OK"
                    } else {
                        txtImgTestStatus.text = "Error: $status"
                    }
                }
            }.start()
        }

        fun enqueue(url: String, filename: String, subdir: String, unzip: Boolean = false) {
            if (url.isBlank()) { Toast.makeText(this, "Enter a URL", Toast.LENGTH_SHORT).show(); return }
            val data = Data.Builder()
                .putString("url", url)
                .putString("filename", filename)
                .putString("subdir", subdir)
                .putBoolean("unzip", unzip)
                .build()
            val req = OneTimeWorkRequestBuilder<com.jeswaim.apphub.models.ModelDownloadWorker>().setInputData(data).build()
            WorkManager.getInstance(this).enqueue(req)
            Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show()
        }

        btnDownloadAesth.setOnClickListener { enqueue(editUrlAesth.text.toString(), "aesthetic.tflite", "aesthetic") }
        btnDownloadNsfw.setOnClickListener { enqueue(editUrlNsfw.text.toString(), "nsfw.tflite", "nsfw") }
        btnDownloadEmbed.setOnClickListener { enqueue(editUrlEmbed.text.toString(), "model.onnx", "embeddings") }
        btnDownloadSd.setOnClickListener { enqueue(editUrlSd.text.toString(), "sd_models.zip", "sd", unzip = true) }

        btnClearMem.setOnClickListener {
            getSharedPreferences("chat_history", MODE_PRIVATE).edit().clear().apply()
            Toast.makeText(this, "Memory cleared", Toast.LENGTH_SHORT).show()
        }
    }
}
