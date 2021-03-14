package com.weikun.androidutils.ui.widget.recyclerview
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView适配器基类
 * @author lwk
 * @date 2019/7/31
 */
abstract class BaseAdapter<T> : RecyclerView.Adapter<BaseViewHolder> {
    companion object {
        const val TYPE_BOTTOM_VIEW = 1
    }
    init {
//        registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver(){
//            override fun onChanged() {
//                notifyDataSetChanged()
//            }
//
//            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
//                notifyItemRangeChanged(positionStart, itemCount, payload)
//            }
//        })
    }

    internal interface MultiType<T> {
        fun getLayoutId(item: T, position: Int): Int
    }
    private val notifyListener = object : OnListItemChangeListener {
        override fun onReload() {
            notifyDataSetChanged()
        }

        override fun onRangeLoad(start: Int, end: Int) {
            notifyItemRangeChanged(start,end,1)
        }

        override fun onItemChange(index:Int) {
            notifyItemChanged(index,1)
        }
    }
    var data = ListAdapterViewModel<T>(notifyListener)

    var context: Context
        private set
    private var mLayoutId: Int = 0
    private var mBottomLayoutId: Int? = null
    fun setBottomLayoutId(id: Int) {
        mBottomLayoutId = id
    }

    /**
     * 条目点击/长按监听
     */
    private var clickListener: OnItemClickListener<T>? = null

    /**
     * 多类型布局支持
     */
    private var mTypeSupport: MultiType<T>? = null
    private var mInflater: LayoutInflater

    /**
     * 多布局 ID
     */
    private val resIds = mutableListOf<Int>()

    /**
     * 网格数
     */
    var spanCount = 1
        private set


    /**
     * 构造器
     * @param context 上下文
     * @param layoutId 布局ID
     */
    constructor(context: Context, layoutId: Int, listener: OnItemClickListener<T>?) {
        this.context = context
        mLayoutId = layoutId
        mInflater = LayoutInflater.from(this.context)
        clickListener = listener
        // 解决item数据错乱、闪烁问题
        // https://medium.com/@hanru.yeh/recyclerviews-views-are-blinking-when-notifydatasetchanged-c7b76d5149a2
        setHasStableIds(true)
    }


    constructor(context: Context, layoutId: Int) {
        this.context = context
        mLayoutId = layoutId
        mInflater = LayoutInflater.from(this.context)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        //绑定数据
        //底部样式不需要绑定数据
        if (position < data.itemCount) {
            bindData(holder, position)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            //局部刷新
            refreshData(holder, position, payloads[0])
        }

    }

    /**
     * 设置点击事件
     * @param v 点击View
     * @param position 条目位置
     */
    protected fun setViewClickListener(v: View, position: Int) {
        if (clickListener != null) {
            v.setOnClickListener { clickListener!!.onItemClick(v, data.get(position), position) }
            v.setOnLongClickListener { clickListener!!.onItemLongClick(v, data.get(position), position) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        if (mTypeSupport != null) {
            //多布局
            mLayoutId = viewType
        }
        val view = if (viewType == TYPE_BOTTOM_VIEW) {
            mInflater.inflate(mBottomLayoutId!!, parent, false)
        } else {
            mInflater.inflate(mLayoutId, parent, false)
        }
        return BaseViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        if (position < data.itemCount) {
            return super.getItemViewType(position)
        } else {
            return TYPE_BOTTOM_VIEW
        }
    }

    /**
     * 绑定数据
     * @param holder
     * @param position
     */
    protected abstract fun bindData(holder: BaseViewHolder, position: Int)

    /***
     * 局部刷新
     * @param holder
     * @param position
     */
    protected open fun refreshData(holder: BaseViewHolder, position: Int, payload: Any) {

    }


    private fun removeItem(position: Int) {
        data.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int {
        return if (mBottomLayoutId == null) {
            data.itemCount
        } else {
            //多一个底部样式
            data.itemCount + 1
        }
    }

    fun setLayout(layoutId: Int) {
        mLayoutId = layoutId
    }


    /**
     * item索引作为ID
     */
    override fun getItemId(position: Int): Long {
        return data.get(position).hashCode().toLong()
    }


    fun updateSpanCount(spanCount: Int) {
        this.spanCount = spanCount
        notifyDataSetChanged()
    }

    var refreshListener: OnRefreshListener? = null
    fun onRefresh(){
        if (refreshListener!=null){
            refreshListener!!.onRefresh()
        }
    }
}