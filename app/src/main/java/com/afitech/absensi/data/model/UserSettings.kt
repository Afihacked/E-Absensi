package com.afitech.absensi.data.model

import com.google.firebase.Timestamp

data class UserSettings(
    val uid: String = "",

    val namaDisplay: String? = null,
    val lokasiDefault: String? = null,

    val gunakanTanggalManual: Boolean = false,
    val tanggalManual: Timestamp? = null,

    val gunakanWaktuManual: Boolean = false,
    val waktuManual: String? = null,

    val updatedAt: Timestamp? = null
)

