package com.example.drawingapp.utils

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection

/**
 * Utility class for sharing images via Intent.
 */
object ShareUtils {

    /**
     * Shares an image located at the specified directory using Intent.ACTION_SEND.
     *
     * @param context The context from which the sharing operation is initiated.
     * @param directory The directory path where the image file is located.
     */
    fun shareImage(context: Context, directory: String) {
        // Scan the file to ensure it is accessible by MediaStore
        MediaScannerConnection.scanFile(context, arrayOf(directory), null) { _, uri ->
            // Create a new share Intent
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.apply {
                putExtra(Intent.EXTRA_STREAM, uri)
                type = Constants.IMAGE_MIME_TYPE // Set the MIME type of the content
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant read permission to receiving app
            }

            // Create a chooser Intent to select an app for sharing
            val chooser: Intent = Intent.createChooser(shareIntent, Constants.SHARE_IMAGE)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Ensure the chooser opens in a new task
            context.startActivity(chooser) // Start the Intent to share the image
        }
    }
}
