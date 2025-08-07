package com.xigong.xiaozhuan.channel.sbd

import com.aliyun.oss.ClientBuilderConfiguration
import com.aliyun.oss.ClientException
import com.aliyun.oss.OSS
import com.aliyun.oss.OSSClientBuilder
import com.aliyun.oss.OSSException
import com.aliyun.oss.common.auth.CredentialsProvider
import com.aliyun.oss.common.auth.DefaultCredentialProvider
import com.aliyun.oss.common.comm.SignVersion
import com.aliyun.oss.event.ProgressEvent
import com.aliyun.oss.event.ProgressEventType
import com.aliyun.oss.event.ProgressListener
import com.aliyun.oss.model.PutObjectRequest
import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * OSS上传监听器
 */
interface OssUploadListener {
    /**
     * 上传进度回调
     * @param progress 进度百分比，取值范围0-1
     */
    fun onProgress(progress: Float)

    /**
     * 上传完成回调
     * @param success 是否成功
     */
    fun onComplete(success: Boolean)
}


/**
 * 阿里云OSS上传工具类
 */
class OssUploadUtil private constructor(
    private val accessKeyId: String,
    private val secretAccessKey: String,
    private val bucketName: String,
    private val objectName: String,
) {
    // OSS客户端配置
    private val clientConfig: ClientBuilderConfiguration = ClientBuilderConfiguration().apply {
        signatureVersion = SignVersion.V4
    }

    /**
     * 上传文件到OSS
     * @param bucketName Bucket名称
     * @param objectName 上传到OSS的文件路径（不含Bucket名称）
     * @param localFilePath 本地文件路径
     * @param progressListener 进度监听器，可为null
     */
    fun uploadFile(
        localFilePath: String,
        progressListener: ProgressListener? = null,
        uploadListener: OssUploadListener? = null
    ): Boolean {
        var ossClient: OSS? = null
        return try {
            // 创建OSS客户端
            val credentialsProvider: CredentialsProvider =
                DefaultCredentialProvider(
                    accessKeyId,
                    secretAccessKey,
                    ""
                )
            ossClient = OSSClientBuilder.create()
                .endpoint("oss-cn-shanghai.aliyuncs.com")
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientConfig)
                .region("cn-shanghai")
                .build()

            // 创建上传请求
            val file = File(localFilePath)
            val putObjectRequest =
                PutObjectRequest(bucketName, objectName, file)

            // 设置进度监听器
            val listener = progressListener ?: DefaultProgressListener(uploadListener)
            putObjectRequest.withProgressListener<PutObjectRequest>(listener)

            // 执行上传
            ossClient.putObject(putObjectRequest)
            true
        } catch (oe: OSSException) {
            println("OSS异常: ${oe.errorMessage}")
            println("错误代码: ${oe.errorCode}")
            uploadListener?.onComplete(false)
            false
        } catch (ce: ClientException) {
            println("客户端异常: ${ce.message}")
            runBlocking {
                uploadListener?.onComplete(false)
            }
            false
        } finally {
            // 关闭客户端
            ossClient?.shutdown()
        }
    }

    /**
     * 默认的上传进度监听器
     */
    open class DefaultProgressListener(private val uploadListener: OssUploadListener? = null) :
        ProgressListener {
        private var bytesWritten: Long = 0
        private var totalBytes: Long = -1
        private var isSucceed: Boolean = false

        fun isSucceed(): Boolean = isSucceed

        override fun progressChanged(progressEvent: ProgressEvent) {
            val bytes = progressEvent.bytes
            when (progressEvent.eventType) {
                ProgressEventType.TRANSFER_STARTED_EVENT ->
                    println("开始上传......")

                ProgressEventType.REQUEST_CONTENT_LENGTH_EVENT -> {
                    totalBytes = bytes
                    println("总大小: $totalBytes 字节")
                }

                ProgressEventType.REQUEST_BYTE_TRANSFER_EVENT -> {
                    bytesWritten += bytes
                    if (totalBytes != -1L) {
                        val percent = (bytesWritten * 100.0 / totalBytes).toInt()
                        println("上传进度: $percent% ($bytesWritten/$totalBytes)")

                        // 回调进度给外部
                        val progressFloat = (bytesWritten * 1.0f / totalBytes).coerceIn(0f, 1f)
                        uploadListener?.onProgress(progressFloat)
                    } else {
                        println("已上传: $bytesWritten 字节")
                    }
                }

                ProgressEventType.TRANSFER_COMPLETED_EVENT -> {
                    isSucceed = true
                    println("上传成功，总传输: $bytesWritten 字节")
                    uploadListener?.onProgress(1.0f)
                    runBlocking {
                        uploadListener?.onComplete(true)
                    }
                }

                ProgressEventType.TRANSFER_FAILED_EVENT -> {
                    println("上传失败，已传输: $bytesWritten 字节")
                    runBlocking {
                        uploadListener?.onComplete(false)
                    }
                }

                else -> {}
            }
        }
    }

    companion object {
        /**
         * 创建工具类实例
         * @param endpoint OSS服务端点
         * @param region 区域
         * @param credentialsProvider 凭证提供者
         */
        fun create(
            accessKeyId: String,
            secretAccessKey: String,
            bucketName: String,
            objectName: String,
        ): OssUploadUtil {
            return OssUploadUtil(accessKeyId, secretAccessKey, bucketName, objectName)
        }
    }
}

// 使用示例
fun main() {
    // 配置信息
    val localFilePath = "D:\\query_results.csv"

    val accessKeyId = ""
    val secretAccessKey = ""
    val bucketName = "frontend-static"
    val objectName = "file/111.apk"

    val ossUploadUtil = OssUploadUtil.create(accessKeyId, secretAccessKey, bucketName, objectName)

    // 使用默认进度监听器上传
    val success = ossUploadUtil.uploadFile(
        localFilePath,
        OssUploadUtil.DefaultProgressListener()
    )

    if (success) {
        println("文件上传成功")
    } else {
        println("文件上传失败")
    }
}
