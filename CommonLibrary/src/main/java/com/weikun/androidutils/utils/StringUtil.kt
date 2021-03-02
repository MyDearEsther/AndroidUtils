package com.weikun.androidutils.utils

import java.lang.StringBuilder

/**
 *   字符串工具类
 *   @author lwk
 *   @date   2020/10/13
 *
 */
object StringUtil {
    /**
     * 连接字符
     */
    @JvmStatic
    fun connectString(set: Collection<String>,symbol:String):String{
        val builder = StringBuilder()
        for ((index,str) in set.withIndex()){
            builder.append(str)
            if (index<set.size-1){
                builder.append(symbol)
            }
        }
        return builder.toString()
    }
}