package com.denisov.taxi.presentation.ui

import android.view.GestureDetector
import android.view.MotionEvent

interface GestureListener : GestureDetector.OnGestureListener {

    override fun onShowPress(e: MotionEvent?) {
    }

    override fun onDown(e: MotionEvent?): Boolean = true

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean = false

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean = false

    override fun onLongPress(e: MotionEvent?) {
    }
}