package com.weikun.androidutils.utils

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.Fragment
import com.weikun.androidutils.base.ui.BaseFragment
import com.weikun.androidutils.base.ui.FragmentActivity

/**
 *   @author linweikun
 *   @date   2021/1/13
 *
 */
object CommonUtils {
    /**
     * 收起软键盘
     * @param view 当前操作视图
     */
    @JvmStatic
    fun hideKeyboard(view: View?) {
        view?.let{
            val imm = it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    @JvmStatic
    fun createNewFragmentActivity(context: Context, fragmentName: String, title: String, data: Bundle?, options: ActivityOptions?) {
        val intent = Intent(context, FragmentActivity::class.java)
        val bundle = Bundle()
        bundle.putString("fragment_name", fragmentName)
        bundle.putString("fragment_title", title)
        intent.putExtras(bundle)
        if (data!=null){
            intent.putExtra("fragment_data", data)
        }
        if (options==null){
            context.startActivity(intent)
        }else{
            context.startActivity(intent,options.toBundle())
        }
    }



    fun inflateMenuItems(context: Context,menuId:Int):ArrayList<MenuItem>{
        val menu = PopupMenu(context,null).menu
        MenuInflater(context).inflate(menuId,menu)
        val array = ArrayList<MenuItem>()
        for (i in 0 until menu.size){
            array.add(menu[i])
        }
        return array
    }
}