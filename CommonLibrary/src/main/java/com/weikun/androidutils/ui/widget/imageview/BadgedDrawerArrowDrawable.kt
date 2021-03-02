package com.weikun.androidutils.ui.widget.imageview

import android.content.Context
import android.graphics.Canvas
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable

/**
 * @author lwk
 * @date 2020/4/24
 */
class BadgedDrawerArrowDrawable(context: Context?) : DrawerArrowDrawable(context) {
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        //        Paint paint = new Paint();
//        paint.setStyle(Paint.Style.FILL);
//        paint.setColor(Color.RED);
//        paint.setTextSize(60);
//        canvas.drawText("!", canvas.getWidth() - 60, 25, paint);
    }

    /**
     * @param context used to get the configuration for the drawable from
     */
    init {
//        color = getCurrentColor(R.attr.VectorIconColor)
    }
}