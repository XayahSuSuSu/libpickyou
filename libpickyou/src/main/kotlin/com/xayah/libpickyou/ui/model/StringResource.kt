package com.xayah.libpickyou.ui.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

internal sealed class StringResourceToken {
    data class StringIdToken(@StringRes val id: Int) : StringResourceToken()
    data class StringToken(val value: String) : StringResourceToken()
    class StringArgsToken(vararg val args: StringResourceToken) : StringResourceToken()

    companion object
}

internal fun StringResourceToken.Companion.fromStringId(@StringRes id: Int): StringResourceToken {
    return StringResourceToken.StringIdToken(id = id)
}

internal fun StringResourceToken.Companion.fromString(value: String): StringResourceToken {
    return StringResourceToken.StringToken(value = value)
}

internal fun StringResourceToken.Companion.fromStringArgs(vararg args: StringResourceToken): StringResourceToken {
    return StringResourceToken.StringArgsToken(args = args)
}

internal val StringResourceToken.value: String
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
