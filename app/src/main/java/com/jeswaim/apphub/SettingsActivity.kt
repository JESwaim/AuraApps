package com.jeswaim.apphub

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import androidx.lifecycle.lifecycleScope
import com.jeswaim.apphub.backup.CloudBackupManager
import com.jeswaim.apphub.backup.CloudBackupManagerImpl
import com.jeswaim.apphub.backup.BackupWorker
import com.jeswaim.apphub.backup.BackupScheduler
import kotlinx.coroutines.launch
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

        // Cloud Backup UI elements
        val spinnerBackupProvider = findViewById<Spinner>(R.id.spinnerBackupProvider)
        val switchAutoBackup = findViewById<Switch>(R.id.switchAutoBackup)
        val switchBackupEncryption = findViewById<Switch>(R.id.switchBackupEncryption)
        val txtBackupStatus = findViewById<TextView>(R.id.txtBackupStatus)
        val btnManualBackup = findViewById<Button>(R.id.btnManualBackup)
        val btnBackupSettings = findViewById<Button>(R.id.btnBackupSettings)

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
        
        // Initialize Cloud Backup UI
        initializeBackupUI(spinnerBackupProvider, switchAutoBackup, switchBackupEncryption, 
                          txtBackupStatus, btnManualBackup, btnBackupSettings)
    }
    
    /**
     * Initialize cloud backup UI components and functionality
     */
    private fun initializeBackupUI(
        spinnerBackupProvider: Spinner,
        switchAutoBackup: Switch,
        switchBackupEncryption: Switch,
        txtBackupStatus: TextView,
        btnManualBackup: Button,
        btnBackupSettings: Button
    ) {
        val backupManager = CloudBackupManagerImpl.getInstance()
        
        // Setup provider spinner
        val providers = arrayOf("Google Drive", "Dropbox", "OneDrive")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, providers)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBackupProvider.adapter = adapter
        
        // Load current backup configuration
        val currentConfig = backupManager.getConfig(this)
        if (currentConfig != null) {
            val providerIndex = when (currentConfig.provider) {
                CloudBackupManager.Provider.GOOGLE_DRIVE -> 0
                CloudBackupManager.Provider.DROPBOX -> 1
                CloudBackupManager.Provider.ONEDRIVE -> 2
            }
            spinnerBackupProvider.setSelection(providerIndex)
            switchAutoBackup.isChecked = currentConfig.autoBackupEnabled
            switchBackupEncryption.isChecked = currentConfig.encryptionEnabled
        } else {
            // Default settings
            spinnerBackupProvider.setSelection(0)
            switchAutoBackup.isChecked = false
            switchBackupEncryption.isChecked = true
        }
        
        // Update backup status display
        updateBackupStatus(txtBackupStatus)
        
        // Setup provider selection listener
        spinnerBackupProvider.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                saveBackupConfig(spinnerBackupProvider, switchAutoBackup, switchBackupEncryption)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Setup switch listeners
        switchAutoBackup.setOnCheckedChangeListener { _, _ ->
            saveBackupConfig(spinnerBackupProvider, switchAutoBackup, switchBackupEncryption)
        }
        
        switchBackupEncryption.setOnCheckedChangeListener { _, _ ->
            saveBackupConfig(spinnerBackupProvider, switchAutoBackup, switchBackupEncryption)
        }
        
        // Manual backup button
        btnManualBackup.setOnClickListener {
            performManualBackup()
        }
        
        // Backup settings/configuration button
        btnBackupSettings.setOnClickListener {
            showBackupConfiguration()
        }
    }
    
    /**
     * Save backup configuration to preferences
     */
    private fun saveBackupConfig(
        spinnerBackupProvider: Spinner,
        switchAutoBackup: Switch,
        switchBackupEncryption: Switch
    ) {
        val provider = when (spinnerBackupProvider.selectedItemPosition) {
            0 -> CloudBackupManager.Provider.GOOGLE_DRIVE
            1 -> CloudBackupManager.Provider.DROPBOX
            2 -> CloudBackupManager.Provider.ONEDRIVE
            else -> CloudBackupManager.Provider.GOOGLE_DRIVE
        }
        
        val config = CloudBackupManager.BackupConfig(
            provider = provider,
            encryptionEnabled = switchBackupEncryption.isChecked,
            autoBackupEnabled = switchAutoBackup.isChecked,
            backupFrequencyHours = 24,
            selectedApps = setOf("aura-core") // Default to core app data
        )
        
        val backupManager = CloudBackupManagerImpl.getInstance()
        backupManager.updateConfig(this, config)
        
        // Update backup scheduling
        BackupScheduler.scheduleAutoBackup(this, config)
    }
    
    /**
     * Update backup status display
     */
    private fun updateBackupStatus(txtBackupStatus: TextView) {
        val backupManager = CloudBackupManagerImpl.getInstance()
        val status = backupManager.getBackupStatus(this)
        
        val statusText = when {
            !status.isConfigured -> "Status: Not configured"
            status.lastBackupTimestamp != null -> {
                val lastBackup = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.US)
                    .format(java.util.Date(status.lastBackupTimestamp))
                "Last backup: $lastBackup"
            }
            else -> "Status: Configured, no backups yet"
        }
        
        txtBackupStatus.text = statusText
    }
    
    /**
     * Perform manual backup using BackupScheduler
     */
    private fun performManualBackup() {
        val config = CloudBackupManagerImpl.getInstance().getConfig(this)
        if (config == null) {
            Toast.makeText(this, "Please configure backup settings first", Toast.LENGTH_LONG).show()
            return
        }
        
        BackupScheduler.scheduleManualBackup(this, config.provider)
        Toast.makeText(this, "Backup started...", Toast.LENGTH_SHORT).show()
        
        // Update status after a short delay to show backup started
        findViewById<TextView>(R.id.txtBackupStatus).postDelayed({
            updateBackupStatus(findViewById(R.id.txtBackupStatus))
        }, 2000)
    }
    
    /**
     * Show backup configuration dialog or activity
     */
    private fun showBackupConfiguration() {
        // TODO: Implement detailed backup configuration
        // This could open a dialog or separate activity for:
        // - Provider authentication
        // - App selection for backup
        // - Backup scheduling options
        // - Restore options
        
        Toast.makeText(this, "Backup configuration coming soon", Toast.LENGTH_SHORT).show()
    }
}
