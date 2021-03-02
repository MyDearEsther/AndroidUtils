package com.weikun.androidutils.ui.widget.recyclerview



/**
 *   列表数据模型
 *   @author lwk
 *   @date   2020/8/19
 *
 */
class ListAdapterViewModel<T>(private val notifyListener: OnListItemChangeListener) {

    interface OnItemStateChangeListener<T> {
        fun onItemStateChange(index: Int, item: ListItem<T>)
        fun onSelectModeChange(enable: Boolean)
    }

    var selectListener: OnItemStateChangeListener<T>? = null
    private val data = ArrayList<ListItem<T>>()
    private val selectedData = ArrayList<T>()

//    fun observe(owner: LifecycleOwner, observer: Observer<ArrayList<ListItem<T>>>) {
//        data.observe(owner, observer)
//    }

    var selectEnable = false
        set(value) {
            field = value
            if (!value){
                selectedData.clear()
                for (item in data){
                    item.selected = false
                }
            }
            notifyListener.onRangeLoad(0, data.size)
            if (selectListener != null) {
                selectListener!!.onSelectModeChange(value)
            }
        }

    fun setSelected(index: Int, check: Boolean) {
        data[index].selected = check
        val exist = selectedData.contains(data[index].item)
        if (check) {
            if (!exist){
                selectedData.add(data[index].item)
            }
        } else if (exist){
            selectedData.remove(data[index].item)
        }
        if (selectListener != null) {
            selectListener!!.onItemStateChange(index, data[index])
        }
        notifyListener.onItemChange(index)
    }

    fun setSelected(startIndex: Int, endIndex: Int, check: Boolean) {
        for (index in startIndex..endIndex) {
            setSelected(index, check)
        }
    }

    fun setAllSelected(check: Boolean) {
        setSelected(0, itemCount - 1, check)
    }


    fun setSelectReverse(index: Int) {
        setSelected(index, !isSelected(index))
    }


    fun isSelected(index: Int): Boolean {
        return data[index].selected
    }

    val selectItems: ArrayList<T>
        get() {
            return selectedData
        }

    /**
     * 选择模式 是否已全选
     */
    val isAllSelected: Boolean
        get() {
            return selectedData.size == data.size
        }

    fun add(item: T): Boolean {
        return data.add(ListItem(item))
    }

    fun addAll(list: ArrayList<T>): Boolean {
        val newList = ArrayList<ListItem<T>>()
        for (item in list) {
            newList.add(ListItem(item))
        }
        val start = data.size
        val ret = data.addAll(newList)
        notifyListener.onRangeLoad(start, data.size)
        return ret
    }

    fun set(index: Int, item: T) {
        data[index] = ListItem(item)
    }

    fun remove(item: T): Boolean {
        return data.remove(ListItem(item))
    }

    fun removeAt(index: Int): T {
        return data.removeAt(index).item
    }

    fun clear() {
        data.clear()
    }

    fun setData(newData: ArrayList<T>) {
        data.clear()
        val list = ArrayList<ListItem<T>>()
        for (item in newData) {
            list.add(ListItem(item))
        }
        data.addAll(list)
        notifyListener.onReload()
    }

    val itemCount: Int
        get() {
            return data.size
        }

    fun get(index: Int): T {
        return data[index].item
    }


    val items:ArrayList<T>
        get() {
            val list = ArrayList<T>()
            for (item in data) {
                list.add(item.item)
            }
            return list
        }


}