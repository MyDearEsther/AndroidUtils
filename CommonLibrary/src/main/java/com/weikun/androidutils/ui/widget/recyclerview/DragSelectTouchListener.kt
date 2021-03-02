package com.weikun.androidutils.ui.widget.recyclerview

import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import android.widget.OverScroller
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

/**
 *   滑动选择监听器
 *   @author lwk
 *   @date   2020/8/10
 */
class DragSelectTouchListener: RecyclerView.OnItemTouchListener {
    companion object {
        //
        private const val DELAY = 25L
        private const val MAX_SCROLL_DISTANCE = 25
        //滚动因素 值越大速度增加越慢
        private const val SCROLL_FACTOR = 6
    }

    init {
        reset()
    }

    interface onSelectListener {
        /**
         * 选择回调
         * @param start 起始位置
         * @param end 结束位置
         * @param isSelected 是否选中
         */
        fun onSelectChange(start: Int, end: Int, isSelected: Boolean)
    }

    var isActive = true
    private var start = 0
    private var end = 0
    private var recyclerView: RecyclerView? = null
    private var mTopBound = 0
    private var mBottomBound = 0
    private var inTopSpot = false
    private var inBottomSpot = false
    private var scrollDistance = 0
    private var lastStart = 0
    private var lastEnd = 0
    private var lastX = 0F
    private var lastY = 0F
    private var scroller: OverScroller? = null
    var selectListener: onSelectListener? = null
    private val autoScrollDistance = Resources.getSystem().displayMetrics.density * 56
    private var autoScrollHandler:Handler? = null

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        if (!isActive) {
            return
        }
        when (e.action) {
            MotionEvent.ACTION_MOVE -> {
                if (!inTopSpot && !inBottomSpot) {
                    //更新滑动选择区域
                    updateSelectedRange(rv, e.x, e.y)
                }
                //在顶部或者底部触发自动滑动
                processAutoScroll(e)
            }
            MotionEvent.ACTION_CANCEL
                , MotionEvent.ACTION_UP
                , MotionEvent.ACTION_POINTER_UP -> {
                //结束滑动选择，初始化各状态值
                reset()
            }
        }

    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        if (!isActive && rv.adapter!!.itemCount > 1) {
            return false
        }
        when (MotionEventCompat.getActionMasked(e)) {
            MotionEvent.ACTION_POINTER_DOWN,MotionEvent.ACTION_DOWN -> {
                reset()
            }
        }
        this.recyclerView = rv
        mTopBound = -20
        mBottomBound = rv.height - (autoScrollDistance.toInt())
        return true
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    }

    private val scrollRunnable = object : Runnable {
        override fun run() {
            if (!inTopSpot && !inBottomSpot) {
                return
            }
            scrollBy(scrollDistance)
            autoScrollHandler!!.postDelayed(this, DELAY)
        }
    }

    fun startAutoScroll() {
        if (recyclerView == null) {
            return;
        }
        if (scroller == null) {
            scroller = OverScroller(recyclerView!!.context, LinearInterpolator())
        }
        if (scroller!!.isFinished) {
            recyclerView!!.removeCallbacks(scrollRunnable);
            scroller!!.startScroll(0, scroller!!.currY, 0, 5000, 100000);
            ViewCompat.postOnAnimation(recyclerView!!, scrollRunnable);
        }
    }

    private fun stopAutoScroll() {
        if (scroller != null && !scroller!!.isFinished) {
            recyclerView!!.removeCallbacks(scrollRunnable)
            scroller!!.abortAnimation()
        }
    }

    private fun processAutoScroll(event: MotionEvent) {
        val y = event.y.toInt()
        if (y < mTopBound) {
            lastX = event.x
            lastY = event.y
            scrollDistance = -(mTopBound - y) / SCROLL_FACTOR
            if (!inTopSpot) {
                inTopSpot = true
                startAutoScroll()
            }
        } else if (y > mBottomBound) {
            lastX = event.x
            lastY = event.y
            scrollDistance = (y - mBottomBound) / SCROLL_FACTOR
            if (!inBottomSpot) {
                inBottomSpot = true
                startAutoScroll()
            }
        } else {
            inBottomSpot = false
            inTopSpot = false
            lastX = Float.MIN_VALUE
            lastY = Float.MIN_VALUE
            stopAutoScroll()
        }
    }


    private fun reset() {
        isActive = false
        start = RecyclerView.NO_POSITION
        end = RecyclerView.NO_POSITION
        lastStart = RecyclerView.NO_POSITION
        lastEnd = RecyclerView.NO_POSITION
        autoScrollHandler = Handler(Looper.getMainLooper())
        autoScrollHandler!!.removeCallbacks(scrollRunnable)
        inTopSpot = false
        inBottomSpot = false
        lastX = Float.MIN_VALUE
        lastY = Float.MIN_VALUE
        stopAutoScroll()
    }

    private fun scrollBy(distance: Int) {
        val scrollDistance = if (distance > 0) {
            distance.coerceAtMost(MAX_SCROLL_DISTANCE)
        } else {
            distance.coerceAtLeast(-MAX_SCROLL_DISTANCE)
        }
        recyclerView!!.scrollBy(0, scrollDistance)
        if (lastX != Float.MIN_VALUE && lastY != Float.MIN_VALUE) {
            updateSelectedRange(recyclerView!!, lastX, lastY)
        }
    }

    private fun updateSelectedRange(rv: RecyclerView, x: Float, y: Float) {
        val childView = rv.findChildViewUnder(x, y)
        if (childView != null) {
            val position = rv.getChildAdapterPosition(childView)
            if (position != RecyclerView.NO_POSITION && end != position) {
                end = position
                notifySelectRangeChange()
            }
        }
    }

    private fun notifySelectRangeChange() {
        if (selectListener == null) {
            return
        }
        if (start == RecyclerView.NO_POSITION || end == RecyclerView.NO_POSITION) {
            return
        }
        val newStart = start.coerceAtMost(end)
        val newEnd = start.coerceAtLeast(end)
        if (lastStart == RecyclerView.NO_POSITION || lastEnd == RecyclerView.NO_POSITION) {
            if (newEnd - newStart == 1) {
                selectListener!!.onSelectChange(newStart, newStart, true)
            } else {
                selectListener!!.onSelectChange(newStart, newEnd, true)
            }
        } else {
            if (newStart > lastStart) {
                selectListener!!.onSelectChange(lastStart, newStart - 1, false)
            } else if (newStart < lastStart) {
                selectListener!!.onSelectChange(newStart, lastStart - 1, true)
            }
            if (newEnd > lastEnd) {
                selectListener!!.onSelectChange(lastEnd + 1, newEnd, true)
            } else if (newEnd < lastEnd) {
                selectListener!!.onSelectChange(newEnd + 1, lastEnd, false)
            }
        }
        lastStart = newStart
        lastEnd = newEnd
    }

    fun setStartSelectPosition(position:Int){
        isActive = true
        start = position;
        end = position;
        lastStart = position;
        lastEnd = position;
    }


}