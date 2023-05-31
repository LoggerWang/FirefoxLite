package de.blinkt.openvpn.model

import java.io.Serializable

data class ServerNodeBean (
    val id: String,
    val regionImg: String,
    val serverOrgName: String,
    val regionCode: String,
    var regionName: String,
    val nodeIp: String,
    val serverName: String,
    val serverOrgId: String,
    val serverId: String,
    var delayMills: String?,
    var vpnFileStr: String,
    var downloadUrl: String,
    var salt:String
): Serializable

data class ServerBean(val list: ArrayList<ServerNodeBean>):Serializable

