package com.xayah.materialyoufileexplorer.model

import androidx.documentfile.provider.DocumentFile

data class FileInfo(
    var name: String,
    var isDir: Boolean,
    var documentFile: DocumentFile? = null
)

