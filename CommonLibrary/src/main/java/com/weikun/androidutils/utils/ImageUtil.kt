package com.weikun.androidutils.utils

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.media.MediaScannerConnection
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.drawToBitmap
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.experimental.and

/**
 * 图像工具类
 * @author lwk
 * @date 2020/4/15
 */
object ImageUtil {

    /**
     * Bitmap -> byte[]
     */
    @JvmStatic
    fun bitmap2Bytes(bitmap: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        return baos.toByteArray()
    }

    /**
     * byte[] -> Bitmap
     */
    @JvmStatic
    fun bytes2Bitmap(bytes: ByteArray): Bitmap? {
        if (bytes.isNotEmpty()) {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
        return null
    }

    /**
     * 从Resources获取Bitmap
     * @param context
     * @param resId 图片资源ID
     */
    @JvmStatic
    fun getBitmapFromResources(context: Context, resId: Int): Bitmap {
        return BitmapFactory.decodeResource(context.resources, resId)
    }

    /**
     * Drawable -> Bitmap
     */
    @JvmStatic
    fun drawable2Bitmap(drawable: Drawable): Bitmap {
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        val colorConfig = if (drawable.opacity == PixelFormat.OPAQUE) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888
        val bitmap = Bitmap.createBitmap(width, height, colorConfig)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }


    /**
     * Bitmap -> Drawable
     */
    @JvmStatic
    fun bitmap2Drawable(context: Context, bitmap: Bitmap): Drawable {
        return BitmapDrawable(context.resources, bitmap)
    }


    /**
     * 获取图片分辨率
     */
    @JvmStatic
    fun getImageResolution(path: String?): IntArray {
        val opts = BitmapFactory.Options()
        //只请求图片宽高，不解析图片像素(请求图片属性但不申请内存，解析bitmap对象，该对象不占内存)
        opts.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, opts)
        val imageWidth = opts.outWidth
        val imageHeight = opts.outHeight
        return intArrayOf(imageWidth, imageHeight)
    }


    private fun getExifInterface(context: Context,uri: Uri): ExifInterface? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        return ExifInterface(inputStream)
    }

    /**
     * 获取图片信息
     * @param uri 文件Uri
     */
    @JvmStatic
    fun getImageInfo(context: Context,uri: Uri): ImageExif? {
        val exif = getExifInterface(context, uri)
                ?: return null
        //图片宽度
        val width = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH)
        //图片高度
        val height = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH)
        //描述
        val description = exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION)
        //日期
        val dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME)
        //光圈值
        val aperture = exif.getAttribute(ExifInterface.TAG_APERTURE_VALUE)
        //ISO感光度
        val iso = exif.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS)
//        //海拔
//        val altitude = exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE)
//        //海拔名称
//        val altitudeName = exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF)
        //白平衡
        val whiteBalance = exif.getAttributeInt(ExifInterface.TAG_WHITE_BALANCE,0)
        //焦距
        val focalLength = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)
//        //纬度名称
//        val latitudeName = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
//        //经度名称
//        val longitudeName = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)
        //曝光时间
        val exposureTime = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)
        //旋转度
        val orientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION)
        //设备型号
        val model = exif.getAttribute(ExifInterface.TAG_MODEL)
        //闪光灯
        val flash = exif.getAttributeInt(ExifInterface.TAG_FLASH,0)
        return ImageExif(width, height, description, dateTime, orientation, aperture, iso, exposureTime, flash, whiteBalance, focalLength, model)
    }

    /**
     * 更新图片描述
     * @param uri
     * @param description
     */
    @JvmStatic
    @WorkerThread
    fun setImageDescription(context: Context,uri: Uri, description: String): Boolean {
        val exif = getExifInterface(context, uri)
                ?: return false
        //设置属性值
        exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, description)
        //保存 较耗时，内部会遍历保存的所有值
        exif.saveAttributes()
        return true
    }

    /**
     * 获取缩略图
     * @param uri 文件Uri
     */
    @JvmStatic
    fun getThumbnail(context: Context,uri: Uri): Bitmap? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val exif = ExifInterface(inputStream)
        if (!exif.hasThumbnail()) {
            //无缩略图
            return null
        }
        //getThumbnail() 生成缩略图 native方法
        return bytes2Bitmap(exif.thumbnail)
    }

    /**
     * Drawable换色
     * @param context
     * @param drawable
     * @param colorId 颜色资源ID
     */
    @JvmStatic
    fun tintDrawableByColorId(context: Context, drawable: Drawable, colorId: Int): Drawable {
        return tintDrawable(drawable, ContextCompat.getColor(context, colorId))
    }

    /**
     * Drawable换色
     * @param drawable
     * @param color 颜色值
     */
    @JvmStatic
    fun tintDrawable(drawable: Drawable, color: Int): Drawable {
        val list = ColorStateList.valueOf(color)
        val wrappedDrawable = DrawableCompat.wrap(drawable)
        DrawableCompat.setTintList(wrappedDrawable, list)
        return wrappedDrawable
    }

    /**
     * 根据资源名称获取ID
     */
    @JvmStatic
    fun getResId(variableName: String?, c: Class<*>): Int {
        return try {
            val idField = c.getDeclaredField(variableName!!)
            idField.getInt(idField)
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }


    /**
     * 压缩Bitmap
     * @param bitmap 图片资源
     * @param format 压缩格式
     * PNG为无损格式，此时quality参数无作用，大小上不会有变化
     * WEBP是Google退出的图片格式，比JPEG更省空间，大概可以优化30%
     * @param quality 压缩质量
     */
    @JvmStatic
    @WorkerThread
    fun compressBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int): Boolean {
        val outputStream = ByteArrayOutputStream()
        //quality 为0～100，0表示最小体积，100表示最高质量
        return bitmap.compress(format, quality, outputStream)
    }


    /**
     * 获取圆角图片
     * @return Bitmap
     */
    @JvmStatic
    fun getRoundedCornerBitmap(bitmap: Bitmap, roundPx: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()
        val rect = Rect(0, 0, width, height)
        val rectF = RectF(rect)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = -0xbdbdbe
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    /**
     * Bitmap缩放
     * @param bitmap
     * @param scaleFactor 缩放比例 <1缩小 >1放大
     * @return Bitmap
     */
    @JvmStatic
    fun zoomBitmap(bitmap: Bitmap, scaleFactor: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val matrix = Matrix()
        matrix.postScale(scaleFactor, scaleFactor)
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    }

    /**
     * Drawable缩放
     * @param drawable
     * @param scaleFactor 缩放比例 <1缩小 >1放大
     * @return Drawable
     */
    @JvmStatic
    fun zoomDrawable(context: Context, drawable: Drawable, scaleFactor: Float): Drawable {
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        // drawable转换成bitmap
        val oldBmp: Bitmap = drawable2Bitmap(drawable)
        // 创建操作图片用的Matrix对象
        val matrix = Matrix()
        // 设置缩放比例
        matrix.postScale(scaleFactor, scaleFactor)
        // 建立新的bitmap，其内容是对原bitmap的缩放后的图
        val newBmp = Bitmap.createBitmap(oldBmp, 0, 0, width, height, matrix, true)
        return BitmapDrawable(context.resources, newBmp)
    }


    /**
     * 保存图像
     * @param context 上下文
     * @param imageView ImageView
     * @param path 保存路径
     */
    @JvmStatic
    @WorkerThread
    fun saveImage(context: Context, imageView: ImageView, path: String): Boolean {
        val bitmap = imageView.drawToBitmap()
        val file = File(path)
        try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
            //通知MediaStore进行刷新
            MediaScannerConnection.scanFile(context, arrayOf(path), arrayOf("image/*"), null)
            return true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    @JvmStatic
    fun createBitmapFromView(view: View): Bitmap? {
        if (view is ImageView) {
            val drawable = view.drawable
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }
        }
        view.clearFocus()
        val bitmap = createBitmapSafely(view.width,
                view.height, Bitmap.Config.ARGB_8888, 1)
        if (bitmap != null) {
            val canvas = Canvas()
            canvas.setBitmap(bitmap)
            view.draw(canvas)
            canvas.setBitmap(null)
        }
        return bitmap
    }

    private fun createBitmapSafely(width: Int, height: Int, config: Bitmap.Config?, retryCount: Int): Bitmap? {
        return try {
            Bitmap.createBitmap(width, height, config!!)
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            if (retryCount > 0) {
                System.gc()
                return createBitmapSafely(width, height, config, retryCount - 1)
            }
            null
        }
    }

    @JvmStatic
    fun convertYUVToRGB(width: Int,height: Int,yData:ByteArray,uData:ByteArray,vData:ByteArray):Bitmap{
        val argb = IntArray(width * height)
        var y = 0
        var u = 0
        var v = 0
        for (row in 0 until height) {
            for (col in 0 until width) {
                // 3.1 获取YUV
                y = (yData[row * width + col] and 0xff.toByte()).toInt() // Y 数组是紧密排布的
                if (col and 0x1 == 0) { // UV 每行的奇数元素是有用的，偶数位不要
                    u = (uData[row / 2 * width + col] and 0xff.toByte()).toInt() // U 是两行合并一行的
                    v = (vData[row / 2 * width + col] and 0xff.toByte()).toInt() // V 和U是一样的
                }
                // 3.2 转换公式转换
                val facter = 128
                var r = (y + 1.4022 * (v - facter)).toInt()
                var g = (y - 0.3456 * (u - facter) - 0.7145 * (v - facter)).toInt()
                var b = (y + 1.771 * (u - facter)).toInt()
                // 3.3 防止出现负数和超范围
                r = if (r < 0) 0 else r.coerceAtMost(255)
                g = if (g < 0) 0 else g.coerceAtMost(255)
                b = if (b < 0) 0 else b.coerceAtMost(255)
                // 3.5 把3个byte组成一个32位的int类型，一个int包含RGBA四个通道，
                argb[col * height + height - 1 - row] = (-0x1000000
                        or (r shl 16 and 0xff0000)
                        or (g shl 8 and 0xff00)
                        or (b and 0xff))
            }
        }
        // 4. 创建Bitmap
        return Bitmap.createBitmap(argb,height,width, Bitmap.Config.ARGB_8888)
    }


    data class ImageExif(
        //图片宽度
        val width: String?,
        //图片高度
        val height: String?,
        //描述
        val description: String?,
        //日期时间
        val dateTime: String?,
        //旋转度
        val orientation: String?,
        //光圈值
        val aperture: String?,
        //ISO感光度
        val iso: String?,
        //曝光时间
        val exposureTime: String?,
        //闪光灯
        val flash: Int?,
        //白平衡
        val whiteBalance: Int?,
        //焦距
        val focalLength: String?,
        //设备型号
        val model: String?
    )
}