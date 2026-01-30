package com.afitech.absensi.utils.ads


import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

object BannerController {
    fun attach(adView: AdView) {
        adView.loadAd(AdRequest.Builder().build())
    }
}