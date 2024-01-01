package com.xayah.libpickyou.ui.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class StringResourceToken {
    data class StringIdToken(@StringRes val id: Int) : StringResourceToken()
    data class StringToken(val value: String) : StringResourceToken()
    class StringArgsToken(vararg val args: StringResourceToken) : StringResourceToken()

    companion object
}

fun StringResourceToken.Companion.fromStringId(@StringRes id: Int): StringResourceToken {
    return StringResourceToken.StringIdToken(id = id)
}

fun StringResourceToken.Companion.fromString(value: String): StringResourceToken {
    return StringResourceToken.StringToken(value = value)
}

fun StringResourceToken.Companion.fromStringArgs(vararg args: StringResourceToken): StringResourceToken {
    return StringResourceToken.StringArgsToken(args = args)
}

val StringResourceToken.value: String
    @Composable
    get() = when (this) {
        is StringResourceToken.StringIdToken -> {
            stringResource(id = id)
        }

        is StringResourceToken.StringToken -> {
            value
        }

        is StringResourceToken.StringArgsToken -> {
            args.map { it.value }.joinToString(separator = "")
        }
    }
