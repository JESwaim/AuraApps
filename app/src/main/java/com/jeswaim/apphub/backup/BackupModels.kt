package com.jeswaim.apphub.backup

/**
 * Information about a backup stored in the cloud
 */
data class BackupInfo(
    val id: String,
    val timestamp: Long,
    val size: Long,
    val version: String = "1.0",
    val provider: CloudBackupManager.Provider,
    val encrypted: Boolean = true,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Current backup status information
 */
data class BackupStatus(
    val isConfigured: Boolean = false,
    val lastBackupTimestamp: Long? = null,
    val nextScheduledBackup: Long? = null,
    val isBackupInProgress: Boolean = false,
    val lastBackupResult: CloudBackupManager.BackupResult? = null,
    val availableBackups: Int = 0,
    val totalBackupSize: Long = 0L
)

/**
 * Structure representing the backup data format
 */
data class BackupData(
    val metadata: BackupMetadata,
    val apps: Map<String, AppBackupData> = emptyMap(),
    val memories: MemoryBackupData? = null,
    val settings: Map<String, Any> = emptyMap()
) {
    
    data class BackupMetadata(
        val version: String = "1.0",
        val timestamp: Long = System.currentTimeMillis(),
        val deviceId: String,
        val appVersion: String,
        val encrypted: Boolean = true
    )
    
    data class AppBackupData(
        val appId: String,
        val data: Map<String, Any> = emptyMap(),
        val preferences: Map<String, Any> = emptyMap(),
        val files: List<String> = emptyList()
    )
    
    data class MemoryBackupData(
        val immediate: Map<String, Any> = emptyMap(),
        val shared: Map<String, Any> = emptyMap(),
        val chatHistory: Map<String, Any> = emptyMap()
    )
}