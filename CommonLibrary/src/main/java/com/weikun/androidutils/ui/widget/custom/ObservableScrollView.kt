package com.weikun.kungallery.ui.view

import android.content.Context
import android.widget.ScrollView

/**
 * @author lwk
 * @date 2020/5/8
 */
class ObservableScrollView(context: Context?) : ScrollView(context) {
    private var scrollViewListener: ScrollViewListener? = null
    fun setScrollViewListener(scrollViewListener: ScrollViewListener?) {
        this.scrollViewListener = scrollViewListener
    }

    override fun onScrollChanged(x: Int, y: Int, oldx: Int, oldy: Int) {
        super.onScrollChanged(x, y, oldx, oldy)
        if (scrollViewListener != null) {
            scrollViewListener!!.onScrollChanged(this, x, y, oldx, oldy)
        }
    }

    interface ScrollViewListener {
        fun onScrollChanged(scrollView: ObservableScrollView?, x: Int, y: Int, oldx: Int, oldy: Int)
    }
}