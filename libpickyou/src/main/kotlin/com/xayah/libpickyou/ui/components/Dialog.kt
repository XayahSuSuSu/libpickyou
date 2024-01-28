package com.xayah.libpickyou.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.xayah.libpickyou.R
import com.xayah.libpickyou.ui.model.ImageVectorToken
import com.xayah.libpickyou.ui.model.StringResourceToken
import com.xayah.libpickyou.ui.model.fromDrawable
import com.xayah.libpickyou.ui.model.fromStringId
import com.xayah.libpickyou.ui.model.value
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Creates a [DialogState] and acts as a slot with [DialogState.Insert].
 */
@Composable
internal fun rememberDialogState(): DialogState {
    val state = remember { DialogState() }
    state.Insert()
    return state
}

internal class DialogState {
    private var content: (@Composable () -> Unit)? by mutableStateOf(null)

    @Composable
    internal fun Insert() = content?.invoke()

    private fun dismiss() {
        content = null
    }

    /**
     * Return **Pair<Boolean, T>**.
     *
     * If user clicks **confirmButton**, then return **Pair(true, T)**,
     * otherwise return **Pair(false, T)**.
     */
    suspend fun <T> open(
        initialState: T,
        title: StringResourceToken,
        icon: ImageVectorToken? = null,
        confirmText: StringResourceToken? = null,
        dismissText: StringResourceToken? = null,
        block: @Composable (MutableState<T>) -> Unit,
    ): Pair<Boolean, T> {
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { dismiss() }
            content = {
                val uiState = remember { mutableStateOf(initialState) }
                AlertDialog(
                    onDismissRequest = {
                        dismiss()
                        continuation.resume(Pair(false, uiState.value))
                    },
                    confirmButton = {
                        Button(text = confirmText ?: StringResourceToken.fromStringId(R.string.confirm), onClick = {
                            dismiss()
                            continuation.resume(Pair(true, uiState.value))
                        })
                    },
                    dismissButton = {
                        TextButton(text = dismissText ?: StringResourceToken.fromStringId(R.string.cancel), onClick = {
                            dismiss()
                            continuation.resume(Pair(false, uiState.value))
                        })
                    },
                    title = { Text(text = title.value) },
                    icon = icon?.let { { Icon(imageVector = icon.value, contentDescription = null) } },
                    text = {
                        block(uiState)
                    },
                )
            }
        }
    }

    suspend fun openEdit() = open(
        initialState = "",
        title = StringResourceToken.fromStringId(R.string.create_folder),
        icon = ImageVectorToken.fromDrawable(R.drawable.ic_rounded_folder),
        block = { state ->
            TextField(
                value = state.value,
                onValueChange = { state.value = it },
                label = { Text(StringResourceToken.fromStringId(R.string.name).value) },
                singleLine = true
            )
        }
    )
}
