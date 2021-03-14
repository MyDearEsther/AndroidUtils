package com.weikun.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

/**
 *   相机数据分析处理
 *   @author linweikun
 *   @date   2021/3/5
 */
abstract class CameraAnalyzer: ImageAnalysis.Analyzer {
    protected fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }

}