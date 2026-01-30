package com.afitech.absensi

import android.app.Application
import com.afitech.absensi.utils.ads.AdManager

class Afitech : Application() {
    override fun onCreate() {
        super.onCreate()
        AdManager.init(this)
    }
}