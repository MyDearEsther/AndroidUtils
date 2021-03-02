package com.weikun.androidutils.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


/**
 *   @author lwk
 *   @date   2020/8/3
 *
 */
object JsonUtil {

    @JvmStatic
    fun <T> encode(data: T):String {
        return Gson().toJson(data)
    }
    @JvmStatic
    fun <T> decode(jsonStr:String,clz:Class<T>):T{
        return Gson().fromJson(jsonStr,clz)
    }
    @JvmStatic
    fun <T> decodeList(jsonStr:String,clz:Class<T>):List<T>{
        return Gson().fromJson(jsonStr, object : TypeToken<List<T>>() {}.type)
    }

}