package com.weikun.androidutils.base.ui
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.keyIterator
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

/**
 * Fragment基类
 * @author lwk
 * @date 2020/4/13
 */
abstract class BaseDataBindingFragment: BaseFragment() {
    var binding: ViewDataBinding?=null
        private set
    abstract val dataBindingConfig:DataBindingConfig
    final override val layoutId = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, dataBindingConfig.layoutId, container, false)
        binding!!.lifecycleOwner = this
        mHandler = LifecycleHandler(uiController)
        initViews(savedInstanceState)
        binding!!.setVariable(dataBindingConfig.variableId,dataBindingConfig.viewModel)
        val params = dataBindingConfig.bindingParams
        for (key in params.keyIterator()){
            binding!!.setVariable(key,params[key])
        }
        return binding!!.root
    }


    override fun <VT : View> searchViewById(id: Int): VT {
        return binding!!.root.findViewById(id)
                ?: throw NullPointerException("This resource ID is invalid.")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.let {
            it.unbind()
            binding = null
        }
    }
}