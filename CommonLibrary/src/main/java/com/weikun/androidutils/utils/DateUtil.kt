package com.weikun.androidutils.utils

import android.content.Context
import com.weikun.androidutils.R
import com.weikun.androidutils.model.DateTime
import java.text.SimpleDateFormat
import java.util.*

/***
 * 时间 工具类
 * @author lwk
 * @date 2020/4/13
 */
object DateUtil {
    //常用时间格式
    val SUPPORTED_DATE_FORMATS =
            arrayOf(
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    "EEE, dd MMM yyyy HH:mm:ss zzz",
                    "yyyy-MM-dd'T'HH:mm:ss.sss'Z'",
                    "yyyy-MM-dd'T'HH:mm:ssZ",
                    "EEE MMM dd HH:mm:ss zzz yyyy",
                    "EEEEEE, dd-MMM-yy HH:mm:ss zzz",
                    "EEE MMMM d HH:mm:ss yyyy")

    //星期数组
    val WEEK = arrayOf(7,1,2,3,4,5,6)

    //时间格式 年月日时分秒
    const val TIME_FORMAT1 = "yyyy-MM-dd HH:mm:ss"
    //时间格式 年月日
    const val TIME_FORMAT2 = "yyyy-MM-dd"

    /**
     * 时间友好转换
     * 根据给定格式将 毫秒时间转为 时间字符串
     */
    @JvmStatic
    fun convertLong2DateStr(time: Long, format: String?): String {
        val sdf = SimpleDateFormat(format, Locale.CHINA)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = Date(time)
        return sdf.format(date)
    }

    /**
     * 时间友好转换
     * 将毫秒时间 转为 DateTime对象
     */
    @JvmStatic
    fun convertLong2DateTime(mills: Long): DateTime {
        val sdf = SimpleDateFormat(TIME_FORMAT1, Locale.CHINA)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = Date(mills)
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH]
        val day = calendar[Calendar.DAY_OF_MONTH]
        val weekIndex = calendar[Calendar.DAY_OF_WEEK] - 1
        val weekDay = WEEK[weekIndex]
        val sec = calendar[Calendar.SECOND]
        val min = calendar[Calendar.MINUTE]
        val hour = calendar[Calendar.HOUR_OF_DAY]
        return DateTime(year,month+1,day,weekDay,hour,min,sec)
    }

    /**
     * 时间友好转换
     * 将毫秒时间转换为固定格式字符串
     */
    @JvmStatic
    fun convertLong2DateStr(context: Context,time: Long,detail:Boolean): String {
        val dateTime = convertLong2DateTime(time)
        return convertDateTime2Str(context, dateTime, detail)
    }

    /**
     * 时间友好转换
     * 将DateTime对象转换为固定格式字符串
     */
    @JvmStatic
    fun convertDateTime2Str(context: Context,dateTime: DateTime,detail:Boolean):String{
        val month = context.resources.getStringArray(R.array.month)[dateTime.month-1]
        val weekDay = context.resources.getStringArray(R.array.week)[dateTime.weekDay-1]
        var dateStr = String.format(context.getString(R.string.date_format),
                dateTime.year, month, dateTime.dayOfMonth, weekDay)
        if (detail){
            dateStr += String.format("  %d:%d:%d",dateTime.hour,dateTime.min,dateTime.sec)
        }
        return dateStr
    }

}