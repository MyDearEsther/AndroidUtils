package com.weikun.androidutils.ui.widget.recyclerview

import android.view.View

interface NestedOnItemClickListener<T>{
    /**
     * 点击事件
     * @param item 数据
     */
    fun onItemClick(v: View, item: T, parentPosition:Int,totalPosition: Int,childPosition:Int)

    /**
     * 长按点击事件
     * @param item 数据
     * @param position 条目位置
     */
    fun onItemLongClick(v: View, item: T, parentPosition:Int,totalPosition: Int,childPosition:Int): Boolean
}