package org.mozilla.rocket.config;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.mozilla.focus.history.model.Site;
import org.mozilla.rocket.bean.NetSet;
import org.mozilla.rocket.bean.NetSites;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RemoteConfigHelper {

    //获取默认搜索引擎
    public static String getDefaultSearchEngine() {
        return RemoteConfigController.getFirebaseString("key_default_search_engine");
    }

    public static boolean isForceUpdateApk() {
        return RemoteConfigController.getFirebaseBoolean("key_force_update");
    }

    public static boolean isShowHotSites() {
        return RemoteConfigController.getFirebaseBoolean("key_show_home_hotsite");
    }

    public static String getHotSites(){
        return RemoteConfigController.getFirebaseString("key_home_hotsite_s");
    }

    public static boolean isShowHomeWatchSites(){
        return RemoteConfigController.getFirebaseBoolean("key_show_home_watchsite");
    }

    public static String getUpdateDialogMessage(){
        return RemoteConfigController.getFirebaseString("key_update_message");
    }

    public static String getUpdateDialogTitle(){
        return RemoteConfigController.getFirebaseString("key_update_title");
    }

    public static long getUpdateVersion(){
        return RemoteConfigController.getFirebaseLong("key_update_vertion");
    }

    public static String getUpdateVersionName(){
        return RemoteConfigController.getFirebaseString("key_update_version_name");
    }

    public static List<Site> getHomeWatchSites(){
        String remoteStr =  RemoteConfigController.getFirebaseString("key_home_watch_sites");
        List<Site> netSets = new Gson().fromJson(remoteStr, new TypeToken<List<Site>>(){}.getType());
        return netSets;
    }

    public static List<Site> getHomeHotSites(){
        String remoteStr =  RemoteConfigController.getFirebaseString("key_home_hot_sites");
        List<Site> netSets = new Gson().fromJson(remoteStr, new TypeToken<List<Site>>(){}.getType());
        return netSets;
    }
}
