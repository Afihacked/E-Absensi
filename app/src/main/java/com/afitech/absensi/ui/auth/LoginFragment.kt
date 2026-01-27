package com.afitech.absensi.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.afitech.absensi.R
import com.afitech.absensi.data.firebase.UserRepository
import com.afitech.absensi.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding

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
                    Toast.makeText(requireContext(), "Email tidak terdaftar", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun doLogin() {
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString().trim()

        if (email.isBlank() || pass.isBlank()) {
            Toast.makeText(requireContext(), "Email / Password kosong", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->

                val user = result.user!!

                if (!user.isEmailVerified) {
                    FirebaseAuth.getInstance().signOut()
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

}

