package com.xayah.libpickyou.ui.tokens

internal object LibPickYouTokens {
    const val PARCEL_TMP_FILE_PATH = "/data/local/tmp"
    const val PARCEL_TMP_FILE_NAME = "pick_you_tmp"

    val DefaultPathList = listOf("", "storage", "emulated", "0")
    val SpecialPathAndroid = listOf("", "storage", "emulated", "0", "Android")
    val SpecialPathAndroidData = listOf("", "storage", "emulated", "0", "Android", "data")
    val SpecialPathAndroidObb = listOf("", "storage", "emulated", "0", "Android", "obb")
    const val DOCUMENT_AUTHORITY = "com.android.externalstorage.documents"
    const val PROVIDER_SHOW_ADVANCED = "android.provider.extra.SHOW_ADVANCED"
    const val CONTENT_SHOW_ADVANCED = "android.content.extra.SHOW_ADVANCED"
    const val DOCUMENT_URI_ANDROID_DATA = "primary:Android/data"
    const val DOCUMENT_URI_ANDROID_OBB = "primary:Android/obb"

    const val PATH_SEPARATOR = "/"

    const val INTENT_EXTRA_PATH = "path"

    const val STRING_PLACEHOLDER = ""
    const val PATH_PREFIX_HIDDEN_NUM = 0
}
