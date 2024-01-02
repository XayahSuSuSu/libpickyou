package com.xayah.libpickyou.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.topjohnwu.superuser.Shell
import com.xayah.libpickyou.ui.model.PermissionType

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
        fun checkStoragePermissions(state: MultiplePermissionsState, permissionType: PermissionType): Boolean =
            when (permissionType) {
                PermissionType.ROOT -> {
                    if (PreferencesUtil.readRequestedRoot().not()) false
                    else if (Shell.getShell().isRoot.not()) {
                        PreferencesUtil.saveRequestedRoot(false)
                        false
                    } else {
                        true
                    }
                }

                PermissionType.NORMAL -> {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                        state.allPermissionsGranted
                    } else {
                        Environment.isExternalStorageManager()
                    }
                }
            }

        @ExperimentalPermissionsApi
        fun requestStoragePermissions(context: Context, state: MultiplePermissionsState) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                state.launchMultiplePermissionRequest()
            } else {
                context.startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                })
            }
        }
    }
}
