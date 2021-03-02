package com.weikun.androidutils.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import okhttp3.*
import java.util.concurrent.TimeUnit


object NetworkUtil {


    /**
     * 创建网络请求并执行 (结合协程)
     * @param url 请求地址
     * @param parameters 请求参数
     * @param callback 异步请求回调 null时同步调用
     * */
    @JvmStatic
    fun createGetRequest(url: String,parameters:HashMap<String,String>?, callback: Callback?) {
        val client = OkHttpClient.Builder()
                .retryOnConnectionFailure(false)
                .connectTimeout(5, TimeUnit.SECONDS)
                //添加请求参数
                .addInterceptor(ParamsInterceptor(parameters))
                .build()
        //构建者模式创建Request请求，传入目的Url
        val request = Request.Builder()
                .url(url)
                .build()
        //创建Call会话
        val call = client.newCall(request)
        if (callback == null) {
            val response = call.execute()
        } else {
            //异步请求 入队
            call.enqueue(callback)
        }
    }

    @JvmStatic
    fun isNetworkAvailable(context: Context):Boolean{
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT < 23){
            val networkInfo = manager.activeNetworkInfo
            if (networkInfo!=null&&networkInfo.isAvailable){
                return true
            }
        }else{
            val network = manager.activeNetwork
            if (network != null) {
                val nc = manager.getNetworkCapabilities(network)
                if (nc!=null){
                    return (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI));
                }
            }
        }
        return false
    }

    private class ParamsInterceptor(val params:HashMap<String,String>?): Interceptor {
        private val METHOD_GET = "GET"
        private val METHOD_POST = "POST"
        private val HEADER_KEY_USER_AGENT = "User-Agent"
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val requestBuilder = request.newBuilder()
            val urlBuilder = request.url.newBuilder()
            when(request.method){
                METHOD_GET -> {
                    if (params!=null){
                        for (entry in params.entries){
                            urlBuilder.addQueryParameter(entry.key,entry.value)
                        }
                        val httpUrl = urlBuilder.build()
                        requestBuilder.url(httpUrl)
                    }
                }
                METHOD_POST -> {
                    val bodyBuilder = FormBody.Builder()

                }
            }
            return chain.proceed(requestBuilder.build())
        }

    }
}