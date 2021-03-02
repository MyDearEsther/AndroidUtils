package com.weikun.androidutils.ui.widget.recyclerview

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.ScaleGestureDetector
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.weikun.androidutils.utils.LogUtil

/**
 * 滑动监听
 */
interface OnScrollCallback{
    /**
     * 拉到底部
     */
    fun onScrollToBottom()

    /**
     * 拉到顶部
     */
    fun onScrollToTop()
}
/**
 * 自定义RecyclerView
 * @author lwk
 * @date 2020/4/16
 */
class Recyclerview : RecyclerView, SwipeRefreshLayout.OnRefreshListener{
    companion object{
        //水平方向最多项目数
        var MAX_SPAN_COUNT = 5
        //水平方向最小项目数
        var MIN_SPAN_COUNT = 1
        //当前水平方向项目数
    }
    private var currentSpanCount = 1
    //是否固定网格大小
    private var spanCountFixed = false
    //缩放手势检测
    private var mScaleGestureDetector: ScaleGestureDetector? = null
    private var itemDecoration: SpaceItemDecoration?=null
    private var mOnScrollCallback:OnScrollCallback?=null
    private var mScrollBar:ScrollBar? = null
    private val mTouchListener = DragSelectTouchListener()

    private val mScrollListener: OnScrollListener = object : OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            mOnScrollCallback?.let {
                if (isBottomViewVisible()) {
                    it.onScrollToBottom()
                }
            }
        }
    }

    //是否开启滑动条
    var scrollBarEnable: Boolean = false
        set(value) {
            field = value
            if (value && mScrollBar == null) {
                mScrollBar = ScrollBar(this)
            }
        }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    fun init(adapter: BaseAdapter<*>?,orientation:Int,spanCount:Int) {
        this.adapter = adapter
        //Item高度固定 避免requestLayout浪费资源
        setHasFixedSize(true)
        if (spanCount>1){
            layoutManager = GridLayoutManager(context,spanCount)
        }else{
            layoutManager = LinearLayoutManager(context, orientation,false)
        }
    }

    fun setGridLayout(spanCount:Int){
        currentSpanCount = if (spanCount > MAX_SPAN_COUNT) {
            MAX_SPAN_COUNT
        } else {
            spanCount.coerceAtLeast(MIN_SPAN_COUNT)
        }
        layoutManager = GridLayoutManager(context, currentSpanCount)

    }


    /**
     * 设置间隔
     * */
    fun setSpace(lr: Int,tb: Int){
        if (itemDecoration!=null){
            removeItemDecoration(itemDecoration!!)
        }
        itemDecoration =
            SpaceItemDecoration(
                lr,
                tb,
                true
            )
        addItemDecoration(itemDecoration!!)
    }

    fun removeAllListeners(){
        removeOnScrollListener(mScrollListener)
    }

    private fun isBottomViewVisible(): Boolean {
        val lastVisibleItem = getLastVisibleItemPosition()
        return lastVisibleItem != NO_POSITION && lastVisibleItem == adapter!!.itemCount - 1
    }

    private fun getLastVisibleItemPosition(): Int {
        val manager = layoutManager
        return if (manager is LinearLayoutManager) {
            manager.findLastVisibleItemPosition()
        } else NO_POSITION
    }

    /***
     *  多选模式
     *  @param enable 是否开启
     */
    fun setMultiSelectMode(enable:Boolean,swipeSelect:Boolean){


        if (swipeSelect){
            requestDisallowInterceptTouchEvent(enable)
            addOnItemTouchListener(mTouchListener)
            mTouchListener.selectListener = object : DragSelectTouchListener.onSelectListener{
                override fun onSelectChange(start: Int, end: Int, isSelected: Boolean) {
                    LogUtil.e("onSelectChange $start $end $isSelected")
                    (adapter as BaseAdapter<*>).data.setSelected(start,end,isSelected)
                }
            }
        }else{
            removeOnItemTouchListener(mTouchListener)
        }
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        if (scrollBarEnable) {
            mScrollBar!!.onMeasure(widthSpec, heightSpec)
        }
        super.onMeasure(widthSpec, heightSpec)
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (scrollBarEnable) {
            mScrollBar!!.onLayout(changed, l, t, r, b)
        }
    }

    override fun onRefresh() {
    }

    /**
     * 间隔装饰类
     */
    private class SpaceItemDecoration(val lr: Int, val tb: Int, val isVertical: Boolean)
        : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect,
                                    view: View,
                                    parent: RecyclerView,
                                    state: State) {
            outRect.top = tb
            outRect.bottom = tb
            outRect.left = lr
            outRect.right = lr
        }
    }


}