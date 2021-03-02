package com.weikun.androidutils.ui.widget.custom

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.Gravity.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.view.children
import androidx.core.view.get
import com.weikun.androidutils.R
import com.weikun.androidutils.common.OnMenuItemClickListener
import com.weikun.androidutils.utils.ImageUtil
import com.weikun.androidutils.utils.ScreenUtil
import kotlin.collections.ArrayList


/**
 *   菜单工具栏
 *   布局结构
 *   - LinearLayout(根布局)
 *     - Scrollview(横向滚动容器)
 *          - LinearLayout(横向菜单容器)
 *              - LinearLayout(纵向菜单选项容器)
 *                  - FrameLayout(图标容器)
 *                      - ImageView (图标)
 *                  - TextView(标题)
 *              - ...
 *     - ...
 *     - BottomView (自定义View 宽度一致)
 *     - ...
 *   @author lwk
 *   @date   2020/8/25
 */
class ToolWindow : LinearLayout {
    companion object {
        //垂直弹出式
        const val STYLE_VERTICAL = 0

        //横向工具条式
        const val STYLE_HORIZONTAL = 1

        //悬浮中心弹出式
        const val STYLE_CIRCLE = 2

//        //默认图标大小
//        const val DEFAULT_ICON_SIZE = 20

        //默认文本大小
        const val DEFAULT_TEXT_SIZE = 12f
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initAttrs(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initAttrs(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initAttrs(attrs)
    }

    private fun initAttrs(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ToolWindow)
        titleTextColor = typedArray.getInteger(R.styleable.ToolWindow_toolTitleTextColor, Color.BLACK)
        displayCount = typedArray.getInteger(R.styleable.ToolWindow_toolDisplayCount, 4)
        titleTextSize = typedArray.getFloat(R.styleable.ToolWindow_toolTitleTextSize, DEFAULT_TEXT_SIZE)
        style = typedArray.getInteger(R.styleable.ToolWindow_toolStyle, STYLE_HORIZONTAL)
        selectable = typedArray.getBoolean(R.styleable.ToolWindow_toolItemSelectable, false)
        fixedWidth = typedArray.getDimensionPixelSize(R.styleable.ToolWindow_fixedItemWidth, 0)
        iconColor = typedArray.getInteger(R.styleable.ToolWindow_toolIconColor, -1)
        val menuId = typedArray.getResourceId(R.styleable.ToolWindow_toolMenu, -1)
        if (menuId != -1) {
            menuIds = intArrayOf(menuId)
        }
        typedArray.recycle()
    }


    //图标容器 布局参数
    private val iconContainerLp = LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
        weight = 1f
    }

    //标题 布局参数
    private val titleLp = LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
        setMargins(0, ScreenUtil.dip2px(8f).toInt(), 0, ScreenUtil.dip2px(8f).toInt())
        gravity = CENTER_HORIZONTAL
    }

    //图标 布局参数
    private val iconLp = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, CENTER)

    //显示数量
    private var displayCount = 4

    //标题字体颜色
    var titleTextColor = -1

    //标题字体大小
    private var titleTextSize = 12f

    //显示样式
    private var style = STYLE_HORIZONTAL

    //菜单资源ID
    private var menuIds: IntArray = intArrayOf()

    //选择模式
    var selectable: Boolean = false

    private var fixedWidth: Int = 0

    private var iconColor = -1


    //菜单点击监听器
    var clickListener: OnMenuItemClickListener? = null

    private val bottomViews = ArrayList<View?>()


    init {
        this.orientation = VERTICAL
    }

    private var touchTool = false

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        //拦截点击事件 如果点击区域在工具栏内则传递点击事件
        for (index in 0 until this.childCount) {
            if (isTouchPointInView(this[index], ev.rawX.toInt(), ev.rawY.toInt())) {
                touchTool = true
                return super.dispatchTouchEvent(ev)
            }
        }
        if (touchTool) {
            if (ev.action == MotionEvent.ACTION_MOVE) {
                return super.dispatchTouchEvent(ev)
            } else if (ev.action == MotionEvent.ACTION_UP) {
                touchTool = false
            }
        }
        return false
    }

    /**
     * 加载并显示菜单项
     * @param menuIds 菜单XML资源ID
     */
    fun inflateMenu(menuIds: IntArray, animated: Boolean) {
        inflateMenu(menuIds, animated, null)
    }

    /**
     * 显示并加载菜单项
     */
    fun inflateMenu(menuIds: IntArray, animated: Boolean, views: Array<View?>?) {
        for (view in bottomViews) {
            this.removeView(view)
        }
        this.bottomViews.clear()
        if (views != null) {
            for (view in views) {
                this.bottomViews.add(view)
            }
        }
        this.menuIds = menuIds
        inflateMenu(animated)
    }



    private fun inflateBottomView() {
        if (bottomViews.size > 0) {
            for (bottomView in bottomViews) {
                this.addView(bottomView)
            }
        }
    }


    private val exitAnimation = AnimationUtils.loadAnimation(context, R.anim.bottom_exit).apply {
        setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                //清除父容器已添加的子View
                clearMenu()
                inflateMenu(true)
            }

            override fun onAnimationStart(p0: Animation?) {
                //动画开始时防止点击
                for (menuBar in children) {
                    if (menuBar is ScrollView && menuBar.childCount > 0) {
                        val container = menuBar[0]
                        if (container is LinearLayout) {
                            for (index in 0 until container.childCount) {
                                container[index].isEnabled = false
                            }
                        }
                    }
                }
            }
        })
    }

    private val enterAnimation = AnimationUtils.loadAnimation(context, R.anim.bottom_enter).apply {
        setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                for (menuBar in children) {
                    if (menuBar is ScrollView && menuBar.childCount > 0) {
                        val container = menuBar[0]
                        if (container is LinearLayout) {
                            for (index in 0 until container.childCount) {
                                container[index].isEnabled = true
                            }
                        }
                    }
                }
            }

            override fun onAnimationStart(p0: Animation?) {
                inflateBottomView()
            }
        })
    }

    /**
     * 加载并显示菜单项
     */
    fun inflateMenu(animated: Boolean) {
        if (childCount > 0) {
            if (animated) {
                startAnimation(exitAnimation)
                return
            } else {
                clearMenu()
            }
        }
        if (menuIds.isNotEmpty()) {
            for (menuId in menuIds) {
                //使用PopupMenu自建菜单及MenuInflater解析菜单XML
                val menu = PopupMenu(context, null).menu
                menu.clear()
                MenuInflater(context).inflate(menuId, menu)
                val scrollView = createMenuContainer()
                val container = scrollView[0] as LinearLayout
                container.weightSum = menu.size().toFloat()
                //创建菜单选项
                for (index in 0 until menu.size()) {
                    val item = menu[index]
                    //Item容器
                    val itemContainer = createItemContainer(item.itemId)
                    //Item图标
                    val icon = createIcon(item.icon)
                    itemContainer.addView(icon)
                    //Item标题
                    if (item.title != null) {
                        val title = createTitle(item.title.toString())
                        itemContainer.addView(title)
                    }
                    container.addView(itemContainer)
                }
                this.addView(scrollView)
            }
        }
        if (animated) {
            startAnimation(enterAnimation)
        } else {
            inflateBottomView()
        }
    }

    fun clearMenu() {
        this.removeAllViews()
    }

    /**
     * 创建标题
     * @param text 标题文字
     */
    private fun createTitle(text: String): TextView {
        val titleView = TextView(context)
        titleView.text = text
        titleView.layoutParams = titleLp
        titleView.textSize = titleTextSize
        titleView.setTextColor(titleTextColor)
        titleView.maxLines = 1
        return titleView
    }

    /**
     * 创建图标
     * @param drawable 图标资源
     * */
    private fun createIcon(drawable: Drawable): FrameLayout {
        val iconContainer = FrameLayout(context)
        iconContainer.layoutParams = iconContainerLp
        val icon = ImageView(context)
        val coloredDrawable = ImageUtil.tintDrawable(drawable, iconColor)
        icon.setImageDrawable(coloredDrawable)
        icon.layoutParams = iconLp
        iconContainer.addView(icon)

        return iconContainer
    }

    private fun createMenuContainer(): ScrollView {
        val menuBar = ScrollView(context)
        menuBar.layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        val container = LinearLayout(context)
        container.layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        container.orientation = HORIZONTAL
        menuBar.addView(container)
        return menuBar
    }

    /**
     * 创建菜单容器
     * @param id 菜单ID
     */
    private fun createItemContainer(id: Int): LinearLayout {
        val container = LinearLayout(context)
        container.orientation = VERTICAL
        val size = ScreenUtil.dip2px(80f).toInt()
        val containerLp = if (fixedWidth == 0) {
            //自适应充满
            LayoutParams(0, WRAP_CONTENT).apply {
                weight = 1f
                topMargin = ScreenUtil.dip2px(25f).toInt()
                bottomMargin = ScreenUtil.dip2px(25f).toInt()
            }
        } else {
            //固定宽度
            LayoutParams(size, size)
        }
        container.layoutParams = containerLp
        val typedValue = TypedValue()
        val attribute = intArrayOf(android.R.attr.selectableItemBackgroundBorderless)
        val typedArray = context.theme.obtainStyledAttributes(typedValue.resourceId, attribute)
        val drawable = typedArray.getDrawable(0)
        container.background = drawable
        typedArray.recycle()
        //Item容器点击事件
        container.setOnClickListener {
            if (clickListener != null) {
                clickListener!!.onItemClick(id)
            }
        }
        //Item容器长按事件
        container.setOnLongClickListener {
            if (clickListener != null && clickListener!!.onItemLongClick(id)) {
                return@setOnLongClickListener true
            }
            false
        }
        return container
    }

    fun setIconColor(menuId: Int, color: Int, itemIndex: Int) {
        val index = this.menuIds.indexOf(menuId)
        if (index == -1) {
            return
        }
        val container = getChildAt(index)
        if (container !is ScrollView) {
            return
        }
        val menuContainer = container[0]
        if (menuContainer !is LinearLayout) {
            return
        }
        if (itemIndex == -1) {
            for (child in menuContainer.children) {
                ImageUtil.tintDrawable((((child as LinearLayout)[0] as FrameLayout)[0] as ImageView).drawable, color)
            }
        } else {
            ImageUtil.tintDrawable((((menuContainer[itemIndex] as LinearLayout)[0] as FrameLayout)[0] as ImageView).drawable, color)

        }
    }

    /**
     * 判断(x,y)是否在指定View的区域范围内
     */
    private fun isTouchPointInView(view: View, x: Int, y: Int): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val left = location[0]
        val top = location[1]
        val right: Int = left + view.measuredWidth
        val bottom: Int = top + view.measuredHeight
        //view.isClickable() &&
        return y in top..bottom && x >= left && x <= right
    }

    /**
     * 在菜单上方显示Toast
     */
    private fun displayToastAboveButton(v: View, message: String) {
        var xOffset = 0
        var yOffset = 0
        val gvr = Rect()
        val parent = v.parent as View
        val parentHeight = parent.height
        if (v.getGlobalVisibleRect(gvr)) {
            val root = v.rootView
            val halfWidth = root.right / 2
            val halfHeight = root.bottom / 2
            val parentCenterX: Int = (gvr.right - gvr.left) / 2 + gvr.left
            val parentCenterY: Int = (gvr.bottom - gvr.top) / 2 + gvr.top
            yOffset = if (parentCenterY <= halfHeight) {
                -(halfHeight - parentCenterY) - parentHeight
            } else {
                parentCenterY - halfHeight - parentHeight
            }
            if (parentCenterX < halfWidth) {
                xOffset = -(halfWidth - parentCenterX)
            }
            if (parentCenterX >= halfWidth) {
                xOffset = parentCenterX - halfWidth
            }
        }
        val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast.setGravity(CENTER, xOffset, yOffset)
        toast.show()
    }
}