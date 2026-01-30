package com.afitech.absensi.utils.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class InterstitialController(context: Context) {

    private val appContext = context.applicationContext
    private var interstitialAd: InterstitialAd? = null

    fun load() {
        val request = AdRequest.Builder().build()

        InterstitialAd.load(
            appContext,
            "ca-app-pub-2480965620056986/3913576570", // ✅ FIXED
            request,
            object : InterstitialAdLoadCallback() {

                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    fun show(activity: Activity, onDismiss: () -> Unit) {

        val ad = interstitialAd

        if (ad == null) {
            load() // 🔁 siapin buat klik berikutnya
            onDismiss()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                load() // 🔁 preload lagi
                onDismiss()
            }

            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                interstitialAd = null
                load()
                onDismiss()
            }
        }

        ad.show(activity)
    }
}