package com.xayah.libpickyou.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.topjohnwu.superuser.Shell
import com.xayah.libpickyou.parcelables.DirChildrenParcelable
import com.xayah.libpickyou.ui.PickYouLauncher
import com.xayah.libpickyou.ui.model.PickerType
import com.xayah.libpickyou.ui.model.isRoot
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens
import com.xayah.libpickyou.util.PreferencesUtil
import com.xayah.libpickyou.util.RemoteRootService
import com.xayah.libpickyou.util.toPath

internal data class IndexUiState(
    val path: List<String>,
    val selection: List<String> = listOf(),
    val children: DirChildrenParcelable = DirChildrenParcelable(),
    val type: PickerType,
    val limitation: Int,
    val title: String,
    val pathPrefixHiddenNum: Int,
) : UiState {
    val canUp: Boolean
        get() = isAccessible(path.toMutableList().apply { removeLast() })

    val pathString: String
        get() = run {
            val newPath = path.toMutableList()
            repeat(pathPrefixHiddenNum) {
                newPath.removeFirstOrNull()
            }
            newPath.toPath()
        }

    val selectedItems: String
        get() = selection.joinToString(separator = LibPickYouTokens.SelectedItemsSeparator)
}

internal sealed class IndexUiIntent : UiIntent {
    data class Enter(val item: String) : IndexUiIntent()
    object Exit : IndexUiIntent()
    data class JumpToList(val target: List<String>) : IndexUiIntent()
    data class JumpTo(val target: String) : IndexUiIntent()
    data class UpdateChildren(val children: DirChildrenParcelable) : IndexUiIntent()
    data class JoinSelection(val name: String) : IndexUiIntent()
    data class RemoveSelection(val name: String) : IndexUiIntent()

}

internal class LibPickYouViewModel(
    path: List<String>,
    type: PickerType,
    limitation: Int,
    title: String,
    pathPrefixHiddenNum: Int,
) : BaseViewModel<IndexUiState, IndexUiIntent, IndexUiEffect>(
    IndexUiState(
        path = path,
        type = type,
        limitation = limitation,
        title = title,
        pathPrefixHiddenNum = pathPrefixHiddenNum
    )
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        class Factory(
            private val path: List<String>,
            private val type: PickerType,
            private val limitation: Int,
            private val title: String,
            private val pathPrefixHiddenNum: Int,
        ) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return LibPickYouViewModel(
                    path = path,
                    type = type,
                    limitation = limitation,
                    title = title,
                    pathPrefixHiddenNum = pathPrefixHiddenNum
                ) as T
            }
        }
    }

    lateinit var remoteRootService: RemoteRootService

    override suspend fun onEvent(state: IndexUiState, intent: IndexUiIntent) {
        when (intent) {
            is IndexUiIntent.Enter -> {
                if (intent.item.isEmpty()) return
                val path = state.path.toMutableList()
                path.add(intent.item)
                emitState(state.copy(path = path.toList()))
            }

            is IndexUiIntent.Exit -> {
                if (!state.canUp) return
                val path = state.path.toMutableList()
                path.removeLast()
                onAccessible(path) {
                    emitState(state.copy(path = path.toList()))
                }
            }

            is IndexUiIntent.JumpToList -> {
                if (intent.target.isEmpty()) return
                onAccessible(intent.target) {
                    emitState(state.copy(path = intent.target))
                }
            }

            is IndexUiIntent.JumpTo -> {
                emitIntent(IndexUiIntent.JumpToList(intent.target.split(LibPickYouTokens.PathSeparator)))
            }

            is IndexUiIntent.UpdateChildren -> {
                emitState(state.copy(children = intent.children))
            }

            is IndexUiIntent.JoinSelection -> {
                if (intent.name.isEmpty() || isItemSelected(intent.name)) return
                val selection = state.selection.toMutableList()
                selection.add(getPathString(intent.name))
                emitState(state.copy(selection = selection.toList()))
            }

            is IndexUiIntent.RemoveSelection -> {
                val index = state.selection.indexOf(getPathString(intent.name))
                if (index == -1) return
                val selection = state.selection.toMutableList()
                selection.removeAt(index)
                emitState(state.copy(selection = selection.toList()))
            }
        }
    }

    private fun getPathString(name: String? = null): String {
        val uiState = uiState.value

        return if (!name.isNullOrEmpty()) {
            "${uiState.pathString}/$name"
        } else
            uiState.pathString
    }

    fun isItemSelected(name: String): Boolean {
        return uiState.value.selection.indexOf(getPathString(name)) != -1
    }

    private fun onAccessible(path: List<String>, block: () -> Unit) {
        if (isAccessible(path)) block()
    }
}

private fun isAccessible(path: List<String>): Boolean {
    if (path.isEmpty()) return false
    val rootPath = PickYouLauncher.rootPathList.toPath()
    if (rootPath in path.toPath()) return true
    return if (PickYouLauncher.permissionType.isRoot() && PreferencesUtil.readRequestedRoot()) Shell.getShell().isRoot else false
}
