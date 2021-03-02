package com.weikun.androidutils.base.ui
import android.os.Bundle
import android.os.Looper
import android.os.Message
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.weikun.androidutils.ui.dialog.Dialog

/**
 * Activity基类
 * @author lwk
 * @date 2020/4/14
 */
abstract class BaseActivity : AppCompatActivity() {

    companion object {
        private const val MSG_ACTIVITY_BASE = 0
        private const val MSG_TOAST = MSG_ACTIVITY_BASE + 1
        private const val MSG_SHOW_DIALOG = MSG_ACTIVITY_BASE + 2
        const val MSG_ACTIVITY_START = MSG_ACTIVITY_BASE + 10
    }

    init {
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_CREATE -> {
                        afterCreate()
                    }
                    Lifecycle.Event.ON_START -> {
                        afterStart()
                    }
                    Lifecycle.Event.ON_RESUME -> {
                        afterResume()
                    }
                    Lifecycle.Event.ON_PAUSE -> {
                        beforeActivityPause()
                    }
                    Lifecycle.Event.ON_STOP -> {
                        beforeStop()
                    }
                    Lifecycle.Event.ON_DESTROY -> {
                        //Activity结束后释放Handler 避免内存泄漏
                        mHandler!!.removeCallbacksAndMessages(null)
                        beforeDestroy()
                    }
                    Lifecycle.Event.ON_ANY -> {

                    }
                }
            }
        })
    }


    /**
     * 布局ID
     */
    abstract val layoutId: Int

    //region Activity回调
    override fun onCreate(savedInstanceState: Bundle?) {
        beforeCreate()
        super.onCreate(savedInstanceState)
        setContentView(layoutId)
        mHandler = LifecycleHandler(uiController)
        initViews(savedInstanceState)
    }

    /**
     * onCreate方法 初始化View
     */
    open fun initViews(savedInstanceState: Bundle?) {}
    open fun beforeDestroy() {}
    open fun afterDestroy() {}
    open fun beforeResume() {}
    open fun afterResume() {}
    open fun beforeStop() {}
    open fun beforeCreate() {}
    open fun afterCreate() {}
    open fun afterStart() {}
    open fun beforeStart() {}
    open fun beforeActivityPause() {}
    override fun onStart() {
        beforeStart()
        super.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        afterDestroy()
    }

    override fun onResume() {
        beforeResume()
        super.onResume()
    }


//    /**
//     * 监听返回键事件
//     */
//    override fun onBackPressed() {
//        val fragments = supportFragmentManager.fragments
//        for (fragment in fragments) {
//            //判断当前显示的Fragment是否拦截返回键事件
//            if (fragment is OnBackPressed && fragment.isVisible) {
//                if (!fragment.onBackPressed()) {
//                    return
//                }
//                break
//            }
//        }
//        super.onBackPressed()
//    }

    //region Handler
    private var mHandler: LifecycleHandler<*>? = null

    /**
     * UI处理回调
     */
    abstract val uiController: UiController<*>

    fun sendMessage(msg: Message?) {
        mHandler!!.sendMessage(msg!!)
    }

    fun sendMessage(what: Int) {
        mHandler!!.obtainMessage(what).sendToTarget()
    }

    fun sendMessage(what: Int, arg1: Int) {
        mHandler!!.obtainMessage(what, arg1).sendToTarget()
    }

    fun sendMessage(what: Int, arg1: Int, arg2: Int) {
        mHandler!!.obtainMessage(what, arg1, arg2).sendToTarget()
    }

    fun sendMessage(what: Int, obj: Any?) {
        mHandler!!.obtainMessage(what, obj).sendToTarget()
    }

    fun sendMessage(what: Int, arg1: Int, arg2: Int, obj: Any?) {
        mHandler!!.obtainMessage(what, arg1, arg2, obj).sendToTarget()
    }
    //endregion

    //region UI提示
    private val isOnUiThread: Boolean
        get() = Looper.getMainLooper().thread.id == Thread.currentThread().id
    fun toast(text: String?) {
        if (isOnUiThread) {
            Toast.makeText(this, text, Toast.LENGTH_LONG).show()
        } else {
            sendMessage(MSG_TOAST, text)
        }
    }

    fun showDialog(dialog: Dialog?) {
        sendMessage(MSG_SHOW_DIALOG, dialog)
    }
    //endregion

}

