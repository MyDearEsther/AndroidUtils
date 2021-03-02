package com.weikun.androidutils.base.ui
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.lang.ref.WeakReference

/**
 * Handler基类
 * 传递弱引用
 * @author lwk
 * @date 2020/4/15
 */
class LifecycleHandler<T:LifecycleOwner?>(private val controller: UiController<T>?) : Handler(Looper.myLooper()!!) {

    private var reference: WeakReference<T>?=null
    override fun handleMessage(msg: Message) {
        controller?.handleOnUiThread(reference?.get()!!,msg)
    }

    init {
        controller?.let {
            it.owner!!.lifecycle.addObserver(object : LifecycleEventObserver{
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_DESTROY){
                        removeCallbacksAndMessages(null)
                    }
                }
            })
            reference = WeakReference(it.owner)
        }

    }
}