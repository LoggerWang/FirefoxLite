package org.mozilla.focus.activity

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.blinkt.openvpn.OpenVpnApi
import de.blinkt.openvpn.model.ZoneBean
import de.blinkt.openvpn.utils.ConnectState
import de.blinkt.openvpn.utils.Settings
import org.mozilla.focus.R
import org.mozilla.focus.vpn.IOnItemClick
import org.mozilla.focus.vpn.ServerAdapter
import java.util.HashMap

class SetVpnActivity : AppCompatActivity() {

    private lateinit var ivWorldBg: ImageView
    private lateinit var ivConnBg: ImageView
    private lateinit var ivState: ImageView
    private lateinit var tvConnState: TextView
    private lateinit var tvConnStateHint: TextView
    private lateinit var rvZone: RecyclerView

    private lateinit var rotationAnim: ObjectAnimator
    private lateinit var settings: Settings

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
        val autoPosition = zoneList!!.indexOfFirst { it.auto == 1 }
        rvZone.layoutManager = LinearLayoutManager(this)
        rvZone.adapter = ServerAdapter(zoneList, object : IOnItemClick {
            override fun iOnItemClick(position: Int, nodeBean: ZoneBean) {
                if (autoPosition == position)
                    settings.set("connectZoneId", "") // 上次连接的zone_id, 如果是自动的, 则为空""
                else
                    settings.set("connectZoneId", nodeBean.zone_id)
            }
        }, autoPosition)

        rotationAnim = ObjectAnimator.ofFloat(ivState, "rotation", 0f, 360f)
        rotationAnim.repeatCount = ValueAnimator.INFINITE

        val connectState = OpenVpnApi.serverStateLiveData.value
        setConnectState(connectState)

        OpenVpnApi.serverStateLiveData.observe(this) {
            setConnectState(it)
        }

        ivConnBg.setOnClickListener {
            val state = OpenVpnApi.serverStateLiveData.value
            if (state == null || state == ConnectState.STATE_DISCONNECTED) {
                connectVpn()
            } else if (state == ConnectState.STATE_START) {
                OpenVpnApi.stopVpn()
            } else {
                // 连接中
            }
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
        } else if (connectState == ConnectState.STATE_START) {
            ivWorldBg.setImageResource(R.mipmap.bg_world_connected)
            ivConnBg.setImageResource(R.mipmap.bg_conn_connected)
            ivState.setImageResource(R.mipmap.conn_connected)
            if (rotationAnim.isStarted) rotationAnim.cancel()
            ivState.rotation = 0f
            tvConnState.text = getString(R.string.vpn_protected)
            tvConnStateHint.text = getString(R.string.vpn_protected_hint)
        } else {
            ivWorldBg.setImageResource(R.mipmap.bg_world_unconnect)
            ivConnBg.setImageResource(R.mipmap.bg_conn_unconnect)
            ivState.setImageResource(R.mipmap.conn_connecting)
            rotationAnim.start()
            tvConnState.text = getString(R.string.vpn_connecting)
            tvConnStateHint.text = ""
        }
    }

    private fun connectVpn() {
        val map = HashMap<String, String>()
        map.put("trace_id", "muccc")
        map.put("app_id", "com.sailfishvpn.fastly.ios")
        map.put("app_version", "4010079")
        map.put("os_version", "29")
        map.put("user_id", "a.5242925349028eb5")
//            map.put("country","")
//            map.put("gaid","")
        map.put("beyla_id", "fa441a4acf544cf0b9179d7d898cd7b3")
//            map.put("ip","")
//            map.put("device_id","")
//            map.put("release_channel","")
        var zoneId = settings.get("connectZoneId", "")
        if (zoneId.isNullOrEmpty()) {
            zoneId =
                OpenVpnApi.zoneLiveData.value!!.firstOrNull { zoneBean -> zoneBean.auto == 1 }!!.zone_id
        }
        OpenVpnApi.getZoneProfile(map, zoneId)
    }

}