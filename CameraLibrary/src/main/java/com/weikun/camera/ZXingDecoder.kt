package com.weikun.camera

import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.weikun.androidutils.utils.LogUtil

/**
 *   @author linweikun
 *   @date   2021/3/5
 *
 */
class ZXingDecoder:CameraAnalyzer() {
    var decoded = false
    interface ScanningCallback{
        fun onResult(result:String)
    }
    var callback:ScanningCallback? = null

    private fun decodeYUV(data:ByteArray,width:Int,height:Int):String?{
        val rotatedData = ByteArray(data.size)
        for (y in 0 until height) {
            for (x in 0 until width) {
                rotatedData[x * height + height - y - 1] = data[x + y * width]
            }
        }
        try {
            val source = PlanarYUVLuminanceSource(data,height,width,0,0,height,width,false)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val result = MultiFormatReader().decode(binaryBitmap)
            result?.let {
                return it.text
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
        return null
    }

    override fun analyze(image: ImageProxy) {
        val data = image.planes[0].buffer.toByteArray()
        val result = decodeYUV(data,image.width,image.height)
        LogUtil.d("anayze")
        if (!decoded){
            result?.let{
                LogUtil.d("result:$it")
                decoded = true
                callback?.onResult(it)
            }

        }
    }
}