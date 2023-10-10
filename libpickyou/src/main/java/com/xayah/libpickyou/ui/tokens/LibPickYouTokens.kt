package com.xayah.libpickyou.ui.tokens

internal object LibPickYouTokens {
    const val ServiceDisconnectedToast = "Service disconnected."
    const val ServiceBindingDiedToast = "Binding died."
    const val ServiceNullBindingToast = "Null binding."
    const val ServiceNullToast = "Service is null."
    const val ServiceDeadToast = "Service is dead."

    const val ParcelTmpFilePath = "/data/local/tmp"
    const val ParcelTmpFileName = "pick_you_tmp"

    val DefaultPathList = listOf("", "storage", "emulated", "0")

    const val SelectedItemsSeparator = ", "
    const val SelectedItemsInLineSeparator = "\n"
    const val PathSeparator = "/"

    const val IntentExtraPath = "path"
    const val IntentExtraType = "type"
    const val IntentExtraTitle = "title"
    const val IntentExtraLimitation = "limitation"
    const val IntentPathPrefixHiddenNum = "pathPrefixHiddenNum"

    const val EnumPickerTypeFile = "type_file"
    const val EnumPickerTypeDirectory = "type_directory"
    const val EnumPickerTypeBoth = "type_both"
    const val EnumPickerTypePrefix = "type_"

    const val NoLimitation = 0
    const val StringPlaceHolder = ""
    const val PathPrefixHiddenNum = 0
}
