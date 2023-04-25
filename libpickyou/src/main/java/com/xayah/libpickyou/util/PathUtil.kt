package com.xayah.libpickyou.util

internal fun List<String>.subPath(index: Int): List<String> {
    return subList(0, index + 1)
}

internal fun List<String>.toPath(index: Int): String {
    return subPath(index).joinToString(separator = "/")
}
