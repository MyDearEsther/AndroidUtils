package com.weikun.androidutils.utils

import android.app.Activity
import android.os.Build

/**
 *  系统工具类
 *   @author lwk
 *   @date   2020/8/19
 */
object SystemUtil {
    /**
     * 适配高帧率
     * @param force 强制最高帧率
     * */
    @JvmStatic
    fun supportHighFrameRate(force: Boolean,activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val modeId = if (force) {
            // 获取系统window支持的模式
            val modes = activity.window.windowManager.defaultDisplay.supportedModes
            // 对获取的模式，基于刷新率的大小进行排序，从小到大排序
            modes.sortBy {
                it.refreshRate
            }
            modes.last().modeId
        } else {
            activity.window.windowManager.defaultDisplay.mode.modeId
        }
        activity.window.let {
            val lp = it.attributes
            // 取出最大的那一个刷新率，直接设置给window
            lp.preferredDisplayModeId = modeId
            it.attributes = lp
        }
    }
}