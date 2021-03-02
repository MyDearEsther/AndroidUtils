package com.weikun.androidutils.ui.widget.recyclerview
import android.view.View

/**
 * 列表适配器点击事件接口
 * @author lwk
 * @date 2019/7/31
 */
interface OnItemClickListener<T> {
    /**
     * 点击事件
     * @param item 数据
     * @param position 条目位置
     */
    fun onItemClick(v:View,item: T, position: Int)

    /**
     * 长按点击事件
     * @param item 数据
     * @param position 条目位置
     */
    fun onItemLongClick(v:View,item: T, position: Int): Boolean
}