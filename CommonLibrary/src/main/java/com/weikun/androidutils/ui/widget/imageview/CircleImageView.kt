package com.weikun.androidutils.ui.widget.imageview

import android.R.attr.radius
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView


/**
 *   @author lwk
 *   @date   2020/10/13
 */
class CircleImageView : AppCompatImageView {
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }
    private var mRadius = 0f

    private var mPaint: Paint? = null

    //用来裁剪图片的path
    private var mPath: Path? = null

    // 图片区域大小的path
    private var mSrcPath: Path? = null

    // 图片占的矩形区域
    private var mSrcRectF: RectF? = null

    // 边框的矩形区域
    private var mBorderRectF: RectF? = null
    private var mXfermode: Xfermode? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initSrcRectF()
    }

    override fun onDraw(canvas: Canvas) {
        // 使用离屏缓存，新建一个srcRectF区域大小的图层
        canvas.saveLayer(mSrcRectF, null, Canvas.ALL_SAVE_FLAG)
        // ImageView自身的绘制流程
        super.onDraw(canvas)
        mPath!!.addCircle(width / 2.0f, height / 2.0f, mRadius, Path.Direction.CCW);
        mPaint!!.isAntiAlias = true
        // 画笔为填充模式
        mPaint!!.style = Paint.Style.FILL
        // 设置混合模式
        mPaint!!.xfermode = mXfermode
        // 绘制path
        canvas.drawPath(mPath!!, mPaint!!);
        // 清除Xfermode
        mPaint!!.xfermode = null
        // 恢复画布状态
        canvas.restore();
    }

    private fun init() {
        mBorderRectF = RectF()
        mSrcRectF = RectF()
        mPath = Path()
        mPaint = Paint()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            mXfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        } else {
            mXfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            mSrcPath = Path()
        }
    }

    /**
     * 计算图片原始区域的RectF
     */
    private fun initSrcRectF() {
        mRadius = width.coerceAtMost(height) / 2.0f
        mSrcRectF!!.set(width / 2.0f - radius, height / 2.0f - radius, width / 2.0f + radius, height / 2.0f + radius)
    }
}