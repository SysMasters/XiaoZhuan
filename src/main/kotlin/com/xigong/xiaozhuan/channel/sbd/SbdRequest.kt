package com.bxjjren.common.network.entity


/**
 * 请求数据类型
 */
class SbdRequest(accessToken: String?, body: Any) {

    var head = Head(accessToken)
    var body: Any? = body
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