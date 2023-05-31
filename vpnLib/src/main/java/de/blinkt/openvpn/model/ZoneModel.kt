package de.blinkt.openvpn.model

import java.io.Serializable

data class ZoneModel(val zones:ArrayList<ZoneBean>): Serializable

data class ZoneBean (
    var zone_id: String,
    var zone_name: String,
    var auto:Int // 是否为自动节点，0否，1是
): Serializable
