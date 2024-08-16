package com.xayah.libpickyou.ui.model

enum class PermissionType {
    NORMAL,
    ROOT,
}

enum class PickerType {
    FILE,
    DIRECTORY
}

internal fun PermissionType.isRoot() = this == PermissionType.ROOT
internal fun PermissionType.isStorage() = this == PermissionType.NORMAL
