package de.blinkt.openvpn.utils

enum class ProxyModeEnum {
    PROXY_SMART, // 智能
    PROXY_ALL, // 所有
    PROXY_CUSTOM,  // 自定义
    PROXY_APP // APP条目
}

enum class ConnectState{
    STATE_DISCONNECTED,
    STATE_PREPARE,
    STATE_CONNECTING,
    STATE_START
}
enum class ServerState{
    SERVER_STATE_SUCCESS,
    SERVER_STATE_ERROR
}