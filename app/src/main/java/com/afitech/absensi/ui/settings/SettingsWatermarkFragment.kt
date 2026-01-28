package com.afitech.absensi.ui.settings

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.afitech.absensi.R
import com.afitech.absensi.data.firebase.SettingsRepository
import com.afitech.absensi.data.model.UserSettings
import com.afitech.absensi.databinding.FragmentSettingsWatermarkBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SettingsWatermarkFragment : Fragment(R.layout.fragment_settings_watermark) {

    private lateinit var binding: FragmentSettingsWatermarkBinding

    private var selectedDate: Timestamp? = null
    private var selectedTime: String? = null

    private val dateFormat =
        SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsWatermarkBinding.bind(view)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        ensureDefaultName(uid)
        // 🔥 WAJIB: tampilkan tombol (XML default = gone)
        binding.btnTanggal.visibility = View.VISIBLE
        binding.btnWaktu.visibility = View.VISIBLE

        // ===== DEFAULT TEXT =====
        binding.btnTanggal.text = "Ubah Tanggal"
        binding.btnWaktu.text = "Ubah Waktu"

        // ===== LOAD SETTINGS =====
        SettingsRepository.getSettings(
            uid,
            onSuccess = { settings ->
                settings?.let { applySettings(it) }
            },
            onError = { }
        )

        // ===== PICK TANGGAL =====
        binding.btnTanggal.setOnClickListener {
            val cal = Calendar.getInstance()

            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    cal.set(y, m, d)
                    selectedDate = Timestamp(cal.time)
                    binding.btnTanggal.text =
                        dateFormat.format(cal.time)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // ===== PICK WAKTU =====
        binding.btnWaktu.setOnClickListener {
            val cal = Calendar.getInstance()

            TimePickerDialog(
                requireContext(),
                { _, h, m ->
                    selectedTime = String.format("%02d:%02d", h, m)
                    binding.btnWaktu.text = selectedTime
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }

        // ===== SIMPAN =====
        binding.btnSimpan.setOnClickListener {

            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val nama = binding.etNama.text.toString()
            val alamatManual = binding.etLokasi1.text.toString().trim()
            val latLngManual = binding.etLokasi2.text.toString().trim()

            if (alamatManual.isNotBlank() && latLngManual.isNotBlank()) {
                Toast.makeText(requireContext(),
                    "Isi salah satu: Alamat ATAU LatLng saja",
                    Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            fun save(alamatFinal: String?, latlngFinal: String?) {
                val settings = UserSettings(
                    uid = uid,
                    namaDisplay = nama,
                    alamatText = alamatFinal,
                    latLngManual = latlngFinal,
                    gunakanTanggalManual = selectedDate != null,
                    tanggalManual = selectedDate,
                    gunakanWaktuManual = selectedTime != null,
                    waktuManual = selectedTime
                )

                SettingsRepository.saveOrUpdateSettings(settings,
                    {
                        Toast.makeText(requireContext(),"Pengaturan disimpan",Toast.LENGTH_SHORT).show()
                        applySettings(settings) // 🔥 update UI langsung
                    },
                    {
                        Toast.makeText(requireContext(),"Gagal menyimpan",Toast.LENGTH_LONG).show()
                    }
                )
            }

            // ===== USER ISI LATLNG =====
            if (latLngManual.isNotBlank()) {
                latLngToAddress(latLngManual) { autoAddress ->
                    binding.etLokasi1.setText(autoAddress ?: "")
                    save(autoAddress, latLngManual)
                }
                return@setOnClickListener
            }

            // ===== USER ISI ALAMAT =====
            if (alamatManual.isNotBlank()) {
                addressToLatLng(alamatManual) { autoLatLng ->
                    binding.etLokasi2.setText(autoLatLng ?: "")
                    save(alamatManual, autoLatLng)
                }
                return@setOnClickListener
            }

            // ===== TANPA LOKASI =====
            save(null, null)
        }

        // ===== RESET (KECUALI NAMA) =====
        binding.btnReset.setOnClickListener {

            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener {
                    val defaultName = it.getString("nama") ?: "User"
                    binding.etNama.setText(defaultName)
                }

            binding.etLokasi1.setText("")
            binding.etLokasi2.setText("")
            selectedDate = null
            selectedTime = null
            binding.btnTanggal.text = "Ubah Tanggal"
            binding.btnWaktu.text = "Ubah Waktu"

            Toast.makeText(requireContext(),
                "Semua pengaturan direset",
                Toast.LENGTH_SHORT).show()
        }

    }

    private fun addressToLatLng(address: String, callback: (String?) -> Unit) {
        try {
            val geo = android.location.Geocoder(requireContext(), Locale("id","ID"))
            val list = geo.getFromLocationName(address, 1)
            if (!list.isNullOrEmpty()) {
                val l = list[0]
                callback(String.format(Locale.US, "%.6f, %.6f", l.latitude, l.longitude))
            } else callback(null)
        } catch (e: Exception) {
            callback(null)
        }
    }

    private fun latLngToAddress(latlng: String, callback: (String?) -> Unit) {
        try {
            val parts = latlng.split(",")
            if (parts.size != 2) return callback(null)

            val lat = parts[0].trim().toDouble()
            val lng = parts[1].trim().toDouble()

            val geo = android.location.Geocoder(requireContext(), Locale("id","ID"))
            val list = geo.getFromLocation(lat, lng, 1)

            if (!list.isNullOrEmpty()) {
                val a = list[0]
                val address = listOf(
                    a.thoroughfare,
                    a.subLocality,
                    a.locality,
                    a.subAdminArea
                ).filter { !it.isNullOrBlank() }.joinToString(", ")

                callback(address)
            } else callback(null)

        } catch (e: Exception) {
            callback(null)
        }
    }
    private fun ensureDefaultName(uid: String) {
        SettingsRepository.getSettings(uid, { s ->
            if (s?.namaDisplay.isNullOrBlank()) {
                FirebaseFirestore.getInstance().collection("users").document(uid).get()
                    .addOnSuccessListener {
                        val defaultName = it.getString("nama") ?: "User"
                        binding.etNama.setText(defaultName)
                    }
            }
        }, {})
    }
    private fun applySettings(s: UserSettings) {

        binding.etNama.setText(s.namaDisplay ?: "")
        binding.etLokasi1.setText(s.alamatText ?: "")
        binding.etLokasi2.setText(s.latLngManual ?: "")

        if (s.gunakanTanggalManual && s.tanggalManual != null) {
            selectedDate = s.tanggalManual
            binding.btnTanggal.text =
                SimpleDateFormat("dd MMM yyyy", Locale("id","ID"))
                    .format(s.tanggalManual.toDate())
        } else {
            selectedDate = null
            binding.btnTanggal.text = "Ubah Tanggal"
        }

        if (s.gunakanWaktuManual && !s.waktuManual.isNullOrBlank()) {
            selectedTime = s.waktuManual
            binding.btnWaktu.text = s.waktuManual
        } else {
            selectedTime = null
            binding.btnWaktu.text = "Ubah Waktu"
        }
    }

    override fun onResume() {
        super.onResume()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        SettingsRepository.getSettings(uid,
            { it?.let { s -> applySettings(s) } },
            {}
        )
    }
}
