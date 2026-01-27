package com.afitech.absensi.ui.auth

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.afitech.absensi.R
import com.afitech.absensi.data.firebase.UserRepository
import com.google.firebase.auth.FirebaseAuth

class SplashFragment : Fragment(R.layout.fragment_splash) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Handler(Looper.getMainLooper()).postDelayed({

            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                // 🔥 SUDAH LOGIN
                findNavController().navigate(
                    R.id.action_splash_to_home,
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.splashFragment, true)
                        .build()
                )
            } else {
                // 🔥 BELUM LOGIN
                findNavController().navigate(
                    R.id.action_splash_to_login,
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.splashFragment, true)
                        .build()
                )
            }

        }, 800) // animasi splash
    }
}

