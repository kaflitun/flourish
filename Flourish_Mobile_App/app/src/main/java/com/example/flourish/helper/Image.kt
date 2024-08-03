package com.example.flourish.helper

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import androidx.core.net.toUri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Image class is a helper class to handle image related operations
class Image {
    companion object {

        // deleteImageFromStorage function is used to delete image from internal app storage
        fun deleteImageFromStorage(imgPath: String): Boolean {
            val file = File(imgPath)
            return file.delete()
        }

        // encodeBitmapToBase64 function is used to encode bitmap image to base64 string
        fun encodeBitmapToBase64(bitmap: Bitmap): String {
            val outputStream = ByteArrayOutputStream()
            // Compress the bitmap image to JPEG format with 100% quality and write it to output stream
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val byteArray = outputStream.toByteArray()
            // Encode the image to base64 string
            return Base64.encodeToString(byteArray, Base64.NO_WRAP)
        }

        // saveUri function is used to save image URI to internal app storage
        fun saveUri(context: Context, uri: Uri): String? {
            // Generate a unique file name for the image using timestamp
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "image_$timeStamp.png"
            // Get the app's internal storage directory
            val directory = context.filesDir
            val file = File(directory, fileName)

            return try {
                // Open input stream to read the image from URI
                val inputStream = context.contentResolver.openInputStream(uri)
                // Open output stream to write the image to internal storage
                inputStream?.use { input ->
                    val outputStream = FileOutputStream(file)
                    // Copy the image from input stream to output stream
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                // Return the absolute path of the saved image if image is saved successfully
                if (isImageSaved(file.toUri())) {
                    file.absolutePath
                } else {
                    null
                }
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }

        // isImageSaved function is used to check if the image is saved in internal app storage
        private fun isImageSaved(uri: Uri): Boolean {
            val file = File(uri.path)
            return file.exists()
        }
    }
}