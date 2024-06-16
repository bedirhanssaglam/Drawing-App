package com.example.drawingapp.utils

/**
 * Constants used throughout the Drawing App.
 */
object Constants {
    /** MIME type for image files (PNG format) */
    const val IMAGE_MIME_TYPE = "image/png"

    /** Share image sheet title */
    const val SHARE_IMAGE = "Share Image"

    /** Prefix for file names saved by the Drawing App */
    const val DRAWING_APP_FILE_NAME_PREFIX = "DrawingApp_"

    /** Success message displayed when a file is saved successfully */
    const val SUCCESS_FILE_SAVED_MESSAGE = "File saved successfully:"

    /** Error message displayed when something goes wrong while saving a file */
    const val ERROR_MESSAGE = "Something went wrong while saving the file."

    /** Message displayed when storage permission is granted */
    const val STORAGE_PERMISSION_GRANTED_MESSAGE = "Permission granted, now you can read the storage files."

    /** Message displayed when storage permission is denied */
    const val STORAGE_PERMISSION_DENIED_MESSAGE = "Oops! You just denied the permission."

    /** Message displayed when requesting external storage access permission */
    const val EXTERNAL_STORAGE_PERMISSION_REQUEST_MESSAGE = "Please grant permission to access external storage."

    /** Message displayed in rationale dialog for accessing external storage */
    const val RATIONALE_DIALOG_MESSAGE = "Drawing App needs to Access your External Storage"
}
