package com.xigong.xiaozhuan.util

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.downloadDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Window
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JFileChooser.APPROVE_OPTION
import javax.swing.JFileChooser.DIRECTORIES_ONLY
import javax.swing.JFileChooser.FILES_ONLY
import javax.swing.filechooser.FileNameExtensionFilter

//private val fileSelector = if (isWindows()) JFileSelector else FileKitSelector
private val fileSelector = FileKitSelector

interface FileSelector {


    /**
     * 选择目录
     * @param defaultDir 默认打开的文件夹
     */
    suspend fun selectedDir(defaultDir: File? = null): File?

    /**
     * 选择文件
     * @param defaultFile 默认选中的文件夹
     * @param desc 描述
     * @param extensions 文件名扩展名,不可为空
     */
    suspend fun selectedFile(
        defaultFile: File? = null,
        desc: String?,
        extensions: List<String>
    ): File?

    companion object : FileSelector by fileSelector

}

/**
 * 使用Swing内置的JFileChooser 实现的文件选择器
 * 已知故障：Mac上会卡死，然后不能选择初始化文件
 */
private object JFileSelector : FileSelector {
    override suspend fun selectedDir(defaultDir: File?): File? {
        return JFileChooser(defaultDir).apply {
            fileSelectionMode = DIRECTORIES_ONLY
        }.awaitSelectedFile()
    }

    override suspend fun selectedFile(
        defaultFile: File?, desc: String?, extensions: List<String>
    ): File? {
        require(extensions.isNotEmpty()) { "文件扩展名不能为空" }
        return JFileChooser(defaultFile).apply {
            fileSelectionMode = FILES_ONLY
            fileFilter = FileNameExtensionFilter(desc, * extensions.toTypedArray())
        }.awaitSelectedFile()
    }

    private suspend fun JFileChooser.awaitSelectedFile(): File? = withContext(Dispatchers.IO) {
        val result = showOpenDialog(getWindow())
        selectedFile?.takeIf { result == APPROVE_OPTION }
    }

}

private fun getWindow(): Window? {
    return Window.getWindows().firstOrNull()
}


/**
 * 开源的FileKit 实现的文件选择器
 */
private object FileKitSelector : FileSelector {
    override suspend fun selectedDir(defaultDir: File?): File? {
        return FileKit.openFilePicker(
            directory = if (defaultDir == null) FileKit.downloadDir else PlatformFile(
                defaultDir.absolutePath ?: ""
            ),
            dialogSettings = FileKitDialogSettings.createDefault()
        )?.file
    }

    override suspend fun selectedFile(
        defaultFile: File?,
        desc: String?,
        extensions: List<String>
    ): File? {
        return FileKit.openFilePicker(
            mode = FileKitMode.Single,
            type = FileKitType.File(extensions),
            directory = if (defaultFile == null) FileKit.downloadDir else PlatformFile(
                defaultFile.absolutePath ?: ""
            ),
            dialogSettings = FileKitDialogSettings.createDefault()
        )?.file
    }

}