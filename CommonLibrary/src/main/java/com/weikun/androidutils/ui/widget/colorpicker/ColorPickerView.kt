package com.weikun.androidutils.ui.widget.colorpicker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import com.weikun.androidutils.utils.ImageUtil
import com.weikun.androidutils.utils.ScreenUtil
import kotlin.math.roundToInt

class ColorPickerView(context: Context?, initColor: Int, private var mHeight: Int, private var mWidth: Int, listener: OnSelectedColorListener?) : View(context) {
    private var mPaint //渐变色环画笔
            : Paint? = null
    private var mCenterPaint //中间圆画笔
            : Paint? = null
    private var mLinePaint //分隔线画笔
            : Paint? = null
    private var mRectPaint //渐变方块画笔
            : Paint? = null

    private var rectShader //渐变方块渐变图像
            : Shader? = null
    private var rectLeft //渐变方块左x坐标
            = 0f
    private var rectTop //渐变方块右x坐标
            = 0f
    private var rectRight //渐变方块上y坐标
            = 0f
    private var rectBottom //渐变方块下y坐标
            = 0f

    private lateinit var mCircleColors //渐变色环颜色
            : IntArray
    private lateinit var mRectColors //渐变方块颜色
            : IntArray
    private var r //色环半径(paint中部)
            = 0f
    private var centerRadius //中心圆半径
            = 0f

    private var downInCircle = true //按在渐变环上

    private var downInRect //按在渐变方块上
            = false
    private var highlightCenter //高亮
            = false
    private var highlightCenterLittle //微亮
            = false
    private val mRectF = RectF()
    private var mOnSelectedColorListener: OnSelectedColorListener? = listener

    private var mDrawable: Drawable? = null
    private var mBitmapShader: BitmapShader? = null

    fun setDrawable(drawable: BitmapDrawable) {
        mDrawable = drawable
        mBitmapShader = BitmapShader(drawable.bitmap, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR)
        invalidate()
    }

    fun getDrawable(): Drawable? {
        return mDrawable
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        //移动中心
        canvas.translate(mWidth / 2.toFloat(), mHeight / 2 - 50.toFloat())
        if (mDrawable != null) {
            mCenterPaint!!.shader = mBitmapShader
        } else {
            mCenterPaint!!.shader = null
        }
        //画中心圆
        canvas.drawCircle(0f, 0f, centerRadius, mCenterPaint!!)
        //是否显示中心圆外的小圆环
        if (highlightCenter || highlightCenterLittle) {
            val c = mCenterPaint!!.color
            mCenterPaint!!.style = Paint.Style.STROKE
            if (highlightCenter) {
                mCenterPaint!!.alpha = 0xFF
            } else if (highlightCenterLittle) {
                mCenterPaint!!.alpha = 0x90
            }
            canvas.drawCircle(0f, 0f,
                    centerRadius + mCenterPaint!!.strokeWidth, mCenterPaint!!)
            mCenterPaint!!.style = Paint.Style.FILL
            mCenterPaint!!.color = c
        }
        mRectF[-r, -r, r] = r
        //画色环
        canvas.drawOval(mRectF, mPaint!!)
        //画黑白渐变块
        if (downInCircle) {
            if (mRectColors[1] != mCenterPaint!!.color) {
                mRectColors[1] = mCenterPaint!!.color
                rectShader = LinearGradient(rectLeft, 0f, rectRight, 0f, mRectColors, null, Shader.TileMode.MIRROR)
            }
        }
        if (rectShader == null) {
            rectShader = LinearGradient(rectLeft, 0f, rectRight, 0f, mRectColors, null, Shader.TileMode.MIRROR)
        }
        mRectPaint!!.shader = rectShader
        canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, mRectPaint!!)
        val offset = mLinePaint!!.strokeWidth / 2
        canvas.drawLine(rectLeft - offset, rectTop - offset * 2,
                rectLeft - offset, rectBottom + offset * 2, mLinePaint!!) //左
        canvas.drawLine(rectLeft - offset * 2, rectTop - offset,
                rectRight + offset * 2, rectTop - offset, mLinePaint!!) //上
        canvas.drawLine(rectRight + offset, rectTop - offset * 2,
                rectRight + offset, rectBottom + offset * 2, mLinePaint!!) //右
        canvas.drawLine(rectLeft - offset * 2, rectBottom + offset,
                rectRight + offset * 2, rectBottom + offset, mLinePaint!!) //下
        super.onDraw(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x - mWidth / 2
        val y = event.y - mHeight / 2 + 50
        val inCircle = inColorCircle(x, y,
                r + mPaint!!.strokeWidth / 2, r - mPaint!!.strokeWidth / 2)
        val inCenter = inCenter(x, y, centerRadius)
        val inRect = inRect(x, y)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downInCircle = inCircle
                downInRect = inRect
                highlightCenter = inCenter
                if (downInCircle && inCircle) { //down按在渐变色环内, 且move也在渐变色环内
                    val angle = Math.atan2(y.toDouble(), x.toDouble()).toFloat()
                    var unit = (angle / (2 * Math.PI)).toFloat()
                    if (unit < 0) {
                        unit += 1f
                    }
                    mDrawable = null
                    mCenterPaint!!.color = interpCircleColor(mCircleColors, unit)
                } else if (downInRect && inRect) { //down在渐变方块内, 且move也在渐变方块内
                    mDrawable = null
                    mCenterPaint!!.color = getRectColor(mRectColors, x)
                }
                if (highlightCenter && inCenter || highlightCenterLittle && inCenter) { //点击中心圆, 当前移动在中心圆
                    highlightCenter = true
                    highlightCenterLittle = false
                } else if (highlightCenter || highlightCenterLittle) { //点击在中心圆, 当前移出中心圆
                    highlightCenter = false
                    highlightCenterLittle = true
                } else {
                    highlightCenter = false
                    highlightCenterLittle = false
                }
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                if (downInCircle && inCircle) {
                    val angle = Math.atan2(y.toDouble(), x.toDouble()).toFloat()
                    var unit = (angle / (2 * Math.PI)).toFloat()
                    if (unit < 0) {
                        unit += 1f
                    }
                    mDrawable = null
                    mCenterPaint!!.color = interpCircleColor(mCircleColors, unit)
                } else if (downInRect && inRect) {
                    mDrawable = null
                    mCenterPaint!!.color = getRectColor(mRectColors, x)
                }
                if (highlightCenter && inCenter || highlightCenterLittle && inCenter) {
                    highlightCenter = true
                    highlightCenterLittle = false
                } else if (highlightCenter || highlightCenterLittle) {
                    highlightCenter = false
                    highlightCenterLittle = true
                } else {
                    highlightCenter = false
                    highlightCenterLittle = false
                }
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                if (highlightCenter && inCenter) { //点击在中心圆, 且当前启动在中心圆
                    if (mOnSelectedColorListener != null) {
                        mOnSelectedColorListener!!.onSelected(mCenterPaint!!.color)
                    }
                }
                if (downInCircle) {
                    downInCircle = false
                }
                if (downInRect) {
                    downInRect = false
                }
                if (highlightCenter) {
                    highlightCenter = false
                }
                if (highlightCenterLittle) {
                    highlightCenterLittle = false
                }
                invalidate()
            }
        }
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(mWidth, mHeight)
    }

    /**
     * 坐标是否在色环上
     *
     * @param x         坐标
     * @param y         坐标
     * @param outRadius 色环外半径
     * @param inRadius  色环内半径
     * @return
     */
    private fun inColorCircle(x: Float, y: Float, outRadius: Float, inRadius: Float): Boolean {
        val outCircle = Math.PI * outRadius * outRadius
        val inCircle = Math.PI * inRadius * inRadius
        val fingerCircle = Math.PI * (x * x + y * y)
        return fingerCircle < outCircle && fingerCircle > inCircle
    }

    /**
     * 坐标是否在中心圆上
     *
     * @param x            坐标
     * @param y            坐标
     * @param centerRadius 圆半径
     * @return
     */
    private fun inCenter(x: Float, y: Float, centerRadius: Float): Boolean {
        val centerCircle = Math.PI * centerRadius * centerRadius
        val fingerCircle = Math.PI * (x * x + y * y)
        return fingerCircle < centerCircle
    }

    /**
     * 坐标是否在渐变色中
     *
     * @param x
     * @param y
     * @return
     */
    private fun inRect(x: Float, y: Float): Boolean {
        return x <= rectRight && x >= rectLeft && y <= rectBottom && y >= rectTop
    }

    /**
     * 获取圆环上颜色
     *
     * @param colors
     * @param unit
     * @return
     */
    private fun interpCircleColor(colors: IntArray, unit: Float): Int {
        if (unit <= 0) {
            return colors[0]
        }
        if (unit >= 1) {
            return colors[colors.size - 1]
        }
        var p = unit * (colors.size - 1)
        val i = p.toInt()
        p -= i.toFloat()

        // now p is just the fractional part [0...1) and i is the index
        val c0 = colors[i]
        val c1 = colors[i + 1]
        val a = ave(Color.alpha(c0), Color.alpha(c1), p)
        val r = ave(Color.red(c0), Color.red(c1), p)
        val g = ave(Color.green(c0), Color.green(c1), p)
        val b = ave(Color.blue(c0), Color.blue(c1), p)
        return Color.argb(a, r, g, b)
    }

    /**
     * 获取渐变块上颜色
     *
     * @param colors
     * @param x
     * @return
     */
    private fun getRectColor(colors: IntArray, x: Float): Int {
        val a: Int
        val r: Int
        val g: Int
        val b: Int
        val c0: Int
        val c1: Int
        val p: Float
        if (x < 0) {
            c0 = colors[0]
            c1 = colors[1]
            p = (x + rectRight) / rectRight
        } else {
            c0 = colors[1]
            c1 = colors[2]
            p = x / rectRight
        }
        a = ave(Color.alpha(c0), Color.alpha(c1), p)
        r = ave(Color.red(c0), Color.red(c1), p)
        g = ave(Color.green(c0), Color.green(c1), p)
        b = ave(Color.blue(c0), Color.blue(c1), p)
        return Color.argb(a, r, g, b)
    }

    private fun ave(s: Int, d: Int, p: Float): Int {
        return s + (p * (d - s)).roundToInt()
    }

    fun setColor(color: Int) {
        mCenterPaint!!.color = color
        mRectColors[1] = mCenterPaint!!.color
    }

    fun getColor(): Int {
        return mCenterPaint!!.color
    }

    interface OnSelectedColorListener {
        fun onSelected(color: Int)
    }

    init {
        minimumHeight = mHeight
        minimumWidth = mWidth
        mCircleColors = intArrayOf(-0x10000, -0xff01, -0xffff01,
                -0xff0001, -0xff0100, -0x100, -0x10000)
        val s: Shader = SweepGradient(0f, 0f, mCircleColors, null)
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint!!.shader = s
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.strokeWidth = ScreenUtil.dip2px(30f)
        r = mWidth / 2 * 0.7f - mPaint!!.strokeWidth * 0.5f
        mCenterPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mCenterPaint!!.color = initColor
        mCenterPaint!!.strokeWidth = 5f
        centerRadius = (r - mPaint!!.strokeWidth / 2) * 0.7f
        mLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mLinePaint!!.color = Color.parseColor("#72A1D1")
        mLinePaint!!.strokeWidth = 4f
        mRectColors = intArrayOf(-0x1000000, mCenterPaint!!.color, -0x1)
        mRectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mRectPaint!!.strokeWidth = ScreenUtil.dip2px(5f)
        rectLeft = -r - mPaint!!.strokeWidth * 0.5f
        rectTop = r + mPaint!!.strokeWidth * 0.5f + mLinePaint!!.strokeMiter * 0.5f + 15
        rectRight = r + mPaint!!.strokeWidth * 0.5f
        rectBottom = rectTop + ScreenUtil.dip2px(30f)
    }
}