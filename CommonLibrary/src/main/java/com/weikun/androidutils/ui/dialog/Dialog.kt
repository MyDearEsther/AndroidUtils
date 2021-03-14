package com.weikun.androidutils.ui.dialog

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner


/**
 * @author lwk
 * 自定义对话框 基于AlertDialog封装
 * 提供统一的对话框标题、对话框按钮、样式的设置，有三种自定义方式：
 * 1.带TextView的普通提示对话框
 * 2.添加自定义View
 * 3.添加自定义Fragment，实现类似于DialogFragment
 */
open class Dialog(val context: Context) {

    //是否悬浮于应用
    private var overlay = false


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
    private var mPositiveListener: KeyDownCallback? = null

    //确定按钮名称
    private var mPositiveTitle: String? = null

    //取消按钮点击监听
    private var mNegativeListener: KeyDownCallback? = null

    //取消按钮名称
    private var mNegativeTitle: String? = null

    //中立按钮点击监听
    private var mNeutralListener: KeyDownCallback? = null

    //对话框关闭事件回调
    private var mDismissListener: KeyDownCallback? = null

    //中立按钮名称
    private var mNeutralTitle: String? = null

    //AlertDialog对象
    private var mDialog: AlertDialog? = null

    //内嵌Fragment
    private var mFragment: Fragment? = null

    interface KeyDownCallback {
        fun onEvent(): Boolean
    }

    var cancelable = true
        set(value) {
            field = value
            mDialog?.setCancelable(value)
        }

    //对话框显示事件回调
    var onShowListener: DialogInterface.OnShowListener? = null


    //对话框标题
    var title: String? = null
        set(value) {
            field = value
            if (mDialog != null) {
                mDialog!!.setTitle(title)
            }
        }

    //对话框默认文本
    var text: String? = null
        set(value) {
            field = value
            if (mDialog != null) {
                mDialog!!.setMessage(value)
            }
        }

    //对话框自定义View
    var customView: View? = null
        set(value) {
            field = value
            if (mDialog != null) {
                mDialog!!.setView(value)
            }
        }

    //是否能够点击外部取消对话框
    var canTouchOutside = false
        set(value) {
            field = value
            mDialog?.setCanceledOnTouchOutside(!canTouchOutside)
        }


    //设置对话框透明
    var tranparent = false
        set(value) {
            field = value
            mDialog?.let {
                if (it.isShowing){
                    makeDialogTransparent()
                }
            }
        }

    fun setPositiveButton(text: String?, clickListener: KeyDownCallback?) {
        mPositiveTitle = text
        mPositiveListener = clickListener
    }

    fun setPositiveButton(resId: Int, clickListener: KeyDownCallback?) {
        mPositiveTitle = context.getString(resId)
        mPositiveListener = clickListener
    }


    fun setNegativeButton(text: String?, clickListener: KeyDownCallback?) {
        mNegativeTitle = text
        mNegativeListener = clickListener
    }

    fun setNegativeButton(resId: Int, clickListener: KeyDownCallback?) {
        mNegativeTitle = context.getString(resId)
        mNegativeListener = clickListener
    }

    fun setNeutralButton(text: String?, clickListener: KeyDownCallback?) {
        mNeutralTitle = text
        mNeutralListener = clickListener
    }

    fun setNeutralButton(resId: Int, clickListener: KeyDownCallback?) {
        mNeutralTitle = context.getString(resId)
        mNeutralListener = clickListener
    }

    fun setDismissListener(clickListener: KeyDownCallback?) {
        mDismissListener = clickListener
    }

    private fun create(): AlertDialog {
        return create(null)
    }

    /**
     * 创建对话框
     */
    fun create(styleId: Int?): AlertDialog {
        val builder = if (styleId == null) {
            AlertDialog.Builder(context)
        } else {
            AlertDialog.Builder(context, styleId)
        }
        builder.setTitle(title)
        if (mPositiveTitle != null || mPositiveListener != null) {
            builder.setPositiveButton(mPositiveTitle, null)
        }
        if (mNegativeTitle != null || mNegativeListener != null) {
            builder.setNegativeButton(mNegativeTitle, null)
        }
        if (mNeutralTitle != null || mNeutralListener != null) {
            builder.setNeutralButton(mNeutralTitle, null)
        }
        builder.setOnDismissListener {
            if (mFragment != null) {
                //存在自定义Fragment，关闭对话框后要及时将其解绑
                val transaction = mFragment!!.parentFragmentManager.beginTransaction()
                transaction.remove(mFragment!!)
                transaction.commit()
                mFragment = null
                customView = null
            }
            if (mDismissListener != null) {
                //自定义 关闭回调
                mDismissListener!!.onEvent()
            }
        }
        if (customView != null) {
            builder.setView(customView)
        }
        if (text != null) {
            builder.setMessage(text)
        }
        builder.setCancelable(cancelable)
        mDialog = builder.create()
        if (overlay && mDialog!!.window != null) {
            //全局效果
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mDialog!!.window!!.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
            } else {
                mDialog!!.window!!.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
            }
        }
        mDialog!!.setCanceledOnTouchOutside(!canTouchOutside)
        return mDialog!!
    }

    /***
     * 对话框透明
     */
    private fun makeDialogTransparent() {
        val back = ColorDrawable(Color.TRANSPARENT)
        val inset = InsetDrawable(back, 0)
        mDialog!!.window!!.setBackgroundDrawable(inset)
    }

    /**
     * 显示对话框
     */
    fun show() {
        if (mDialog == null) {
            //第一次创建
            if (mFragment != null) {
                //将自定义Fragment的View attach到Dialog上
                customView = mFragment!!.view
            }
            mDialog = create()
        }
        if (onShowListener != null) {
            //自定义 显示回调
            mDialog!!.setOnShowListener(onShowListener)
        }
        if (!mDialog!!.isShowing) {
            mDialog!!.show()
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
        if (this.mFragment != null) {
            //已经设置过fragment，将其移除
            transaction.remove(this.mFragment!!)
        }
        this.mFragment = fragment
        //添加Fragment
        transaction.add(this.mFragment!!, null)
        transaction.commit()
        this.mFragment!!.lifecycle.addObserver(object : LifecycleEventObserver {
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
        if (mPositiveTitle != null || mPositiveListener != null) {
            initPositiveButton()
        }
        if (mNegativeTitle != null || mNegativeListener != null) {
            initNegativeButton()
        }
        if (mPositiveTitle != null || mPositiveListener != null) {
            initNeutralButton()
        }
    }

    private fun initPositiveButton() {
        mDialog!!.getButton(DialogInterface.BUTTON_POSITIVE).text = mPositiveTitle
        mDialog!!.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            if (mPositiveListener == null || !mPositiveListener!!.onEvent()) {
                mDialog!!.dismiss()
            }
        }
    }

    private fun initNeutralButton() {
        mDialog!!.getButton(DialogInterface.BUTTON_NEUTRAL).text = mNeutralTitle
        mDialog!!.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener {
            if (mNeutralListener == null || !mNeutralListener!!.onEvent()) {
                mDialog!!.dismiss()
            }
        }
    }

    private fun initNegativeButton() {
        mDialog!!.getButton(DialogInterface.BUTTON_NEGATIVE).text = mNegativeTitle
        mDialog!!.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener {
            if (mNegativeListener == null || !mNegativeListener!!.onEvent()) {
                mDialog!!.dismiss()
            }
        }
    }

    fun dismiss() {
        if (mDialog != null) {
            mDialog!!.dismiss()
        }
    }

    fun getButton(whichButton:Int):Button?{
        mDialog?.let {
            return it.getButton(whichButton)
        }
        return null
    }

}