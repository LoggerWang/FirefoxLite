package org.mozilla.focus.activity

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anysitebrowser.base.core.log.Logger
import com.anysitebrowser.base.core.net.NetUtils
import com.anysitebrowser.tools.core.utils.Utils
import de.blinkt.openvpn.OpenVpnApi
import de.blinkt.openvpn.model.ZoneBean
import de.blinkt.openvpn.utils.ConnectState
import de.blinkt.openvpn.utils.Settings
import org.mozilla.focus.R
import org.mozilla.focus.vpn.IOnItemClick
import org.mozilla.focus.vpn.ServerAdapter
import org.mozilla.rocket.buriedpoint.BuriedPointUtil
import org.mozilla.rocket.util.isNetworkAvailable

class SetVpnActivity : VpnBaseActivity() {

    private lateinit var ivWorldBg: ImageView
    private lateinit var ivConnBg: ImageView
    private lateinit var ivState: ImageView
    private lateinit var tvConnState: TextView
    private lateinit var tvConnStateHint: TextView
    private lateinit var rvZone: RecyclerView

    private lateinit var rotationAnim: ObjectAnimator
    override lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_vpn)

        findViewById<ImageView>(R.id.ivVpnBack).setOnClickListener { onBackPressed() }
        ivWorldBg = findViewById(R.id.ivWorldBg)
        ivConnBg = findViewById(R.id.ivConnBg)
        ivState = findViewById(R.id.ivState)
        tvConnState = findViewById(R.id.tvConnState)
        tvConnStateHint = findViewById(R.id.tvConnStateHint)
        rvZone = findViewById(R.id.rvZone)

        settings = Settings(this, "vpn_settings")

        val zoneList = OpenVpnApi.zoneLiveData.value
//        val autoPosition = zoneList!!.indexOfFirst { it.auto == 1 }
        var connectPos = settings.getInt("connect_pos",0)
        settings.set("connectZoneId", zoneList?.get(connectPos)?.zone_id)
        rvZone.layoutManager = LinearLayoutManager(this)
        rvZone.adapter = zoneList?.let {
            ServerAdapter(it, object : IOnItemClick {
                override fun iOnItemClick(position: Int, nodeBean: ZoneBean) {
    //                if (autoPosition == position)
    //                    settings.set("connectZoneId", "") // 上次连接的zone_id, 如果是自动的, 则为空""
    //                else
                    if (!isNetworkAvailable(this@SetVpnActivity)) {
                        OpenVpnApi.serverStateLiveData.value= ConnectState.STATE_DISCONNECTED
                        Toast.makeText(this@SetVpnActivity,"please check your network", Toast.LENGTH_LONG).show()
                        return
                    }
                    BuriedPointUtil.addClick("/VPN/node_area/x","node_area",nodeBean.zone_name)
                    settings.set("connectZoneId", nodeBean.zone_id)
                    settings.setInt("connect_pos", position)
                    OpenVpnApi.stopVpn()
                    rvZone.postDelayed({
                        OpenVpnApi.connectType = "2"
                        connectVpn()
                    }, 500)
                    rvZone.postDelayed({ connectVpn() },500)
//                    when(position){
//                        0 -> BuriedPointUtil.addClick("/VPN/node_area/x","node_area","Europe")
//                        1 -> BuriedPointUtil.addClick("/VPN/node_area/x", "node_area", "Asia")
//                        2 -> BuriedPointUtil.addClick("/VPN/node_area/x", "node_area", "America")
//                    }
                }
            }, connectPos)
        }

        rotationAnim = ObjectAnimator.ofFloat(ivState, "rotation", 0f, 360f)
        rotationAnim.repeatCount = ValueAnimator.INFINITE

        val connectState = OpenVpnApi.serverStateLiveData.value
        setConnectState(connectState)
        Logger.d("legend","===SetVpnActivity==connectState==$connectState")

        OpenVpnApi.serverStateLiveData.observe(this) {
            Logger.d("legenddd","===SetVpnActivity==OpenVpnApi.serverStateLiveData.observe==$it")
            setConnectState(it)

        }

        ivConnBg.setOnClickListener {
            if (!isNetworkAvailable(this@SetVpnActivity)) {
                OpenVpnApi.serverStateLiveData.value= ConnectState.STATE_DISCONNECTED
                Toast.makeText(this@SetVpnActivity,"please check your network", Toast.LENGTH_LONG).show()
            }
            val state = OpenVpnApi.serverStateLiveData.value
            if (state == null || state == ConnectState.STATE_DISCONNECTED) {
                OpenVpnApi.connectType = "2"
                connectVpn()
            } else if (state == ConnectState.STATE_START) {
                OpenVpnApi.stopVpn()
                OpenVpnApi.showToast(OpenVpnApi.mActivity.getString(de.blinkt.openvpn.R.string.connection_disconnected))
            } else {
                // 连接中
            }
            BuriedPointUtil.addClick("/VPN/VPN_switch/x")
        }
    }

    private fun setConnectState(connectState: ConnectState?) {
        if (connectState == null || connectState == ConnectState.STATE_DISCONNECTED) {
            ivWorldBg.setImageResource(R.mipmap.bg_world_unconnect)
            ivConnBg.setImageResource(R.mipmap.bg_conn_unconnect)
            ivState.setImageResource(R.mipmap.conn_unconnect)
            if (rotationAnim.isStarted) rotationAnim.cancel()
            ivState.rotation = 0f
            tvConnState.text = getString(R.string.vpn_unprotected)
            tvConnStateHint.text = getString(R.string.vpn_unprotected_hint)
            ivConnBg.isClickable = true
            rvZone.isEnabled = true
            rvZone.isClickable = true
            Logger.d("legenddd", "===断开成功上报====SetVpnActivity==${OpenVpnApi.connectType}")
            if (OpenVpnApi.connectType == "2") {
                var zoneList = OpenVpnApi.zoneLiveData.value
                var connectPos = settings.getInt("connect_pos",0)
                var node = zoneList?.get(connectPos)
                Logger.d("legenddd", "===断开成功上报====SetVpnActivity==")
                var connectStartTime = OpenVpnApi.connectStartTime
                OpenVpnApi.disconnectStartTime- connectStartTime
                BuriedPointUtil.resultDisconnect("1", node?.zone_name,"",connectStartTime.toString(),Utils.createUniqueId(),node?.zone_id,
                    NetUtils.getNetworkTypeName(this))
            }
        } else if (connectState == ConnectState.STATE_START) {
            ivWorldBg.setImageResource(R.mipmap.bg_world_connected)
            ivConnBg.setImageResource(R.mipmap.bg_conn_connected)
            ivState.setImageResource(R.mipmap.conn_connected)
            if (rotationAnim.isStarted) rotationAnim.cancel()
            ivState.rotation = 0f
            tvConnState.text = getString(R.string.vpn_protected)
            tvConnStateHint.text = getString(R.string.vpn_protected_hint)
            ivConnBg.isClickable = true
            rvZone.isEnabled = true
            rvZone.isClickable = true
            Logger.d("legenddd", "===链接成功上报====SetVpnActivity==${OpenVpnApi.connectType}")
            if (OpenVpnApi.connectType == "2") {
                var zoneList = OpenVpnApi.zoneLiveData.value
                var connectPos = settings.getInt("connect_pos",0)
                var node = zoneList?.get(connectPos)
                Logger.d("legenddd", "===链接成功上报====SetVpnActivity==")
                BuriedPointUtil.resultConnect("1", node?.zone_name,"success","",Utils.createUniqueId(),node?.zone_id)
            }
        } else {
            ivWorldBg.setImageResource(R.mipmap.bg_world_unconnect)
            ivConnBg.setImageResource(R.mipmap.bg_conn_unconnect)
            ivState.setImageResource(R.mipmap.conn_connecting)
            rotationAnim.start()
            tvConnState.text = getString(R.string.vpn_connecting)
            tvConnStateHint.text = ""
            if (connectState == ConnectState.STATE_PREPARE || connectState == ConnectState.STATE_CONNECTING){
                ivConnBg.isClickable = false
                rvZone.isEnabled = false
                rvZone.isClickable = false
            }
        }
    }

//    private fun connectVpn() {
//        val map = HashMap<String, String>()
//        map.put("trace_id", "muccc")
//        map.put("app_id", "com.sailfishvpn.fastly.ios")
//        map.put("app_version", "4010079")
//        map.put("os_version", "29")
//        map.put("user_id", "a.5242925349028eb5")
////            map.put("country","")
////            map.put("gaid","")
//        map.put("beyla_id", "fa441a4acf544cf0b9179d7d898cd7b3")
////            map.put("ip","")
////            map.put("device_id","")
////            map.put("release_channel","")
//        var zoneId = settings.get("connectZoneId", "")
////        if (zoneId.isNullOrEmpty()) {
////            zoneId =
////                OpenVpnApi.zoneLiveData.value!!.firstOrNull { zoneBean -> zoneBean.auto == 1 }!!.zone_id
////        }
//        if (zoneId != null) {
//            Logger.d("legend","===SetVanActivity==getZoneProfile==")
//            OpenVpnApi.getZoneProfile(map, zoneId)
//        }
//    }

    private var inpageTime : Long = 0
    override fun onResume() {
        super.onResume()
        inpageTime = System.currentTimeMillis()
        BuriedPointUtil.addActivityInpage("/VPN/x/x","/home/x/x")
    }
    override fun onPause() {
        super.onPause()
        BuriedPointUtil.addActivityOutPage("/VPN/x/x", "/home/x/x", System.currentTimeMillis() - inpageTime)
    }

}