package com.xayah.materialyoufileexplorer.util

import android.net.Uri
import android.provider.DocumentsContract

class UriUtil {
    companion object {
        private val TREE_URI_PRIMARY_ANDROID: Uri = DocumentsContract.buildTreeDocumentUri(
            "com.android.externalstorage.documents", "primary"
        )

        private val TREE_URI_PRIMARY_ANDROID_DATA: Uri = DocumentsContract.buildTreeDocumentUri(
            "com.android.externalstorage.documents", "primary:Android/data"
        )

        private val TREE_URI_PRIMARY_ANDROID_OBB: Uri = DocumentsContract.buildTreeDocumentUri(
            "com.android.externalstorage.documents", "primary:Android/obb"
        )

        val DOCUMENT_URI_ANDROID_DATA: Uri = DocumentsContract.buildDocumentUriUsingTree(
            TREE_URI_PRIMARY_ANDROID, "primary:Android/data"
        )

        val DOCUMENT_URI_ANDROID_OBB: Uri = DocumentsContract.buildDocumentUriUsingTree(
            TREE_URI_PRIMARY_ANDROID, "primary:Android/obb"
        )

        val DOCUMENT_URI_ANDROID_DATA_ACCESS: Uri = DocumentsContract.buildDocumentUriUsingTree(
            TREE_URI_PRIMARY_ANDROID_DATA, "primary:Android/data"
        )

        val DOCUMENT_URI_ANDROID_OBB_ACCESS: Uri = DocumentsContract.buildDocumentUriUsingTree(
            TREE_URI_PRIMARY_ANDROID_OBB, "primary:Android/obb"
        )
    }
}