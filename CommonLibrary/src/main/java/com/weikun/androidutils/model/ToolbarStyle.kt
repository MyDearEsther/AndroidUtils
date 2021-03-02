package com.weikun.androidutils.model

import com.weikun.androidutils.common.OnMenuItemClickListener

/**
 * Toolbar属性 实体类
 * 定制Toolbar
 * @author lwk
 * @date 2020/4/22
 */
class ToolbarStyle {
    /**
     * 标题
     */
    var title: String? = null

    /**
     * 导航图标资源ID
     */
    var navIconId: Int? = null

    var iconColor: Int? = null

    var textColor: Int? = null

    var backgroundColor: Int? = null

    /**
     * 菜单资源ID
     */
    var menuId: Int? = null

    /**
     * Toolbar菜单回调
     */
    var menuCallback: OnMenuItemClickListener? = null


    constructor()

    constructor(title: String?, navIconId: Int?, iconColor: Int?, textColor: Int?, backgroundColor: Int?) {
        this.title = title
        this.navIconId = navIconId
        this.iconColor = iconColor
        this.textColor = textColor
        this.backgroundColor = backgroundColor
    }
}