package org.mozilla.rocket.config;


public class RemoteConfigHelper {

    //获取默认搜索引擎
    public static String getDefaultSearchEngine() {
        return RemoteConfigController.getFirebaseString("key_default_search_engine");
    }

    public static boolean isForceUpdateApk() {
        return RemoteConfigController.getFirebaseBoolean("key_force_update");
    }

    public static boolean isShowHotSites() {
        return RemoteConfigController.getFirebaseBoolean("key_home_hotsite");
    }

    public static String getHotSites(){
        return RemoteConfigController.getFirebaseString("key_home_hotsite_s");
    }

    public static boolean isShowHomeWatchSites(){
        return RemoteConfigController.getFirebaseBoolean("key_home_watchsite");
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
}
