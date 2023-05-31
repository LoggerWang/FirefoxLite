package de.blinkt.openvpn.model

import com.mucc.flownet.ApiException


class BaseResponse<T> {
    val data: T? = null;
    val result_code: Int? = null;
    val message = "";
    val msg = "";

    fun isValid(): Boolean {
        return result_code == 200
    }

    @Throws(ApiException::class)
    fun throwAPIException() {
        if (!isValid()) {
            throw ApiException(result_code, message)
        }
    }
}

