package com.afitech.absensi.utils.ads

import android.content.Context

object AdManager {
    lateinit var interstitial: InterstitialController
    lateinit var reward: RewardController

    fun init(context: Context) {
        interstitial = InterstitialController(context)
        reward = RewardController(context)
    }
}