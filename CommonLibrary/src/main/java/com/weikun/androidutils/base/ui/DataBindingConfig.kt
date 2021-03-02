package com.weikun.androidutils.base.ui
import android.util.SparseArray
import androidx.core.util.contains
import androidx.lifecycle.ViewModel

/**
 *   @author linweikun
 *   @date   2021/1/22
 *
 */
class DataBindingConfig(val layoutId:Int,val variableId:Int,val viewModel:ViewModel?){

    val bindingParams = SparseArray<Any>()

    fun addBindingParam(variableId:Int,obj:Any):DataBindingConfig{
        bindingParams.put(variableId,obj)
        return this
    }
}