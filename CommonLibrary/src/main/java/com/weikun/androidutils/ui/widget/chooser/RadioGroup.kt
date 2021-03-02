package com.weikun.androidutils.ui.widget.chooser

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.*
import com.weikun.androidutils.R

/**
 *   单选框列表
 *   @author lwk
 *   @date   2020/6/17
 */
@SuppressLint("ViewConstructor")
class RadioGroup(context: Context?,
                 private val nameArray: Array<String>,
                 private val iconArray: Array<Drawable>?,
                 var checkedIndex: Int) : ScrollView(context) {
    private val buttons = ArrayList<RadioButton>()
    val checkedRes:String
        get() {
            return nameArray[checkedIndex]
        }
    interface RadioGroupListener {
        fun onChecked(position: Int)
    }

    init {
        createView()
    }


    fun setCheck(position: Int) {
        buttons[position].isChecked = true
    }

    private fun createView() {
        removeAllViews()
        this.buttons.clear()
        val group = LinearLayout(context)
        group.orientation = LinearLayout.VERTICAL
        for ((index, title) in nameArray.withIndex()) {
            val itemView = View.inflate(context, R.layout.item_radio_button, null)
            val radioBtn = itemView.findViewById<RadioButton>(R.id.radio_button)
            radioBtn.isChecked = index == checkedIndex
            radioBtn.text = title
            radioBtn.setOnCheckedChangeListener { _, b ->
                run {
                    if (b) {
                        checkedIndex = index
                        for ((i,btn) in buttons.withIndex()){
                            if (i!=index){
                                btn.isChecked = false
                            }
                        }
                    }
                }
            }
            radioBtn.setOnLongClickListener() {
                if(radioBtn.isChecked){
                    radioBtn.isChecked = false
                    checkedIndex = -1
                }
                true
            }
            if (iconArray != null && iconArray.size > index) {
                val icon = itemView.findViewById<ImageView>(R.id.icon)
                icon.setImageDrawable(iconArray[index])
            }
            buttons.add(radioBtn)
            group.addView(itemView)
        }
        this.addView(group)
    }
}