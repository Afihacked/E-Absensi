package com.afitech.absensi.data.firebase

import com.afitech.absensi.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore

object UserRepository {

    private val db = FirebaseFirestore.getInstance()

    fun createUserProfile(profile: UserProfile, onResult: (Boolean) -> Unit) {
        db.collection("users")
            .document(profile.uid)
            .set(profile)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getUser(
        uid: String,
        onSuccess: (UserProfile?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->

                if (!doc.exists()) {
                    onSuccess(null)
                    return@addOnSuccessListener
                }

                val profile = UserProfile(
                    uid = doc.getString("uid") ?: "",
                    nama = doc.getString("nama") ?: "",
                    email = doc.getString("email") ?: "",
                    role = doc.getString("role") ?: "user",
                    createdAt = doc.getTimestamp("createdAt")
                )

                onSuccess(profile)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    fun updateNama(
        uid: String,
        nama: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("users").document(uid)
            .update("nama", nama)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }
}
