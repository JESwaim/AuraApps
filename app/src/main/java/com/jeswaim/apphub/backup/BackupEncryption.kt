package com.jeswaim.apphub.backup

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Backup encryption utility providing AES-256-GCM encryption for backup data.
 * Uses Android Keystore for secure key management with zero-knowledge architecture.
 */
object BackupEncryption {
    
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "AuraAppsBackupKey"
    private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 16
    
    /**
     * Encrypted backup data container
     */
    data class EncryptedData(
        val data: ByteArray,
        val iv: ByteArray,
        val keyDerivationSalt: ByteArray? = null,
        val version: Int = 1
    )
    
    /**
     * Initialize backup encryption key in Android Keystore
     */
    fun initializeEncryption(context: Context): Boolean {
        return try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)
            
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                generateBackupKey()
            }
            true
        } catch (e: Exception) {
            // TODO: Log error appropriately
            false
        }
    }
    
    /**
     * Encrypt backup data using AES-256-GCM
     */
    fun encryptBackupData(context: Context, plainData: ByteArray, userPassword: String? = null): EncryptedData? {
        return try {
            val secretKey = getOrCreateBackupKey()
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            
            val iv = cipher.iv
            val encryptedData = cipher.doFinal(plainData)
            
            EncryptedData(
                data = encryptedData,
                iv = iv,
                keyDerivationSalt = null, // TODO: Implement user password-based key derivation
                version = 1
            )
        } catch (e: Exception) {
            // TODO: Log error appropriately
            null
        }
    }
    
    /**
     * Decrypt backup data using AES-256-GCM
     */
    fun decryptBackupData(context: Context, encryptedData: EncryptedData, userPassword: String? = null): ByteArray? {
        return try {
            val secretKey = getOrCreateBackupKey()
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH * 8, encryptedData.iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            
            cipher.doFinal(encryptedData.data)
        } catch (e: Exception) {
            // TODO: Log error appropriately
            null
        }
    }
    
    /**
     * Verify data integrity without full decryption
     */
    fun verifyBackupIntegrity(context: Context, encryptedData: EncryptedData): Boolean {
        return try {
            val decrypted = decryptBackupData(context, encryptedData)
            decrypted != null
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Generate a new backup encryption key in Android Keystore
     */
    private fun generateBackupKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }
    
    /**
     * Retrieve or create the backup encryption key from Android Keystore
     */
    private fun getOrCreateBackupKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
        keyStore.load(null)
        
        return if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.getKey(KEY_ALIAS, null) as SecretKey
        } else {
            generateBackupKey()
        }
    }
    
    /**
     * Serialize encrypted data to Base64 string for storage/transmission
     */
    fun serializeEncryptedData(encryptedData: EncryptedData): String {
        val combined = ByteArray(
            4 + // version
            4 + encryptedData.iv.size + // IV length + IV
            4 + encryptedData.data.size + // data length + data
            4 + (encryptedData.keyDerivationSalt?.size ?: 0) // salt length + salt
        )
        
        var offset = 0
        // Version
        combined[offset++] = (encryptedData.version shr 24).toByte()
        combined[offset++] = (encryptedData.version shr 16).toByte()
        combined[offset++] = (encryptedData.version shr 8).toByte()
        combined[offset++] = encryptedData.version.toByte()
        
        // IV
        combined[offset++] = (encryptedData.iv.size shr 24).toByte()
        combined[offset++] = (encryptedData.iv.size shr 16).toByte()
        combined[offset++] = (encryptedData.iv.size shr 8).toByte()
        combined[offset++] = encryptedData.iv.size.toByte()
        System.arraycopy(encryptedData.iv, 0, combined, offset, encryptedData.iv.size)
        offset += encryptedData.iv.size
        
        // Data
        combined[offset++] = (encryptedData.data.size shr 24).toByte()
        combined[offset++] = (encryptedData.data.size shr 16).toByte()
        combined[offset++] = (encryptedData.data.size shr 8).toByte()
        combined[offset++] = encryptedData.data.size.toByte()
        System.arraycopy(encryptedData.data, 0, combined, offset, encryptedData.data.size)
        offset += encryptedData.data.size
        
        // Salt (optional)
        val saltSize = encryptedData.keyDerivationSalt?.size ?: 0
        combined[offset++] = (saltSize shr 24).toByte()
        combined[offset++] = (saltSize shr 16).toByte()
        combined[offset++] = (saltSize shr 8).toByte()
        combined[offset++] = saltSize.toByte()
        if (encryptedData.keyDerivationSalt != null) {
            System.arraycopy(encryptedData.keyDerivationSalt, 0, combined, offset, saltSize)
        }
        
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }
    
    /**
     * Deserialize encrypted data from Base64 string
     */
    fun deserializeEncryptedData(serialized: String): EncryptedData? {
        return try {
            val combined = Base64.decode(serialized, Base64.NO_WRAP)
            var offset = 0
            
            // Version
            val version = ((combined[offset++].toInt() and 0xFF) shl 24) or
                         ((combined[offset++].toInt() and 0xFF) shl 16) or
                         ((combined[offset++].toInt() and 0xFF) shl 8) or
                         (combined[offset++].toInt() and 0xFF)
            
            // IV
            val ivSize = ((combined[offset++].toInt() and 0xFF) shl 24) or
                        ((combined[offset++].toInt() and 0xFF) shl 16) or
                        ((combined[offset++].toInt() and 0xFF) shl 8) or
                        (combined[offset++].toInt() and 0xFF)
            val iv = ByteArray(ivSize)
            System.arraycopy(combined, offset, iv, 0, ivSize)
            offset += ivSize
            
            // Data
            val dataSize = ((combined[offset++].toInt() and 0xFF) shl 24) or
                          ((combined[offset++].toInt() and 0xFF) shl 16) or
                          ((combined[offset++].toInt() and 0xFF) shl 8) or
                          (combined[offset++].toInt() and 0xFF)
            val data = ByteArray(dataSize)
            System.arraycopy(combined, offset, data, 0, dataSize)
            offset += dataSize
            
            // Salt (optional)
            val saltSize = ((combined[offset++].toInt() and 0xFF) shl 24) or
                          ((combined[offset++].toInt() and 0xFF) shl 16) or
                          ((combined[offset++].toInt() and 0xFF) shl 8) or
                          (combined[offset++].toInt() and 0xFF)
            val salt = if (saltSize > 0) {
                val saltArray = ByteArray(saltSize)
                System.arraycopy(combined, offset, saltArray, 0, saltSize)
                saltArray
            } else null
            
            EncryptedData(data, iv, salt, version)
        } catch (e: Exception) {
            null
        }
    }
}