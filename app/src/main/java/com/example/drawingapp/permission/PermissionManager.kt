package com.example.drawingapp.permission

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.drawingapp.databinding.ActivityMainBinding
import com.example.drawingapp.utils.Constants

/**
 * Helper class to manage runtime permissions related to storage and gallery access.
 *
 * @property activity The parent activity to manage permissions and launch activities.
 * @property activityMainBinding Binding object for accessing views and resources in the main activity.
 */
class PermissionManager(private val activity: AppCompatActivity, private val activityMainBinding: ActivityMainBinding) {

    // ActivityResultLauncher for handling gallery opening
    private val openGalleryLauncher: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imageBackGround: ImageView = activityMainBinding.ivBackground
                imageBackGround.setImageURI(result.data?.data)
            }
        }

    // ActivityResultLauncher for handling permission requests
    private val requestPermission: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value

                if (isGranted) {
                    Toast.makeText(activity, Constants.STORAGE_PERMISSION_GRANTED_MESSAGE, Toast.LENGTH_SHORT).show()
                } else {
                    if (permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {
                        Toast.makeText(activity, Constants.STORAGE_PERMISSION_DENIED_MESSAGE, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    /**
     * Requests storage permission based on Android version.
     * Handles both legacy and scoped storage permissions.
     */
    fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Handle scoped storage permission on Android R and above
            if (Environment.isExternalStorageManager()) {
                // Permission already granted
                openGallery()
            } else {
                // Request permission to manage all files access
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri: Uri = Uri.fromParts("package", activity.packageName, null)
                intent.data = uri
                activity.startActivity(intent)
                Toast.makeText(activity, Constants.EXTERNAL_STORAGE_PERMISSION_REQUEST_MESSAGE, Toast.LENGTH_LONG)
                    .show()
            }
        } else {
            // Handle legacy storage permission on Android 10 and below
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                // Show rationale dialog if needed
                showRationalDialog()
            } else {
                // Request permission without showing rationale
                requestPermission.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            }
        }
    }

    /**
     * Opens the system gallery for image selection.
     */
    private fun openGallery() {
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        openGalleryLauncher.launch(pickIntent)
    }

    /**
     * Displays a rationale dialog explaining the need for storage access.
     * This method can be customized to show a dialog before requesting permission.
     */
    private fun showRationalDialog() {
        // Implement your rationale dialog here if needed
        Toast.makeText(activity, Constants.RATIONALE_DIALOG_MESSAGE, Toast.LENGTH_SHORT).show()
    }
}
