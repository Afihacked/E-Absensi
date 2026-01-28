package com.afitech.absensi.data.firebase

import android.util.Log
import com.afitech.absensi.data.model.UserSettings
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object SettingsRepository {

    private val db = FirebaseFirestore.getInstance()

    // ================= GET SETTINGS =================
    fun getSettings(
        uid: String,
        onSuccess: (UserSettings?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("settings")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    onSuccess(null)
                    return@addOnSuccessListener
                }

                val settings = UserSettings(
                    uid = uid,
                    namaDisplay = doc.getString("namaDisplay"),
                    alamatText = doc.getString("alamatText"),       // ✅ FIX
                    latLngManual = doc.getString("latLngManual"),   // ✅ FIX
                    gunakanTanggalManual = doc.getBoolean("gunakanTanggalManual") ?: false,
                    tanggalManual = doc.getTimestamp("tanggalManual"),
                    gunakanWaktuManual = doc.getBoolean("gunakanWaktuManual") ?: false,
                    waktuManual = doc.getString("waktuManual"),
                    updatedAt = doc.getTimestamp("updatedAt")
                )

                onSuccess(settings)
            }
            .addOnFailureListener { e ->
                Log.e("SETTINGS_GET", "ERROR", e)
                onError(e)
            }
    }

    // ================= SAVE / UPDATE SETTINGS =================
    fun saveOrUpdateSettings(
        settings: UserSettings,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val data = hashMapOf(
            "uid" to settings.uid,
            "namaDisplay" to settings.namaDisplay,

            // 🔥 INI YANG SEBELUMNYA HILANG
            "alamatText" to settings.alamatText,
            "latLngManual" to settings.latLngManual,

            "gunakanTanggalManual" to settings.gunakanTanggalManual,
            "tanggalManual" to settings.tanggalManual,
            "gunakanWaktuManual" to settings.gunakanWaktuManual,
            "waktuManual" to settings.waktuManual,

            "updatedAt" to FieldValue.serverTimestamp()
        )

        db.collection("settings")
            .document(settings.uid)
            .set(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                Log.e("SETTINGS_SAVE", "ERROR", e)
                onError(e)
            }
    }
}