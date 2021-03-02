package com.weikun.androidutils.ui.widget.chooser

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.*
import com.weikun.androidutils.R

/**
 *   多选框列表
 *   Created by lwk
 *   2020/8/23
 */
@SuppressLint("ViewConstructor")
class CheckBoxGroup(
        context: Context,
        //标题
        private val nameArray: Array<String>,
        //图标
        private val iconArray: Array<Drawable>?,
        //选中位置
        var checkedIndexes: ArrayList<Int>?) : ScrollView(context) {
    private val checkBoxes = ArrayList<CheckBox>()
    val checkedRes: ArrayList<String>
        get() {
            val array = ArrayList<String>()
            for (index in checkedIndexes!!){
                array.add(nameArray[index])
            }
            return array
        }

    init {
        if (checkedIndexes == null) {
            checkedIndexes = ArrayList()
        }
    }

    init {
        createView()
    }

    fun reset(checked: Boolean) {
        for (checkbox in checkBoxes) {
            checkbox.isChecked = checked
        }
    }

    private fun createView() {
        val group = LinearLayout(context)
        removeAllViews()
        checkBoxes.clear()
        group.orientation = LinearLayout.VERTICAL
        for ((index, name) in nameArray.withIndex()) {
            val itemView = View.inflate(context, R.layout.item_checkbox, null)
            val checkBox = itemView.findViewById<CheckBox>(R.id.checkbox)
            checkBox.text = name
            checkBox.isChecked = checkedIndexes!!.contains(index)
            checkBox.setOnCheckedChangeListener { _, p1 ->
                run {
                    if (p1) {
                        if (!checkedIndexes!!.contains(index)) {
                            checkedIndexes!!.add(index)
                        }
                    } else {
                        if (checkedIndexes!!.contains(index)) {
                            checkedIndexes!!.remove(index)
                        }
                    }
                }
            }
            checkBoxes.add(checkBox)
            if (iconArray != null && iconArray.size > index) {
                val icon = itemView.findViewById<ImageView>(R.id.icon)
                icon.setImageDrawable(iconArray[index])
            }
            group.addView(itemView)
        }
        this.addView(group)
    }
}