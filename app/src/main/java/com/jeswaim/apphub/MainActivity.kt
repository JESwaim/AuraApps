package com.jeswaim.apphub

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.jeswaim.apphub.guard.GuardService
import com.jeswaim.apphub.llm.LlmBridge
import com.jeswaim.apphub.modules.HotOrNotModule
import com.jeswaim.apphub.vision.VisionBridge

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var txtResult: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var spinnerModule: Spinner
    private lateinit var spinnerPersona: Spinner
    private lateinit var txtModelStatus: TextView
    private lateinit var btnVoice: Button
    private lateinit var btnHistory: Button
    private lateinit var btnSettings: Button

    private var capturedPhotoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        txtResult = findViewById(R.id.txtResult)
        progressBar = findViewById(R.id.progressBar)
        spinnerModule = findViewById(R.id.spinnerModule)
        spinnerPersona = findViewById(R.id.spinnerPersona)
        txtModelStatus = findViewById(R.id.txtModelStatus)
        btnVoice = findViewById(R.id.btnVoice)
        btnHistory = findViewById(R.id.btnHistory)
        btnSettings = findViewById(R.id.btnSettings)

        val modules = listOf("Hot or Not", "AI Friend", "AI Image", "AI Tutor")
        spinnerModule.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, modules)

        val personas = listOf("Mary", "Genie", "Fred")
        spinnerPersona.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, personas)

        spinnerModule.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val choice = modules[position]
                if (choice == "AI Friend") {
                    spinnerPersona.visibility = android.view.View.VISIBLE
                    btnHistory.visibility = android.view.View.VISIBLE
                } else if (choice == "AI Image") {
                    spinnerPersona.visibility = android.view.View.GONE
                    btnHistory.visibility = android.view.View.GONE
                    startActivity(Intent(this@MainActivity, ImageGenActivity::class.java))
                } else {
                    spinnerPersona.visibility = android.view.View.GONE
                    btnHistory.visibility = android.view.View.GONE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        findViewById<Button>(R.id.btnPick).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 1001)
        }
        findViewById<Button>(R.id.btnCapture).setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 1002)
        }
        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        btnHistory.setOnClickListener {
            val persona = spinnerPersona.selectedItem?.toString() ?: "Mary"
            startActivity(Intent(this, ChatHistoryActivity::class.java).putExtra("persona", persona))
        }

        updateModelStatus()
    }

    private fun updateModelStatus() {
        val ok = "✅"
        val no = "❌"
        val aesth = try { com.jeswaim.apphub.vision.VisionBridge(this).modelAvailable } catch (_: Throwable) { false }
        val hasEmb = try { (assets.list("models/embeddings") ?: emptyArray()).contains("model.onnx") } catch (_: Throwable) { false }
        val mlc = getSharedPreferences("settings", MODE_PRIVATE).getBoolean("use_mlc", false)
        txtModelStatus.text = "Aesthetics ${if(aesth) ok else no}  Embeddings ${if(hasEmb) ok else no}  MLC ${if(mlc) ok else no}"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        if (requestCode == 1001) {
            val uri: Uri = data?.data ?: return
            val path = FileUtil.getPath(this, uri) ?: return
            runHotOrNot(path)
        } else if (requestCode == 1002) {
            val bmp = data?.extras?.get("data") as? android.graphics.Bitmap ?: return
            imageView.setImageBitmap(bmp)
            val path = MediaStore.Images.Media.insertImage(contentResolver, bmp, "captured", "captured")
            val uri = Uri.parse(path)
            val real = FileUtil.getPath(this, uri) ?: return
            runHotOrNot(real)
        }
    }

    private fun runHotOrNot(path: String) {
        val guard = GuardService(this).check(path)
        if (!guard.allowed) {
            Toast.makeText(this, guard.reason ?: "Blocked", Toast.LENGTH_SHORT).show()
            return
        }
        progressBar.visibility = android.view.View.VISIBLE
        imageView.setImageBitmap(BitmapFactory.decodeFile(path))
        Thread {
            val result = HotOrNotModule(VisionBridge(this), LlmBridge(this)).rate(path)
            runOnUiThread {
                progressBar.visibility = android.view.View.GONE
                txtResult.text = "Score: ${result.score}/10\n\n${result.commentary}"
            }
        }.start()
    }
}
