package com.weikun.androidutils.utils

import java.io.*
import java.net.URLDecoder
import java.net.URLEncoder
import kotlin.jvm.Throws

/**
 * 序列化与反序列化 工具类
 * @author lwk
 * @date 2020/4/13
 */
object SerializeUtil {
    /**
     * 对象序列化为字符串
     * @param obj
     */
    @JvmStatic
    @Throws(IOException::class)
    fun encode(obj: Any?): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(
                byteArrayOutputStream)
        objectOutputStream.writeObject(obj)
        var serStr = byteArrayOutputStream.toString()
        serStr = URLEncoder.encode(serStr, "UTF-8")
        objectOutputStream.close()
        byteArrayOutputStream.close()
        return serStr
    }

    /**
     * 字符串反序列化为对象
     * @param encodedText
     */
    @JvmStatic
    @Throws(IOException::class, ClassNotFoundException::class)
    fun <T> decode(encodedText: String?): T {
        val redStr = URLDecoder.decode(encodedText, "UTF-8")
        val byteArrayInputStream = ByteArrayInputStream(redStr.toByteArray())
        val objectInputStream = ObjectInputStream(
                byteArrayInputStream)
        val data = objectInputStream.readObject() as T
        objectInputStream.close()
        byteArrayInputStream.close()
        return data
    }
}