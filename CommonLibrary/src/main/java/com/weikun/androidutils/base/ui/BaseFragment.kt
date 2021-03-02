package com.weikun.androidutils.base.ui
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Fragment基类
 * @author lwk
 * @date 2020/4/13
 */
abstract class BaseFragment: Fragment() {
    open val uiController: UiController<*>?=null
    protected var mHandler: LifecycleHandler<*>? = null
    private var mContainerView:View? = null
    abstract val layoutId:Int
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mContainerView = View.inflate(context,layoutId,null)
        mHandler = LifecycleHandler(uiController)
        initViews(savedInstanceState)
        return mContainerView
    }


    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //使Fragment onCreateOptionsMenu生效 否则没有菜单
        setHasOptionsMenu(true)
        lifecycle.addObserver(object : LifecycleEventObserver{
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event==Lifecycle.Event.ON_RESUME){
                    onFragmentResume()
                }
            }

        })
    }

    open fun onFragmentResume(){
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler!!.removeCallbacksAndMessages(null)
        onFragmentDestroy()
    }

    override fun onDetach() {
        super.onDetach()
        onFragmentDetach()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        onSaveFragmentState(outState)
        super.onSaveInstanceState(outState)
    }

    open fun onSaveFragmentState(state: Bundle) {

    }

    open fun onFragmentDestroy() {}


    fun onFragmentDetach() {}

    /**
     * 初始化View
     */
    protected abstract fun initViews(savedInstanceState: Bundle?)


    open fun <VT : View> searchViewById(id: Int): VT {
        return mContainerView!!.findViewById(id)
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


    companion object {
        private const val MSG_FRAGMENT_BASE = 100
        const val MSG_FRAGMENT_START = MSG_FRAGMENT_BASE + 10
    }


}