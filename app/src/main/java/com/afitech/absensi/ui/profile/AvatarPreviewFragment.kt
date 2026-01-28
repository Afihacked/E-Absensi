package com.afitech.absensi.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.afitech.absensi.R
import com.afitech.absensi.databinding.FragmentAvatarPreviewBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class AvatarPreviewFragment : Fragment(R.layout.fragment_avatar_preview) {

    private lateinit var binding: FragmentAvatarPreviewBinding
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentAvatarPreviewBinding.bind(view)

        val user = FirebaseAuth.getInstance().currentUser ?: return
        loadPreviewAvatar(user)

        // PICK FOTO BARU
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let { saveLocalAvatar(it) }
            }

        binding.btnEditAvatar.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // 🔥 HAPUS AVATAR CUSTOM
        binding.btnDeleteAvatar.setOnClickListener {
            removeCustomAvatar(user.uid)
        }
    }

    // ================= LOAD PREVIEW =================
    private fun loadPreviewAvatar(user: FirebaseUser) {

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->

                val localPhoto = doc.getString("photoCustomLocal")
                val updatedAt = doc.getLong("avatarUpdatedAt") ?: 0L

                when {
                    !localPhoto.isNullOrEmpty() && java.io.File(localPhoto).exists() -> {
                        Glide.with(this)
                            .load(localPhoto)
                            .signature(com.bumptech.glide.signature.ObjectKey(updatedAt)) // 🔥 ANTI CACHE
                            .into(binding.imgPreview)
                    }

                    user.photoUrl != null -> {
                        val highRes = user.photoUrl.toString().replace("s96-c", "s400-c")
                        Glide.with(this).load(highRes).into(binding.imgPreview)
                    }

                    else -> binding.imgPreview.setImageResource(R.drawable.ic_user_avatar)
                }
            }
    }

    // ================= SIMPAN AVATAR LOKAL =================
    private fun saveLocalAvatar(uri: Uri) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val file = java.io.File(requireContext().filesDir, "avatar_$uid.jpg")

        requireContext().contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .update(
                mapOf(
                    "photoCustomLocal" to file.absolutePath,
                    "avatarUpdatedAt" to System.currentTimeMillis() // anti cache
                )
            )
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Avatar diperbarui", Toast.LENGTH_SHORT).show()

                // ✅ SAMAKAN DENGAN HAPUS → LANGSUNG TUTUP PREVIEW
                findNavController().popBackStack()
            }
    }

    // ================= HAPUS AVATAR CUSTOM =================
    private fun removeCustomAvatar(uid: String) {

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .update("photoCustomLocal", null)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Avatar dikembalikan ke Google", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
    }
}