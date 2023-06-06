package org.mozilla.focus.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anysitebrowser.base.core.thread.TaskHelper;

import org.mozilla.focus.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @desc:
 * @author: wanglezhi
 * @createTime: 2023/6/5 3:24 PM
 */
public class StartView extends FrameLayout {
    private static final String TAG = "StartView";
    private Timer overTimer;
    private static long LIMIT_TIME = 1500L;

    public StartView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public StartView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public StartView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        setBackgroundColor(Color.WHITE);
        LayoutInflater.from(context).inflate(R.layout.view_startview, this);
    }

    private void startTimer() {
        /******开始计时广告超时*******/
        overTimer = new Timer();
        overTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TaskHelper.exec(new TaskHelper.UITask() {
                    @Override
                    public void callback(Exception e) {
                        Log.d(TAG, "-----------> " + "closeFlashView（）0000000000");
                        setVisibility(View.GONE);
                    }
                });
            }
        }, LIMIT_TIME);
    }

    public void startShow() {
        setVisibility(VISIBLE);
        startTimer();
    }
}
