package com.xayah.libpickyou.ui.components

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

fun NavHostController.navigateAndPopBackStack(route: String) = navigate(route) { popBackStack() }

@Composable
fun NavHostController.currentRoute() = currentBackStackEntryAsState().value?.destination?.route
