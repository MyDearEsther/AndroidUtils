package com.weikun.androidutils.ui.widget.recyclerview

interface NestedOnItemSelectListener{

    fun onItemSelect(parentPosition:Int,childPosition:Int,totalPosition: Int,check:Boolean)

    fun onSelectModeChange(enable:Boolean)
}