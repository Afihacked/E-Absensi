package com.afitech.absensi.data.firebase

import android.util.Log
import com.afitech.absensi.data.model.Absensi
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

object FirestoreRepository {

    private const val TAG = "FIRESTORE_ABSENSI"
    private val db = FirebaseFirestore.getInstance()

    fun saveAbsensi(
        absensi: Absensi,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val data = hashMapOf(
            "uid" to absensi.uid,
            "nama" to absensi.nama,
            "lokasi" to absensi.lokasi,
            "latLng" to absensi.latLng,   // ✅ INI YANG HILANG
            "photoLocal" to absensi.photoLocal,
            "photoCode" to absensi.photoCode,
            "imageHash" to absensi.imageHash,
            "createdAt" to absensi.createdAt
        )

        db.collection("absensi")
            .add(data)
            .addOnSuccessListener {
                Log.d(TAG, "saveAbsensi: SUCCESS")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "saveAbsensi: ERROR", e)
                onError(e)
            }
    }


    fun observeAbsensiByUser(
        uid: String,
        onUpdate: (List<Absensi>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {

        Log.d(TAG, "observeAbsensiByUser: start listen uid=$uid")

        return db.collection("absensi")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e(TAG, "snapshotListener ERROR", error)
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.w(TAG, "snapshot NULL")
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot.isEmpty) {
                    Log.d(TAG, "snapshot EMPTY (no data)")
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }

                val rawList = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val createdAtAny = doc.get("createdAt")

                        val createdAtMillis = when (createdAtAny) {
                            is com.google.firebase.Timestamp -> createdAtAny.toDate().time
                            is Long -> createdAtAny
                            else -> 0L
                        }

                        Absensi(
                            uid = doc.getString("uid") ?: "",
                            nama = doc.getString("nama") ?: "",
                            lokasi = doc.getString("lokasi") ?: "",
                            latLng = doc.getString("latLng"),          // ✅ TAMBAHKAN
                            photoLocal = doc.getBoolean("photoLocal") ?: false,
                            photoCode = doc.getString("photoCode") ?: "", // opsional
                            imageHash = doc.getString("imageHash") ?: "", // opsional
                            createdAt = createdAtMillis
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()


                // 🔥 SORT AMAN DI CLIENT
                val sortedList = rawList.sortedByDescending {
                    it.createdAt
                }

                Log.d(
                    TAG,
                    "snapshot UPDATE size=${sortedList.size}"
                )

                onUpdate(sortedList)
            }
    }
    fun observeAbsensiRingkasRealtime(
        uid: String,
        limit: Int,
        onUpdate: (List<Absensi>) -> Unit
    ): ListenerRegistration {

        return db.collection("absensi")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshot, _ ->

                val raw = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val createdAtAny = doc.get("createdAt")

                        val createdAtMillis = when (createdAtAny) {
                            is com.google.firebase.Timestamp -> createdAtAny.toDate().time
                            is Long -> createdAtAny
                            else -> 0L
                        }

                        Absensi(
                            uid = doc.getString("uid") ?: "",
                            nama = doc.getString("nama") ?: "",
                            lokasi = doc.getString("lokasi") ?: "",
                            latLng = doc.getString("latLng"),          // ✅ TAMBAHKAN
                            photoLocal = doc.getBoolean("photoLocal") ?: false,
                            photoCode = doc.getString("photoCode") ?: "", // opsional
                            imageHash = doc.getString("imageHash") ?: "", // opsional
                            createdAt = createdAtMillis
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()


                val sorted = raw
                    .sortedByDescending { it.createdAt }
                    .take(limit)

                onUpdate(sorted)
            }
    }
    fun checkDuplicateAbsensi(
        uid: String,
        imageHash: String,
        callback: (Boolean, String) -> Unit
    ) {
        db.collection("absensi")
            .whereEqualTo("uid", uid)
            .whereEqualTo("imageHash", imageHash) // 🔐 LANGSUNG FILTER
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->

                if (!snapshot.isEmpty) {
                    Log.d("FASE3", "DUPLICATE IMAGE HASH")
                    callback(true, "Foto yang sama terdeteksi")
                    return@addOnSuccessListener
                }

                Log.d("FASE3", "PASS HASH VALIDATION")
                callback(false, "")
            }
            .addOnFailureListener {
                Log.e("FASE3", "CHECK FAILED", it)
                callback(false, "")
            }
    }
    fun getAbsensiByPhotoCode(
        photoCode: String,
        callback: (Absensi?) -> Unit
    ) {
        db.collection("absensi")
            .whereEqualTo("photoCode", photoCode)
            .limit(1)
            .get()
            .addOnSuccessListener { snap ->
                if (snap.isEmpty) {
                    callback(null)
                } else {
                    callback(snap.documents[0].toObject(Absensi::class.java))
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }

}
