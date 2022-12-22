package com.xayah.materialyoufileexplorer.util

import android.content.Context
import android.content.Intent
import com.topjohnwu.superuser.ipc.RootService
import com.topjohnwu.superuser.nio.ExtendedFile
import com.topjohnwu.superuser.nio.FileSystemManager
import com.xayah.materialyoufileexplorer.service.RemoteFileSystemConnection
import com.xayah.materialyoufileexplorer.service.RemoteFileSystemService


class FileSystemManager() {
    lateinit var fileSystemManager: FileSystemManager

    constructor(context: Context) : this() {
        initialize(context)
    }

    /**
     * Before it, you should {@link #initialize()} first
     */
    fun ExtendedFile(path: String): ExtendedFile {
        return fileSystemManager.getFile(path)
    }

    val isInitialized
        get() = this::fileSystemManager.isInitialized

    /**
     *
     */
    private fun initialize(context: Context) {
        val intent = Intent(context, RemoteFileSystemService::class.java)
        val remoteFileSystemConnection = RemoteFileSystemConnection().apply {
            setOnServiceConnected {
                fileSystemManager = it
            }
        }
        RootService.bind(intent, remoteFileSystemConnection)
    }
}