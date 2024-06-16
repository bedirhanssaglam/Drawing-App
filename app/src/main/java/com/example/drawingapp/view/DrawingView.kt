package com.example.drawingapp.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

/**
 * Custom view for drawing on a canvas.
 *
 * @property context Context in which the view is instantiated.
 * @property attrs AttributeSet containing attributes from the XML declaration.
 */
class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var drawPath: CustomPath? = null
    private var canvasBitmap: Bitmap? = null
    private var drawPaint: Paint? = null
    private var canvasPaint: Paint? = null

    private var brushSize: Float = 0f
    private var color = Color.BLACK
    private var canvas: Canvas? = null
    private val paths = ArrayList<CustomPath>()
    private val undoPaths = ArrayList<CustomPath>()

    init {
        setUpDrawing()
    }

    /**
     * Sets up the initial configurations for drawing.
     */
    private fun setUpDrawing() {
        drawPaint = Paint()
        drawPath = CustomPath(color, brushSize)
        drawPaint!!.apply {
            color = color
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
        canvasPaint = Paint(Paint.DITHER_FLAG)
    }

    /**
     * Undo the last drawn path by moving it from [paths] to [undoPaths].
     */
    fun onClickUndo() {
        if (paths.isNotEmpty()) {
            undoPaths.add(paths.removeAt(paths.lastIndex))
            invalidate()
        }
    }

    /**
     * Called when the size of the view changes.
     *
     * @param w Current width of the view.
     * @param h Current height of the view.
     * @param oldw Old width of the view.
     * @param oldh Old height of the view.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(canvasBitmap!!)
    }

    /**
     * Draws the canvas and all paths on the view.
     *
     * @param canvas The canvas on which to draw.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(canvasBitmap!!, 0f, 0f, canvasPaint)

        for (path: CustomPath in paths) {
            drawPaint!!.apply {
                strokeWidth = path.brushThickness
                color = path.color
                canvas.drawPath(path, this)
            }
        }

        if (!drawPath!!.isEmpty) {
            drawPaint!!.apply {
                strokeWidth = drawPath!!.brushThickness
                color = drawPath!!.color
            }
            canvas.drawPath(drawPath!!, drawPaint!!)
        }
    }

    /**
     * Handles touch events for drawing on the view.
     *
     * @param event The motion event being processed.
     * @return True if the event was handled, false otherwise.
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX: Float? = event?.x
        val touchY: Float? = event?.y

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                drawPath!!.color = color
                drawPath!!.apply {
                    brushThickness = brushSize
                    reset()
                    if (touchX != null && touchY != null) moveTo(touchX, touchY)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (touchX != null && touchY != null) drawPath!!.lineTo(touchX, touchY)
            }

            MotionEvent.ACTION_UP -> {
                paths.add(drawPath!!)
                drawPath = CustomPath(color, brushSize)
            }

            else -> {
                return false
            }
        }

        invalidate()

        return true
    }

    /**
     * Sets the size of the brush used for drawing.
     *
     * @param newSize The new size of the brush in density-independent pixels (dp).
     */
    fun setSizeForBrush(newSize: Float) {
        brushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, resources.displayMetrics)
        drawPaint!!.strokeWidth = brushSize
    }

    /**
     * Sets the color used for drawing.
     *
     * @param newColor The new color represented as a string (e.g., "#RRGGBB").
     */
    fun setColor(newColor: String) {
        color = Color.parseColor(newColor)
        drawPaint!!.color = color
    }

    /**
     * Custom path class that extends [Path] and includes color and brush thickness properties.
     *
     * @property color The color of the path.
     * @property brushThickness The thickness of the brush used for the path.
     */
    internal inner class CustomPath(var color: Int, var brushThickness: Float) : Path()
}
