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
        Log.i("browser buried point", "in_page " + map.toString());
    }

    public static void addActivityOutPage(String curPage, String prePage, long durationTime) {
        HashMap<String, String> map = new HashMap<>();
        map.put("pve_cur", curPage);
        if (!TextUtils.isEmpty(prePage)) {
            map.put("pve_pre", prePage);
        }
        map.put("result_dur_Num", String.valueOf(durationTime));
        Stats.onEvent(ObjectStore.getContext(), "out_page", map);
        Log.i("browser buried point", "out_page" + map.toString());
    }

    public static void addClick(String curPage) {
        HashMap<String, String> map = new HashMap<>();
        map.put("pve_cur", curPage);
        Stats.onEvent(ObjectStore.getContext(), "click_ve", map);
        Log.i("browser buried point", "click_ve " + map.toString());
    }

    public static void addShowVe(String curPage) {
        HashMap<String, String> map = new HashMap<>();
        map.put("pve_cur", curPage);
        Stats.onEvent(ObjectStore.getContext(), "show_ve", map);
        Log.i("browser buried point", "show_ve " + map.toString());
    }

    public static void addShowVe(String curPage, String key, String value) {
        HashMap<String, String> map = new HashMap<>();
        map.put("pve_cur", curPage);
        map.put(key, value);
        Stats.onEvent(ObjectStore.getContext(), "show_ve", map);
        Log.i("browser buried point", "show_ve " + map.toString());
    }


    public static void addClick(String curPage, String key1, String value1) {
        HashMap<String, String> map = new HashMap<>();
        map.put("pve_cur", curPage);
        map.put(key1, value1);
        Stats.onEvent(ObjectStore.getContext(), "click_ve", map);
        Log.i("browser buried point", "click_ve " + map.toString());
    }

    public static void addClick(String curPage, String key1, String value1, String key2, String value2) {
        HashMap<String, String> map = new HashMap<>();
        map.put("pve_cur", curPage);
        map.put(key1, value1);
        map.put(key2, value2);
        Stats.onEvent(ObjectStore.getContext(), "click_ve", map);
        Log.i("browser buried point", "click_ve " + map.toString());
    }

    public static void addAppStar(String starType) {
        HashMap<String, String> map = new HashMap<>();
        map.put("app_portal", starType);
        Stats.onEvent(ObjectStore.getContext(), "UF_PortalInfo", map);
        Log.i("browser buried point", "UF_PortalInfo " + map.toString());
    }

    public static void addSearchResult(String curPage, String result, String searchKey, String searchMode, String host, String url){
        HashMap<String, String> map = new HashMap<>();
        map.put("pve_cur", curPage);
        map.put("key1", result);
        map.put("key2", searchKey);
        map.put("key3", searchMode);
        map.put("key4", host);
        map.put("key5", url);
        Stats.onEvent(ObjectStore.getContext(), "search_result", map);
        Log.i("browser buried point", "search_result " + map.toString());
    }
    public static void resultConnect(String connectType,String nodeArea,String connect_result,String error,String request_id,String node_id) {
        HashMap<String, String> map = new HashMap<>();
        map.put("connect_type", connectType);
        map.put("node_area", nodeArea);
        map.put("connect_result", connect_result);
        map.put("error", error);
        map.put("request_id", request_id);
        map.put("node_id", node_id);
        map.put("pve_cur", connectType.equals("1")?"/home/x/x":"VPN/x/x");
        Log.i("resultConnect", "result_connect " + map.toString());
        Stats.onEvent(ObjectStore.getContext(), "result_connect", map);
    }
    public static void resultDisconnect(String connectType,String nodeArea,String error,String duration,String request_id,String node_id,String net_type) {
        HashMap<String, String> map = new HashMap<>();
        map.put("connect_type", connectType);
        map.put("node_area", nodeArea);
        map.put("error", error);
        map.put("duration", duration);
        map.put("request_id", request_id);
        map.put("node_id", node_id);
        map.put("net_type", net_type);
        map.put("pve_cur", connectType.equals("1")?"/home/x/x":"VPN/x/x");
        Stats.onEvent(ObjectStore.getContext(), "result_disconnect", map);
        Log.i("real buried point", "result_disconnect " + map.toString());
    }
}
