package com.xigong.xiaozhuan.channel.sbd

import com.xigong.xiaozhuan.channel.ChannelTask
import com.xigong.xiaozhuan.channel.MarketInfo
import com.xigong.xiaozhuan.channel.ReviewState
import com.xigong.xiaozhuan.channel.VersionParams
import com.xigong.xiaozhuan.log.AppLogger
import com.xigong.xiaozhuan.util.ApkInfo
import java.io.File
import kotlin.math.roundToInt

class SbdChannelTask : ChannelTask() {


    override val channelName: String = "平台"

    override val fileNameIdentify: String = "sbd"

    override val paramDefine: List<Param> =
        listOf(CLIENT_ID, CLIENT_SECRET, ACCESS_KEY_ID, SECRET_ACCESS_KEY)

    private val connectClient = SbdClient()

    private var clientId = ""

    private var clientSecret = ""
    private var accessKeyId = ""
    private var secretAccessKey = ""

    override fun init(params: Map<Param, String?>) {
        clientId = params[CLIENT_ID] ?: ""
        clientSecret = params[CLIENT_SECRET] ?: ""
        accessKeyId = params[ACCESS_KEY_ID] ?: ""
        secretAccessKey = params[SECRET_ACCESS_KEY] ?: ""
    }

    override suspend fun performUpload(
        file: File,
        apkInfo: ApkInfo,
        versionParams: VersionParams,
        progress: (Int) -> Unit
    ) {
        connectClient.uploadApk(
            file,
            apkInfo,
            clientId,
            clientSecret,
            accessKeyId,
            secretAccessKey,
            versionParams
        ) {
            progress((it * 100).roundToInt())
        }
    }

    override suspend fun getMarketState(applicationId: String): MarketInfo {
        val appInfo = connectClient.getAppInfo()
        AppLogger.info(channelName, "应用市场状态:${appInfo}")
        return MarketInfo(
            reviewState = ReviewState.Online,
            lastVersion = MarketInfo.Version(appInfo?.versionCode?.toLong(), appInfo?.versionName)
        )
    }


    companion object {
        private val CLIENT_ID = Param("account", desc = "账号(手机号)")
        private val CLIENT_SECRET = Param("pwd", desc = "密码")
        private val ACCESS_KEY_ID = Param("accessKeyId", desc = "阿里云oss accessKeyId")
        private val SECRET_ACCESS_KEY = Param("secretAccessKey", desc = "阿里云oss secretAccessKey")
    }

}