package com.afitech.absensi.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.afitech.absensi.R
import com.afitech.absensi.databinding.FragmentSettingsBinding
import com.afitech.absensi.utils.ads.BannerController
import com.afitech.absensi.utils.ads.InterstitialController
import com.google.android.gms.ads.AdView

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var interstitial: InterstitialController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)
        val adView = view.findViewById<AdView>(R.id.adView)
        BannerController.attach(adView)

        interstitial = InterstitialController(requireContext())
        interstitial.load() // preload saat fragment buka

        binding.cardWatermark.setOnClickListener {
            interstitial.show(requireActivity()) {
                findNavController().navigate(R.id.settingsWatermarkFragment)
            }
        }
    }
}