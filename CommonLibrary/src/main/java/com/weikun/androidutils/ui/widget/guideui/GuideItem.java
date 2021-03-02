package com.weikun.androidutils.ui.widget.guideui;

import android.graphics.RectF;
import android.view.View;

/**
 * @author linweikun
 * @date 2021/3/1
 */
public class GuideItem {

    private View mHighlightView;
    private String mDescription;
    private int mShape;

    public static final int SHAPE_CIRCLE = 0;

    public static final int SHAPE_RECTANGLE = 1;

    public static final int SHAPE_OVAL = 2;

    public GuideItem(View highlightView, int shape, String description) {
        this.mHighlightView = highlightView;
        this.mShape = shape;
        this.mDescription = description;
    }

    public String getDescription(){
        return this.mDescription;
    }

    public int getShape(){
        return mShape;
    }

    public int getWidth() {
        return this.mHighlightView.getWidth();
    }

    public int getHeight() {
        return this.mHighlightView.getHeight();
    }

    public RectF getRectF() {
        RectF rectF = new RectF();
        if (mHighlightView != null) {
            int[] location = new int[2];
            mHighlightView.getLocationOnScreen(location);
            rectF.left = location[0];
            rectF.top = location[1];
            rectF.right = location[0] + mHighlightView.getWidth();
            rectF.bottom = location[1] + mHighlightView.getHeight();
        }
        return rectF;
    }

    public int[] getHighlightViewLocationOnScreen(){
        int[] location = new int[2];
        mHighlightView.getLocationOnScreen(location);
        return location;
    }
}
