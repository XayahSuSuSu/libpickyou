package com.xayah.libpickyou.ui.model

internal sealed class PickYouRoutes(val route: String) {
    data object Permission : PickYouRoutes(route = "permission")
    data object PickYou : PickYouRoutes(route = "pickyou")
    data object Selection : PickYouRoutes(route = "selection")
}