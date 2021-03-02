package com.weikun.androidutils.ui.widget.recyclerview

interface ItemCreator {
    val resId:Int
    fun onBindView(holder: BaseViewHolder,position:Int)
}