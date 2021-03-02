package com.weikun.androidutils.utils

import android.util.Log

/**
 * 日志 工具类
 * @author lwk
 * @date 2020/4/24
 */
object LogUtil {
    var TAG = "Debug"
    var debug: Boolean = true

    @JvmStatic
    fun e(text: String) {
        if (debug){
            Log.e(TAG, text)
        }
    }

    @JvmStatic
    fun d(text: String) {
        if (debug){
            Log.d(TAG, text)
        }
    }
}