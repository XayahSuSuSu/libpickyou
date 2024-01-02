package com.xayah.libpickyou

import android.app.Application

internal class PickYouApplication : Application() {
    companion object {
        lateinit var application: Application
    }

    override fun onCreate() {
        super.onCreate()
        application = this
    }
}
