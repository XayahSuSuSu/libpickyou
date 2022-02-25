package com.xayah.materialyoufileexplorer

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

class MaterialYouFileExplorer {
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    fun initialize(activity: ComponentActivity, callback: (path: String) -> Unit) {
        activityResultLauncher =
            activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                callback(it.data?.getStringExtra("path") ?: "")
            }
    }

    fun toExplorer(activity: ComponentActivity, isFile: Boolean) {
        activityResultLauncher.launch(
            Intent(
                activity,
                ExplorerActivity::class.java
            ).putExtra("isFile", isFile)
        )
    }
}