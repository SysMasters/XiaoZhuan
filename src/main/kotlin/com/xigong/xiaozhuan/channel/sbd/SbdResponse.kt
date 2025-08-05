package com.xigong.xiaozhuan.channel.sbd

import com.xigong.xiaozhuan.channel.ApiException


class SbdResponse<R> {
    var head: Head = Head()
    var body: R? = null
    var code = ""
    var desc = ""
    var costTime:String? = ""

    val isSuccess: Boolean
        get() = code == "00000000"

    class Head {
        var accessToken: String? = ""
        var traceId: String? = ""
        var systemId: String? = ""
        var orgIdString: String? = ""
    }

    fun throwOnFail(action: String) {
        if (!isSuccess) {
            throw ApiException(code.toInt(), action, desc)
        }
    }
}