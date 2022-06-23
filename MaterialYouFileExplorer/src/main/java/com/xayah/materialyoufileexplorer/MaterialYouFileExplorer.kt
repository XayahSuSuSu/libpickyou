package com.xayah.materialyoufileexplorer

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import com.dylanc.activityresult.launcher.StartActivityLauncher
import com.xayah.materialyoufileexplorer.util.PathUtil

class MaterialYouFileExplorer {
    private lateinit var startActivityLauncher: StartActivityLauncher
    var isFile: Boolean = true
    var title: String = "default"
    var suffixFilter: ArrayList<String>? = null
    var filterWhitelist: Boolean = true
    var defPath: String = PathUtil.STORAGE_EMULATED_0

    fun initialize(activity: ComponentActivity) {
        startActivityLauncher = StartActivityLauncher(activity)
    }

    fun initialize(fragment: Fragment) {
        startActivityLauncher = StartActivityLauncher(fragment)
    }

    fun toExplorer(context: Context, callback: (path: String, isFile: Boolean) -> Unit) {
        val intent = Intent(
            context,
            ExplorerActivity::class.java
        ).putExtra("isFile", isFile).putExtra("title", title)
            .putStringArrayListExtra("suffixFilter", suffixFilter)
            .putExtra("filterWhitelist", filterWhitelist)
            .putExtra("defPath", defPath)
        startActivityLauncher.launch(intent) { resultCode, data ->
            if (resultCode == RESULT_OK) {
                callback(
                    data?.getStringExtra("path") ?: "",
                    data?.getBooleanExtra("isFile", false) ?: false
                )
            }
        }
    }
}