package com.weikun.androidutils.ui.widget.recyclerview

/**
 *   列表单项实体
 *   包含数据及其他状态量
 *   @author lwk
 *   @date   2020/8/19
 *
 */
class ListItem<E>(val item: E) {
    /**
     * 是否已被选择
     */
    var selected: Boolean = false
}
