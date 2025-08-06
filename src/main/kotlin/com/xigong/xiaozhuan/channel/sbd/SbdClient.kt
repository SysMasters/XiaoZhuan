package com.xigong.xiaozhuan.channel.sbd

import com.xigong.xiaozhuan.channel.sbd.SbdRequest
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
        account: String,
        pwd: String,
        accessKeyId: String,
        secretAccessKey: String,
        versionParams: VersionParams,
        progressChange: ProgressChange
    ): Unit = AppLogger.action(LOG_TAG, "提交新版本") {

//        val bucketName = "frontend-static"
        val bucketName = "hautoosstest"
        val objectName = "shoubaodan3.0/app/img/home/shoubaodan_new.apk"

        val ossUploadUtil =
            OssUploadUtil.create(accessKeyId, secretAccessKey, bucketName, objectName)
        val uploadResult = ossUploadUtil.uploadFile(
            file.absolutePath,
            object : OssUploadUtil.DefaultProgressListener() {

            })


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
                loginName = account,
                loginPassword = AESUtils.shaEncrypt(password)
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