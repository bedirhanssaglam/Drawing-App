package com.example.drawingapp.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Utility object for handling file operations related to bitmap saving.
 */
object FileUtils {

    /**
     * Saves a bitmap to either MediaStore (for Android Q and above) or to a file (for below Android Q).
     *
     * @param context The context in which the operation is performed.
     * @param bitmap The bitmap to be saved.
     * @return The absolute path of the saved file, or null if saving fails.
     */
    suspend fun saveBitmap(context: Context, bitmap: Bitmap): String? {
        return withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveBitmapToMediaStore(context, bitmap)
            } else {
                saveBitmapToFile(context, bitmap)
            }
        }
    }

    /**
     * Saves the bitmap to MediaStore for Android Q and above.
     *
     * @param context The context in which the operation is performed.
     * @param bitmap The bitmap to be saved.
     * @return The absolute path of the saved file, or null if saving fails.
     */
    private fun saveBitmapToMediaStore(context: Context, bitmap: Bitmap): String? {
        val resolver: ContentResolver = context.contentResolver
        val contentValues: ContentValues = ContentValues().apply {
            put(
                MediaStore.Images.Media.DISPLAY_NAME,
                Constants.DRAWING_APP_FILE_NAME_PREFIX + System.currentTimeMillis() / 1000 + ".png"
            )
            put(MediaStore.Images.Media.MIME_TYPE, Constants.IMAGE_MIME_TYPE)
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        }

        val uri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            val outputStream: OutputStream? = resolver.openOutputStream(it)
            outputStream?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
                return uri.path
            }
        }
        return null
    }

    /**
     * Saves the bitmap to a file for Android versions below Q.
     *
     * @param context The context in which the operation is performed.
     * @param bitmap The bitmap to be saved.
     * @return The absolute path of the saved file.
     */
    private fun saveBitmapToFile(context: Context, bitmap: Bitmap): String {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)

        val file =
            File(context.externalCacheDir?.absoluteFile.toString() + File.separator + Constants.DRAWING_APP_FILE_NAME_PREFIX + System.currentTimeMillis() / 1000 + ".png")
        FileOutputStream(file).use {
            it.write(bytes.toByteArray())
        }
        return file.absolutePath
    }
}
