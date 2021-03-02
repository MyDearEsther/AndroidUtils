package com.weikun.androidutils.base

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import android.os.Process
import android.widget.Toast
import androidx.lifecycle.*

/**
 *   @author linweikun
 *   @date   2021/2/18
 *
 */
abstract class BaseApp : Application(), ViewModelStoreOwner{

    override fun onCreate() {
        super.onCreate()
        instance = this
        mAppViewModelStore = ViewModelStore()
        mApplicationProvider = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(this)
        )
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                onLifeCycleEvent(event)
            }
        })
    }


    open fun onLifeCycleEvent(event: Lifecycle.Event){

    }

    companion object{
        private var mApplicationProvider: ViewModelProvider? = null
        private var mAppViewModelStore: ViewModelStore?=null
        /**
         * 全局Application单例
         */
        @JvmStatic
        var instance: Application? = null
            private set

        /**
         * 重启应用
         */
        @JvmStatic
        fun restartApp(desActivity: Class<*>?) {
            if (desActivity==null){
                val intent = instance!!.packageManager.getLaunchIntentForPackage(instance!!.packageName);
                intent?.let {
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    instance!!.startActivity(it)
                }
            }else{
                val intent = Intent(instance, desActivity)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                instance!!.startActivity(intent)
            }
//            Process.killProcess(Process.myPid())
        }


        /**
         * 获取全局字符资源
         */
        @JvmStatic
        fun getAppString(id: Int): String {
            return instance!!.getString(id)
        }

        /**
         * 获取全局应用ContentResolver
         */
        @JvmStatic
        val appContentResolver: ContentResolver
            get() = instance!!.contentResolver

        /**
         * 判断当前线程是否为UI线程(主线程)
         */
        @JvmStatic
        val isOnUiThread: Boolean
            get() = Looper.getMainLooper().thread.id == Thread.currentThread().id
        //        return Looper.myLooper() != null;

        fun isUiThread(thread: Thread):Boolean {
            return Looper.getMainLooper().thread.id == thread.id
        }

        /**
         * 全局吐司
         */
        @JvmStatic
        fun toast(text: String?, duration: Int) {
            var myLooper = Looper.myLooper()
            if (myLooper == null) {
                Looper.prepare()
                myLooper = Looper.myLooper()
            }
            Toast.makeText(instance, text, duration).show()
            if (myLooper != null) {
                Looper.loop()
                myLooper.quit()
            }
        }

        /**
         * 判断Android API Level
         */
        @JvmStatic
        fun meetVersion(api: Int): Boolean {
            return Build.VERSION.SDK_INT >= api
        }

        /**
         * 获取版本名称
         */
        @JvmStatic
        fun getVersionName(context: Context): String {
            val packageManager = context.packageManager
            val packageInfo: PackageInfo
            var versionName = ""
            try {
                packageInfo = packageManager.getPackageInfo(context.packageName, 0)
                versionName = packageInfo.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return versionName
        }

        @JvmStatic
        fun <T : ViewModel?> getApplicationScopeViewModel(modelClass: Class<T>): T? {
            mApplicationProvider?.let {
                return it.get(modelClass)
            }
            return null
        }
    }

    override fun getViewModelStore(): ViewModelStore {
        return mAppViewModelStore!!
    }
}