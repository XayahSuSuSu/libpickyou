package com.xayah.libpickyou.ui.model

internal sealed class PickYouRoutes(val route: String) {
    object Permission : PickYouRoutes(route = "permission")
    object PickYou : PickYouRoutes(route = "pickyou")
    object Selection : PickYouRoutes(route = "selection")
}