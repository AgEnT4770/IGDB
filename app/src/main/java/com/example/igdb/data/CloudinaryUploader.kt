package com.example.igdb.data

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object CloudinaryUploader {
    private const val TAG = "CloudinaryUploader"
    @Volatile
    private var isInitialized = false
    
    fun init(context: Context, cloudName: String, apiKey: String, apiSecret: String) {
        if (isInitialized) {
            Log.d(TAG, "MediaManager already initialized, skipping initialization")
            return
        }
        
        try {
            val config = hashMapOf(
                "cloud_name" to cloudName,
                "api_key" to apiKey,
                "api_secret" to apiSecret
            )
            MediaManager.init(context, config)
            isInitialized = true
            Log.d(TAG, "MediaManager initialized successfully")
        } catch (e: IllegalStateException) {
            if (e.message?.contains("already initialized") == true) {
                Log.d(TAG, "MediaManager was already initialized by another instance")
                isInitialized = true
            } else {
                Log.e(TAG, "Failed to initialize MediaManager", e)
                throw e
            }
        }
    }

    fun uploadProfilePicture(
        context: Context,
        imageUri: Uri,
        userId: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        try {
            val file = uriToFile(context, imageUri, userId)
            if (file == null) {
                Toast.makeText(context, "Failed to process image", Toast.LENGTH_SHORT).show()
                onComplete(false, null)
                return
            }

            val publicId = "igdb/profile_pictures/${userId}_${UUID.randomUUID()}"
            val filePath = file.absolutePath

            val uploadRequest = MediaManager.get().upload(filePath)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d(TAG, "Upload started: $requestId")
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        val progress = (bytes * 100 / totalBytes).toInt()
                        Log.d(TAG, "Upload progress: $progress%")
                    }

                    override fun onSuccess(requestId: String, resultData: Map<Any?, Any?>) {
                        val secureUrl = resultData["secure_url"] as? String
                        val url = resultData["url"] as? String
                        val imageUrl = secureUrl ?: url
                        
                        if (imageUrl != null) {
                            Log.d(TAG, "Upload successful: $imageUrl")
                            file.delete()
                            onComplete(true, imageUrl)
                        } else {
                            Log.e(TAG, "Upload succeeded but no URL returned")
                            file.delete()
                            onComplete(false, null)
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e(TAG, "Upload failed: ${error.description}")
                        file.delete()
                        val errorMessage = when {
                            error.description?.contains("network", ignoreCase = true) == true -> {
                                "Network error. Please check your internet connection."
                            }
                            error.description?.contains("timeout", ignoreCase = true) == true -> {
                                "Upload timeout. Please try again."
                            }
                            else -> {
                                "Failed to upload image: ${error.description ?: "Unknown error"}"
                            }
                        }
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        onComplete(false, null)
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.w(TAG, "Upload rescheduled: ${error.description}")
                    }
                })
            
            uploadRequest.dispatch()
        } catch (e: Exception) {
            Log.e(TAG, "Exception during upload", e)
            Toast.makeText(context, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
            onComplete(false, null)
        }
    }

    private fun uriToFile(context: Context, uri: Uri, userId: String): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                val tempFile = File(context.cacheDir, "temp_profile_${userId}_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(tempFile)
                stream.copyTo(outputStream)
                outputStream.close()
                tempFile
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error converting URI to file", e)
            null
        }
    }
}

