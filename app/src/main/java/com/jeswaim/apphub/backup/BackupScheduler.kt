package com.jeswaim.apphub.backup

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * Utility class for managing backup scheduling using WorkManager.
 * Integrates with the existing WorkManager infrastructure.
 */
object BackupScheduler {
    
    private const val BACKUP_WORK_TAG = "cloud_backup_work"
    private const val AUTO_BACKUP_WORK_NAME = "auto_backup_periodic"
    
    /**
     * Schedule automatic backups based on configuration
     */
    fun scheduleAutoBackup(context: Context, config: CloudBackupManager.BackupConfig) {
        if (!config.autoBackupEnabled) {
            cancelAutoBackup(context)
            return
        }
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .setRequiresStorageNotLow(true)
            .build()
        
        val data = Data.Builder()
            .putString(BackupWorker.PARAM_BACKUP_TYPE, BackupWorker.BACKUP_TYPE_SCHEDULED)
            .putString(BackupWorker.PARAM_PROVIDER, config.provider.name)
            .putBoolean(BackupWorker.PARAM_FORCE_BACKUP, false)
            .build()
        
        val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(
            config.backupFrequencyHours.toLong(),
            TimeUnit.HOURS
        )
            .setInputData(data)
            .setConstraints(constraints)
            .addTag(BACKUP_WORK_TAG)
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                AUTO_BACKUP_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                backupRequest
            )
    }
    
    /**
     * Cancel automatic backup scheduling
     */
    fun cancelAutoBackup(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(AUTO_BACKUP_WORK_NAME)
    }
    
    /**
     * Schedule a one-time manual backup
     */
    fun scheduleManualBackup(context: Context, provider: CloudBackupManager.Provider) {
        val data = Data.Builder()
            .putString(BackupWorker.PARAM_BACKUP_TYPE, BackupWorker.BACKUP_TYPE_MANUAL)
            .putString(BackupWorker.PARAM_PROVIDER, provider.name)
            .putBoolean(BackupWorker.PARAM_FORCE_BACKUP, true)
            .build()
        
        val backupRequest = OneTimeWorkRequestBuilder<BackupWorker>()
            .setInputData(data)
            .addTag(BACKUP_WORK_TAG)
            .build()
        
        WorkManager.getInstance(context).enqueue(backupRequest)
    }
    
    /**
     * Check if backup work is currently running
     */
    fun isBackupRunning(context: Context): Boolean {
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosByTag(BACKUP_WORK_TAG).get()
        return workInfos.any { it.state == WorkInfo.State.RUNNING }
    }
    
    /**
     * Get information about scheduled backup work
     */
    fun getScheduledBackupInfo(context: Context): WorkInfo? {
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosForUniqueWork(AUTO_BACKUP_WORK_NAME).get()
        return workInfos.firstOrNull()
    }
    
    /**
     * Cancel all backup-related work
     */
    fun cancelAllBackupWork(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(BACKUP_WORK_TAG)
    }
}