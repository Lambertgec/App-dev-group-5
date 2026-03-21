package com.group5.gue.api

import android.util.Log
import io.github.jan.supabase.storage.storage
import java.util.UUID

/**
 * Repository for handling Supabase Storage file uploads and downloads.
 *
 * After using [getInstance], the suspend functions can be called from a coroutine.
 */
class FileRepository private constructor() {

	private val client = SupabaseProvider.supabaseClient

	companion object {
		@Volatile
		private var instance: FileRepository? = null

		private const val TAG = "FileRepository"

		/**
		 * Returns the singleton instance of [FileRepository].
		 */
		fun getInstance(): FileRepository {
			return instance ?: synchronized(this) {
				instance ?: FileRepository().also { instance = it }
			}
		}
	}

	/**
	 * Uploads a file to Supabase storage
	 *
	 * Files are stored as a random UUID and the provided [fileExtension].
	 * When [folderPath] is provided, the file is stored under that folder.
	 *
	 * @param bucketName 
	 * @param byteArray 
	 * @param fileExtension Extension without dot
	 * @param folderPath Default is root
	 * @param upsert If true, files with the same name will be overwritten.
	 * @return The path of the uploaded file, or 'null' if failed.
	 */
	suspend fun uploadFile(
		bucketName: String,
		byteArray: ByteArray,
		fileExtension: String,
		folderPath: String? = null,
		upsert: Boolean = false,
	): String? {
		return try {
			val fileName = "${UUID.randomUUID()}.$fileExtension"
			val normalizedFolder = folderPath
				?.trim('/')
				?.takeIf { it.isNotBlank() }

			val filePath = if (normalizedFolder != null) "$normalizedFolder/$fileName" else fileName
			
			client.storage.from(bucketName).upload(
				path = filePath,
				data = byteArray
			) {
				this.upsert = upsert
			}

			filePath
		} catch (e: Exception) {
			Log.e(TAG, "Failed to upload file to bucket=$bucketName", e)
			null
		}
	}

	/**
	 * Downloads a file from a Supabase storage
	 *
	 * @param bucketName
	 * @param filePath 
	 * @return File bytes, or `null` when download fails.
	 */
	suspend fun downloadFile(bucketName: String, filePath: String): ByteArray? {
		return try {
			client.storage.from(bucketName).downloadAuthenticated(filePath)
		} catch (e: Exception) {
			Log.e(TAG, "Failed to download file from bucket=$bucketName path=$filePath", e)
			null
		}
	}

	/**
	 * Get public url for a file to store in the database
	 *
	 * @param bucketName
	 * @param filePath
	 * @return Public URL string for the file path.
	 */
	fun getPublicImageUrl(bucketName: String, filePath: String): String {
		return client.storage.from(bucketName).publicUrl(filePath)
	}

	/**
	 * Deletes a file from Supabase storage.
	 *
	 * @return True if deletion request succeeded, false otherwise.
	 */
	suspend fun deleteFile(bucketName: String, filePath: String): Boolean {
		return try {
			client.storage.from(bucketName).delete(filePath)
			true
		} catch (e: Exception) {
			Log.e(TAG, "Failed to delete file from bucket=$bucketName path=$filePath", e)
			false
		}
	}
}
