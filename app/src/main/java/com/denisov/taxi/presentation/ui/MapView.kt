package com.denisov.taxi.presentation.ui

import android.animation.Animator
import android.animation.AnimatorSet
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.animation.addListener
import androidx.core.view.GestureDetectorCompat
import com.denisov.taxi.computeAngleBetweenPoints
import com.denisov.taxi.createFloatAnim
import com.denisov.taxi.dto.Car
import com.denisov.taxi.dto.Point

class MapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs), GestureListener {

    private var carDrawable: CarDrawable? = null
    private val gestureDetector = GestureDetectorCompat(context, this)
    private val carCoordinates: Point?
        get() = carDrawable?.let {
            Point(
                it.translationX.toInt(),
                it.translationY.toInt()
            )
        }

    var mapListener: MapListener? = null
    var enableTouches = true

    fun setCar(car: Car) {
        carDrawable = CarDrawable(car.bitmap)
            .apply { callback = this@MapView }
        requestLayout()
    }

    fun startDriving(points: List<Point>) {
        carCoordinates?.let {
            val list = mutableListOf<Animator>()

            var lastPoint = it
            var lastAngle = carDrawable?.rotationAngle ?: 0f

            points.forEach {
                val angle =
                    computeAngleBetweenPoints(lastPoint, it)
                rotateCar(lastAngle, angle).apply { list.add(this) }
                move(lastPoint, it).apply { list.add(this) }

                lastPoint = it
                lastAngle = angle
            }
            AnimatorSet().apply {
                playSequentially(list)
                addListener(onEnd = {
                    checkMapBounds { mapListener?.onDrivingEnd() }
                })
                start()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        carDrawable?.also { car ->
            val width = measuredWidth.div(7)
            val scale = car.originalWidth.toFloat() / car.originalHeight.toFloat()
            val scaledHeight = (width.toFloat() / scale).toInt()
            car.setBounds(0, 0, width, scaledHeight)
        }
    }

    override fun verifyDrawable(who: Drawable): Boolean =
        super.verifyDrawable(who) || who is CarDrawable

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        e?.let { event ->
            carDrawable?.let { car ->
                mapListener?.onMapClick(
                    Point(
                        car.translationX.toInt(),
                        car.translationY.toInt()
                    ),
                    Point(event.x.toInt(), event.y.toInt())
                )
            }
        }
        return true
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return event
            ?.takeIf { enableTouches }
            ?.let {
                when {
                    gestureDetector.onTouchEvent(event) -> true
                    else -> false
                }
            } ?: super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        carDrawable?.draw(canvas)
    }

    private fun rotateCar(startAngle: Float, endAngle: Float): Animator {
        return createFloatAnim(
            startAngle,
            endAngle,
            { carDrawable?.rotationAngle = it }
        )
    }

    private fun move(start: Point, end: Point): Animator {
        val first = createFloatAnim(
            start.x.toFloat(),
            end.x.toFloat(),
            { carDrawable?.translationX = it }
        )
        val second = createFloatAnim(
            start.y.toFloat(),
            end.y.toFloat(),
            { carDrawable?.translationY = it }
        )
        return AnimatorSet().apply { playTogether(first, second) }
    }

    private fun checkMapBounds(onEnd: () -> Unit) {
        carDrawable?.also { car ->
            var newXTranslation = 0f
            var newYTranslation = 0f
            if (car.translationX - car.bounds.width() < paddingLeft) {
                newXTranslation = car.translationX + car.bounds.width()
            }
            if (car.translationX + car.bounds.width() > measuredWidth - paddingRight) {
                newXTranslation = car.translationX - car.bounds.width()
            }
            if (car.translationY - car.bounds.height() < paddingTop) {
                newYTranslation = car.translationY + car.bounds.height()
            }
            if (car.translationY + car.bounds.height() > measuredHeight - paddingBottom) {
                newYTranslation = car.translationY - car.bounds.height()
            }

            carCoordinates
                ?.takeIf { newXTranslation != 0f || newYTranslation != 0f }
                ?.also {
                    val point = Point(
                        newXTranslation.toInt().takeIf { it != 0 } ?: car.translationX.toInt(),
                        newYTranslation.toInt().takeIf { it != 0 } ?: car.translationY.toInt()
                    )
                    val angle = computeAngleBetweenPoints(it, point)

                    AnimatorSet().apply {
                        playSequentially(
                            rotateCar(car.rotationAngle, angle),
                            move(it, point)
                        )
                        addListener(onEnd = { onEnd() })
                        start()
                    }
                } ?: onEnd()
        } ?: onEnd()
    }

    interface MapListener {
        fun onMapClick(carPoint: Point, finalPoint: Point)
        fun onDrivingEnd()
    }
}