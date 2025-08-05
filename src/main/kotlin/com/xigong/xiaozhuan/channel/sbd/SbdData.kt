package com.xigong.xiaozhuan.channel.sbd

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class SbdTokenParams(
    val loginName: String,
    val loginPassword: String,
    val registrationId: String = "1507bfd3f730933d43f",
    val grantType: String = "pwd",
    val clientId: String = "app",
    val clientSecret: String = "app",
    val systemId: String = "sbd_platform",
    val orgId: String = "713423460730159100",
    val operatorOrgId: String = "sbd_platform",
    val operatorSystemId: String = "713423460730159100",
) {

}

@JsonClass(generateAdapter = false)
data class SbdTokenResp(
    val accessToken: String?,
)


@JsonClass(generateAdapter = false)
data class SbdAppInfo(
    val id: String = "",
    val versionCode: Int = 0,
    val versionName: String = "",
    val title: String = "",
    val url: String = "",
    val appType: String = "",
    val description: String = "",
    val forceUpdate: Int = 0
)