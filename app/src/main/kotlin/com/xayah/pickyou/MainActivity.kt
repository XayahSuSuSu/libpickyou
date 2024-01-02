package com.xayah.pickyou

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xayah.libpickyou.ui.PickYouLauncher
import com.xayah.libpickyou.ui.model.PermissionType
import com.xayah.libpickyou.ui.model.PickerType
import com.xayah.pickyou.ui.theme.PickYouTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val launcher = PickYouLauncher()

    private fun SnackbarHostState.launch(
        path: String,
        type: PickerType,
        number: String,
        title: String,
        pathPrefixHiddenNum: String,
        permissionType: PermissionType,
        scope: CoroutineScope,
    ) {
        val num = number.toIntOrNull()
        val hiddenNum = pathPrefixHiddenNum.toIntOrNull()
        if (num == null || hiddenNum == null) {
            scope.launch {
                showSnackbar("Please type number")
            }
        } else {
            launcher.setDefaultPath(path)
            launcher.setType(type)
            launcher.setLimitation(num)
            launcher.setTitle(title)
            launcher.setPathPrefixHiddenNum(hiddenNum)
            launcher.setPermissionType(permissionType)
            launcher.launch(this@MainActivity) { path ->
                scope.launch {
                    showSnackbar(path.toString())
                }
            }
        }
    }

    private suspend fun SnackbarHostState.launchSuspended(
        path: String,
        type: PickerType,
        number: String,
        title: String,
        pathPrefixHiddenNum: String,
        permissionType: PermissionType,
    ) {
        val num = number.toIntOrNull()
        val hiddenNum = pathPrefixHiddenNum.toIntOrNull()
        if (num == null || hiddenNum == null) {
            showSnackbar("Please type number")
        } else {
            launcher.setDefaultPath(path)
            launcher.setType(type)
            launcher.setLimitation(num)
            launcher.setTitle(title)
            launcher.setPathPrefixHiddenNum(hiddenNum)
            launcher.setPermissionType(permissionType)
            val path = launcher.awaitPickerOnce(this@MainActivity)
            showSnackbar(path.toString())
        }
    }

    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PickYouTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    "PickYou sample",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    },
                    snackbarHost = {
                        SnackbarHost(snackbarHostState)
                    },
                ) { innerPadding ->
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(innerPadding.calculateTopPadding())
                        )

                        var title by remember { mutableStateOf("Let's pick it up!") }
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title") }
                        )
                        var defaultPath by remember { mutableStateOf("/storage/emulated/0") }
                        OutlinedTextField(
                            value = defaultPath,
                            onValueChange = { defaultPath = it },
                            label = { Text("Default path") },
                        )

                        var number by remember { mutableStateOf("0") }
                        OutlinedTextField(
                            value = number,
                            onValueChange = { number = it },
                            label = { Text("Number(0: No limitation)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        var pathPrefixHiddenNum by remember { mutableStateOf("0") }
                        OutlinedTextField(
                            value = pathPrefixHiddenNum,
                            onValueChange = { pathPrefixHiddenNum = it },
                            label = { Text("Path prefix hidden num(0: Default)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        var permissionType by remember { mutableStateOf(PermissionType.NORMAL) }
                        val options = remember {
                            listOf("Normal mode" to PermissionType.NORMAL, "Root mode" to PermissionType.ROOT)
                        }
                        Column(modifier = Modifier.selectableGroup()) {
                            options.forEach { item ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .selectable(
                                            selected = (item.second == permissionType),
                                            onClick = {
                                                permissionType = item.second
                                            },
                                            role = Role.RadioButton
                                        )
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (item.second == permissionType),
                                        onClick = null
                                    )
                                    Text(
                                        text = item.first,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(start = 16.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                snackbarHostState.launch(defaultPath, PickerType.FILE, number, title, pathPrefixHiddenNum, permissionType, coroutineScope)
                            }, content = { Text(text = "Pick file") }
                        )
                        Button(
                            onClick = {
                                snackbarHostState.launch(defaultPath, PickerType.DIRECTORY, number, title, pathPrefixHiddenNum, permissionType, coroutineScope)
                            },
                            content = { Text(text = "Pick directory") }
                        )
                        Button(
                            onClick = {
                                snackbarHostState.launch(defaultPath, PickerType.BOTH, number, title, pathPrefixHiddenNum, permissionType, coroutineScope)
                            },
                            content = { Text(text = "Pick file or directory") }
                        )
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    runCatching {
                                        snackbarHostState.launchSuspended(defaultPath, PickerType.BOTH, number, title, pathPrefixHiddenNum, permissionType)
                                    }.onFailure {
                                        snackbarHostState.showSnackbar(it.message.orEmpty())
                                    }
                                }
                            }, content = { Text(text = "Pick file suspendly") }
                        )

                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(innerPadding.calculateBottomPadding())
                        )
                    }
                }
            }
        }
    }
}
