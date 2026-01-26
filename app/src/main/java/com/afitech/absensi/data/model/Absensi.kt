package com.afitech.absensi.data.model

data class Absensi(
    val uid: String = "",
    val nama: String = "",
    val lokasi: String = "",
    val photoLocal: Boolean = true,
    val createdAt: Long = 0L,
    val photoCode: String = "",
    val imageHash: String = "",
    val latLng: String? = null
)
