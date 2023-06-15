package de.blinkt.openvpn.flowapi

import com.mucc.flownet.retrofit
import de.blinkt.openvpn.model.*
import okhttp3.ResponseBody
import retrofit2.http.*

interface ApiInterface {

    // 获取服务节点对应的ovpn文件地址
    @GET("vpn/profile/allocate")
    suspend fun getNodeFileMsgNew(@QueryMap body: HashMap<String, String>): BaseResponse<VpnProfileBean>

    @GET("vpn/zone/list")
    suspend fun getZoneList(@QueryMap body: HashMap<String, String>): BaseResponse<ZoneModel>

    @GET("vpn/zone/profile/get")
    suspend fun getZoneProfile(@QueryMap body: HashMap<String, String>): BaseResponse<ZoneProfileModel>

    @GET("server/node")
    suspend fun getServerNode(@Query("appId") appId: String): BaseResponse<ServerBean>

    @POST("server/connect")
    suspend fun getNodeFileMsg(@Body body: HashMap<String, String>): BaseResponse<String>

    @Streaming
    @GET
    suspend fun downloadVpnFile(@Url fileUrl: String): ResponseBody
}

val Api: ApiInterface by lazy {
    retrofit.create(ApiInterface::class.java)
}