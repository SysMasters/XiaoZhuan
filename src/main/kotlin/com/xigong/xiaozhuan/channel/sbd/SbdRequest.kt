package com.xigong.xiaozhuan.channel.sbd


/**
 * 请求数据类型
 */
class SbdRequest(val accessToken: String?, var body: Any?) {

    var head = Head(accessToken)
    var tail = Tail()

    class Head(val accessToken: String?) {
        var encryptionType = ""
        var encryptionKey = ""
        var sign = ""
    }

    class Tail {
        var channel = ""
        var product = ""
        var system = "sbd_platform"
    }
}