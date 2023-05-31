package org.mozilla.focus.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import de.blinkt.openvpn.OpenVpnApi
import de.blinkt.openvpn.model.ZoneBean
import org.mozilla.focus.R
import org.mozilla.focus.vpn.IOnItemClick
import org.mozilla.focus.vpn.ServerAdapter

class SetVpnActivity : AppCompatActivity() {

    lateinit var ivWorldBg: ImageView
    lateinit var ivConnBg: ImageView
    lateinit var ivState: ImageView
    lateinit var tvConnState: TextView
    lateinit var tvConnStateHint: TextView
    lateinit var rvZone: RecyclerView

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


        val zoneList = OpenVpnApi.zoneLiveData.value
        rvZone.adapter = ServerAdapter(zoneList!!, object : IOnItemClick {
            override fun iOnItemClick(position: Int, nodeBean: ZoneBean) {

            }
        }, zoneList.indexOfFirst { it.auto == 1 })
    }
}