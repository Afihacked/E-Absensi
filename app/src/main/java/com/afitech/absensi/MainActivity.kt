package com.afitech.absensi

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.afitech.absensi.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Status bar & window inset (punyamu, TIDAK DIUBAH)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAnonymousAuth()
        setupNavigation()
    }

    // ===============================
    // 🔐 ANONYMOUS AUTH (AUTO)
    // ===============================
    private fun setupAnonymousAuth() {
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnSuccessListener {
                    // ✅ Anonymous user siap dipakai
                }
                .addOnFailureListener {
                    // ❌ Kalau gagal → Anonymous belum diaktifkan di Firebase Console
                }
        }
    }

    // ===============================
    // 🧭 NAVIGATION (PUNYAMU)
    // ===============================
    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splashFragment,
                R.id.loginFragment,
                R.id.registerFragment -> {
                    binding.bottomNav.visibility = View.GONE
                }
                else -> {
                    binding.bottomNav.visibility = View.VISIBLE
                }
            }
        }
    }
}
