package com.afitech.absensi.data.firebase

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

object StorageUploader {

    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun uploadAbsensiPhoto(
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: run {
            onError(Exception("User belum login"))
            return
        }

        val fileName = "absen_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
        val storageRef = storage
            .reference
            .child("absensi")
            .child(userId)
            .child(fileName)

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        onSuccess(uri.toString())
                    }
                    .addOnFailureListener { e ->
                        onError(e)
                    }
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }
}
