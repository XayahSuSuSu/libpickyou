package com.xayah.libpickyou.ui.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource

sealed class ImageVectorToken {
    data class DrawableToken(@DrawableRes val resID: Int) : ImageVectorToken()
    data class VectorToken(val imageVector: ImageVector) : ImageVectorToken()

    companion object
}

fun ImageVectorToken.Companion.fromDrawable(@DrawableRes resID: Int): ImageVectorToken {
    return ImageVectorToken.DrawableToken(resID = resID)
}

fun ImageVectorToken.Companion.fromVector(imageVector: ImageVector): ImageVectorToken {
    return ImageVectorToken.VectorToken(imageVector = imageVector)
}

val ImageVectorToken.value: ImageVector
    @Composable
    get() = when (this) {
        is ImageVectorToken.DrawableToken -> {
            ImageVector.vectorResource(id = resID)
        }

        is ImageVectorToken.VectorToken -> {
            imageVector
        }
    }

