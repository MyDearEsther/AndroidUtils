package com.weikun.androidutils.ui.widget.chooser

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.weikun.androidutils.utils.ImageUtil
import com.weikun.androidutils.utils.ScreenUtil

/**
 *   自定义滑动条
 *   @author lwk
 *   @date   2020/8/31
 */
class SeekBar:LinearLayout{
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    interface OnSeekBarChangeListener{
        fun onChange(seekBar: SeekBar,value:Int)
        fun onStart(seekBar: SeekBar)
        fun onEnd(seekBar: SeekBar)
    }

    private val seekBar = SeekBar(context).apply {
        val lp = LayoutParams(0, WRAP_CONTENT)
        lp.weight = 3f
        lp.topMargin = ScreenUtil.dip2px(8f).toInt()
        lp.bottomMargin = ScreenUtil.dip2px(8f).toInt()
        lp.marginStart = ScreenUtil.dip2px(16f).toInt()
        lp.marginEnd = ScreenUtil.dip2px(16f).toInt()
        layoutParams = lp
        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar, p1: Int, p2: Boolean) {
                if (changeListener!=null){
                    changeListener!!.onChange(p0,p1)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar) {
                changeListener!!.onStart(p0)
            }

            override fun onStopTrackingTouch(p0: SeekBar) {
                changeListener!!.onEnd(p0)
            }

        })
    }
    private val titleView = TextView(context).apply {
        val lp = LayoutParams(0, WRAP_CONTENT)
        lp.weight = 1f
        lp.topMargin = ScreenUtil.dip2px(8f).toInt()
        lp.bottomMargin = ScreenUtil.dip2px(8f).toInt()
        lp.marginStart = ScreenUtil.dip2px(8f).toInt()
        lp.marginEnd = ScreenUtil.dip2px(8f).toInt()
        layoutParams = lp
        text = title
        setTextColor(Color.WHITE)
    }
    private val valueView = TextView(context).apply {
        val lp = LayoutParams(0, WRAP_CONTENT)
        lp.weight = 1f
        lp.topMargin = ScreenUtil.dip2px(8f).toInt()
        lp.bottomMargin = ScreenUtil.dip2px(8f).toInt()
        lp.marginStart = ScreenUtil.dip2px(8f).toInt()
        lp.marginEnd = ScreenUtil.dip2px(8f).toInt()
        layoutParams = lp
        setTextColor(Color.WHITE)
    }

    var title:String? = null
        set(value) {
            field = value
            titleView.text = title
        }

    var changeListener:OnSeekBarChangeListener? = null

    init {
        this.layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        this.orientation = HORIZONTAL
        this.addView(titleView)
        this.addView(seekBar)
        this.addView(valueView)
    }

}