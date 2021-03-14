package com.weikun.androidutils.ui.widget.imageview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.weikun.androidutils.R
import com.weikun.androidutils.utils.LogUtil


/**
 *   圆形图片
 *   Path切割
 *   Android显示圆形图片的4种方式:
 *   https://segmentfault.com/a/1190000012253911
 *   @author lwk
 *   @date   2020/10/13
 */
class RoundImageView : AppCompatImageView {
    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        getAttributes(context,attrs)
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        getAttributes(context,attrs)
        initView(context)
    }

    private lateinit var mRect: RectF
    private lateinit var mPath: Path
    private var mRadius = 0f


    /**
     * 获取属性
     */
    private fun getAttributes(context: Context, attrs: AttributeSet?) {
        attrs?.let {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView)
            mRadius = ta.getDimension(R.styleable.RoundImageView_radius, 0f)
            ta.recycle()
        }
    }

    /**
     * 初始化
     */
    private fun initView(context: Context) {
        mRect = RectF()
        mPath = Path()
        setLayerType(View.LAYER_TYPE_SOFTWARE, null) // 禁用硬件加速
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (mRadius < 0) {
            clipCircle(w, h)
        } else {
            clipRoundRect(w, h)
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.clipPath(mPath)
        // ImageView自身的绘制流程
        super.onDraw(canvas)
    }

    /**
     * 圆角
     */
    private fun clipRoundRect(width: Int, height: Int) {
        mRect.left = 0f
        mRect.top = 0f
        mRect.right = width.toFloat()
        mRect.bottom = height.toFloat()
        mPath.addRoundRect(mRect, mRadius, mRadius, Path.Direction.CW)
    }

    /**
     * 圆形
     */
    private fun clipCircle(width: Int, height: Int) {
        val radius = width.coerceAtMost(height) / 2
        mPath.addCircle((width / 2).toFloat(), (height / 2).toFloat(), radius.toFloat(), Path.Direction.CW)
    }


}