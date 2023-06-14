package org.mozilla.focus.activity

import android.os.Build
import android.widget.Toast
import com.anysitebrowser.base.core.log.Logger
import com.anysitebrowser.base.core.utils.app.AppDist
import com.anysitebrowser.base.core.utils.device.DeviceHelper
import com.anysitebrowser.base.core.utils.lang.ObjectStore
import com.anysitebrowser.tools.core.utils.Utils
import de.blinkt.openvpn.OpenVpnApi
import de.blinkt.openvpn.model.ZoneBean
import de.blinkt.openvpn.utils.ConnectState
import de.blinkt.openvpn.utils.Settings
import org.mozilla.rocket.util.isNetworkAvailable
import java.lang.Exception

/**
 * @desc:
 * @author: wanglezhi
 * @createTime: 2023/6/13 3:12 PM
 */
open class VpnBaseActivity : BaseActivity() {

    lateinit var zoneList: ArrayList<ZoneBean>
    lateinit var settings: Settings

    override fun applyLocale() {}
    val vpnZone: Unit
        get() {}

    fun getVpnZoneList(){
        val map = HashMap<String, String>()
//        map.put("trace_id", "muccc")
//        map.put("app_id", "com.sailfishvpn.fastly.ios")
//        map.put("app_version", "4010079")
//        map.put("os_version", "29")
//        map.put("user_id", "a.5242925349028eb5")
//        map.put("beyla_id", "fa441a4acf544cf0b9179d7d898cd7b3")

        map.put("trace_id", Utils.createUniqueId())
        map.put("app_id", AppDist.getAppId(ObjectStore.getContext()))
        map.put("app_version", Utils.getVersionCode(ObjectStore.getContext()).toString())
        map.put("os_version", Build.VERSION.SDK_INT.toString())
        map.put("user_id", DeviceHelper.getOrCreateDeviceId(this))
//            map.put("country","")
//            map.put("gaid","")
        map.put("beyla_id", DeviceHelper.getOrCreateDeviceId(this))

        if (OpenVpnApi.serverStateLiveData.value== ConnectState.STATE_DISCONNECTED || OpenVpnApi.serverStateLiveData.value==null) {
            OpenVpnApi.getZoneList(map)
        }
    }
     fun connectVpn() {
         if (!isSettingsInit()) {
            settings = Settings(this, "vpn_settings")
         }
//        if (!isAdded) {
//            Logger.d("legend","===HomeFragment==connectVpn=时候，isAdded==false???===${OpenVpnApi.serverStateLiveData.value}")
//            return
//        }
        if (!isNetworkAvailable(this)) {
            OpenVpnApi.serverStateLiveData.value= ConnectState.STATE_DISCONNECTED
            Toast.makeText(this,"please check your network", Toast.LENGTH_LONG).show()
            return

        }
        if (OpenVpnApi.serverStateLiveData.value==ConnectState.STATE_PREPARE
            || OpenVpnApi.serverStateLiveData.value==ConnectState.STATE_CONNECTING
            || OpenVpnApi.serverStateLiveData.value==ConnectState.STATE_START) {
            Logger.d("legend","===HomeFragment==connectVpn=时候，正在连接货已经连接成功===${OpenVpnApi.serverStateLiveData.value}")
            return
        }
         try {
            zoneList = OpenVpnApi.zoneLiveData.value!!
         }catch (e:Exception){
             OpenVpnApi.serverStateLiveData.value= ConnectState.STATE_DISCONNECTED
             return
         }

        if(!this::zoneList.isInitialized || zoneList.isEmpty()){
//            OpenVpnApi.serverStateLiveData.value=ConnectState.STATE_DISCONNECTED
            return
        }
        val map = HashMap<String, String>()
//        map.put("trace_id", "muccc")
//        map.put("app_id", "com.sailfishvpn.fastly.ios")
//        map.put("app_version", "4010079")
//        map.put("os_version", "29")
//        map.put("user_id", "a.5242925349028eb5")
////            map.put("country","")
////            map.put("gaid","")
//        map.put("beyla_id", "fa441a4acf544cf0b9179d7d898cd7b3")

         map.put("app_id", AppDist.getAppId(ObjectStore.getContext()))
        map.put("trace_id", Utils.createUniqueId())
        map.put("app_version", Utils.getVersionCode(ObjectStore.getContext()).toString())
        map.put("os_version", Build.VERSION.SDK_INT.toString())
        map.put("user_id", DeviceHelper.getOrCreateDeviceId(this))
//            map.put("country","")
//            map.put("gaid","")
        map.put("beyla_id", DeviceHelper.getOrCreateDeviceId(this))


//        val bean = zoneList.firstOrNull { zoneBean ->
            var connectZoneId = settings.get("connectZoneId", "")
         if (connectZoneId!!.isEmpty()) {
             connectZoneId = zoneList[0].zone_id
         }
//            if (connectZoneId!!.isEmpty()) zoneBean.auto == 1 else connectZoneId == zoneBean.zone_id
//        }
        Logger.d("legend", "===HomeFragment==getZoneProfile====$connectZoneId")
        OpenVpnApi.getZoneProfile(map, connectZoneId)

    }
    fun isSettingsInit():Boolean{
       return ::settings.isInitialized
    }


}