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
import com.afitech.absensi.utils.ads.BannerController
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private var isEditMode = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)
        val adView = view.findViewById<AdView>(R.id.adView)
        BannerController.attach(adView)
        setupGoogleClient()
        setupToolbar()
        loadProfile()
        setupEditLogic()

        binding.imgAvatar.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser ?: return@setOnClickListener
            val isGoogleUser = user.providerData.any { it.providerId == "google.com" }

            if (isGoogleUser) {
                findNavController().navigate(R.id.avatarPreviewFragment)
            }
        }
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

        val rawUrl = user.photoUrl?.toString()
        android.util.Log.d("AVATAR_URL", "URL = $rawUrl")

        // ===== LOAD NAMA DARI FIRESTORE =====
        UserRepository.getUser(
            user.uid,
            onSuccess = { profile ->
                binding.tvName.text = profile?.nama ?: "User"
            },
            onError = {}
        )

        // ===== LOAD AVATAR =====
        loadUserAvatar(user)
    }

    private fun loadUserAvatar(user: FirebaseUser) {

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->

                val localPhotoPath = doc.getString("photoCustomLocal")
                val updatedAt = doc.getLong("avatarUpdatedAt") ?: 0L // 🔥 penentu cache

                when {
                    // ===== AVATAR CUSTOM LOKAL =====
                    !localPhotoPath.isNullOrEmpty() -> {
                        val file = java.io.File(localPhotoPath)

                        if (file.exists()) {
                            Glide.with(this)
                                .load(file)
                                .signature(com.bumptech.glide.signature.ObjectKey(updatedAt)) // 🔥 WAJIB
                                .circleCrop()
                                .placeholder(R.drawable.ic_user_avatar)
                                .error(R.drawable.ic_user_avatar)
                                .into(binding.imgAvatar)
                        } else {
                            binding.imgAvatar.setImageResource(R.drawable.ic_user_avatar)
                        }
                    }

                    // ===== AVATAR GOOGLE =====
                    user.providerData.any { it.providerId == "google.com" } && user.photoUrl != null -> {
                        val highResUrl = user.photoUrl.toString().replace("s96-c", "s400-c")

                        Glide.with(this)
                            .load(highResUrl)
                            .circleCrop()
                            .placeholder(R.drawable.ic_user_avatar)
                            .error(R.drawable.ic_user_avatar)
                            .into(binding.imgAvatar)
                    }

                    // ===== DEFAULT =====
                    else -> binding.imgAvatar.setImageResource(R.drawable.ic_user_avatar)
                }
            }
            .addOnFailureListener {
                binding.imgAvatar.setImageResource(R.drawable.ic_user_avatar)
            }
    }

    private fun setupEditLogic() {
        binding.btnEdit.setOnClickListener {
            if (!isEditMode) {
                // ENTER EDIT MODE
                isEditMode = true
                binding.layoutEdit.visibility = View.VISIBLE
                binding.tvName.visibility = View.GONE
                binding.tvSimpan.visibility = View.VISIBLE
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
        binding.tvSimpan.visibility = View.GONE
        binding.tvName.visibility = View.VISIBLE
        binding.btnEdit.setImageResource(R.drawable.ic_edit)
    }
}
