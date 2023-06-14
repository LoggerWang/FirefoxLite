package org.mozilla.rocket.buriedpoint;

import android.text.TextUtils;
import android.util.Log;

import com.anysitebrowser.base.core.stats.Stats;
import com.anysitebrowser.base.core.utils.lang.ObjectStore;

import java.util.HashMap;

public class BuriedPointUtil {

    public static void addActivityInpage(String pve_cur, String prePage) {
        HashMap<String, String> map = new HashMap<>();
        map.put("pve_cur", pve_cur);
        if (!TextUtils.isEmpty(prePage)) {
            map.put("pve_pre", prePage);
        }
        Stats.onEvent(ObjectStore.getContext(), "in_page", map);
        Log.i("real buried point", "in_page " + map.toString());
    }

    public static void addActivityOutPage(String curPage, String prePage, long durationTime) {
        HashMap<String, String> map = new HashMap<>();
        map.put("pve_cur", curPage);
        if (!TextUtils.isEmpty(prePage)) {
            map.put("pve_pre", prePage);
        }
        map.put("result_dur_Num", String.valueOf(durationTime));
        Stats.onEvent(ObjectStore.getContext(), "out_page", map);
        Log.i("real buried point", "out_page" + map.toString());
    }

    public static void addClick(String curPage) {
        HashMap<String, String> map = new HashMap<>();
        map.put("pve_cur", curPage);
        Stats.onEvent(ObjectStore.getContext(), "click_ve", map);
        Log.i("real buried point", "click_ve " + map.toString());
    }

    public static void addShowVe(String curPage) {
        HashMap<String, String> map = new HashMap<>();
        map.put("pve_cur", curPage);
        Stats.onEvent(ObjectStore.getContext(), "show_ve", map);
        Log.i("real buried point", "show_ve " + map.toString());
    }

    public static void addShowVe(String curPage, String key, String value) {
        HashMap<String, String> map = new HashMap<>();
        map.put("pve_cur", curPage);
        map.put(key, value);
        Stats.onEvent(ObjectStore.getContext(), "show_ve", map);
        Log.i("real buried point", "show_ve " + map.toString());
    }


    public static void addClick(String curPage, String key1, String value1) {
        HashMap<String, String> map = new HashMap<>();
        map.put("pve_cur", curPage);
        map.put(key1, value1);
        Stats.onEvent(ObjectStore.getContext(), "click_ve", map);
        Log.i("real buried point", "click_ve " + map.toString());
    }

    public static void addClick(String curPage, String key1, String value1, String key2, String value2) {
        HashMap<String, String> map = new HashMap<>();
        map.put("pve_cur", curPage);
        map.put(key1, value1);
        map.put(key2, value2);
        Stats.onEvent(ObjectStore.getContext(), "click_ve", map);
        Log.i("real buried point", "click_ve " + map.toString());
    }

    public static void addAppStar(String starType) {
        HashMap<String, String> map = new HashMap<>();
        map.put("app_portal", starType);
        Stats.onEvent(ObjectStore.getContext(), "UF_PortalInfo", map);
        Log.i("real buried point", "UF_PortalInfo " + map.toString());
    }
}
