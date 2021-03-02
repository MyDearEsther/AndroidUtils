package com.weikun.androidutils.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.view.View
import android.view.WindowManager

/**
 *   @author linweikun
 *   @date   2021/1/22
 *
 */
object ScreenUtil {
    @JvmStatic
    fun getScreenWidth(context: Context):Int{
        val point = Point()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getRealSize(point)
        return point.x
    }
    @JvmStatic
    fun getScreenHeight(context: Context):Int{
        val point = Point()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getRealSize(point)
        return point.y
    }


    private var DENSITY = Resources.getSystem().displayMetrics.density

    /**
     * dp -> 像素
     */
    @JvmStatic
    fun dip2px(dpValue: Float): Float {
        return dpValue * DENSITY + 0.5f
    }

    /**
     * 像素 -> dp
     */
    @JvmStatic
    fun px2dip(pxValue: Int): Int {
        val scale = DENSITY
        return (pxValue / scale + 0.5f).toInt()
    }
}