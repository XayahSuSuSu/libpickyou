package com.xayah.materialyoufileexplorer

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class MaterialYouFileExplorer {
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    fun initialize(activity: ComponentActivity, callback: (path: String) -> Unit) {
        activityResultLauncher =
            activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                callback(it.data?.getStringExtra("path") ?: "")
            }
    }

    fun initialize(fragment: Fragment, callback: (path: String) -> Unit) {
        activityResultLauncher =
            fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                callback(it.data?.getStringExtra("path") ?: "")
            }
    }

    fun toExplorer(context: Context, isFile: Boolean) {
        activityResultLauncher.launch(
            Intent(
                context,
                ExplorerActivity::class.java
            ).putExtra("isFile", isFile)
        )
    }
}