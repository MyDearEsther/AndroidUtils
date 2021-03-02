package com.weikun.kungallery.ui.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup

class RippleAnimation : View {
    private var mRootView: ViewGroup? = null
    private var mBackground: Bitmap? = null
    private var mStartX = 0f
    private var mStartY = 0f
    private var mMaxRadius = 0
    private var mStartRadius = 0
    private var mCurrentRadius = 0
    private var mPaint: Paint? = null
    private var isStarted = false
    private var mDuration: Long = 400
    private var mOnAnimationEndListener: OnAnimationEndListener? = null
    private var mAnimatorListener: Animator.AnimatorListener? = null
    private var mAnimatorUpdateListener: AnimatorUpdateListener? = null

    constructor(context: Context, startX: Float, startY: Float, radius: Int) : super(context) {
        mRootView = (context as Activity).window.decorView as ViewGroup
        mStartX = startX
        mStartY = startY
        mStartRadius = radius
        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        //设置为擦除模式
        mPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {}

    /**
     * try get host activity from view.
     * views hosted on floating window like dialog and toast will sure return null.
     *
     * @return host activity
     */
    private fun getActivityFromContext(context: Context): Activity {
        var context: Context? = context
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = context.baseContext
        }
        throw RuntimeException("Activity not found!")
    }

    override fun onDraw(canvas: Canvas) {
        //在新的图层上面绘制
        val layer = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        canvas.drawBitmap(mBackground!!, 0f, 0f, null)
        canvas.drawCircle(mStartX, mStartY, mCurrentRadius.toFloat(), mPaint!!)
        canvas.restoreToCount(layer)
    }

    /**
     * 根据起始点将屏幕分成4个小矩形,mMaxRadius就是取它们中最大的矩形的对角线长度
     * 这样的话, 无论起始点在屏幕中的哪一个位置上, 我们绘制的圆形总是能覆盖屏幕
     */
    private fun updateMaxRadius() {
        //将屏幕分成4个小矩形
        val leftTop = RectF(0f, 0f, mStartX + mStartRadius, mStartY + mStartRadius)
        val rightTop = RectF(leftTop.right, 0f, mRootView!!.right.toFloat(), leftTop.bottom)
        val leftBottom = RectF(0f, leftTop.bottom, leftTop.right, mRootView!!.bottom.toFloat())
        val rightBottom = RectF(leftBottom.right, leftTop.bottom, mRootView!!.right.toFloat(), leftBottom.bottom)
        //分别获取对角线长度
        val leftTopHypotenuse = Math.sqrt(Math.pow(leftTop.width().toDouble(), 2.0) + Math.pow(leftTop.height().toDouble(), 2.0))
        val rightTopHypotenuse = Math.sqrt(Math.pow(rightTop.width().toDouble(), 2.0) + Math.pow(rightTop.height().toDouble(), 2.0))
        val leftBottomHypotenuse = Math.sqrt(Math.pow(leftBottom.width().toDouble(), 2.0) + Math.pow(leftBottom.height().toDouble(), 2.0))
        val rightBottomHypotenuse = Math.sqrt(Math.pow(rightBottom.width().toDouble(), 2.0) + Math.pow(rightBottom.height().toDouble(), 2.0))
        //取最大值
        mMaxRadius = Math.max(
                Math.max(leftTopHypotenuse, rightTopHypotenuse),
                Math.max(leftBottomHypotenuse, rightBottomHypotenuse)).toInt()
    }

    fun start() {
        if (!isStarted) {
            isStarted = true
            updateBackground()
            attachToRootView()
            animator.start()
        }
    }

    /**
     * 更新屏幕截图
     */
    private fun updateBackground() {
        if (mBackground != null && !mBackground!!.isRecycled) {
            mBackground!!.recycle()
        }
        mRootView!!.isDrawingCacheEnabled = true
        mBackground = mRootView!!.drawingCache
        mBackground = Bitmap.createBitmap(mBackground!!)
        mRootView!!.isDrawingCacheEnabled = false
    }

    /**
     * 添加到根视图
     */
    private fun attachToRootView() {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mRootView!!.addView(this)
    }

    private val animator: ValueAnimator
        private get() {
            val valueAnimator = ValueAnimator.ofFloat(0f, mMaxRadius.toFloat()).setDuration(mDuration)
            valueAnimator.addUpdateListener(mAnimatorUpdateListener)
            valueAnimator.addListener(mAnimatorListener)
            return valueAnimator
        }

    private fun initListener() {
        mAnimatorListener = object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (mOnAnimationEndListener != null) {
                    mOnAnimationEndListener!!.onAnimationEnd()
                }
                isStarted = false
                //动画播放完毕, 移除本View
                detachFromRootView()
            }
        }
        mAnimatorUpdateListener = AnimatorUpdateListener { animation -> //更新圆的半径
            mCurrentRadius = (animation.animatedValue as Float).toInt() + mStartRadius
            postInvalidate()
        }
    }

    /**
     * 从根视图中移除
     */
    private fun detachFromRootView() {
        mRootView!!.removeView(this)
    }

    fun setDuration(duration: Long): RippleAnimation {
        mDuration = duration
        return this
    }

    /**
     * 设置动画结束监听器
     */
    fun setOnAnimationEndListener(listener: OnAnimationEndListener?): RippleAnimation {
        mOnAnimationEndListener = listener
        return this
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return true
    }

    interface OnAnimationEndListener {
        fun onAnimationEnd()
    }

    companion object {
        fun create(onClickView: View): RippleAnimation {
            val context = onClickView.context
            val newWidth = onClickView.width / 2
            val newHeight = onClickView.height / 2
            //计算起点位置
            val startX = getAbsoluteX(onClickView) + newWidth
            val startY = getAbsoluteY(onClickView) + newHeight
            //起始半径
            //因为我们要避免遮挡按钮
            val radius = Math.max(newWidth, newHeight)
            return RippleAnimation(context, startX, startY, radius)
        }

        /**
         * 获取view在屏幕中的绝对x坐标
         */
        private fun getAbsoluteX(view: View): Float {
            var x = view.x
            val parent = view.parent
            if (parent is View) {
                x += getAbsoluteX(parent as View)
            }
            return x
        }

        /**
         * 获取view在屏幕中的绝对y坐标
         */
        private fun getAbsoluteY(view: View): Float {
            var y = view.y
            val parent = view.parent
            if (parent is View) {
                y += getAbsoluteY(parent as View)
            }
            return y
        }
    }
}