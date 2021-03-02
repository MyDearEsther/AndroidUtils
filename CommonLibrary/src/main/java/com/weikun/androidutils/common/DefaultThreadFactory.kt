package com.weikun.androidutils.common

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * 默认线程工厂
 * @author lwk
 * @date 2019/8/2
 */
class DefaultThreadFactory private constructor() : ThreadFactory {
    private val group: ThreadGroup
    private val threadNumber = AtomicInteger(1)
    private val namePrefix: String
    override fun newThread(var1: Runnable): Thread {
        val thread = Thread(group, var1, namePrefix + threadNumber.getAndIncrement(), 0L)
        if (thread.isDaemon) {
            thread.isDaemon = false
        }
        if (thread.priority != DEFAULT_PRIORITY) {
            thread.priority =
                DEFAULT_PRIORITY
        }
        return thread
    }

    companion object {
        private val POOLNUMBER = AtomicInteger(1)
        private const val DEFAULT_PRIORITY = 5
        private const val THREAD_BASE_NAME = "KunGalleryThread-"
        @JvmStatic
        var instance: DefaultThreadFactory? = null
            get() {
                if (field == null) {
                    synchronized(DefaultThreadFactory::class.java) {
                        if (field == null) {
                            field =
                                DefaultThreadFactory()
                        }
                    }
                }
                return field
            }
            private set
    }

    init {
        val securityManager = System.getSecurityManager()
        group = if (securityManager != null) securityManager.threadGroup else Thread.currentThread().threadGroup!!
        namePrefix = THREAD_BASE_NAME + POOLNUMBER.getAndIncrement() + "-thread-"
    }
}