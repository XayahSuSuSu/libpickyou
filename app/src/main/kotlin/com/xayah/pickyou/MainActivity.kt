package com.xayah.pickyou

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xayah.libpickyou.ui.PickYouLauncher
import com.xayah.libpickyou.ui.activity.PickerType
import com.xayah.pickyou.ui.theme.PickYouTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val launcher = PickYouLauncher()

    private fun launch(path: String, type: PickerType, number: String, title: String, pathPrefixHiddenNum: String) {
        val num = number.toIntOrNull()
        val hiddenNum = pathPrefixHiddenNum.toIntOrNull()
        if (num == null || hiddenNum == null) {
            Toast.makeText(this@MainActivity, "Please type number", Toast.LENGTH_SHORT)
                .show()
        } else {
            launcher.setDefaultPath(path)
            launcher.setType(type)
            launcher.setLimitation(num)
            launcher.setTitle(title)
            launcher.setPathPrefixHiddenNum(hiddenNum)
            launcher.launch(this@MainActivity) { path ->
                Toast.makeText(this@MainActivity, path.toString(), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private suspend fun SnackbarHostState.launch(path: String, type: PickerType, number: String, title: String, pathPrefixHiddenNum: String) {
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

                        Button(
                            onClick = {
                                launch(defaultPath, PickerType.FILE, number, title, pathPrefixHiddenNum)
                            }, content = { Text(text = "Pick file") }
                        )
                        Button(
                            onClick = {
                                launch(defaultPath, PickerType.DIRECTORY, number, title, pathPrefixHiddenNum)
                            },
                            content = { Text(text = "Pick directory") }
                        )
                        Button(
                            onClick = {
                                launch(defaultPath, PickerType.BOTH, number, title, pathPrefixHiddenNum)
                            },
                            content = { Text(text = "Pick file or directory") }
                        )
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    runCatching {
                                        snackbarHostState.launch(defaultPath, PickerType.BOTH, number, title, pathPrefixHiddenNum)
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
