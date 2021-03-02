package com.weikun.androidutils.common

/**
 * 菜单点击事件
 * @author lwk
 * @date 2020/8/25
 */
interface OnMenuItemClickListener {
    /**
     * 点击事件
     */
    fun onItemClick(itemId: Int): Boolean

    /**
     * 长按点击事件
     */
    fun onItemLongClick(itemId: Int): Boolean
}