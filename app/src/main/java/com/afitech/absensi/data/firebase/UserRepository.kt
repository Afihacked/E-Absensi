package com.afitech.absensi.data.firebase

import com.afitech.absensi.data.model.UserProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object UserRepository {

    private val db = FirebaseFirestore.getInstance()

    fun saveOrUpdateProfile(
        profile: UserProfile,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val data = hashMapOf(
            "uid" to profile.uid,
            "nama" to profile.nama,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        db.collection("users")
            .document(profile.uid)
            .set(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e) }
    }

    fun getProfile(
        uid: String,
        onSuccess: (UserProfile?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    onSuccess(doc.toObject(UserProfile::class.java))
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { e -> onError(e) }
    }
}
