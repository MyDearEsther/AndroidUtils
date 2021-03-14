package com.weikun.camera

import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat

/**
 *   @author linweikun
 *   @date   2021/3/4
 *
 */
class CameraManager(private val activity:AppCompatActivity,private val viewFinder:PreviewView){
    private var mPreview:Preview? = null
    private var mCamera:Camera? = null
    fun startPreview(whichCamera:Int,analyzer: CameraAnalyzer, size: Size?){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        val executor = ContextCompat.getMainExecutor(activity)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            mPreview = Preview.Builder()
                .build()
            val cameraSelector = CameraSelector.Builder().requireLensFacing(whichCamera).build()
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                val builder = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
                size?.let {
                    builder.setTargetResolution(it)
                }
                val imageAnalysis = builder.build()
                imageAnalysis.setAnalyzer(executor, analyzer)
                // Bind use cases to camera
                mCamera = cameraProvider.bindToLifecycle(
                    activity, cameraSelector, mPreview)
                viewFinder.surfaceProvider
                mPreview?.setSurfaceProvider(viewFinder.surfaceProvider)
            } catch(e: Exception) {
                e.printStackTrace()
            }

        }, executor)
    }



}