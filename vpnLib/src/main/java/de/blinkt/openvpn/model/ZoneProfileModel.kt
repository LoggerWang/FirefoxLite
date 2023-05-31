package de.blinkt.openvpn.model

import java.io.Serializable

data class ZoneProfileModel(val code: Int, val client_ip: String, val profile: ZoneProfileBean) :
    Serializable

data class ZoneProfileBean(
    var id: Long,
    var profile_id: String,// 配置文件id，pritunl配置文件名
    var link: String,// 下载链接
    var server_code: String,// 服务code
    var salt: String, // 解密密钥
    var expired_timestamp: Long, // 配置使用过期时间
    var active_time: String // 配置使用时长，单位：秒
) : Serializable
