package com.weikun.camera

import android.os.Bundle
import android.os.Message
import com.weikun.androidutils.base.ui.UiController
import com.weikun.androidutils.utils.LogUtil

class ZXingDecodingFragment: CameraPreviewFragment() {
    companion object{
        var callback:ZXingDecoder.ScanningCallback? = null
    }
    private val MSG_DECODE_RESULT = 0
    override fun initCamera(whichCamera:Int) {
        mCameraManager?.startPreview(whichCamera,decoder,null)
    }

    override fun initViews(savedInstanceState: Bundle?) {
        super.initViews(savedInstanceState)
        callback = arguments?.getParcelable("callback")
    }



    override val uiController = object : UiController<CameraPreviewFragment> {
        override fun handleOnUiThread(instance: CameraPreviewFragment, msg: Message) {
            if (msg.what == MSG_DECODE_RESULT){
                callback?.onResult(msg.obj as String)
            }
        }

        override val owner = this@ZXingDecodingFragment
    }

    private val decoder = ZXingDecoder().also {
        it.callback = object : ZXingDecoder.ScanningCallback{
            override fun onResult(result: String) {
                LogUtil.d("result:$result")
                sendMessage(MSG_DECODE_RESULT,result)
            }

        }
    }
}