package org.mozilla.focus.utils;

import android.app.PendingIntent;
import android.os.Build;

/**
 * @desc: 升级android 12 用
 * @author: wanglezhi
 * @createTime: 2022/8/30 4:24 下午
 */
public class PendingIntentUtils {
    public static int getFlag(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S?  PendingIntent.FLAG_MUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;
    }
}
