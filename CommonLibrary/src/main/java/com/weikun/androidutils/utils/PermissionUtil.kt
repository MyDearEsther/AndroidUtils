package com.weikun.androidutils.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import com.weikun.androidutils.R
import com.weikun.androidutils.ui.dialog.Dialog

/**
 * Android权限申请 工具类
 * @author lwk
 * @date 2019/11/11
 */
object PermissionUtil {
    /**
     * 请求码
     * */
    val BASE_REQUEST_CODE = 0

    /**
     * 存储权限
     * */
    val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

    /**
     * 相机权限
     * */
    val PERMISSION_CAMERA = arrayOf(Manifest.permission.CAMERA)

    /**
     * 网络权限
     * */
    val PERMISSIONS_NETWORK = arrayOf(Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE)

    /**
     * 蓝牙权限
     * */
    val PERMISSIONS_BLUETOOTH = arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)


    /**
     * 检查权限
     * @param context 环境上下文
     * @param permission 权限名称
     */
    @JvmStatic
    fun checkPermission(context: Context?, permission: String?): Boolean {
        return (ContextCompat.checkSelfPermission(context!!, permission!!) == PackageManager.PERMISSION_GRANTED
                && PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED)
    }

    /**
     * 请求权限
     * @param context 上下文环境
     * @param permissions 权限内容
     */
    @JvmStatic
    fun requestPermission(context: Any, permissions: Array<String>) {
        when (context) {
            is Activity -> {
                ActivityCompat.requestPermissions(context, permissions, BASE_REQUEST_CODE)
            }
            is Fragment -> {
                context.requestPermissions(permissions, BASE_REQUEST_CODE)
            }
            else ->{
                throw Throwable("Requesting permission should be only in activity or fragment.")
            }
        }
    }

    @JvmStatic
    fun onRequestPermissionsResult(activity: Activity, requestCode: Int, permissions: Array<out String>, grantResults: IntArray):Boolean {
        if (requestCode == BASE_REQUEST_CODE) {
            for (i in permissions.indices) {
                if (grantResults[i] != 0) {
                    if (!checkPermission(activity, permissions[i])) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i])) {
                            askForPermission(activity)
                            return false
                        }
                        break
                    }
                }
            }
        }
        return true
    }


    /**
     * 显示 请求跳转权限设置 对话框
     */
    private fun askForPermission(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Need Permission!")
        builder.setNegativeButton(R.string.cancel, null)
        builder.setPositiveButton(R.string.confirm) { _, _ ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:\$packageName") // 根据包名打开对应的设置界面
            context.startActivity(intent)
        }
        builder.create().show()
    }

    /**
     * 检查悬浮窗权限
     */
    @JvmStatic
    @RequiresApi(Build.VERSION_CODES.M)
    fun checkOverlay(context: Context){
        if (!isOverlayPermitted(context)){
            val dialog = Dialog(context)
            dialog.title = context.getString(R.string.alert)
            dialog.setPositiveButton(context.getString(R.string.confirm),object : Dialog.KeyDownCallback {
                override fun onEvent(): Boolean {
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return false
                }
            })
            dialog.setNegativeButton(context.getString(R.string.cancel),null)
        }
    }

    @JvmStatic
    @RequiresApi(Build.VERSION_CODES.M)
    fun isOverlayPermitted(context: Context):Boolean{
        return Settings.canDrawOverlays(context)
    }
}