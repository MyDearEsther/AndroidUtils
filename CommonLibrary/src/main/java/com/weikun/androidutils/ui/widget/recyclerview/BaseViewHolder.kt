package com.weikun.androidutils.ui.widget.recyclerview
import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * ViewHolder基类
 * @author lwk
 * @date 2019/7/31
 */
class BaseViewHolder(var containerView: View) : RecyclerView.ViewHolder(containerView) {
    var mViews: SparseArray<View?>? = null
    /**
     * 构造函数
     * @param context  上下文对象
     * @param parent   父类容器
     * @param layoutId 布局的ID
     */
    private fun baseViewHolder(context: Context, parent: ViewGroup, layoutId: Int): BaseViewHolder {
        mViews = SparseArray()
        //构造方法中就指定布局
        containerView = LayoutInflater.from(context).inflate(layoutId, parent, false)
        //设置Tag
        containerView.tag = this
        return BaseViewHolder(containerView)
    }

    /**
     * 得到一个ViewHolder
     * @param context     上下文对象
     * @param convertView 复用的View
     * @param parent      父类容器
     * @param layoutId    布局的ID
     * @return
     */
    operator fun get(context: Context, convertView: View?, parent: ViewGroup, layoutId: Int): BaseViewHolder { //如果为空  直接新建一个ViewHolder
        return if (convertView == null) {
            baseViewHolder(context, parent, layoutId)
        } else { //否则返回一个已经存在的ViewHolder
            containerView.tag as BaseViewHolder
        }
    }

    /**
     * 通过ViewId获取控件
     * @param viewId View的Id
     * @param <T>    View的子类
     * @return 返回View
     */
    inline fun <reified T : View?> getView(viewId: Int): T? {
        if (mViews == null) {
            mViews = SparseArray()
        }
        var view = mViews!![viewId]
        if (view == null) {
            view = containerView.findViewById(viewId)
            mViews!!.put(viewId, view)
        }
        return if (view!! is T){
            view as T
        }else{
            null
        }
    }

    /**
     * 为文本设置text
     * @param viewId view的Id
     * @param text   文本
     * @return 返回ViewHolder
     */
    fun setText(viewId: Int, text: CharSequence): BaseViewHolder {
        val view = getView<TextView>(viewId)!!
        view.text = text.toString()
        return this
    }

    /**
     * 设置ImageView
     * @param viewId view的Id
     * @param resId  资源Id
     * @return
     */
    fun setImageResource(viewId: Int, resId: Int): BaseViewHolder {
        val iv = getView<ImageView>(viewId)!!
        iv.setImageResource(resId)
        return this
    }

}