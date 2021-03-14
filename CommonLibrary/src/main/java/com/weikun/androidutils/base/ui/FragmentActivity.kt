package com.weikun.androidutils.base.ui

import android.os.Bundle
import android.os.Message
import androidx.fragment.app.Fragment
import com.weikun.androidutils.R

/**
 *   @author linweikun
 *   @date   2021/3/5
 *
 */
class FragmentActivity:BaseActivity() {
    override val layoutId = R.layout.activity_fragment
    override val uiController = object : UiController<FragmentActivity>{
        override fun handleOnUiThread(instance: FragmentActivity, msg: Message) {

        }

        override val owner = this@FragmentActivity

    }

    override fun initViews(savedInstanceState: Bundle?) {
        var fragmentName: String? = null
        var title: String? = null
        val bundle = intent.extras
        if (bundle != null) {
            fragmentName = bundle.getString("fragment_name")
            title = bundle.getString("fragment_title")
        }
        if (fragmentName != null) {
            val fragment = supportFragmentManager.fragmentFactory
                        .instantiate(ClassLoader.getSystemClassLoader(), fragmentName)

            val data = intent.getBundleExtra("fragment_data")
            if (data != null) {
                fragment.arguments = data
            }
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment).commit()
        }
    }
}