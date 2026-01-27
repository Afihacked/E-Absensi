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
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                onSuccess(doc.toObject(UserProfile::class.java))
            }
            .addOnFailureListener { onError(it) }
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
