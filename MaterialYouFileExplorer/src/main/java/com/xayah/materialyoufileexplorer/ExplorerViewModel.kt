package com.xayah.materialyoufileexplorer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.xayah.materialyoufileexplorer.model.FileInfo

class ExplorerViewModel : ViewModel() {
    var pathList =
        MutableLiveData(mutableListOf("", "storage", "emulated", "0"))
    var fileList: MutableList<FileInfo> = mutableListOf()
    val folders = mutableListOf<FileInfo>()
    val files = mutableListOf<FileInfo>()

    fun getPath(): String {
        return pathList.value?.joinToString(separator = "/") ?: ""
    }

    fun addPath(path: String) {
        val newPath = pathList.value ?: mutableListOf("", "storage", "emulated", "0")
        newPath.add(path)
        pathList.value = newPath
    }

    fun removePath() {
        val newPath = pathList.value ?: mutableListOf("", "storage", "emulated", "0")
        newPath.removeLast()
        pathList.value = newPath
    }

    fun returnPath(index: Int) {
        val newPath = pathList.value ?: mutableListOf("", "storage", "emulated", "0")
        for (i in 1 until newPath.size - index) {
            newPath.removeLast()
        }
        pathList.value = newPath
    }
}