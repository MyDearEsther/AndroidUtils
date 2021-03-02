package com.weikun.androidutils.base.ui
import android.os.Message
import androidx.lifecycle.LifecycleOwner

/**
 * @author lwk
 * @date 2020/4/16
 */
interface UiController<T:LifecycleOwner?> {
    /**
     * 消息处理
     * @param msg 消息实体
     */
    fun handleOnUiThread(instance:T,msg: Message)

    /**
     * 设置实例
     * @return Activity或Fragment实例
     */
    val owner: T
}