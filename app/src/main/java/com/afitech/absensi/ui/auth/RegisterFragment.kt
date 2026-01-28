package com.afitech.absensi.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.afitech.absensi.R
import com.afitech.absensi.data.firebase.FirestoreRepository
import com.afitech.absensi.data.firebase.UserRepository
import com.afitech.absensi.data.model.UserProfile
import com.afitech.absensi.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private lateinit var binding: FragmentRegisterBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegisterBinding.bind(view)

        binding.btnRegister.setOnClickListener {
            doRegister()
        }
    }

    private fun doRegister() {
        val nama = binding.etNama.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString().trim()

        if (nama.isBlank() || email.isBlank() || pass.length < 6) {
            Toast.makeText(requireContext(), "Data belum valid", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->

                val user = result.user ?: return@addOnSuccessListener
                val uid = user.uid

                // 🔥 1. KIRIM EMAIL VERIFIKASI
                user.sendEmailVerification()

                // 🔥 2. BUAT PROFILE DI FIRESTORE
                val profile = UserProfile(
                    uid = uid,
                    nama = nama,
                    email = email,
                    createdAt = com.google.firebase.Timestamp.now() // 🔥 INI
                )

                UserRepository.createUserProfile(profile) { success ->
                    if (success) {

                        Toast.makeText(
                            requireContext(),
                            "Akun dibuat. Cek email untuk verifikasi.",
                            Toast.LENGTH_LONG
                        ).show()

                        // 🔥 3. LOGOUT SUPAYA TIDAK BISA MASUK SEBELUM VERIFIKASI
                        FirebaseAuth.getInstance().signOut()

                        // 🔥 4. BALIK KE LOGIN
                        findNavController().navigateUp()

                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Gagal simpan profil",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Email sudah dipakai",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}

