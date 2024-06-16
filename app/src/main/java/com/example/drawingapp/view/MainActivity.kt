package com.example.drawingapp.view

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import com.example.drawingapp.permission.PermissionManager
import com.example.drawingapp.R
import com.example.drawingapp.databinding.ActivityMainBinding
import com.example.drawingapp.databinding.ProgressDialogBinding
import com.example.drawingapp.utils.BrushSizes
import com.example.drawingapp.utils.Constants
import com.example.drawingapp.utils.FileUtils
import com.example.drawingapp.utils.ShareUtils
import kotlinx.coroutines.launch

/**
 * [MainActivity] class for the Drawing App.
 * Handles the main functionality including drawing, color selection, brush size, saving, and sharing.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var activityBinding: ActivityMainBinding // View binding instance for the activity
    private lateinit var dialogBinding: ProgressDialogBinding // Binding instance for the progress dialog
    private lateinit var permissionManager: PermissionManager // Manages permission requests
    private lateinit var drawingView: DrawingView // Instance of the custom drawing view
    private lateinit var progressDialog: Dialog // Dialog instance for showing progress

    private var mImageButtonCurrentPaint: ImageButton? = null // Currently selected paint color button

    /**
     * Called when the activity is first created.
     * Sets up views, listeners, and initializes necessary components.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the activity layout using view binding
        activityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)

        // Setup initial views and components
        initializeViews()
        // Setup click listeners for interactive elements
        setupEventListeners()
    }

    /**
     * Initializes views and components required by the activity.
     * Also sets the initial state for drawing view and paint color buttons.
     */
    private fun initializeViews() {
        // Initialize the custom drawing view and set initial brush size
        drawingView = activityBinding.drawingView
        drawingView.setSizeForBrush(BrushSizes.SMALL.size)

        // Initialize the current paint color button and set its initial state
        val linearLayoutPaintColors: LinearLayout = activityBinding.llPaintColors
        mImageButtonCurrentPaint = linearLayoutPaintColors[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.palet_pressed))

        // Initialize permission manager for handling storage permissions
        permissionManager = PermissionManager(this, activityBinding)
    }

    /**
     * Sets up click listeners for interactive elements like buttons.
     * Handles actions such as requesting permissions, showing brush size dialog, undoing actions,
     * and saving drawings.
     */
    private fun setupEventListeners() {
        // Click listener for the gallery button to request storage permission
        activityBinding.ibGallery.setOnClickListener {
            permissionManager.requestStoragePermission()
        }

        // Click listener for the brush button to show the brush size dialog
        activityBinding.ibBrush.setOnClickListener {
            showBrushSizeDialog()
        }

        // Click listener for the undo button to perform undo action on the drawing view
        activityBinding.ibUndo.setOnClickListener {
            drawingView.onClickUndo()
        }

        // Click listener for the save button to show progress dialog and save the drawing
        activityBinding.ibSave.setOnClickListener {
            showSavingProgressDialog()
            saveDrawingAsBitmap(activityBinding.flDrawingViewContainer)
        }
    }

    /**
     * Shows a dialog for selecting the brush size.
     * Handles interaction with the user to change the brush size on the drawing view.
     */
    private fun showBrushSizeDialog() {
        val brushDialog = Dialog(this)
        brushDialog.apply {
            setContentView(R.layout.dialog_brush_size)
        }

        // Array of brush sizes and corresponding button IDs in the dialog layout
        val sizes: Array<BrushSizes> = arrayOf(BrushSizes.SMALL, BrushSizes.MEDIUM, BrushSizes.LARGE)
        val buttons: Array<Int> = arrayOf(R.id.ib_small_brash, R.id.ib_medium_brash, R.id.ib_large_brash)

        // Set click listeners for each brush size button
        buttons.forEachIndexed { index, id ->
            brushDialog.findViewById<View>(id).setOnClickListener {
                drawingView.setSizeForBrush(sizes[index].size)
                brushDialog.dismiss()
            }
        }

        brushDialog.show()
    }

    /**
     * Handles click events on paint color buttons to change the drawing color.
     * Updates the UI to reflect the currently selected paint color.
     */
    fun paintClicked(view: View) {
        if (view !== mImageButtonCurrentPaint) {
            val imageButton: ImageButton = view as ImageButton
            val colorTag: String = imageButton.tag.toString()
            drawingView.setColor(colorTag)

            // Update UI to indicate the selected paint color
            imageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.palet_pressed))
            mImageButtonCurrentPaint?.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_normal))
            mImageButtonCurrentPaint = view
        }
    }

    /**
     * Saves the current state of the drawing view as an image file.
     * Displays progress dialog while saving and handles success/error scenarios.
     */
    private fun saveDrawingAsBitmap(view: View) {
        lifecycleScope.launch {
            // Convert the drawing view to a bitmap image
            val bitmap: Bitmap = captureBitmapFromView(view)
            try {
                // Attempt to save the bitmap to storage and get the saved file path
                val savedFilePath: String? = FileUtils.saveBitmap(this@MainActivity, bitmap)
                savedFilePath?.let {
                    // If saved successfully, dismiss the progress dialog and show success message
                    dismissProgressDialog()
                    Toast.makeText(this@MainActivity, "${Constants.SUCCESS_FILE_SAVED_MESSAGE} $it", Toast.LENGTH_SHORT)
                        .show()
                    // Share the saved image file
                    ShareUtils.shareImage(this@MainActivity, it)
                } ?: run {
                    // If saving failed, show an error message
                    Toast.makeText(this@MainActivity, Constants.ERROR_MESSAGE, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Catch any exceptions during bitmap saving process and show error message
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Shows a progress dialog indicating ongoing operations like saving.
     * Provides visual feedback to the user during time-consuming tasks.
     */
    private fun showSavingProgressDialog() {
        progressDialog = Dialog(this@MainActivity)
        dialogBinding = ProgressDialogBinding.inflate(layoutInflater)
        progressDialog.apply {
            setContentView(dialogBinding.root)
            show()
        }
    }

    /**
     * Dismisses the progress dialog if it is currently shown.
     * Called after completing tasks like saving or sharing to clean up UI.
     */
    private fun dismissProgressDialog() {
        progressDialog.dismiss()
    }

    /**
     * Converts a given view into a bitmap image.
     * Used to capture the current state of the drawing view for saving or sharing.
     */
    private fun captureBitmapFromView(view: View): Bitmap {
        val bitmap: Bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable: Drawable? = view.background
        // Draw background or default to white color
        bgDrawable?.draw(canvas) ?: canvas.drawColor(Color.WHITE)
        // Draw the view content onto the canvas
        view.draw(canvas)
        return bitmap
    }
}
