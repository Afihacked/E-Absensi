package com.afitech.absensi.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.afitech.absensi.R
import com.afitech.absensi.data.firebase.UserRepository
import com.afitech.absensi.databinding.FragmentProfileBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private var isEditMode = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)

        setupGoogleClient()
        setupToolbar()
        loadProfile()
        setupEditLogic()
    }

    private fun setupGoogleClient() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun setupToolbar() {
        binding.toolbarProfile.setOnMenuItemClickListener {
            if (it.itemId == R.id.menuLogout) {
                logoutUser()
                true
            } else false
        }
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        googleSignInClient.signOut()
        googleSignInClient.revokeAccess()

        Toast.makeText(requireContext(), "Berhasil logout", Toast.LENGTH_SHORT).show()

        findNavController().navigate(
            R.id.loginFragment,
            null,
            NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build()
        )
    }

    private fun loadProfile() {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        binding.tvEmail.text = user.email

        UserRepository.getUser(user.uid,
            onSuccess = { profile ->
                binding.tvName.text = profile?.nama ?: "User"
            },
            onError = {}
        )
    }

    private fun setupEditLogic() {
        binding.btnEdit.setOnClickListener {
            if (!isEditMode) {
                // ENTER EDIT MODE
                isEditMode = true
                binding.layoutEdit.visibility = View.VISIBLE
                binding.tvName.visibility = View.GONE
                binding.etNameEdit.setText(binding.tvName.text)
                binding.btnEdit.setImageResource(R.drawable.ic_check)

            } else {
                // SAVE MODE
                val newName = binding.etNameEdit.text.toString().trim()
                if (newName.isEmpty()) return@setOnClickListener

                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

                UserRepository.updateNama(uid, newName,
                    onSuccess = {
                        binding.tvName.text = newName
                        exitEditMode()
                        Toast.makeText(requireContext(),"Nama diperbarui",Toast.LENGTH_SHORT).show()
                    },
                    onError = {
                        Toast.makeText(requireContext(),"Gagal update nama",Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }

    private fun exitEditMode() {
        isEditMode = false
        binding.layoutEdit.visibility = View.GONE
        binding.tvName.visibility = View.VISIBLE
        binding.btnEdit.setImageResource(R.drawable.ic_edit)
    }
}
