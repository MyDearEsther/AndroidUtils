package com.weikun.androidutils.ui.widget.recyclerview

import android.view.MotionEvent
import android.view.ScaleGestureDetector

/**
 * @author lwk
 * @date 2020/4/16
 */
interface TouchEventCallback {
    fun onTouchEvent(event: MotionEvent?): Boolean
    fun onScale(detector: ScaleGestureDetector?): Boolean
    fun onScaleEnd(detector: ScaleGestureDetector?)
}