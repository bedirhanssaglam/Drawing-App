package com.example.drawingapp

import com.example.drawingapp.view.DrawingView
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test

class DrawingViewTest {
    private lateinit var drawingView: DrawingView

    @Before
    fun setup() {
        drawingView = mockk()
    }

    @Test
    fun testSetColor() {
        every { drawingView.setColor(any()) } just Runs

        drawingView.setColor("#FF0000")
        verify { drawingView.setColor("#FF0000") }
    }

    @Test
    fun testSetBrushSize() {
        every { drawingView.setSizeForBrush(any()) } just Runs

        drawingView.setSizeForBrush(20f)
        verify { drawingView.setSizeForBrush(20f) }
    }

    @After
    fun teardown() {
        unmockkAll()
    }
}