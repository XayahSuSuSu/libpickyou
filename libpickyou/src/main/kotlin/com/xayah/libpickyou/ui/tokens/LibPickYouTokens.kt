package com.xayah.libpickyou.ui.tokens

internal object LibPickYouTokens {
    const val ParcelTmpFilePath = "/data/local/tmp"
    const val ParcelTmpFileName = "pick_you_tmp"

    val DefaultPathList = listOf("", "storage", "emulated", "0")
    val SpecialPathAndroid = listOf("", "storage", "emulated", "0", "Android")
    val SpecialPathAndroidData = listOf("", "storage", "emulated", "0", "Android", "data")
    val SpecialPathAndroidObb = listOf("", "storage", "emulated", "0", "Android", "obb")

    const val SelectedItemsSeparator = ", "
    const val PathSeparator = "/"

    const val IntentExtraPath = "path"

    const val NoLimitation = 0
    const val StringPlaceHolder = ""
    const val PathPrefixHiddenNum = 0
}
