package com.xayah.libpickyou.ui.activity

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.topjohnwu.superuser.Shell
import com.xayah.libpickyou.parcelables.DirChildrenParcelable
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens
import com.xayah.libpickyou.util.RemoteRootService
import com.xayah.libpickyou.util.toPath

enum class PickerType(val type: String) {
    FILE(LibPickYouTokens.EnumPickerTypeFile),
    DIRECTORY(LibPickYouTokens.EnumPickerTypeDirectory),
    BOTH(LibPickYouTokens.EnumPickerTypeBoth);

    companion object {
        fun of(name: String?): PickerType {
            return try {
                PickerType.valueOf(name!!.replace(LibPickYouTokens.EnumPickerTypePrefix, "").uppercase())
            } catch (e: Exception) {
                e.printStackTrace()
                FILE
            }
        }
    }
}

internal data class PickYouUiState(
    val path: List<String> = LibPickYouTokens.DefaultPathList,
    val selection: List<String> = listOf(),
    val children: DirChildrenParcelable = DirChildrenParcelable(),
    val type: PickerType = PickerType.FILE,

    val limitation: Int = LibPickYouTokens.NoLimitation,
    val title: String = LibPickYouTokens.StringPlaceHolder,
    val pathPrefixHiddenNum: Int = LibPickYouTokens.PathPrefixHiddenNum,
) {
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
    val selectedItemsInLine: String
        get() = selection.joinToString(separator = LibPickYouTokens.SelectedItemsInLineSeparator)
}

internal class LibPickYouViewModel : ViewModel() {
    private val _uiState = mutableStateOf(PickYouUiState())
    val uiState: State<PickYouUiState>
        get() = _uiState
    lateinit var remoteRootService: RemoteRootService

    fun setDefaultPathList(path: List<String>) {
        _uiState.value = uiState.value.copy(path = path)
    }

    fun setPickerType(type: PickerType) {
        _uiState.value = uiState.value.copy(type = type)
    }

    fun setLimitation(number: Int) {
        _uiState.value = uiState.value.copy(limitation = number)
    }

    fun setTitle(title: String) {
        _uiState.value = uiState.value.copy(title = title)
    }

    fun setPathPrefixHiddenNum(number: Int) {
        _uiState.value = uiState.value.copy(pathPrefixHiddenNum = number)
    }

    fun enter(item: String): Boolean {
        val uiState by uiState

        if (item.isEmpty()) return false
        val path = uiState.path.toMutableList()
        path.add(item)
        _uiState.value = uiState.copy(path = path.toList())
        return true
    }

    fun exit(): Boolean {
        val uiState by uiState
        if (!uiState.canUp) return false
        val path = uiState.path.toMutableList()
        path.removeLast()
        onAccessible(path) {
            _uiState.value = uiState.copy(path = path.toList())
        }
        return true
    }

    fun jumpPath(newPath: List<String>): Boolean {
        val uiState by uiState

        if (newPath.isEmpty()) return false
        onAccessible(newPath) {
            _uiState.value = uiState.copy(path = newPath)
        }
        return true
    }

    fun jumpPath(newPath: String): Boolean {
        return jumpPath(newPath.split(LibPickYouTokens.PathSeparator))
    }

    fun updateChildren(children: DirChildrenParcelable) {
        _uiState.value = uiState.value.copy(children = children)
    }

    private fun getPathString(name: String? = null): String {
        val uiState by uiState

        return if (!name.isNullOrEmpty()) {
            "${uiState.pathString}/$name"
        } else
            uiState.pathString
    }

    fun isItemSelected(name: String): Boolean {
        return uiState.value.selection.indexOf(getPathString(name)) != -1
    }

    fun addSelection(name: String): Boolean {
        val uiState by uiState

        if (name.isEmpty() || isItemSelected(name)) return false
        val selection = uiState.selection.toMutableList()
        selection.add(getPathString(name))
        _uiState.value = uiState.copy(selection = selection.toList())
        return true
    }

    fun removeSelection(name: String): Boolean {
        val uiState by uiState

        val index = uiState.selection.indexOf(getPathString(name))
        if (index == -1) return false
        val selection = uiState.selection.toMutableList()
        selection.removeAt(index)
        _uiState.value = uiState.copy(selection = selection.toList())
        return true
    }

    private fun onAccessible(path: List<String>, block: () -> Unit) {
        if (isAccessible(path)) block()
    }
}

private fun isAccessible(path: List<String>): Boolean {
    if (path.isEmpty()) return false
    val defaultPath = LibPickYouTokens.DefaultPathList.toPath()
    if (defaultPath in path.toPath()) return true
    if (Shell.getShell().isRoot) return true
    return false
}
