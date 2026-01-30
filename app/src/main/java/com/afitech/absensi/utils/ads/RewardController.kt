package com.afitech.absensi.utils.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class RewardController(context: Context) {

    private val appContext = context.applicationContext
    private var rewardedAd: RewardedAd? = null

    fun load() {
        val request = AdRequest.Builder().build()

        RewardedAd.load(
            appContext,
            "ca-app-pub-2480965620056986/5031991203",
            request,
            object : RewardedAdLoadCallback() {

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            }
        )
    }

    fun show(activity: Activity, onReward: () -> Unit) {

        val ad = rewardedAd

        if (ad == null) {
            load() // 🔁 siapin untuk berikutnya
            onReward() // jangan blok fitur
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                load()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
                load()
                onReward()
            }
        }

        ad.show(activity) {
            onReward() // 🎁 reward dikasih hanya saat user benar2 nonton
        }
    }
}