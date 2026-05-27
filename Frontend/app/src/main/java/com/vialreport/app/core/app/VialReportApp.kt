package com.vialreport.app.core.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration
import java.io.File

@HiltAndroidApp
class VialReportApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().apply {
            userAgentValue  = "VialReport/1.0"
            osmdroidTileCache = File(cacheDir, "osmdroid")
        }
    }
}
