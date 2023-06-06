package org.mozilla.focus.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anysitebrowser.base.core.log.Logger;

/**
 * date：2022/2/28 16:24
 *
 * @author wanglezhi
 * desc:
 */
public class MyWebView extends WebView {

    private OnScrollChangeListener mOnScrollChangeListener;

    public MyWebView(@NonNull Context context) {
        super(context);
    }

    public MyWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollChangeListener!=null) {
            // webview的高度
            float webcontent = getContentHeight() * getScale();

            // 当前webview的高度
            float webnow = getHeight() + getScrollY();
            Logger.d("MyWebView","======onScrollChanged======scrollY==="+ t+"===oldScrollY=="+oldt+"===webcontent=="+webcontent+"===webnow=="+webnow);
            if (Math.abs(webcontent - webnow) < 5) {
                //处于底端
                mOnScrollChangeListener.onPageEnd(l, t, oldl, oldt);
            } else if (getScrollY() == 0) {
                //处于顶端
                mOnScrollChangeListener.onPageTop(l, t, oldl, oldt);
            } else {
                mOnScrollChangeListener.onScrollChanged(l, t, oldl, oldt);
            }
        }

    }

    public void setOnScrollChangeListener(OnScrollChangeListener listener) {
        this.mOnScrollChangeListener = listener;
    }

    public interface OnScrollChangeListener {

        public void onPageEnd(int l, int t, int oldl, int oldt);

        public void onPageTop(int l, int t, int oldl, int oldt);

        public void onScrollChanged(int l, int t, int oldl, int oldt);

    }
}
