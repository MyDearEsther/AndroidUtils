package com.weikun.androidutils.ui.widget.recyclerview
/**
 *   @author lwk
 *   @date   2020/8/19
 *
 */
interface OnListItemChangeListener{
    fun onReload()
    fun onRangeLoad(start:Int,end:Int)
    fun onItemChange(index:Int)
}