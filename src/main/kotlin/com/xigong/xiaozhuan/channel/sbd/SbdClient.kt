package com.xigong.xiaozhuan.channel.sbd

import com.xigong.xiaozhuan.BuildConfig
import com.xigong.xiaozhuan.channel.VersionParams
import com.xigong.xiaozhuan.log.AppLogger
import com.xigong.xiaozhuan.log.action
import com.xigong.xiaozhuan.util.ApkInfo
import com.xigong.xiaozhuan.util.ProgressChange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

        val bucketName = if (BuildConfig.debug) "hautoosstest" else "frontend-static"
        val objectName = if (BuildConfig.debug) {
            "file/pac/shoubaodan_new.apk"
        } else {
            "shoubaodan3.0/app/img/home/shoubaodan_new.apk"
        }
        val url = if (BuildConfig.debug) {
            "https://hautoosstest.oss-cn-shanghai.aliyuncs.com"
        } else {
            "https://frontend-static.shoubaodan.com"
        } + "/$objectName"

        val ossUploadUtil =
            OssUploadUtil.create(accessKeyId, secretAccessKey, bucketName, objectName)
        ossUploadUtil.uploadFile(
            localFilePath = file.absolutePath,
            uploadListener = object : OssUploadListener {
                override suspend fun onProgress(progress: Float) {
                    // 确保进度回调在主线程执行
                    withContext(Dispatchers.Main) {
                        progressChange.invoke(progress)
                    }
                }

                override suspend fun onComplete(success: Boolean) {
                    if (success) {
                        AppLogger.info(LOG_TAG, "上传成功: $url")
                        saveVersion(account, pwd, apkInfo, versionParams, url)
                    }
                }
            }
        )


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
        versionParams: VersionParams,
        url: String
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
                "url" to url,
                "forceUpdate" to if (versionParams.forceUpdate) 1 else 0,
                "appType" to "android",
                "versionType" to "android",
                "newVersion" to 1,
            )
            AppLogger.info(LOG_TAG, "新增版本信息: $version")
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