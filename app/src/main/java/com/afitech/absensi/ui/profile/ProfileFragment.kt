package com.afitech.absensi.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.afitech.absensi.R
import com.afitech.absensi.data.firebase.UserRepository
import com.afitech.absensi.data.model.UserProfile
import com.afitech.absensi.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var binding: FragmentProfileBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // load profile
        UserRepository.getProfile(
            uid = uid,
            onSuccess = { profile ->
                profile?.let {
                    binding.etNama.setText(it.nama)
                }
            },
            onError = {}
        )

        binding.btnSave.setOnClickListener {
            val nama = binding.etNama.text.toString().trim()

            if (nama.isEmpty()) {
                binding.etNama.error = "Nama wajib diisi"
                return@setOnClickListener
            }

            val profile = UserProfile(
                uid = uid,
                nama = nama
            )

            UserRepository.saveOrUpdateProfile(
                profile,
                onSuccess = {
                    Toast.makeText(
                        requireContext(),
                        "Profil disimpan",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onError = {
                    Toast.makeText(
                        requireContext(),
                        "Gagal menyimpan profil",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
    }
}
