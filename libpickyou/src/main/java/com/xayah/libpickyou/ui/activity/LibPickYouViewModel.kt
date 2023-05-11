package com.xayah.libpickyou.ui.activity

import androidx.compose.runtime.State
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

internal data class PickYouState(
    val path: List<String> = LibPickYouTokens.DefaultPathList,
    val selection: List<String> = listOf(),
    val children: DirChildrenParcelable = DirChildrenParcelable(),
    val type: PickerType = PickerType.FILE
)

internal class LibPickYouViewModel : ViewModel() {
    private val _uiState = mutableStateOf(PickYouState())
    val uiState: State<PickYouState>
        get() = _uiState
    lateinit var remoteRootService: RemoteRootService
    val isAtRoot: Boolean
        get() = uiState.value.path.size == 1
    private val pathString: String
        get() = uiState.value.path.toPath()
    val selectedItems: String
        get() = uiState.value.selection.joinToString(separator = LibPickYouTokens.SelectedItemsSeparator)
    val selectedItemsInLine: String
        get() = uiState.value.selection.joinToString(separator = LibPickYouTokens.SelectedItemsInLineSeparator)
    private var limitation: Int = LibPickYouTokens.NoLimitation

    fun setPickerType(type: PickerType) {
        _uiState.value = uiState.value.copy(type = type)
    }

    fun getLimitation(): Int {
        return limitation
    }

    fun setLimitation(number: Int) {
        limitation = number
    }

    fun enter(item: String): Boolean {
        if (item.isEmpty()) return false
        val state = uiState.value
        val path = state.path.toMutableList()
        path.add(item)
        _uiState.value = state.copy(path = path.toList())
        return true
    }

    fun exit(): Boolean {
        if (isAtRoot) return false
        val state = uiState.value
        val path = state.path.toMutableList()
        path.removeLast()
        onAccessible(path.toPath()) {
            _uiState.value = state.copy(path = path.toList())
        }
        return true
    }

    fun jumpPath(newPath: List<String>): Boolean {
        if (newPath.isEmpty()) return false
        onAccessible(newPath.toPath()) {
            val state = uiState.value
            _uiState.value = state.copy(path = newPath)
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
        return if (!name.isNullOrEmpty()) {
            "${pathString}/$name"
        } else
            pathString
    }

    fun isItemSelected(name: String): Boolean {
        return uiState.value.selection.indexOf(getPathString(name)) != -1
    }

    fun addSelection(name: String): Boolean {
        if (name.isEmpty() || isItemSelected(name)) return false
        val state = uiState.value
        val selection = state.selection.toMutableList()
        selection.add(getPathString(name))
        _uiState.value = state.copy(selection = selection.toList())
        return true
    }

    fun removeSelection(name: String): Boolean {
        val index = uiState.value.selection.indexOf(getPathString(name))
        if (index == -1) return false
        val state = uiState.value
        val selection = state.selection.toMutableList()
        selection.removeAt(index)
        _uiState.value = state.copy(selection = selection.toList())
        return true
    }

    private fun onAccessible(path: String, block: () -> Unit) {
        val defaultPath = LibPickYouTokens.DefaultPathList.toPath()
        if (path.contains(defaultPath).not()) {
            if (Shell.getShell().isRoot) block()
        } else {
            block()
        }
    }
}
