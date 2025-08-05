package com.xigong.xiaozhuan.channel.sbd

import com.bxjjren.common.network.entity.SbdRequest
import com.xigong.xiaozhuan.channel.VersionParams
import com.xigong.xiaozhuan.channel.huawei.HWAppInfoResp
import com.xigong.xiaozhuan.channel.huawei.HWTokenParams
import com.xigong.xiaozhuan.log.AppLogger
import com.xigong.xiaozhuan.log.action
import com.xigong.xiaozhuan.util.ApkInfo
import com.xigong.xiaozhuan.util.ProgressChange
import java.io.File

class SbdClient {

    private val connectApi = SbdApi()

    @Throws
    suspend fun uploadApk(
        file: File,
        apkInfo: ApkInfo,
        clientId: String,
        clientSecret: String,
        versionParams: VersionParams,
        progressChange: ProgressChange
    ): Unit = AppLogger.action(LOG_TAG, "提交新版本") {
//        val rawToken = getToken(clientId, clientSecret)
//        val token = "Bearer $rawToken"
//        val appId = getAppId(clientId, token, apkInfo.applicationId)
//        val uploadUrl = getUploadUrl(clientId, token, appId, file)
//        uploadFile(file, uploadUrl, progressChange)
//        val bindResult = bindApk(clientId, token, appId, file, uploadUrl)
//        waitApkReady(clientId, token, appId, bindResult)
//        modifyUpdateDesc(clientId, token, appId, versionParams.updateDesc)
//        submit(clientId, token, appId, versionParams.onlineTime)

    }

    /**
     * 获取App信息
     */
    @Throws
    suspend fun getAppInfo(): SbdAppInfo? = AppLogger.action(LOG_TAG, "获取App信息") {
        val appInfo = connectApi.getAppInfo()
        appInfo.throwOnFail("获取app版本失败")
        appInfo.body
    }

    @Throws
    suspend fun saveVersion(
        account: String,
        pwd: String,
        apkInfo: ApkInfo,
        versionParams: VersionParams
    ): SbdAppInfo? =
        AppLogger.action(LOG_TAG, "新增版本信息") {
            if (token.isNullOrBlank()) {
                getToken(account, pwd)
            }
            val version = mutableMapOf<String, Any>(
                "title" to versionParams.updateTitle,
                "description" to versionParams.updateDesc,
                "versionName" to apkInfo.versionName,
                "versionCode" to apkInfo.versionCode,
                "url" to apkInfo.path,
                "forceUpdate" to if (versionParams.forceUpdate) 1 else 0,
                "appType" to "android",
                "versionType" to "android",
            )
            val request = SbdRequest(token, version)
            val appInfo = connectApi.saveVersion(request)
            appInfo.throwOnFail("新增app版本失败")
            appInfo.body
        }


    /**
     * 获取token
     */
    private suspend fun getToken(
        account: String, password: String
    ): String = AppLogger.action(LOG_TAG, "获取token") {
        val result =
            connectApi.getToken(
                SbdTokenParams(
                    loginName = account,
                    loginPassword = AESUtils.shaEncrypt(password)
                )
            )
        result.throwOnFail("获取token失败")
        val accessToken = result.body?.accessToken
        checkNotNull(accessToken)
        token = accessToken
        return@action accessToken
    }


    companion object {
        private const val LOG_TAG = "收保单Api"
        private var token: String? = ""
    }
}