package com.weikun.androidutils.ui.widget.guideui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.view.WindowManager;
import android.widget.RelativeLayout;

/**
 * @author linweikun
 * @date 2021/3/1
 */
class GuideView extends RelativeLayout {
    private int mScreenWidth;
    private int mScreenHeight;

    private int mBgColor = 0xaa000000;
    private float mStrokeWidth;
    private Paint mPaint;
    private Paint mTextPaint;
    private Bitmap mBitmap;
    private RectF mBitmapRect;
    private RectF outRect = new RectF();
    private int mPosition = 0;

    private Canvas mCanvas;
    private GuideItem mGuide;

    private Xfermode mode;

    public GuideView(Context context) {
        this(context, null);
        init();
    }

    public GuideView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public GuideView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        mScreenWidth = point.x;
        mScreenHeight = point.y;

        initView();
    }

    private void initView() {

        initPaint();

        mBitmapRect = new RectF();
        mode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

        setWillNotDraw(false);
        setClickable(true);
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mBgColor);
        mPaint.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.INNER));
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(Color.RED);
        mTextPaint.setTextSize(32);
    }

    private void initCanvas() {
        if (mBitmapRect.width() > 0 && mBitmapRect.height() > 0) {
            mBitmap = Bitmap.createBitmap((int) mBitmapRect.width(),
                    (int) mBitmapRect.height(),
                    Bitmap.Config.ARGB_8888);
        } else {
            mBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
        }

        // 矩形最大边距
        mStrokeWidth = Math.max(Math.max(mBitmapRect.left, mBitmapRect.top),
                Math.max(mScreenWidth - mBitmapRect.right, mScreenHeight - mBitmapRect.bottom));

        outRect.left = mBitmapRect.left - mStrokeWidth / 2;
        outRect.top = mBitmapRect.top - mStrokeWidth / 2;
        outRect.right = mBitmapRect.right + mStrokeWidth / 2;
        outRect.bottom = mBitmapRect.bottom + mStrokeWidth / 2;

        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(mBgColor);
    }




    public void setGuide(GuideItem guideItem) {
        this.mGuide = guideItem;
        mBitmapRect.union(guideItem.getRectF());
        initCanvas();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mGuide!=null) {
            mPaint.setXfermode(mode);
            mPaint.setStyle(Paint.Style.FILL);
            drawHighlightArea();
            canvas.drawBitmap(mBitmap, mBitmapRect.left, mBitmapRect.top, null);

            mPaint.setXfermode(null);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mStrokeWidth + 0.1f);
            canvas.drawRect(outRect, mPaint);
        }
    }

    public void recyclerBitmap() {
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        recyclerBitmap();
    }

    private  void drawHighlightArea(){
        RectF rectF = mGuide.getRectF();
        rectF.offset(-mBitmapRect.left, -mBitmapRect.top);
        switch (mGuide.getShape()) {
            case GuideItem.SHAPE_CIRCLE:
                mCanvas.drawCircle(rectF.centerX(), rectF.centerY(),
                        Math.min(mGuide.getWidth(), mGuide.getHeight()) / 2,
                        mPaint);
                break;
            case GuideItem.SHAPE_RECTANGLE:
                mCanvas.drawRect(rectF, mPaint);
                break;
            case GuideItem.SHAPE_OVAL:
                mCanvas.drawOval(rectF, mPaint);
                break;
            default:
        }
    }

}
