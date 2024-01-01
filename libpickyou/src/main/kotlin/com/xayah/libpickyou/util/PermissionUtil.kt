package com.xayah.libpickyou.util

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

internal class PermissionUtil {
    companion object {
        @ExperimentalPermissionsApi
        @Composable
        fun getPermissionsState(): MultiplePermissionsState {
            return rememberMultiplePermissionsState(
                listOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                )
            )
        }

        @ExperimentalPermissionsApi
        @Composable
        fun checkStoragePermissions(): Boolean {
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                getPermissionsState().allPermissionsGranted
            } else {
                Environment.isExternalStorageManager()
            }
        }

        @ExperimentalPermissionsApi
        @Composable
        fun RequestStoragePermissions() {
            if (checkStoragePermissions()) return
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                val state = getPermissionsState()
                SideEffect {
                    state.launchMultiplePermissionRequest()
                }
            } else {
                val context = LocalContext.current
                context.startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                })
            }
        }
    }
}
