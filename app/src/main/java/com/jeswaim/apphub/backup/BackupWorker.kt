package com.jeswaim.apphub.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * WorkManager worker for performing cloud backups in the background.
 * Integrates with the existing WorkManager infrastructure used by ModelDownloadWorker.
 */
class BackupWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        const val PARAM_BACKUP_TYPE = "backup_type"
        const val PARAM_PROVIDER = "provider"
        const val PARAM_FORCE_BACKUP = "force_backup"
        
        const val BACKUP_TYPE_MANUAL = "manual"
        const val BACKUP_TYPE_SCHEDULED = "scheduled"
        
        const val PROGRESS_KEY = "progress"
        const val STATUS_KEY = "status"
        const val MESSAGE_KEY = "message"
    }
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val backupType = inputData.getString(PARAM_BACKUP_TYPE) ?: BACKUP_TYPE_MANUAL
            val providerName = inputData.getString(PARAM_PROVIDER)
            val forceBackup = inputData.getBoolean(PARAM_FORCE_BACKUP, false)
            
            setProgress(workDataOf(
                PROGRESS_KEY to 0,
                STATUS_KEY to "Initializing backup...",
                MESSAGE_KEY to "Starting $backupType backup"
            ))
            
            // Get backup manager instance
            val backupManager = CloudBackupManagerImpl.getInstance()
            
            // Check if backup is needed (for scheduled backups)
            if (backupType == BACKUP_TYPE_SCHEDULED && !forceBackup) {
                if (!backupManager.isBackupDue(context)) {
                    return@withContext Result.success(workDataOf(
                        STATUS_KEY to "Backup not needed",
                        MESSAGE_KEY to "Scheduled backup is not due yet"
                    ))
                }
            }
            
            setProgress(workDataOf(
                PROGRESS_KEY to 10,
                STATUS_KEY to "Collecting data...",
                MESSAGE_KEY to "Gathering backup data"
            ))
            
            // Perform the backup
            val result = backupManager.performBackup(context)
            
            setProgress(workDataOf(
                PROGRESS_KEY to 100,
                STATUS_KEY to if (result.success) "Completed" else "Failed",
                MESSAGE_KEY to result.message
            ))
            
            if (result.success) {
                // Save backup result to preferences for status tracking
                saveBackupResult(result)
                Result.success(workDataOf(
                    STATUS_KEY to "success",
                    MESSAGE_KEY to result.message,
                    "backup_id" to (result.backupId ?: ""),
                    "timestamp" to result.timestamp
                ))
            } else {
                Result.failure(workDataOf(
                    STATUS_KEY to "failed",
                    MESSAGE_KEY to result.message
                ))
            }
            
        } catch (e: Exception) {
            setProgress(workDataOf(
                PROGRESS_KEY to 100,
                STATUS_KEY to "Error",
                MESSAGE_KEY to "Backup failed: ${e.message ?: "Unknown error"}"
            ))
            
            Result.failure(workDataOf(
                STATUS_KEY to "error",
                MESSAGE_KEY to e.message ?: "Unknown error occurred during backup"
            ))
        }
    }
    
    /**
     * Save backup result to shared preferences for status tracking
     */
    private fun saveBackupResult(result: CloudBackupManager.BackupResult) {
        val prefs = context.getSharedPreferences("backup_settings", Context.MODE_PRIVATE)
        val resultJson = JSONObject().apply {
            put("success", result.success)
            put("message", result.message)
            put("backup_id", result.backupId ?: "")
            put("timestamp", result.timestamp)
        }
        
        prefs.edit()
            .putString("last_backup_result", resultJson.toString())
            .putLong("last_backup_timestamp", result.timestamp)
            .apply()
    }
    
    /**
     * Create foreground info for long-running backup operations
     */
    override suspend fun getForegroundInfo(): ForegroundInfo {
        // TODO: Implement notification for backup progress
        // This would typically create a notification showing backup progress
        return ForegroundInfo(
            1001, // Notification ID
            createBackupNotification()
        )
    }
    
    /**
     * Create notification for backup progress (placeholder implementation)
     */
    private fun createBackupNotification(): android.app.Notification {
        // TODO: Create proper notification with progress
        // For now, return a basic notification
        return android.app.Notification.Builder(context, "backup_channel")
            .setContentTitle("AuraApps Backup")
            .setContentText("Backing up your data...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
    }
}