package com.jeswaim.apphub.backup

import android.content.Context

/**
 * Main interface for managing cloud-based backups across multiple providers.
 * Provides an extensible structure for implementing various cloud storage solutions.
 */
interface CloudBackupManager {
    
    /**
     * Supported cloud backup providers
     */
    enum class Provider {
        GOOGLE_DRIVE,
        DROPBOX,
        ONEDRIVE
    }
    
    /**
     * Backup operation result
     */
    data class BackupResult(
        val success: Boolean,
        val message: String,
        val backupId: String? = null,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * Restore operation result
     */
    data class RestoreResult(
        val success: Boolean,
        val message: String,
        val restoredItems: Int = 0
    )
    
    /**
     * Backup configuration settings
     */
    data class BackupConfig(
        val provider: Provider,
        val encryptionEnabled: Boolean = true,
        val autoBackupEnabled: Boolean = false,
        val backupFrequencyHours: Int = 24,
        val selectedApps: Set<String> = emptySet()
    )
    
    /**
     * Initialize the backup manager with the specified provider
     */
    suspend fun initialize(context: Context, config: BackupConfig): Boolean
    
    /**
     * Perform a manual backup of all selected data
     */
    suspend fun performBackup(context: Context): BackupResult
    
    /**
     * Restore data from the latest backup
     */
    suspend fun restoreFromBackup(context: Context, backupId: String? = null): RestoreResult
    
    /**
     * List available backups from the cloud provider
     */
    suspend fun listBackups(context: Context): List<BackupInfo>
    
    /**
     * Delete a specific backup
     */
    suspend fun deleteBackup(context: Context, backupId: String): Boolean
    
    /**
     * Get the current backup configuration
     */
    fun getConfig(context: Context): BackupConfig?
    
    /**
     * Update the backup configuration
     */
    fun updateConfig(context: Context, config: BackupConfig): Boolean
    
    /**
     * Check if auto-backup is due
     */
    fun isBackupDue(context: Context): Boolean
    
    /**
     * Get backup status and last backup information
     */
    fun getBackupStatus(context: Context): BackupStatus
}