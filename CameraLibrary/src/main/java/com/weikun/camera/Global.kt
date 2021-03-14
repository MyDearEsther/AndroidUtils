package com.weikun.camera

import android.content.Context
import android.os.Bundle
import com.weikun.androidutils.utils.CommonUtils

object Global {


    @JvmStatic
    fun createZXingDecodingFragment(context:Context,whichCamera:Int,scanningCallback:ZXingDecoder.ScanningCallback){
        ZXingDecodingFragment.callback = scanningCallback
        val data = Bundle()
        data.putInt("whichCamera",whichCamera)
        CommonUtils.createNewFragmentActivity(context,ZXingDecodingFragment::class.java.name,context.getString(R.string.title_qr_scanning),data,null)
    }
}