package com.xayah.materialyoufileexplorer

import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.xayah.materialyoufileexplorer.model.FileInfo
import com.xayah.materialyoufileexplorer.util.PathUtil

class ExplorerViewModel : ViewModel() {
    var defPath = PathUtil.STORAGE_EMULATED_0
    var pathList = MutableLiveData(getDefPath())
    var fileList: MutableList<FileInfo> = mutableListOf()
    val folders = mutableListOf<FileInfo>()
    val files = mutableListOf<FileInfo>()

    var documentFileList = mutableListOf<DocumentFile?>()

    fun getPath(): String {
        var path = pathList.value?.joinToString(separator = "/") ?: ""
        if (path == "") path = "/"
        return path
    }

    fun getPath(name: String): String {
        val newPath = pathList.value ?: getDefPath()
        return newPath.joinToString(separator = "/") + "/" + name
    }

    fun addPath(path: String) {
        val newPath = pathList.value ?: getDefPath()
        newPath.add(path)
        pathList.value = newPath
    }

    fun removePath() {
        val newPath = pathList.value ?: getDefPath()
        newPath.removeLast()
        pathList.value = newPath
    }

    fun returnPath(index: Int) {
        val newPath = pathList.value ?: getDefPath()
        for (i in 1 until newPath.size - index) {
            newPath.removeLast()
        }
        pathList.value = newPath
    }

    fun refreshPath() {
        val newPath = pathList.value ?: getDefPath()
        pathList.value = newPath
    }

    fun getDefPath(): MutableList<String> {
        return defPath.split("/").toMutableList()
    }
}