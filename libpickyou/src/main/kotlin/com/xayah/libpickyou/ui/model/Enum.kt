package com.xayah.libpickyou.ui.model

enum class PermissionType {
    NORMAL,
    ROOT,
}

enum class PickerType {
    FILE,
    DIRECTORY,
    BOTH
}

fun PermissionType.isRoot() = this == PermissionType.ROOT
fun PermissionType.isStorage() = this == PermissionType.NORMAL
