package com.weikun.androidutils.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 *   @author linweikun
 *   @date   2021/1/13
 *
 */
object CommonUtils {
    /**
     * 收起软键盘
     * @param view 当前操作视图
     */
    @JvmStatic
    fun hideKeyboard(view: View?) {
        view?.let{
            val imm = it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}