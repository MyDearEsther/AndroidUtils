package com.weikun.androidutils.ui.widget.recyclerview

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.weikun.androidutils.utils.LogUtil

/**
 *   @author linweikun
 *   @date   2021/2/19
 *
 */
class ScrollBar(private val recyclerView: Recyclerview) {
    var scrollBarHeight: Float = 0f
    var scrollBarWidth: Float = 0f
    var rangeRect: Rect = Rect()
    var rangePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    //Y轴的偏移值
    var yScrollOffset = 0

    //可视区域的高度/所有的子view的总高度 得出的比例
    var range = 0f

    //柱间隙
    var scrollWidthSpace = 0f

    //recyclerView的每个Item项的宽度
    var childWidth = 0

    //滚动条宽度的等分比例
    var scrollWidthWeight = 20f

    //可视区域的高度
    var visualHeight = 0

    //所有的子view的总高度
    var childViewAllHeight = 0f

    //判断触摸焦点
    var isFocus = false

    //手触摸时点的x,y坐标
    var touchX = 0f
    var touchY = 0f

    init {
        rangePaint.style = Paint.Style.FILL
        recyclerView.addOnScrollListener(onScrollListener)
    }


    private val onScrollListener: RecyclerView.OnScrollListener
        get() = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    //当屏幕滚动且用户使用的触碰或手指还在屏幕上，停止加载图片
                    RecyclerView.SCROLL_STATE_DRAGGING,
                        //由于用户的操作，屏幕产生惯性滑动，停止加载图片
                    RecyclerView.SCROLL_STATE_SETTLING -> {
                        Glide.with(recyclerView.context).pauseRequests()
                    }
                    //静止
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        Glide.with(recyclerView.context).resumeRequests()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                yScrollOffset += (dy * range).toInt()
            }
        }


    fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val width = View.MeasureSpec.getSize(widthSpec)
        scrollBarWidth = width / scrollWidthWeight
        scrollWidthSpace = scrollBarWidth / 10
        childWidth = (width - scrollBarWidth).toInt()
        scrollBarWidth -= 2 * scrollWidthSpace
    }

    fun dispatchDraw(canvas: Canvas) {
        rangeRect.set((childWidth + scrollWidthSpace).toInt(),
            yScrollOffset.toInt(),
            (childWidth + scrollBarWidth).toInt(),
            (yScrollOffset + scrollBarHeight).toInt());
        rangePaint.color =
            if (isFocus) Color.parseColor("#2386BF")
            else Color.parseColor("#2EB3FF")
        canvas.drawRect(rangeRect, rangePaint)
    }

    fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        visualHeight = recyclerView.measuredHeight
        if (recyclerView.childCount < 3) {
            visualHeight = 0
            childViewAllHeight = 0f
        } else {
            childViewAllHeight = (recyclerView.getChildAt(2).height
                    * recyclerView.adapter!!.itemCount).toFloat()
        }
        range = 0f
        if (childViewAllHeight != 0f) {
            range = visualHeight / childViewAllHeight
//            LogUtil.d("onLayout $visualHeight/$childViewAllHeight")
        }
        scrollBarHeight = range * visualHeight
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                //获取屏幕上点击的坐标
                touchX = event.x
                touchY = event.y
                if (touchX >= rangeRect.left && touchX <= rangeRect.right
                    && touchY >= rangeRect.top
                    && touchY <= rangeRect.bottom) {
                    isFocus = true
                    recyclerView.invalidate()
                }
            }
            MotionEvent.ACTION_MOVE -> if (touchX >= rangeRect.left
                && touchX <= rangeRect.right && touchY >= rangeRect.top
                && touchY <= rangeRect.bottom) {
                val diffValue = event.y - touchY
                recyclerView.scrollBy(0, (diffValue / visualHeight * childViewAllHeight).toInt())
                touchY = event.y
            }
            MotionEvent.ACTION_UP -> {
                isFocus = false
                recyclerView.invalidate()
            }
        }
        return touchX >= childWidth && touchX <= recyclerView.measuredWidth
                && touchY >= 0 && touchY <= recyclerView.measuredHeight
    }
}