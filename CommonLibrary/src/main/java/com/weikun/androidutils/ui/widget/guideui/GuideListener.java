package com.weikun.androidutils.ui.widget.guideui;

/**
 * @author linweikun
 * @date 2021/3/1
 */
public interface GuideListener {
    /**
     * 显示UI引导
     * @param position 引导序号
     */
    void onGuideShow(int position);

    /**
     * 引导结束
     */
    void onGuideEnd();
}
