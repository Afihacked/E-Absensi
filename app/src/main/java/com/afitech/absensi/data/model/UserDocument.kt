package com.afitech.absensi.data.model

import com.google.firebase.Timestamp

data class UserDocument(
    val uid: String = "",
    val profile: UserProfile = UserProfile(),
    val settings: UserSettings = UserSettings(),
    val updatedAt: Timestamp? = null
)
