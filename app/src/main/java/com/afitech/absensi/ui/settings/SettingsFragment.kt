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
import com.afitech.absensi.databinding.FragmentSettingsBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var binding: FragmentSettingsBinding

    private var selectedDate: Timestamp? = null
    private var selectedTime: String? = null

    private val dateFormat =
        SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

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

            val lokasiGabung = listOf(
                binding.etLokasi1.text.toString(),
                binding.etLokasi2.text.toString()
            ).filter { it.isNotBlank() }
                .joinToString(", ")

            val settings = UserSettings(
                uid = uid,
                namaDisplay = binding.etNama.text.toString(),
                lokasiDefault = lokasiGabung,
                gunakanTanggalManual = selectedDate != null,
                tanggalManual = selectedDate,
                gunakanWaktuManual = selectedTime != null,
                waktuManual = selectedTime
            )

            SettingsRepository.saveOrUpdateSettings(
                settings,
                onSuccess = {
                    Toast.makeText(
                        requireContext(),
                        "Pengaturan disimpan",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onError = {
                    Toast.makeText(
                        requireContext(),
                        "Gagal menyimpan pengaturan",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }

        // ===== RESET (KECUALI NAMA) =====
        binding.btnReset.setOnClickListener {

            // 🔹 reset lokasi
            binding.etLokasi1.setText("")
            binding.etLokasi2.setText("")

            // 🔹 reset tanggal
            selectedDate = null
            binding.btnTanggal.text = "Ubah Tanggal"

            // 🔹 reset waktu
            selectedTime = null
            binding.btnWaktu.text = "Ubah Waktu"

            Toast.makeText(
                requireContext(),
                "Pengaturan tanggal, waktu, dan lokasi direset",
                Toast.LENGTH_SHORT
            ).show()
        }

    }


    private fun applySettings(s: UserSettings) {
        binding.etNama.setText(s.namaDisplay)

        // ===== LOKASI =====
        val lokasi = s.lokasiDefault ?: ""
        val parts = lokasi.split(",").map { it.trim() }
        if (parts.isNotEmpty()) binding.etLokasi1.setText(parts[0])
        if (parts.size > 1) binding.etLokasi2.setText(parts[1])

        // ===== TANGGAL =====
        if (s.gunakanTanggalManual && s.tanggalManual != null) {
            selectedDate = s.tanggalManual
            binding.btnTanggal.text =
                dateFormat.format(s.tanggalManual.toDate())
        } else {
            binding.btnTanggal.text = "Ubah Tanggal"
            selectedDate = null
        }

        // ===== WAKTU =====
        if (s.gunakanWaktuManual && !s.waktuManual.isNullOrBlank()) {
            selectedTime = s.waktuManual
            binding.btnWaktu.text = s.waktuManual
        } else {
            binding.btnWaktu.text = "Ubah Waktu"
            selectedTime = null
        }
    }
}
