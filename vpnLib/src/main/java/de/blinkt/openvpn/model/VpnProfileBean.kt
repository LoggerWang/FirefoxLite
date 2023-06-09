package de.blinkt.openvpn.model

import java.io.Serializable

/**
 * @desc:
 * @author: wanglezhi
 * @createTime: 2023/5/11 5:04 下午
 */
class VpnProfileBean : Serializable {
    //    {
    //        "code":200,
    //            "profile":{
    //        "id":1,
    //                "profile_id":"SHAREit_ptest1_test", "link":"https://profile-cdn.hpstudio.org/profile/test/us15/20230324/udp_user16.ovpnx",
    //                "server_code":"SG",
    //                "salt":"ssfsdk123",
    //                "expired_timestamp":1669473847848,
    //                "active_time":1200
    //    },
    //        "client_ip":"182.20.8.203"
    //    }
    var code = 0
    var client_ip: String? = null
    var profile: VpnProfileDataBean? = null
}

class VpnProfileDataBean : Serializable{
    //    {
    //        "id":1,
    //                "profile_id":"SHAREit_ptest1_test", "link":"https://profile-cdn.hpstudio.org/profile/test/us15/20230324/udp_user16.ovpnx",
    //                "server_code":"SG",
    //                "salt":"ssfsdk123",
    //                "expired_timestamp":1669473847848,
    //                "active_time":1200
    //    }
    var id: String? = null
    var link: String? = null
    var profile_id: String? = null
    var server_code: String? = null
    var salt: String? = null
    var expired_timestamp: String? = null
    var active_time: String? = null
}