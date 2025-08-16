package com.jeswaim.apphub.backup.providers

import android.content.Context
import com.jeswaim.apphub.backup.BackupInfo
import com.jeswaim.apphub.backup.CloudBackupManager

/**
 * Base interface for cloud provider adapters.
 * Each cloud provider (Google Drive, Dropbox, OneDrive) implements this interface.
 */
interface CloudProviderAdapter {
    
    /**
     * Initialize the provider with authentication and configuration
     */
    suspend fun initialize(context: Context): Boolean
    
    /**
     * Upload backup data to the cloud provider
     */
    suspend fun uploadBackup(context: Context, backupData: ByteArray, backupId: String): Boolean
    
    /**
     * Download backup data from the cloud provider
     */
    suspend fun downloadBackup(context: Context, backupId: String): ByteArray?
    
    /**
     * List all available backups from this provider
     */
    suspend fun listBackups(context: Context): List<BackupInfo>
    
    /**
     * Delete a specific backup from the provider
     */
    suspend fun deleteBackup(context: Context, backupId: String): Boolean
    
    /**
     * Check if the provider is authenticated and ready to use
     */
    fun isAuthenticated(context: Context): Boolean
    
    /**
     * Get provider-specific configuration requirements
     */
    fun getConfigurationRequirements(): List<ConfigRequirement>
    
    /**
     * Get the maximum backup size supported by this provider
     */
    fun getMaxBackupSize(): Long
    
    data class ConfigRequirement(
        val key: String,
        val displayName: String,
        val type: Type,
        val required: Boolean = true,
        val description: String = ""
    ) {
        enum class Type {
            TEXT, PASSWORD, TOKEN, BOOLEAN
        }
    }
}