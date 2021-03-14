package com.weikun.camera

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Message
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import com.weikun.androidutils.base.ui.BaseActivity
import com.weikun.androidutils.base.ui.BaseFragment
import com.weikun.androidutils.base.ui.UiController
import com.weikun.androidutils.utils.PermissionUtil

/**
 *   @author linweikun
 *   @date   2021/3/4
 *
 */
abstract class CameraPreviewFragment: BaseFragment() {
    protected var toolbar:Toolbar? = null
    protected var mCameraManager:CameraManager? = null
    override val layoutId = R.layout.layout_camera_preview
    private var mWhichCamera = CameraSelector.LENS_FACING_BACK;

    override fun initViews(savedInstanceState: Bundle?) {
        toolbar = searchViewById(R.id.toolbar)
        (activity as BaseActivity).setSupportActionBar(toolbar)
        toolbar!!.title = "Camera"
        (activity as BaseActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val preview = searchViewById<PreviewView>(R.id.camera_preview)
        mCameraManager = CameraManager(requireActivity() as AppCompatActivity,preview)
        arguments?.let {
            mWhichCamera = it["whichCamera"] as Int
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            requireActivity().finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onFragmentResume() {
        super.onFragmentResume()
        if (PermissionUtil.checkPermission(requireContext(),Manifest.permission.CAMERA)){
            initCamera(mWhichCamera)
        }else{
            PermissionUtil.requestPermission(requireContext(),PermissionUtil.PERMISSION_CAMERA)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (PermissionUtil.onRequestPermissionsResult(requireActivity(),requestCode,permissions,grantResults)){
            initCamera(mWhichCamera)
        }
    }

    protected abstract fun initCamera(whichCamera:Int)

}