package com.weikun.androidutils.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.text.format.Formatter
import com.weikun.androidutils.base.BaseApp
import java.io.*
import java.net.URI
import java.util.*

/**
 * 文件工具类
 * @author lwk
 * @date 2020/4/13
 */
object FileUtil {

    @JvmStatic
    fun getPathFromFilepath(filepath: String): String {
        if (!TextUtils.isEmpty(filepath)) {
            val pos = filepath.lastIndexOf(File.separator)
            if (pos != -1) {
                return filepath.substring(0, pos)
            }
        }
        return ""
    }

    @JvmStatic
    fun getNameFromFilepath(filepath: String): String {
        if (!TextUtils.isEmpty(filepath)) {
            val pos = filepath.lastIndexOf(File.separator)
            if (pos != -1) {
                return filepath.substring(pos + 1)
            }
        }
        return ""
    }


    /**
     * 友好显示文件大小
     */
    @JvmStatic
    fun getFormattedSize(context: Context,size: Long): String {
        return Formatter.formatFileSize(context,size)
    }

    /**
     * 复制文件目录
     *
     * @param srcDir  要复制的源目录 eg:/mnt/sdcard/DB
     * @param destDir 复制到的目标目录 eg:/mnt/sdcard/db/
     * @return
     */
    @JvmStatic
    fun copyDir(srcDir: String?, destDir: String): Boolean {
        val sourceDir = File(srcDir!!)
        //判断文件目录是否存在
        if (!sourceDir.exists()) {
            return false
        }
        //判断是否是目录
        return if (sourceDir.isDirectory) {
            val fileList = sourceDir.listFiles()
            val targetDir = File(destDir)
            //创建目标目录
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }
            //遍历要复制该目录下的全部文件
            for (i in fileList!!.indices) {
                if (fileList[i].isDirectory) { //如果如果是子目录进行递归
                    copyDir(fileList[i].path + "/",
                            destDir + fileList[i].name + "/")
                } else { //如果是文件则进行文件拷贝
                    copyFile(fileList[i].path, destDir + fileList[i].name)
                }
            }
            true
        } else {
            copyFileToDir(File(srcDir), File(destDir))
            true
        }
    }

    /**
     * 复制文件（非目录）
     *
     * @param srcFile  要复制的源文件
     * @param destFile 复制到的目标文件
     * @return
     */
    @JvmStatic
    fun copyFile(srcFile: String?, destFile: String?): Boolean {
        return try {
            val streamFrom: InputStream = FileInputStream(srcFile)
            val streamTo: OutputStream = FileOutputStream(destFile)
            val buffer = ByteArray(1024)
            var len: Int
            while (streamFrom.read(buffer).also { len = it } > 0) {
                streamTo.write(buffer, 0, len)
            }
            streamFrom.close()
            streamTo.close()
            true
        } catch (ex: Exception) {
            false
        }
    }

    /**
     * 把文件拷贝到某一目录下
     *
     * @param srcFile
     * @param destDir
     * @return
     */
    @JvmStatic
    fun copyFileToDir(srcFile: File, desDir: File): File? {
        if (!desDir.exists()) {
            desDir.mkdir()
        }

        val destFile = File(desDir, srcFile.name)
        return try {
            val streamFrom: InputStream = FileInputStream(srcFile)
            val streamTo: OutputStream = FileOutputStream(destFile)
            val buffer = ByteArray(1024)
            var len: Int
            while (streamFrom.read(buffer).also { len = it } > 0) {
                streamTo.write(buffer, 0, len)
            }
            streamFrom.close()
            streamTo.close()
            destFile
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    /**
     * 移动文件目录到某一路径下
     *
     * @param srcFile
     * @param destDir
     * @return
     */
    @JvmStatic
    fun moveFile(srcFile: String?, destDir: String): Boolean { //复制后删除原目录
        if (copyDir(srcFile, destDir)) {
            deleteFile(File(srcFile!!))
            return true
        }
        return false
    }

    /**
     * 删除文件（包括目录）
     *
     * @param delFile
     */
    @JvmStatic
    fun deleteFile(delFile: File) { //如果是目录递归删除
        if (delFile.isDirectory) {
            val files = delFile.listFiles()
            for (file in files!!) {
                deleteFile(file)
            }
        } else {
            delFile.delete()
        }
        //如果不执行下面这句，目录下所有文件都删除了，但是还剩下子目录空文件夹
        delFile.delete()
    }


    private fun isChildFile(file: File, dir: File): Boolean {
        if (!file.exists()) {
            return false
        }
        val parent = file.parent ?: throw Throwable("no parent")
        return parent == dir.path
    }

    private fun isDeepChildFile(path: String?, dir: File?): Boolean {
        if (dir == null || !dir.exists()) {
            return false
        }
        var file: File? = File(path!!)
        if (!file!!.exists()) {
            return false
        }
        while (file != null) {
            file = try {
                if (isChildFile(file, dir)) {
                    return true
                }
                file.parentFile
            } catch (t: Throwable) {
                break
            }
        }
        return false
    }


    private fun getFileType(file: File?): String {
        return ""
    }

    /**
     * File -> Uri
     */
    @JvmStatic
    fun getFileFromUri(context: Context, uri: Uri): File? {
        var path: String? = null
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    path = Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                if (id.startsWith("raw:")) {
                    return File(id.replaceFirst("raw:".toRegex(), ""))
                }
                val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
                path = getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                path = getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            path = getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return File(URI(uri.toString()))
        }
        return if (path==null){
            null
        }else{
            File(path)
        }
    }

    private fun getDataColumn(context: Context, uri: Uri?, selection: String?,
                              selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs,
                    null)
            if (cursor != null && cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(column_index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }


    //外置存储类型——SD卡
    const val EXTERNAL_TYPE_SDCARD = 0

    //外置存储类型——USB
    const val EXTERNAL_TYPE_USB = 1
    /**
     * 获取外置存储路径
     * @param context 环境上下文
     * @param type 外置存储类型 SD卡/U盘
     * @param  writablePath 是否获取可写路径 读取路径是/storage/.. 写权限路径是/mnt/media_rw/..
     * @return 路径数组(一个或多个)
     */
    @JvmStatic
    fun getExternalStorage(context: Context, type: Int,writablePath:Boolean): Array<File> {
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val files = ArrayList<File>()
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                //6.0以下检测方法 获取对象是StorageVolume
                val getVolumeList = StorageManager::class.java.getDeclaredMethod("getVolumeList")
                val volumeList = getVolumeList.invoke(storageManager) as Array<StorageVolume>
                if (volumeList != null) {
                    for (volume in volumeList) {
                        var method = volume.javaClass.getDeclaredMethod("isRemovable")
                        val isRemovable = method.invoke(volume) as Boolean
                        if (isRemovable) {
                            method = volume.javaClass.getDeclaredMethod("getPathFile")
                            val file = method.invoke(volume) as File
                            val path = file.path
                            //根据路径名称判断是否USB或SD卡
                            if (type == EXTERNAL_TYPE_SDCARD && path.contains("sdcard")) {
                                files.add(file)
                            } else if (type == EXTERNAL_TYPE_USB && path.contains("usb")) {
                                files.add(file)
                            }
                        }
                    }
                }
            } else {
                //6.0及以上的检测方法 获取对象是VolumeInfo
                val getVolumes = StorageManager::class.java.getDeclaredMethod("getVolumes")
                val getVolumeInfo = getVolumes.invoke(storageManager) as List<Any>
                //获取对象是VolumeInfo
                for (obj in getVolumeInfo) {
                    val getType = obj.javaClass.getField("type")
                    //存储类型
                    val storageType = getType.getInt(obj)
                    //外置存储 TYPE_PUBLIC
                    if (storageType == 0) {
                        var method = obj.javaClass.getDeclaredMethod(if(writablePath) "getInternalPath" else "getPath")
                        val file = method.invoke(obj) as File
                        method = obj.javaClass.getDeclaredMethod("getDisk")
                        val diskInfo = method.invoke(obj)
                        if (type == EXTERNAL_TYPE_USB) {
                            //通过反射接口判断是否为USB
                            method = diskInfo.javaClass.getDeclaredMethod("isUsb")
                            val isUsb = method.invoke(diskInfo) as Boolean
                            if (isUsb) {
                                files.add(file)
                            }
                        } else if (type == EXTERNAL_TYPE_SDCARD) {
                            //通过反射接口判断是否为SD卡
                            method = diskInfo.javaClass.getDeclaredMethod("isSd")
                            val isSdCard = method.invoke(diskInfo) as Boolean
                            if (isSdCard) {
                                files.add(file)
                            }
                        }
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return files.toTypedArray()
    }

    const val DIR_TYPE_ROOT = 0
    const val DIR_TYPE_DATA_FILES= 2
    const val DIR_TYPE_DATA_CACHE = 3
    const val DIR_TYPE_EXTERNAL_CACHE = 4
    const val DIR_TYPE_EXTERNAL_PRIVATE_MEDIA = 5
    const val DIR_TYPE_EXTERNAL_PUBLIC_MEDIA = 6
    const val DIR_TYPE_PICTURES = 7
    const val DIR_TYPE_MUSIC = 8
    const val DIR_TYPE_DOWNLOAD = 9
    const val DIR_TYPE_REMOVED = 10
    const val DIR_TYPE_CRASH_LOG = 11

    /**
     * 获取APP相关文件夹
     * @param type 路径类型
     */
    @JvmStatic
    fun getDir(context: Context,dirType: Int, createIfNonExist: Boolean): File? {
        val dir = when (dirType) {
            DIR_TYPE_ROOT -> Environment.getRootDirectory()
            DIR_TYPE_DATA_CACHE -> context.cacheDir
            DIR_TYPE_EXTERNAL_CACHE -> context.externalCacheDir
            DIR_TYPE_DATA_FILES -> context.filesDir
            DIR_TYPE_EXTERNAL_PRIVATE_MEDIA -> context.getExternalFilesDir(Environment.DIRECTORY_DCIM)
            DIR_TYPE_EXTERNAL_PUBLIC_MEDIA -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            DIR_TYPE_PICTURES -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            DIR_TYPE_MUSIC -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            DIR_TYPE_DOWNLOAD -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            DIR_TYPE_REMOVED -> context.getExternalFilesDir(Environment.MEDIA_REMOVED)
            DIR_TYPE_CRASH_LOG ->  File(context.filesDir,"crash")
            else -> null
        }
        if (createIfNonExist && dir != null && !dir.exists()) {
            dir.mkdir()
        }
        return dir
    }


}