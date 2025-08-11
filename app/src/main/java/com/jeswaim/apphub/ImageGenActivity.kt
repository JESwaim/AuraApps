package com.jeswaim.apphub

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.jeswaim.apphub.net.ImageGenRemote

class ImageGenActivity : AppCompatActivity() {

    private lateinit var editPrompt: EditText
    private lateinit var btnGenerate: Button
    private lateinit var imgResult: ImageView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_gen)

        editPrompt = findViewById(R.id.editPrompt)
        btnGenerate = findViewById(R.id.btnGenerate)
        imgResult = findViewById(R.id.imgResult)
        progressBar = findViewById(R.id.progressBar)

        btnGenerate.setOnClickListener {
            val prompt = editPrompt.text.toString().trim()
            if (prompt.isEmpty()) {
                Toast.makeText(this, "Please enter a prompt", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = getSharedPreferences("settings", MODE_PRIVATE)
            val url = prefs.getString(ImageGenRemote.PREF_IMG_REMOTE_URL, null)

            if (url.isNullOrBlank()) {
                Toast.makeText(this, "Set Remote Image Gen URL in Settings first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            imgResult.visibility = View.GONE

            Thread {
                val (bmp, status) = ImageGenRemote().testGenerate(url, prompt)
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (bmp != null) {
                        imgResult.setImageBitmap(bmp)
                        imgResult.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(this, "Error: $status", Toast.LENGTH_LONG).show()
                    }
                }
            }.start()
        }
    }
}
