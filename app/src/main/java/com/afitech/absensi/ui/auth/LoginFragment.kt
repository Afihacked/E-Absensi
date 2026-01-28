package com.afitech.absensi.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.afitech.absensi.R
import com.afitech.absensi.data.firebase.UserRepository
import com.afitech.absensi.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        binding.btnLogin.setOnClickListener {
            doLogin()
        }

        binding.btnRegister.setOnClickListener {
            findNavController()
                .navigate(R.id.action_loginFragment_to_registerFragment)
        }
        binding.btnForgot.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()

            if (email.isBlank()) {
                Toast.makeText(requireContext(), "Masukkan email dulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance()
                .sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Link reset password dikirim ke email",
                        Toast.LENGTH_LONG
                    ).show()
                }
                .addOnFailureListener {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Email tidak terdaftar", Toast.LENGTH_SHORT).show()
                }
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        binding.btnGoogleLogin.setOnClickListener {
            showLoading(true)
            val signInIntent = googleSignInClient.signInIntent
            googleLauncher.launch(signInIntent)
        }
        binding.etPassword.doAfterTextChanged { text ->
            val pass = text.toString()
            if (pass.isEmpty()) {
                binding.etPassword.error = null
            } else if (!isPasswordValid(pass)) {
                binding.etPassword.error =
                    "Min 8 karakter, 1 huruf besar & 1 angka"
            } else {
                binding.etPassword.error = null
            }
        }
    }
    private val googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(requireContext(), "Login Google gagal", Toast.LENGTH_SHORT).show()
            }
        }
    private fun firebaseAuthWithGoogle(idToken: String) {

        val credential = GoogleAuthProvider.getCredential(idToken, null)

        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user ?: return@addOnSuccessListener
                finalizeGoogleUser(user)
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(requireContext(), "Auth Firebase gagal", Toast.LENGTH_SHORT).show()
            }
    }
    private fun finalizeGoogleUser(user: com.google.firebase.auth.FirebaseUser) {

        val db = FirebaseFirestore.getInstance()
        val uid = user.uid

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->

                if (!doc.exists()) {
                    // 🔥 USER BARU SAJA PAKAI NAMA GOOGLE
                    val profile = hashMapOf(
                        "uid" to uid,
                        "nama" to (user.displayName ?: "User"),
                        "email" to (user.email ?: ""),
                        "photoUrl" to (user.photoUrl?.toString()),
                        "provider" to "google",
                        "role" to "user",
                        "createdAt" to FieldValue.serverTimestamp()
                    )

                    db.collection("users").document(uid).set(profile)
                        .addOnSuccessListener { goHome() }

                } else {
                    // 🔥 USER LAMA → JANGAN SENTUH NAMA LAGI
                    goHome()
                }
            }
    }

    private fun goHome() {
        showLoading(false)

        findNavController().navigate(
            R.id.homeFragment,
            null,
            androidx.navigation.NavOptions.Builder()
                .setEnterAnim(android.R.anim.fade_in)
                .setExitAnim(android.R.anim.fade_out)
                .build()
        )
    }
    private fun doLogin() {
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString().trim()

        if (email.isBlank() || pass.isBlank()) {
            Toast.makeText(requireContext(), "Email / Password kosong", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isPasswordValid(pass)) {
            binding.etPassword.error =
                "Password harus min 8 karakter, 1 huruf besar & 1 angka"
            return
        }

        showLoading(true)

        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->

                val user = result.user!!

                if (!user.isEmailVerified) {
                    FirebaseAuth.getInstance().signOut()
                    showLoading(false)
                    Toast.makeText(
                        requireContext(),
                        "Verifikasi email dulu sebelum login",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }

                loadUserProfile(user.uid)
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(requireContext(), "Login gagal", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserProfile(uid: String) {
        UserRepository.getUser(
            uid,
            onSuccess = { profile ->
                if (profile != null) {
                    findNavController()
                        .navigate(R.id.action_loginFragment_to_homeFragment)
                } else {
                    Toast.makeText(requireContext(), "Profil tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            },
            onError = {
                Toast.makeText(requireContext(), "Gagal ambil profil", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            binding.loadingLayer.alpha = 0f
            binding.loadingLayer.visibility = View.VISIBLE
            binding.loadingLayer.animate().alpha(1f).setDuration(200).start()

            binding.loadingAnim.playAnimation()

            binding.btnLogin.isEnabled = false
            binding.btnGoogleLogin.isEnabled = false

        } else {
            binding.loadingAnim.cancelAnimation()

            binding.loadingLayer.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    binding.loadingLayer.visibility = View.GONE
                }
                .start()

            binding.btnLogin.isEnabled = true
            binding.btnGoogleLogin.isEnabled = true
        }
    }
    private fun isPasswordValid(password: String): Boolean {
        val passwordRegex = Regex("^(?=.*[A-Z])(?=.*\\d).{8,}$")
        return passwordRegex.matches(password)
    }
}

