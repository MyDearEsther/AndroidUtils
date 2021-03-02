package com.weikun.androidutils.common

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Printer
import com.weikun.androidutils.utils.LogUtil

/**
 * 监测器
 * 检测主线程耗时
 * @author lwk
 * @date   2020/6/23
 *
 */
class Monitor private constructor() {
    private var mHandler: Handler

    init {
        val logThread = HandlerThread("log")
        logThread.start()
        mHandler = Handler(logThread.looper)
    }

    fun startMonitor() {
        mHandler.postDelayed(mLogRunnable, TIME_BLOCK)
    }

    fun stopMonitor() {
        mHandler.removeCallbacks(mLogRunnable)
    }

    private var mLogRunnable: Runnable = Runnable {
        val sb = StringBuilder()
        val stackTrace = Looper.getMainLooper().thread.stackTrace;
        for (s in stackTrace) {
            sb.append(s.toString());
            sb.append("\n");
        }
        LogUtil.e(sb.toString())
    }

    companion object {
        //方法耗时的卡口
        val TIME_BLOCK = 100L

        private val instance
            get() = Monitor()

        @JvmStatic
        fun run() {
            var time:Long = 0
            Looper.getMainLooper().setMessageLogging(object : Printer {
                //分发和处理消息开始前的log
                private val START = ">>>>> Dispatching";
                //分发和处理消息结束后的log
                private val END = "<<<<< Finished";
                override fun println(x: String) {
                    if (x.startsWith(START)) {
                        //开始计时
                        time = System.currentTimeMillis()
                        instance.startMonitor()
                    }
                    if (x.startsWith(END)) {
                        //结束计时，并计算出方法执行时间
                        time = System.currentTimeMillis() - time
                        if(time>= TIME_BLOCK){
                            LogUtil.e("ui method do too much work! method time:$time ms\n")
                        }
                        instance.stopMonitor()
                    }
                }

            })
        }
    }
}