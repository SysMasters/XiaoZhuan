package com.xigong.xiaozhuan.channel.sbd

import com.xigong.xiaozhuan.BuildConfig
import com.xigong.xiaozhuan.channel.sbd.SbdRequest
import com.xigong.xiaozhuan.RetrofitFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

internal fun SbdApi(): SbdApi {
    return RetrofitFactory.create(
        if (BuildConfig.debug) "https://gamma-m.shoubaodan.com/" else "https://www.shoubaodan.com/"
    )
}

internal interface SbdApi {

    /**
     * 获取token
     */
    @FormUrlEncoded
    @POST("api/user-authority/oauth/token")
    suspend fun getToken(
        @Field("loginName") loginName: String,
        @Field("loginPassword") loginPassword: String,
        @Field("registrationId") registrationId: String = "1507bfd3f730933d43f",
        @Field("grant_type") grantType: String = "pwd",
        @Field("client_id") clientId: String = "sbd_platform_713423460730159100",
        @Field("client_secret") clientSecret: String = "sbd_platform_713423460730159100",
        @Field("systemId") systemId: String = "sbd_platform",
        @Field("orgId") orgId: String = "713423460730159100",
        @Field("operatorSystemId") operatorSystemId: String = "sbd_platform",
        @Field("operatorOrgId") operatorOrgId: String = "713423460730159100"
    ): SbdResponse<SbdTokenResp>


    @POST("api/common/version/getSysVersionRecord/1/android")
    suspend fun getAppInfo(): SbdResponse<SbdAppInfo>


    @POST("api/common/version/saveVersion")
    suspend fun saveVersion(@Body request: SbdRequest): SbdResponse<SbdAppInfo>

    /**
     * 获取OSS STS Token
     */
    @POST("api/exapi/oss/getOssStsToken")
    suspend fun getOssStsToken(@Body request: SbdRequest): SbdResponse<SbdOssStsTokenResp>

}