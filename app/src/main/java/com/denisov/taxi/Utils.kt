package com.denisov.taxi

import kotlin.math.atan2
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.addListener
import com.denisov.taxi.dto.Point

fun createFloatAnim(
    start: Float,
    end: Float,
    onUpdate: ((value: Float) -> Unit)? = null,
    onEnd: (() -> Unit)? = null
) = ValueAnimator.ofFloat(start, end).apply {
    interpolator = AccelerateDecelerateInterpolator()
    duration = 800
    onUpdate?.also {
        addUpdateListener { it(it.animatedValue as Float) }
    }
    onEnd?.let { endCallback ->
        addListener(
            onEnd = { endCallback() }
        )
    }
}!!

fun computeAngleBetweenPoints(start: Point, end: Point): Float {
    val dx = (end.x - start.x).toDouble()
    val dy = (end.y - start.y).toDouble()
    return atan2(dy, dx).let {
        Math.toDegrees(it).toFloat() + 90f
    }
}

