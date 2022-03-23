package com.xayah.materialyoufileexplorer.util

import androidx.appcompat.app.AppCompatActivity
import com.topjohnwu.superuser.Shell
import com.xayah.materialyoufileexplorer.ExplorerViewModel

class PathUtil {
    companion object {
        const val STORAGE_EMULATED_0 = "/storage/emulated/0"
        const val STORAGE_EMULATED_0_ANDROID = "/storage/emulated/0/Android"

        fun onBack(model: ExplorerViewModel, activity: AppCompatActivity) {
            val path = model.getPath()
            if (!Shell.rootAccess() && path == STORAGE_EMULATED_0 || path == "") {
                activity.finish()
            } else {
                model.removePath()
            }
        }
    }
}