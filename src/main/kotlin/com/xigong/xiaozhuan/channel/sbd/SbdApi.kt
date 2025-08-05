package com.xigong.xiaozhuan.channel.sbd

import com.bxjjren.common.network.entity.SbdRequest
import com.xigong.xiaozhuan.RetrofitFactory
import retrofit2.http.Body
import retrofit2.http.POST

internal fun SbdApi(): SbdApi {
    return RetrofitFactory.create("https://gamma-m.shoubaodan.com/")
}

internal interface SbdApi {

    /**
     * 获取token
     */
    @POST("api/user-authority/oauth/token")
    suspend fun getToken(
        @Body params: SbdTokenParams
    ): SbdResponse<SbdTokenResp>


    @POST("api/common/version/getSysVersionRecord/1/android")
    suspend fun getAppInfo(): SbdResponse<SbdAppInfo>


    @POST("api/common/version/saveVersion")
    suspend fun saveVersion(@Body request: SbdRequest): SbdResponse<SbdAppInfo>


}