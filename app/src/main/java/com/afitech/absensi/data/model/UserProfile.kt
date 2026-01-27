package com.afitech.absensi.data.model

data class UserProfile(
    val uid: String = "",
    val nama: String = "",
    val email: String = "",
    val role: String = "user",
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
