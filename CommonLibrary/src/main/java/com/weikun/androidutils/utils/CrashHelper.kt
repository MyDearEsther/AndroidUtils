package com.weikun.androidutils.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import com.weikun.androidutils.R
import com.weikun.androidutils.base.BaseApp
import com.weikun.androidutils.ui.dialog.Dialog
import java.io.*
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/**
 *  异常记录
 *   @author lwk
 *   @date   2020/9/7
 */
class CrashHelper private constructor(val context: Context) : Thread.UncaughtExceptionHandler {
    companion object {
        private var instance: CrashHelper? = null

        @JvmStatic
        fun init(context: Context) {
            if (instance == null) {
                instance = CrashHelper(context)
            }
        }


        /**
         * 获取崩溃日志数量
         */
        @JvmStatic
        fun getCrashInfoNum(context: Context): Int {
            val crashDir = FileUtil.getDir(context,FileUtil.DIR_TYPE_CRASH_LOG, false) ?: return 0
            if (crashDir.exists() && crashDir.isDirectory) {
                val files = crashDir.listFiles()
                if (files != null) {
                    return files.size
                }
            }
            return 0
        }

        /**
         * 读取崩溃日志
         */
        @JvmStatic
        fun readCrashInfo(context: Context): ArrayList<String> {
            val array = ArrayList<String>()
            val crashDir = FileUtil.getDir(context,FileUtil.DIR_TYPE_CRASH_LOG, false) ?: return array
            if (crashDir.exists() && crashDir.isDirectory) {
                for (file in crashDir.listFiles()!!) {
                    val input = FileInputStream(file)
                    val bytes = input.readBytes()
                    array.add(String(bytes))
                    input.close()
                }
            }
            return array
        }

    }
    private var mDefaultThreadUncaughtExceptionHandler:Thread.UncaughtExceptionHandler? = null
    private val logs = HashMap<String, String>()

    init {
        Handler(Looper.getMainLooper()).post {
            while (true) {
                try {
                    Looper.loop()
                } catch (e: Throwable) {
                    //主线程异常
                    uncaughtException(Looper.getMainLooper().thread,e)
                }
            }
        }
        mDefaultThreadUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        //子线程异常
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    /**
     * 处理未捕获异常
     */
    override fun uncaughtException(thread: Thread, ex: Throwable) {
        LogUtil.e("uncaught exception")
        collectDeviceInfo()
        saveCrashInfo(ex)
        ex.printStackTrace()
        //已开启悬浮窗权限
        if(PermissionUtil.isOverlayPermitted(context)){
            showAlertDialog(BaseApp.isUiThread(thread))
        }else{
            BaseApp.toast(BaseApp.getAppString(R.string.crash_toast),1000)
        }
    }


    private fun showAlertDialog(isOnUiThread:Boolean) {
        val dialog = Dialog(context, true)
        dialog.title = context.getString(R.string.title_alert)
        dialog.text = context.getString(R.string.crash_warning)
        dialog.setPositiveButton(context.getString(R.string.confirm), object :
            Dialog.KeyDownCallback {
            override fun onEvent(): Boolean {
                BaseApp.restartApp(null)
                return false
            }
        })
        dialog.setNegativeButton(context.getString(R.string.cancel), null)
        if (isOnUiThread){
            dialog.show()
        }else{
            var myLooper = Looper.myLooper()
            if (myLooper == null) {
                Looper.prepare()
                myLooper = Looper.myLooper()
            }
            dialog.show()
            if (myLooper != null) {
                Looper.loop()
                myLooper.quit()
            }
        }
    }


    /**
     *  保存崩溃日志
     */
    private fun saveCrashInfo(ex: Throwable) {
        val sb = StringBuffer()
        for ((key, value) in logs.entries) {
            sb.append("$key=$value\n")
        }
        val writer: Writer = StringWriter()
        val printWriter = PrintWriter(writer)
        ex.printStackTrace(printWriter)
        var cause: Throwable? = ex.cause
        while (cause != null) {
            cause.printStackTrace(printWriter)
            cause = cause.cause
        }
        printWriter.close()
        val result: String = writer.toString()
        sb.append(result)
        try {
            val timeMillis = System.currentTimeMillis()
            val time: String = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault()).format(Date())
            val fileName = "crash$time-$timeMillis.log"
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                val targetFile = File(FileUtil.getDir(context,FileUtil.DIR_TYPE_CRASH_LOG, true), fileName)
                val fos = FileOutputStream(targetFile)
                fos.write(sb.toString().toByteArray())
                fos.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * 收集设备信息
     */
    private fun collectDeviceInfo() {
        val pm = context.packageManager
        try {
            val pi = pm.getPackageInfo(context.packageName, PackageManager.GET_ACTIVITIES)
            if (pi != null) {
                val versionName = if (pi.versionName == null) "null" else pi.versionName
                val versionCode = pi.versionCode.toString() + ""
                logs["versionName"] = versionName
                logs["versionCode"] = versionCode
            }
        } catch (e: PackageManager.NameNotFoundException) {
        }
        val fields: Array<Field> = Build::class.java.declaredFields
        for (field in fields) {
            try {
                field.isAccessible = true
                logs[field.name] = field[null]!!.toString()
            } catch (e: Exception) {
            }
        }
    }
}