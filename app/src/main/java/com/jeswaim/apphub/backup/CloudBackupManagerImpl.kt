package com.jeswaim.apphub.backup

import android.content.Context
import com.jeswaim.apphub.backup.providers.CloudProviderAdapter
import com.jeswaim.apphub.backup.providers.GoogleDriveAdapter
import com.jeswaim.apphub.backup.providers.DropboxAdapter
import com.jeswaim.apphub.backup.providers.OneDriveAdapter
import com.jeswaim.apphub.modules.ChatStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Main implementation of CloudBackupManager.
 * Provides cloud backup functionality with support for multiple providers.
 */
class CloudBackupManagerImpl private constructor() : CloudBackupManager {
    
    companion object {
        @Volatile
        private var INSTANCE: CloudBackupManagerImpl? = null
        
        fun getInstance(): CloudBackupManagerImpl {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CloudBackupManagerImpl().also { INSTANCE = it }
            }
        }
        
        private const val PREFS_BACKUP = "backup_settings"
        private const val PREF_PROVIDER = "backup_provider"
        private const val PREF_ENCRYPTION_ENABLED = "encryption_enabled"
        private const val PREF_AUTO_BACKUP_ENABLED = "auto_backup_enabled"
        private const val PREF_BACKUP_FREQUENCY = "backup_frequency_hours"
        private const val PREF_SELECTED_APPS = "selected_apps"
        private const val PREF_LAST_BACKUP = "last_backup_timestamp"
        private const val PREF_INITIALIZED = "backup_initialized"
    }
    
    private var currentProvider: CloudProviderAdapter? = null
    
    override suspend fun initialize(context: Context, config: CloudBackupManager.BackupConfig): Boolean = withContext(Dispatchers.IO) {
        try {
            // Initialize encryption
            if (!BackupEncryption.initializeEncryption(context)) {
                return@withContext false
            }
            
            // Create provider adapter
            currentProvider = createProviderAdapter(config.provider)
            
            // Initialize the provider
            val providerInitialized = currentProvider?.initialize(context) ?: false
            
            // Save configuration
            if (providerInitialized) {
                updateConfig(context, config)
                val prefs = context.getSharedPreferences(PREFS_BACKUP, Context.MODE_PRIVATE)
                prefs.edit().putBoolean(PREF_INITIALIZED, true).apply()
            }
            
            providerInitialized
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun performBackup(context: Context): CloudBackupManager.BackupResult = withContext(Dispatchers.IO) {
        try {
            val config = getConfig(context) ?: return@withContext CloudBackupManager.BackupResult(
                success = false,
                message = "Backup not configured"
            )
            
            // Ensure provider is initialized
            if (currentProvider == null) {
                currentProvider = createProviderAdapter(config.provider)
                if (currentProvider?.initialize(context) != true) {
                    return@withContext CloudBackupManager.BackupResult(
                        success = false,
                        message = "Failed to initialize backup provider"
                    )
                }
            }
            
            // Collect backup data
            val backupData = collectBackupData(context, config)
            
            // Serialize backup data
            val serializedData = serializeBackupData(backupData)
            
            // Encrypt if enabled
            val finalData = if (config.encryptionEnabled) {
                val encrypted = BackupEncryption.encryptBackupData(context, serializedData)
                    ?: return@withContext CloudBackupManager.BackupResult(
                        success = false,
                        message = "Failed to encrypt backup data"
                    )
                BackupEncryption.serializeEncryptedData(encrypted).toByteArray()
            } else {
                serializedData
            }
            
            // Generate backup ID
            val timestamp = System.currentTimeMillis()
            val backupId = generateBackupId(timestamp)
            
            // Upload to cloud provider
            val uploaded = currentProvider?.uploadBackup(context, finalData, backupId) ?: false
            
            if (uploaded) {
                // Update last backup timestamp
                val prefs = context.getSharedPreferences(PREFS_BACKUP, Context.MODE_PRIVATE)
                prefs.edit().putLong(PREF_LAST_BACKUP, timestamp).apply()
                
                CloudBackupManager.BackupResult(
                    success = true,
                    message = "Backup completed successfully",
                    backupId = backupId,
                    timestamp = timestamp
                )
            } else {
                CloudBackupManager.BackupResult(
                    success = false,
                    message = "Failed to upload backup to cloud provider"
                )
            }
            
        } catch (e: Exception) {
            CloudBackupManager.BackupResult(
                success = false,
                message = "Backup failed: ${e.message ?: "Unknown error"}"
            )
        }
    }
    
    override suspend fun restoreFromBackup(context: Context, backupId: String?): CloudBackupManager.RestoreResult = withContext(Dispatchers.IO) {
        // TODO: Implement restore functionality
        // This is a placeholder implementation
        CloudBackupManager.RestoreResult(
            success = false,
            message = "Restore functionality not yet implemented"
        )
    }
    
    override suspend fun listBackups(context: Context): List<BackupInfo> = withContext(Dispatchers.IO) {
        try {
            currentProvider?.listBackups(context) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun deleteBackup(context: Context, backupId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            currentProvider?.deleteBackup(context, backupId) ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    override fun getConfig(context: Context): CloudBackupManager.BackupConfig? {
        val prefs = context.getSharedPreferences(PREFS_BACKUP, Context.MODE_PRIVATE)
        val providerName = prefs.getString(PREF_PROVIDER, null) ?: return null
        
        val provider = try {
            CloudBackupManager.Provider.valueOf(providerName)
        } catch (e: Exception) {
            return null
        }
        
        val selectedAppsString = prefs.getString(PREF_SELECTED_APPS, "")
        val selectedApps = if (selectedAppsString.isNullOrBlank()) {
            emptySet()
        } else {
            selectedAppsString.split(",").toSet()
        }
        
        return CloudBackupManager.BackupConfig(
            provider = provider,
            encryptionEnabled = prefs.getBoolean(PREF_ENCRYPTION_ENABLED, true),
            autoBackupEnabled = prefs.getBoolean(PREF_AUTO_BACKUP_ENABLED, false),
            backupFrequencyHours = prefs.getInt(PREF_BACKUP_FREQUENCY, 24),
            selectedApps = selectedApps
        )
    }
    
    override fun updateConfig(context: Context, config: CloudBackupManager.BackupConfig): Boolean {
        return try {
            val prefs = context.getSharedPreferences(PREFS_BACKUP, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(PREF_PROVIDER, config.provider.name)
                .putBoolean(PREF_ENCRYPTION_ENABLED, config.encryptionEnabled)
                .putBoolean(PREF_AUTO_BACKUP_ENABLED, config.autoBackupEnabled)
                .putInt(PREF_BACKUP_FREQUENCY, config.backupFrequencyHours)
                .putString(PREF_SELECTED_APPS, config.selectedApps.joinToString(","))
                .apply()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override fun isBackupDue(context: Context): Boolean {
        val config = getConfig(context) ?: return false
        if (!config.autoBackupEnabled) return false
        
        val prefs = context.getSharedPreferences(PREFS_BACKUP, Context.MODE_PRIVATE)
        val lastBackup = prefs.getLong(PREF_LAST_BACKUP, 0)
        val now = System.currentTimeMillis()
        val frequencyMillis = config.backupFrequencyHours * 60 * 60 * 1000L
        
        return (now - lastBackup) >= frequencyMillis
    }
    
    override fun getBackupStatus(context: Context): BackupStatus {
        val prefs = context.getSharedPreferences(PREFS_BACKUP, Context.MODE_PRIVATE)
        val config = getConfig(context)
        val isConfigured = config != null && prefs.getBoolean(PREF_INITIALIZED, false)
        val lastBackup = prefs.getLong(PREF_LAST_BACKUP, 0)
        
        val nextScheduled = if (isConfigured && config?.autoBackupEnabled == true) {
            lastBackup + (config.backupFrequencyHours * 60 * 60 * 1000L)
        } else null
        
        return BackupStatus(
            isConfigured = isConfigured,
            lastBackupTimestamp = if (lastBackup > 0) lastBackup else null,
            nextScheduledBackup = nextScheduled,
            isBackupInProgress = false, // TODO: Track backup progress
            lastBackupResult = null, // TODO: Load last result from preferences
            availableBackups = 0, // TODO: Cache backup count
            totalBackupSize = 0L // TODO: Calculate total backup size
        )
    }
    
    /**
     * Create appropriate provider adapter based on provider type
     */
    private fun createProviderAdapter(provider: CloudBackupManager.Provider): CloudProviderAdapter {
        return when (provider) {
            CloudBackupManager.Provider.GOOGLE_DRIVE -> GoogleDriveAdapter()
            CloudBackupManager.Provider.DROPBOX -> DropboxAdapter()
            CloudBackupManager.Provider.ONEDRIVE -> OneDriveAdapter()
        }
    }
    
    /**
     * Collect all backup data based on configuration
     */
    private fun collectBackupData(context: Context, config: CloudBackupManager.BackupConfig): BackupData {
        val deviceId = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
        
        val metadata = BackupData.BackupMetadata(
            version = "1.0",
            timestamp = System.currentTimeMillis(),
            deviceId = deviceId,
            appVersion = "1.0", // TODO: Get from BuildConfig
            encrypted = config.encryptionEnabled
        )
        
        // Collect app data
        val apps = mutableMapOf<String, BackupData.AppBackupData>()
        
        // Always include core app data
        apps["aura-core"] = BackupData.AppBackupData(
            appId = "aura-core",
            preferences = collectSharedPreferences(context, "settings")
        )
        
        // Collect memory data (chat history)
        val memories = BackupData.MemoryBackupData(
            chatHistory = collectChatHistory(context)
        )
        
        // Collect general settings
        val settings = collectSharedPreferences(context, PREFS_BACKUP)
        
        return BackupData(
            metadata = metadata,
            apps = apps,
            memories = memories,
            settings = settings
        )
    }
    
    /**
     * Collect shared preferences as map
     */
    private fun collectSharedPreferences(context: Context, prefsName: String): Map<String, Any> {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        return prefs.all.filterValues { it != null }.mapValues { it.value!! }
    }
    
    /**
     * Collect chat history data
     */
    private fun collectChatHistory(context: Context): Map<String, Any> {
        // This is a placeholder - in a real implementation, we would
        // iterate through all chat histories stored by ChatStore
        val chatPrefs = context.getSharedPreferences("chat_history", Context.MODE_PRIVATE)
        return chatPrefs.all.filterValues { it != null }.mapValues { it.value!! }
    }
    
    /**
     * Serialize backup data to JSON bytes
     */
    private fun serializeBackupData(backupData: BackupData): ByteArray {
        val json = JSONObject().apply {
            put("metadata", JSONObject().apply {
                put("version", backupData.metadata.version)
                put("timestamp", backupData.metadata.timestamp)
                put("deviceId", backupData.metadata.deviceId)
                put("appVersion", backupData.metadata.appVersion)
                put("encrypted", backupData.metadata.encrypted)
            })
            
            put("apps", JSONObject().apply {
                backupData.apps.forEach { (appId, appData) ->
                    put(appId, JSONObject().apply {
                        put("appId", appData.appId)
                        put("data", JSONObject(appData.data))
                        put("preferences", JSONObject(appData.preferences))
                        put("files", JSONObject().apply {
                            appData.files.forEachIndexed { index, file ->
                                put(index.toString(), file)
                            }
                        })
                    })
                }
            })
            
            backupData.memories?.let { memories ->
                put("memories", JSONObject().apply {
                    put("immediate", JSONObject(memories.immediate))
                    put("shared", JSONObject(memories.shared))
                    put("chatHistory", JSONObject(memories.chatHistory))
                })
            }
            
            put("settings", JSONObject(backupData.settings))
        }
        
        return json.toString().toByteArray()
    }
    
    /**
     * Generate a unique backup ID based on timestamp and provider
     */
    private fun generateBackupId(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val dateString = dateFormat.format(Date(timestamp))
        return "auraapps_backup_${dateString}_${System.nanoTime()}"
    }
}