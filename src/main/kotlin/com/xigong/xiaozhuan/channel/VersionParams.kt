package com.xigong.xiaozhuan.channel

data class VersionParams(
    /**
     * 更新描述
     */
    val updateDesc: String,
    /**
     * 上线时间，为0，表示立即上线,否则保存的是上线时间毫秒值
     */
    val onlineTime: Long,

    /**
     * 是否强制更新
     */
    val forceUpdate: Boolean = true,

    /**
     * 更新标题
     */
    val updateTitle: String = "",
)