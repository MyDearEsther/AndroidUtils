package com.weikun.androidutils.ui.widget.guideui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;

import com.weikun.androidutils.R;
import com.weikun.androidutils.utils.ScreenUtil;
import java.util.ArrayList;
import java.util.List;
import static android.view.Gravity.CENTER;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * 简易的UI引导
 * @author linweikun
 * @date 2021/3/1
 */
public class GuideUI {
    private GuideListener mGuideListener;
    private GuideView mGuideView;
    private Context mContext;
    private List<GuideItem> mGuides = new ArrayList<>();
    private FrameLayout mParentView;
    private int mPosition = -1;
    public GuideUI(Activity activity){
        this.mContext = activity;
        this.mParentView = (FrameLayout) activity.getWindow().getDecorView();
    }

    public void setGuides(@NonNull List<GuideItem> guides){
        this.mGuides = guides;
    }

    public void setGuideListener(GuideListener listener){
        this.mGuideListener = listener;
    }

    public boolean isShowing() {
        return mParentView.indexOfChild(mGuideView) > 0;
    }

    public void show(){
        if (isShowing()||mGuides.isEmpty()){
            return;
        }
        nextGuide();
    }

    private View createTipsView(){
        GuideItem guide = mGuides.get(mPosition);
        LinearLayout tipsView = new LinearLayout(mContext);
        tipsView.setGravity(Gravity.CENTER_HORIZONTAL);
        tipsView.setLayoutParams(new RelativeLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        tipsView.setOrientation(LinearLayout.VERTICAL);
        TextView textView = new TextView(mContext);
        textView.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        int padding = (int)ScreenUtil.dip2px( 5);
        textView.setPadding(padding, padding, padding, padding);
        textView.setGravity(CENTER);
        textView.setText(guide.getDescription());
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(16f);
        tipsView.addView(textView);
        Button btnConfirm = new Button(mContext);
        btnConfirm.setGravity(CENTER);
        btnConfirm.setTextColor(Color.WHITE);
        btnConfirm.setTextSize(13f);
        btnConfirm.setText(R.string.confirm);
        btnConfirm.setBackgroundResource(R.drawable.btn_selector);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        params.topMargin = (int)ScreenUtil.dip2px( 10);
        btnConfirm.setLayoutParams(params);
        int lr = (int)ScreenUtil.dip2px( 8);
        int tb = (int)ScreenUtil.dip2px( 5);
        btnConfirm.setPadding(lr, tb, lr, tb);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextGuide();
            }
        });
        tipsView.addView(btnConfirm);
        return tipsView;
    }

    private void nextGuide(){
        mPosition++;
        if (mPosition==mGuides.size()){
            mPosition = 0;
            dismiss();
            return;
        }
        if (mGuideView!=null){
            mGuideView.recyclerBitmap();
            if (mParentView.indexOfChild(mGuideView)>0){
                mParentView.removeView(mGuideView);
            }
            mGuideView = null;
        }
        mGuideView = new GuideView(mContext);
        mGuideView.setGuide(mGuides.get(mPosition));
        View tipsView = createTipsView();
        addView(tipsView, CENTER, CENTER, new RelativeLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        mParentView.addView(mGuideView,new FrameLayout.LayoutParams(MATCH_PARENT,MATCH_PARENT));
        if (mGuideListener!=null){
            mGuideListener.onGuideShow(mPosition);
        }
    }

    private void addView(View view, int offsetX, int offsetY, RelativeLayout.LayoutParams params) {
        if (params == null) {
            params = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        }
        if (offsetX == CENTER) {
            params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        } else if (offsetX < 0) {
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            params.rightMargin = -offsetX;
        } else {
            params.leftMargin = offsetX;
        }

        if (offsetY == CENTER) {
            params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        } else if (offsetY < 0) {
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            params.bottomMargin = -offsetY;
        } else {
            params.topMargin = offsetY;
        }

        mGuideView.addView(view, params);
    }

    /**
     * 结束引导
     */
    public void dismiss(){
        if (mGuideListener!=null){
            mGuideListener.onGuideEnd();
        }
        mGuideView.recyclerBitmap();
        if (mParentView.indexOfChild(mGuideView)>0){
            mParentView.removeView(mGuideView);
        }
    }
}
