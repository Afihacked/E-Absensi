package com.afitech.absensi.data.model

import com.google.firebase.Timestamp


data class UserProfile(
    val uid: String = "",
    val nama: String = "",
    val email: String = "",
    val role: String = "user",
    val active: Boolean = true,
    val createdAt: Timestamp?= null
)
