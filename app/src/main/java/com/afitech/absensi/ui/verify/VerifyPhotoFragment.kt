package com.afitech.absensi.ui.verify

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.afitech.absensi.R
import com.afitech.absensi.data.firebase.FirestoreRepository
import com.afitech.absensi.data.model.Absensi
import com.afitech.absensi.databinding.FragmentVerifyPhotoBinding
import java.text.SimpleDateFormat
import java.util.*

class VerifyPhotoFragment : Fragment(R.layout.fragment_verify_photo) {

    private lateinit var binding: FragmentVerifyPhotoBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentVerifyPhotoBinding.bind(view)

        val uri = requireArguments().getString("imageUri")!!.toUri()


        binding.imgPhoto.setImageURI(uri)

        val photoCode = readPhotoCodeFromExif(uri)

        if (photoCode == null) {
            showInvalid("Bukan foto absensi dari Afitech")
            return
        }

        FirestoreRepository.getAbsensiByPhotoCode(photoCode) { absensi ->
            if (absensi == null) {
                showInvalid("Photo Code tidak terdaftar")
            } else {
                showValid(absensi)
            }
        }
    }
    private fun showInvalid(reason: String) {
        binding.tvStatus.text = "Foto Absensi Tidak Valid"
        binding.tvReason.text = reason
        binding.tvLatLng.visibility = View.GONE
    }

    private fun readPhotoCodeFromExif(uri: Uri): String? {
        return try {
            requireContext()
                .contentResolver
                .openInputStream(uri)
                ?.use {
                    val exif = androidx.exifinterface.media.ExifInterface(it)
                    val comment = exif.getAttribute(
                        androidx.exifinterface.media.ExifInterface.TAG_USER_COMMENT
                    )

                    if (comment?.startsWith("AFITECH_ABSENSI|") == true) {
                        comment.removePrefix("AFITECH_ABSENSI|")
                    } else null
                }
        } catch (e: Exception) {
            null
        }
    }
    private fun showValid(a: Absensi) {

        binding.tvStatus.text = "Foto Absensi Valid"
        binding.tvReason.text = ""

        binding.tvNama.text = "👤 Nama: ${a.nama}"
        binding.tvLokasi.text = "📍 Lokasi: ${a.lokasi}"
        binding.tvPhotoCode.text = "🔐 Code: ${a.photoCode}"

        val waktu = SimpleDateFormat(
            "dd MMM yyyy HH:mm",
            Locale("id", "ID")
        ).format(Date(a.createdAt))

        binding.tvWaktu.text = "🕒 Waktu: $waktu"

        if (!a.latLng.isNullOrBlank()) {
            showLatLng(a.latLng)
        } else {
            geocodeAddressToLatLng(a.lokasi) { result ->
                if (!result.isNullOrBlank()) {
                    showLatLng(result)
                } else {
                    binding.tvLatLng.visibility = View.GONE
                }
            }
        }
    }

    private fun showLatLng(latlng: String) {

        binding.tvLatLng.visibility = View.VISIBLE
        binding.tvLatLng.text = "🌐 Koordinat: $latlng"

        binding.tvLatLng.setOnLongClickListener {

            val clipboard =
                requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                        as android.content.ClipboardManager

            val clip = android.content.ClipData.newPlainText("latlng", latlng)
            clipboard.setPrimaryClip(clip)

            android.widget.Toast
                .makeText(requireContext(), "Koordinat disalin", android.widget.Toast.LENGTH_SHORT)
                .show()

            true
        }
    }

    private fun geocodeAddressToLatLng(
        address: String,
        callback: (String?) -> Unit
    ) {
        try {
            val geocoder = android.location.Geocoder(requireContext(), Locale("id", "ID"))
            val results = geocoder.getFromLocationName(address, 1)

            if (!results.isNullOrEmpty()) {
                val loc = results[0]
                val latlng = String.format(
                    Locale.US,
                    "%.6f, %.6f",
                    loc.latitude,
                    loc.longitude
                )
                callback(latlng)
            } else callback(null)

        } catch (e: Exception) {
            callback(null)
        }
    }

}
