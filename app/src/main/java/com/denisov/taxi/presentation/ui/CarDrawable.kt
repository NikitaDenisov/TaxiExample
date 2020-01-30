package com.denisov.taxi.presentation.ui

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.graphics.withSave

class CarDrawable(private val originalBitmap: Bitmap) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
    private var resizedBitmap: Bitmap? = null
    private val selfMatrix = Matrix()

    val originalWidth: Int
        get() = originalBitmap.width
    val originalHeight: Int
        get() = originalBitmap.height

    var translationX = 0f
        set(value) {
            field = value
            invalidateSelf()
        }
    var translationY = 0f
        set(value) {
            field = value
            invalidateSelf()
        }
    var rotationAngle = 0f
        set(value) {
            field = value
            invalidateSelf()
        }

    override fun draw(canvas: Canvas) {
        selfMatrix.reset()
        selfMatrix.postRotate(
            rotationAngle,
            bounds.width().div(2).toFloat(),
            bounds.height().div(2).toFloat()
        )
        selfMatrix.postTranslate(translationX, translationY)
        canvas.withSave {
            concat(selfMatrix)
            resizedBitmap?.let { drawBitmap(it, 0f, 0f, paint) }
        }
    }

    override fun onBoundsChange(bounds: Rect) {
        originalBitmap.also {
            resizedBitmap = Bitmap.createScaledBitmap(it, bounds.width(), bounds.height(), true)
        }
    }

    override fun setAlpha(alpha: Int) {

    }

    override fun getOpacity(): Int = PixelFormat.TRANSPARENT

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    fun checkFieldBounds(fieldWidth: Int, fieldHeight: Int) {

    }
}