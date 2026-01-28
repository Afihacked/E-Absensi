package com.afitech.absensi.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.afitech.absensi.R
import com.afitech.absensi.data.firebase.UserRepository
import com.afitech.absensi.databinding.FragmentProfileBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var googleSignInClient: com.google.android.gms.auth.api.signin.GoogleSignInClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)
        binding.btnLogout.setOnClickListener {

            FirebaseAuth.getInstance().signOut()
            googleSignInClient.signOut()        // logout biasa
            googleSignInClient.revokeAccess()   // paksa pilih akun lagi

            Toast.makeText(requireContext(), "Berhasil logout", Toast.LENGTH_SHORT).show()

            findNavController().navigate(
                R.id.loginFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
            )
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // 🔥 LOAD NAMA DARI FIRESTORE
        UserRepository.getUser(
            uid,
            onSuccess = { profile ->
                binding.etNama.setText(profile?.nama ?: "")
            },
            onError = {}
        )

        binding.btnSave.setOnClickListener {
            val nama = binding.etNama.text.toString().trim()

            if (nama.isEmpty()) {
                binding.etNama.error = "Nama wajib diisi"
                return@setOnClickListener
            }

            // 🔥 UPDATE NAMA
            UserRepository.updateNama(
                uid,
                nama,
                onSuccess = {
                    Toast.makeText(requireContext(), "Profil disimpan", Toast.LENGTH_SHORT).show()
                },
                onError = {
                    Toast.makeText(requireContext(), "Gagal menyimpan profil", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}
