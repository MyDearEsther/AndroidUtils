package com.weikun.androidutils.ui.dialog

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner


/**
 * 自定义对话框
 * 提供统一的对话框标题、对话框按钮、样式的设置，有三种自定义方式：
 * 1.带TextView的普通提示对话框
 * 2.添加自定义View
 * 3.添加自定义Fragment，实现类似于DialogFragment
 */
open class Dialog(val context: Context) {
    interface KeyDownCallback {
        fun onEvent(): Boolean
    }

    //对话框标题
    var title: String? = null
        set(value) {
            field = value
            if (dialog != null) {
                dialog!!.setTitle(title)
            }
        }

    //对话框默认文本
    var text: String? = null
        set(value) {
            field = value
            if (dialog != null) {
                dialog!!.setMessage(value)
            }
        }

    //对话框自定义View
    var customView: View? = null
        set(value) {
            field = value
            if (dialog != null) {
                dialog!!.setView(value)
            }
        }

    //是否能够点击外部取消对话框
    var canTouchOutside = false
        set(value) {
            field = value
            if (dialog != null) {
                dialog!!.setCanceledOnTouchOutside(!canTouchOutside)
            }
        }

    //是否悬浮于应用
    private var overlay = false

    //设置对话框透明
    var tranparent = false
        set(value) {
            field = value
            if (dialog != null && dialog!!.isShowing) {
                makeDialogTransparent()
            }
        }

    //自定义Fragment
    private var dialogFragment: DialogFragment? = null


    /**
     * 对话框构造器
     * @param context
     * @param overlay 是否悬浮于应用
     */
    constructor(context: Context, overlay: Boolean) : this(context) {
        this.overlay = overlay
    }

    //确定按钮点击监听
    private var positiveListener: KeyDownCallback? = null

    //确定按钮名称
    private var positiveTitle: String? = null

    //取消按钮点击监听
    private var negativeListener: KeyDownCallback? = null

    //取消按钮名称
    private var negativeTitle: String? = null

    //中立按钮点击监听
    private var neutralListener: KeyDownCallback? = null

    //对话框关闭事件回调
    private var dismissListener: KeyDownCallback? = null

    //对话框显示事件回调
    var onShowListener: DialogInterface.OnShowListener? = null

    //中立按钮名称
    private var neutralTitle: String? = null

    private var cancelable = true

    private var dialog: AlertDialog? = null


    private var fragment: Fragment? = null

    fun setPositiveButton(text: String?, clickListener: KeyDownCallback?) {
        positiveTitle = text
        positiveListener = clickListener
    }

    fun setPositiveButton(resId: Int, clickListener: KeyDownCallback?) {
        positiveTitle = context.getString(resId)
        positiveListener = clickListener
    }


    fun setNegativeButton(text: String?, clickListener: KeyDownCallback?) {
        negativeTitle = text
        negativeListener = clickListener
    }

    fun setNegativeButton(resId: Int, clickListener: KeyDownCallback?) {
        negativeTitle = context.getString(resId)
        negativeListener = clickListener
    }

    fun setNeutralButton(text: String?, clickListener: KeyDownCallback?) {
        neutralTitle = text
        neutralListener = clickListener
    }

    fun setNeutralButton(resId: Int, clickListener: KeyDownCallback?) {
        neutralTitle = context.getString(resId)
        neutralListener = clickListener
    }

    fun setDismissListener(clickListener: KeyDownCallback?) {
        dismissListener = clickListener
    }

    fun setCancelable(cancelable: Boolean) {
        this.cancelable = cancelable
    }

    fun create(): AlertDialog {
        return create(null)
    }

    /**
     * 创建对话框
     */
    fun create(styleId: Int?): AlertDialog {
        val builder = if (styleId != null) {
            AlertDialog.Builder(context, styleId)
        } else {
            AlertDialog.Builder(context)
        }
        builder.setTitle(title)
        if (positiveTitle != null || positiveListener != null) {
            builder.setPositiveButton(positiveTitle, null)
        }
        if (negativeTitle != null || negativeListener != null) {
            builder.setNegativeButton(negativeTitle, null)
        }
        if (neutralTitle != null || neutralListener != null) {
            builder.setNeutralButton(neutralTitle, null)
        }

        builder.setOnDismissListener {
            if (fragment != null) {
                //存在自定义Fragment，关闭对话框后要及时将其解绑
                val transaction = fragment!!.parentFragmentManager.beginTransaction()
                transaction.remove(fragment!!)
                transaction.commit()
                fragment = null
                customView = null
            }
            if (dismissListener != null) {
                //自定义 关闭回调
                dismissListener!!.onEvent()
            }
        }

        if (customView != null) {
            builder.setView(customView)
        }
        if (text != null) {
            builder.setMessage(text)
        }
        builder.setCancelable(cancelable)
        dialog = builder.create()
        if (overlay && dialog!!.window != null) {
            //全局效果
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dialog!!.window!!.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
            } else {
                dialog!!.window!!.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
            }
        }
        dialog!!.setCanceledOnTouchOutside(!canTouchOutside)
        return dialog!!
    }

    /***
     * 对话框透明
     */
    private fun makeDialogTransparent() {
        val back = ColorDrawable(Color.TRANSPARENT)
        val inset = InsetDrawable(back, 0)
        dialog!!.window!!.setBackgroundDrawable(inset)
    }

    /**
     * 显示对话框
     */
    fun show() {
        if (dialog == null) {
            //第一次创建
            if (fragment != null) {
                //将自定义Fragment的View attach到Dialog上
                customView = fragment!!.view
            }
            dialog = create()
        }
        if (onShowListener != null) {
            //自定义 显示回调
            dialog!!.setOnShowListener(onShowListener)
        }
        if (!dialog!!.isShowing) {
            dialog!!.show()
            if (tranparent) {
                //开启透明
                makeDialogTransparent()
            }
            //初始化对话框按钮
            initButtons()
        }

    }

    /**
     * 显示对话框 内嵌Fragment
     * @param fragment 自定义Fragment
     * @param fragmentManager 当前Activity的FragmentManager
     */
    fun show(fragment: Fragment, fragmentManager: FragmentManager) {
        val transaction = fragmentManager.beginTransaction()
        if (this.fragment != null) {
            //已经设置过fragment，将其移除
            transaction.remove(this.fragment!!)
        }
        this.fragment = fragment
        //添加Fragment
        transaction.add(this.fragment!!, null)
        transaction.commit()
        this.fragment!!.lifecycle.addObserver(object : LifecycleEventObserver {
            //在Fragment的View创建之后再显示对话框，否则View没法attach到Dialog上
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_RESUME) {
                    show()
                }
            }
        })
    }

    /**
     * 初始化对话框按钮
     */
    private fun initButtons() {
        if (positiveTitle != null || positiveListener != null) {
            initPositiveButton()
        }
        if (negativeTitle != null || negativeListener != null) {
            initNegativeButton()
        }
        if (positiveTitle != null || positiveListener != null) {
            initNeutralButton()
        }
    }

    private fun initPositiveButton() {
        dialog!!.getButton(DialogInterface.BUTTON_POSITIVE).text = positiveTitle
        dialog!!.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            if (positiveListener == null || !positiveListener!!.onEvent()) {
                dialog!!.dismiss()
            }
        }
    }

    private fun initNeutralButton() {
        dialog!!.getButton(DialogInterface.BUTTON_NEUTRAL).text = neutralTitle
        dialog!!.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener {
            if (neutralListener == null || !neutralListener!!.onEvent()) {
                dialog!!.dismiss()
            }
        }
    }

    private fun initNegativeButton() {
        dialog!!.getButton(DialogInterface.BUTTON_NEGATIVE).text = negativeTitle
        dialog!!.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener {
            if (negativeListener == null || !negativeListener!!.onEvent()) {
                dialog!!.dismiss()
            }
        }
    }

    fun dismiss() {
        if (dialog != null) {
            dialog!!.dismiss()
        }
    }

    fun setPositiveButtonVisible(visible: Boolean) {
        if (dialog != null) {
            dialog!!.getButton(DialogInterface.BUTTON_POSITIVE).visibility = if (visible) View.VISIBLE else View.GONE
        }
    }

    fun setNegativeButtonVisible(visible: Boolean) {
        if (dialog != null) {
            dialog!!.getButton(DialogInterface.BUTTON_NEGATIVE).visibility = if (visible) View.VISIBLE else View.GONE
        }
    }

    fun setNeutralButtonVisible(visible: Boolean) {
        if (dialog != null) {
            dialog!!.getButton(DialogInterface.BUTTON_NEUTRAL).visibility = if (visible) View.VISIBLE else View.GONE
        }
    }

}