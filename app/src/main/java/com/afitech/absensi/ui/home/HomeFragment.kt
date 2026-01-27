package com.afitech.absensi.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.afitech.absensi.R
import com.afitech.absensi.data.firebase.FirestoreRepository
import com.afitech.absensi.data.firebase.SettingsRepository
import com.afitech.absensi.data.model.Absensi
import com.afitech.absensi.data.model.UserSettings
import com.afitech.absensi.databinding.FragmentHomeBinding
import com.afitech.absensi.ui.history.AbsensiAdapter
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding

    private var imageUri: Uri? = null
    private var isFromGallery = false
    private var previewBitmap: Bitmap? = null
    private var userSettings: UserSettings? = null

    private val historyAdapter = AbsensiAdapter()
    private var historyListener: ListenerRegistration? = null

    // ===== GPS RESULT =====
    private var currentLocationAddress: String? = null
    private var currentLocationLatLng: String? = null

    private var currentPhotoCode: String? = null
    private var currentImageHash: String? = null

    private var finalLatLngForWatermark: String? = null

    // ================= GPS PERMISSION =================
    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                fetchCurrentLocation()
            } else {
                currentLocationAddress = "Lokasi tidak diizinkan"
                currentLocationLatLng = null
                updateInfoAbsensiUI()
            }
        }

    // ================= CAMERA =================
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && imageUri != null) {
                isFromGallery = false
                showPreview(imageUri!!)
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                isFromGallery = true
                imageUri = it
                showPreview(it)
            }
        }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                imageUri = createImageUri()
                cameraLauncher.launch(imageUri)
            }
        }

    private val galleryPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) processSave()
        }
    private val verifyPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri ?: return@registerForActivityResult
            verifyPhotoOnly(uri)
        }

    // ================= LIFECYCLE =================
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        updateUserNameUI()
        fetchCurrentLocation()
        binding.rvHistory.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            findNavController().navigate(R.id.loginFragment)
            return
        }

        loadSettings()
        requestLocationIfNeeded()

        binding.btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                imageUri = createImageUri()
                cameraLauncher.launch(imageUri)
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        binding.btnGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.btnCancel.setOnClickListener { clearPreview() }

        binding.btnRetake.setOnClickListener {
            imageUri = createImageUri()
            cameraLauncher.launch(imageUri)
        }

        binding.btnConfirm.setOnClickListener {
            val permission =
                if (android.os.Build.VERSION.SDK_INT >= 33)
                    Manifest.permission.READ_MEDIA_IMAGES
                else
                    Manifest.permission.READ_EXTERNAL_STORAGE

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                processSave()
            } else {
                galleryPermissionLauncher.launch(permission)
            }
        }

        binding.btnHistoryAll.setOnClickListener {
            requireActivity()
                .findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                    R.id.bottomNav
                )
                .selectedItemId = R.id.historyFragment
        }
        binding.btnVerifyPhoto.setOnClickListener {
            verifyPhotoLauncher.launch("image/*")
        }

        observeHistory()
    }

    private fun updateUserNameUI() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener {
                val nama = it.getString("nama") ?: "User"
                binding.tvUserName.text = nama
            }
    }



    // ================= GPS =================
    private fun requestLocationIfNeeded() {
        val granted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            fetchCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchCurrentLocation() {
        val fusedClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        fusedClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    currentLocationLatLng =
                        String.format(
                            Locale.US,
                            "%.6f, %.6f",
                            location.latitude,
                            location.longitude
                        )

                    currentLocationAddress =
                        getReadableAddress(location.latitude, location.longitude)

                } else {
                    currentLocationAddress = "Lokasi tidak tersedia"
                    currentLocationLatLng = null
                }
                updateInfoAbsensiUI()
            }
            .addOnFailureListener {
                currentLocationAddress = "Gagal mengambil lokasi"
                currentLocationLatLng = null
                updateInfoAbsensiUI()
            }
    }


    private fun getReadableAddress(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(requireContext(), Locale("id", "ID"))
            val list = geocoder.getFromLocation(lat, lon, 1)

            if (list.isNullOrEmpty()) {
                "Lokasi tidak tersedia"
            } else {
                val a = list[0]
                listOf(
                    a.thoroughfare,
                    a.subLocality,
                    a.locality,
                    a.subAdminArea
                ).filter { !it.isNullOrBlank() }
                    .joinToString(", ")
            }
        } catch (e: Exception) {
            Log.e("GEOCODER", "Error", e)
            "Lokasi tidak tersedia"
        }
    }

    // ================= INFO UI =================
    private fun updateInfoAbsensiUI() {

        val s = userSettings
        val nama = s?.namaDisplay ?: "Anda Siapa ?"
        val alamatCustom = s?.lokasiDefault

        val alamat =
            if (!alamatCustom.isNullOrBlank())
                alamatCustom
            else
                currentLocationAddress ?: "Lokasi tidak tersedia"

        val tanggalDate =
            if (s?.gunakanTanggalManual == true && s.tanggalManual != null)
                s.tanggalManual.toDate()
            else
                Date()

        val tanggal = SimpleDateFormat(
            "dd MMM yyyy",
            Locale("id", "ID")
        ).format(tanggalDate)

        val jam =
            if (s?.gunakanWaktuManual == true && !s.waktuManual.isNullOrBlank())
                s.waktuManual
            else
                SimpleDateFormat("HH:mm", Locale("id", "ID"))
                    .format(Date())

        binding.tvInfoNama.text = "👤 Nama: $nama"
        binding.tvInfoLokasi.text = "📍 Lokasi:\n$alamat"
        binding.tvInfoWaktu.text = "🕒 Waktu: $tanggal $jam"

        // ================= KOORDINAT AUTO SINKRON =================

        if (!alamatCustom.isNullOrBlank()) {

            // 🔥 PAKAI ALAMAT CUSTOM → GEOCODE
            geocodeAddressToLatLng(alamatCustom) { latlng ->

                finalLatLngForWatermark = latlng

                if (!latlng.isNullOrBlank()) {
                    binding.tvInfoLatLng.text = latlng
                    binding.tvInfoLatLng.visibility = View.VISIBLE
                    setCopyLatLng(latlng)
                } else {
                    binding.tvInfoLatLng.visibility = View.GONE
                }
            }

        } else {

            // 🔥 TANPA CUSTOM → GPS DEVICE
            finalLatLngForWatermark = currentLocationLatLng

            if (!currentLocationLatLng.isNullOrBlank()) {
                binding.tvInfoLatLng.text = currentLocationLatLng
                binding.tvInfoLatLng.visibility = View.VISIBLE
                setCopyLatLng(currentLocationLatLng)
            } else {
                binding.tvInfoLatLng.visibility = View.GONE
            }
        }
    }
    private fun setCopyLatLng(latlng: String?) {

        binding.tvInfoLatLng.setOnLongClickListener {

            if (latlng.isNullOrBlank()) return@setOnLongClickListener true

            val clipboard =
                requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                        as android.content.ClipboardManager

            val clip = android.content.ClipData.newPlainText("latlng", latlng)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(requireContext(), "Koordinat disalin", Toast.LENGTH_SHORT).show()
            true
        }
    }


    // ================= HISTORY =================
    private fun observeHistory() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        historyListener =
            FirestoreRepository.observeAbsensiRingkasRealtime(
                uid = uid,
                limit = 3
            ) { list ->
                if (list.isNotEmpty()) {
                    historyAdapter.submitList(list)
                    showHistoryWithData()
                } else {
                    showHistoryEmpty()
                }
            }
    }

    private fun showHistoryWithData() {
        binding.cardHistory.visibility = View.VISIBLE
        binding.tvHistoryTitle.visibility = View.VISIBLE
        binding.btnHistoryAll.visibility = View.VISIBLE
        binding.tvHistoryEmpty.visibility = View.GONE
    }

    private fun showHistoryEmpty() {
        binding.cardHistory.visibility = View.GONE
        binding.tvHistoryTitle.visibility = View.VISIBLE
        binding.tvHistoryEmpty.visibility = View.VISIBLE
        binding.btnHistoryAll.visibility = View.GONE
    }

    private fun hideHistoryAll() {
        binding.cardHistory.visibility = View.GONE
        binding.tvHistoryTitle.visibility = View.GONE
        binding.tvHistoryEmpty.visibility = View.GONE
        binding.btnHistoryAll.visibility = View.GONE
    }

    // ================= SETTINGS =================
    private fun loadSettings() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        SettingsRepository.getSettings(
            uid,
            onSuccess = {
                userSettings = it
                updateInfoAbsensiUI()
            },
            onError = {
                userSettings = null
                updateInfoAbsensiUI()
            }
        )
    }

    // ================= PREVIEW =================
    private fun showPreview(uri: Uri) {

        val originalBitmap = getBitmapFromUri(uri)

        // 🔐 GENERATE SEKALI SAJA
        currentPhotoCode = generatePhotoCode()
        currentImageHash = generateImageHash(originalBitmap)

        Log.d(
            "FASE3",
            "PREVIEW hash=${currentImageHash?.take(10)} code=$currentPhotoCode"
        )

        var bitmap = addWatermarkFromXml(originalBitmap)
        bitmap = drawVerifiedVertical(bitmap)
        bitmap = drawPhotoCodeBottom(bitmap, currentPhotoCode!!)

        previewBitmap = bitmap

        binding.imgPreview.setImageBitmap(previewBitmap)
        binding.cardPreview.visibility = View.VISIBLE

        binding.btnCamera.visibility = View.GONE
        binding.btnGallery.visibility = View.GONE
        binding.tvNote.visibility = View.GONE

        hideHistoryAll()
    }



    private fun clearPreview() {
        binding.cardPreview.visibility = View.GONE
        binding.btnCamera.visibility = View.VISIBLE
        binding.btnGallery.visibility = View.VISIBLE
        binding.tvNote.visibility = View.VISIBLE

        if (historyAdapter.itemCount > 0) showHistoryWithData()
        else showHistoryEmpty()

        previewBitmap = null
        imageUri = null
        updateInfoAbsensiUI()
    }

    // ================= SAVE =================
    private fun saveAbsensiFinal(
        uid: String,
        bitmap: Bitmap,
        uri: Uri,
        photoCode: String,
        imageHash: String,
        alamatFinal: String,
        latLngFinal: String?
    ) {

        val finalUri: Uri = if (isFromGallery) {
            saveBitmapToGallery(bitmap)
                ?: run {
                    Toast.makeText(requireContext(), "Gagal simpan foto", Toast.LENGTH_LONG).show()
                    return
                }
        } else {
            overwriteOriginalImage(uri, bitmap)
            uri
        }

        // 🔐 Tulis EXIF
        writeAbsensiExif(finalUri, photoCode)

        // 🔥 SIMPAN FIRESTORE (SUDAH SINKRON)
        FirestoreRepository.saveAbsensi(
            Absensi(
                uid = uid,
                nama = userSettings?.namaDisplay ?: "Nama User",
                lokasi = alamatFinal,
                photoLocal = true,
                photoCode = photoCode,
                imageHash = imageHash,
                latLng = latLngFinal,
                createdAt = System.currentTimeMillis()
            ),
            onSuccess = {
                Toast.makeText(requireContext(), "Absensi tersimpan", Toast.LENGTH_SHORT).show()
                currentPhotoCode = null
                currentImageHash = null
                clearPreview()
            },
            onError = {
                Toast.makeText(requireContext(), "Gagal simpan absensi", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun processSave() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val bitmap = previewBitmap ?: return
        val uri = imageUri ?: return
        val photoCode = currentPhotoCode ?: return
        val imageHash = currentImageHash ?: return

        val alamatCustom = userSettings?.lokasiDefault

        FirestoreRepository.checkDuplicateAbsensi(
            uid = uid,
            imageHash = imageHash
        ) { isDuplicate, reason ->

            if (isDuplicate) {
                Toast.makeText(requireContext(), "❌ $reason", Toast.LENGTH_LONG).show()
                return@checkDuplicateAbsensi
            }

            // 🔥 Tentukan sumber alamat & koordinat
            if (!alamatCustom.isNullOrBlank()) {

                // 📍 LATLNG DARI ALAMAT CUSTOM
                geocodeAddressToLatLng(alamatCustom) { latlngFromAddress ->

                    saveAbsensiFinal(
                        uid = uid,
                        bitmap = bitmap,
                        uri = uri,
                        photoCode = photoCode,
                        imageHash = imageHash,
                        alamatFinal = alamatCustom,
                        latLngFinal = latlngFromAddress
                    )
                }

            } else {

                // 📍 LATLNG DARI GPS DEVICE
                saveAbsensiFinal(
                    uid = uid,
                    bitmap = bitmap,
                    uri = uri,
                    photoCode = photoCode,
                    imageHash = imageHash,
                    alamatFinal = currentLocationAddress ?: "Lokasi tidak tersedia",
                    latLngFinal = currentLocationLatLng
                )
            }
        }
    }

    // ================= IMAGE UTIL =================
    private fun createImageUri(): Uri =
        requireContext().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "absen_${System.currentTimeMillis()}.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            }
        )!!

    private fun getBitmapFromUri(uri: Uri): Bitmap =
        if (android.os.Build.VERSION.SDK_INT >= 28)
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().contentResolver, uri))
        else
            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)

    private fun viewToBitmap(view: View): Bitmap {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun applyGradientToTimeDirect(tv: TextView) {
        val paint = tv.paint

        val shader = LinearGradient(
            0f, 0f,
            0f, tv.textSize,
            intArrayOf(
                Color.parseColor("#1E88E5"), // biru atas
                Color.parseColor("#000000")  // hitam bawah
            ),
            null,
            Shader.TileMode.CLAMP
        )
        paint.shader = shader
    }

    private fun addWatermarkFromXml(photo: Bitmap): Bitmap {

        val s = userSettings
        val nama = s?.namaDisplay ?: "Nama User"

        val alamatCustom = s?.lokasiDefault

        val alamat =
            if (!alamatCustom.isNullOrBlank())
                alamatCustom
            else
                currentLocationAddress ?: "Lokasi tidak tersedia"

        // 🔥 KOORDINAT SUDAH DIHITUNG SEBELUMNYA
        val koordinat = finalLatLngForWatermark

        val tanggalDate =
            if (s?.gunakanTanggalManual == true && s.tanggalManual != null)
                s.tanggalManual.toDate()
            else Date()

        val tanggal = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            .format(tanggalDate)

        val jam =
            if (s?.gunakanWaktuManual == true && !s.waktuManual.isNullOrBlank())
                s.waktuManual
            else
                SimpleDateFormat("HH:mm", Locale("id", "ID"))
                    .format(Date())

        val wmView = layoutInflater.inflate(R.layout.layout_watermark, null)

        wmView.findViewById<TextView>(R.id.tvWmName).text = nama
        val tvTime = wmView.findViewById<TextView>(R.id.tvWmTime)
        tvTime.text = jam
        wmView.findViewById<TextView>(R.id.tvWmDate).text = tanggal
        wmView.findViewById<TextView>(R.id.tvWmAddress).text = alamat

        val tvLatLng = wmView.findViewById<TextView>(R.id.tvWmLatLng)

        if (!koordinat.isNullOrBlank()) {
            tvLatLng.text = koordinat
            tvLatLng.visibility = View.VISIBLE
        } else {
            tvLatLng.visibility = View.GONE
        }

        applyGradientToTimeDirect(tvTime)

        val maxWmWidth = (photo.width * 0.85f).toInt()
        val wmBitmap = viewToBitmapWithMaxWidth(wmView, maxWmWidth)

        val result = photo.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        val safeMargin = photo.width * 0.01f
        val left = safeMargin
        val top = photo.height - wmBitmap.height - safeMargin
        val safeTop = maxOf(safeMargin, top)

        canvas.drawBitmap(wmBitmap, left, safeTop, null)
        return result
    }


//VERIFIED
private fun generatePhotoCode(): String {
    val letters = "ABCDEFGHJKLMNPQRSTUVWXYZ"
    val numbers = "23456789"

    val random = Random(System.currentTimeMillis())

    val result = mutableListOf<Char>()

    // 🔒 pastikan minimal
    result += letters[random.nextInt(letters.length)]
    result += numbers[random.nextInt(numbers.length)]

    val allChars = letters + numbers

    // sisanya random bebas
    repeat(10) {
        result += allChars[random.nextInt(allChars.length)]
    }

    result.shuffle(random)

    return "PT" + result.joinToString("")
}

    private fun drawVerifiedVertical(
        base: Bitmap
    ): Bitmap {

        val view = layoutInflater.inflate(
            R.layout.layout_verified_vertical,
            null
        )

        val wmBitmap = viewToBitmap(view)

        val rotated = Bitmap.createBitmap(
            wmBitmap,
            0, 0,
            wmBitmap.width,
            wmBitmap.height,
            Matrix().apply { postRotate(-90f) },
            true
        )

        val result = base.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        val margin = base.width * 0.0f
        val x = base.width - rotated.width - margin
        val y = base.height * 0.40f

        canvas.drawBitmap(rotated, x, y, null)
        return result
    }
    private fun drawPhotoCodeBottom(
        base: Bitmap,
        photoCode: String
    ): Bitmap {

        val view = layoutInflater.inflate(
            R.layout.layout_photo_code,
            null
        )

        view.findViewById<TextView>(R.id.tvPhotoCode).text =
            " Photo Code: $photoCode"

        val wmBitmap = viewToBitmap(view)

        val result = base.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        val margin = base.width * 0.0f

        // 👉 KANAN BAWAH
        val x = base.width - wmBitmap.width - margin
        val y = base.height - wmBitmap.height + (margin * 0.0020f)

        canvas.drawBitmap(wmBitmap, x, y, null)
        return result
    }

    //END

//    HASH
private fun generateImageHash(bitmap: Bitmap): String {
    val output = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
    val bytes = output.toByteArray()

    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(bytes)

    return hash.joinToString("") {
        "%02x".format(it)
    }
}
    private fun writeAbsensiExif(uri: Uri, photoCode: String) {
        try {
            requireContext()
                .contentResolver
                .openFileDescriptor(uri, "rw")
                ?.use {
                    val exif = androidx.exifinterface.media.ExifInterface(it.fileDescriptor)
                    exif.setAttribute(
                        androidx.exifinterface.media.ExifInterface.TAG_USER_COMMENT,
                        "AFITECH_ABSENSI|$photoCode"
                    )
                    exif.saveAttributes()
                }
        } catch (e: Exception) {
            Log.e("FASE3", "EXIF WRITE FAILED", e)
        }
    }

    private fun verifyPhotoOnly(uri: Uri) {
        val action =
            HomeFragmentDirections
                .actionHomeFragmentToVerifyPhotoFragment(uri.toString())

        findNavController().navigate(action)
    }

    //end
    private fun viewToBitmapWithMaxWidth(
        view: View,
        maxWidthPx: Int
    ): Bitmap {

        val widthSpec = View.MeasureSpec.makeMeasureSpec(
            maxWidthPx,
            View.MeasureSpec.AT_MOST
        )

        val heightSpec = View.MeasureSpec.makeMeasureSpec(
            0,
            View.MeasureSpec.UNSPECIFIED
        )

        view.measure(widthSpec, heightSpec)
        view.layout(
            0,
            0,
            view.measuredWidth,
            view.measuredHeight
        )

        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }



    private fun overwriteOriginalImage(uri: Uri, bitmap: Bitmap) {
        requireContext().contentResolver.openOutputStream(uri)?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
        }
    }

    private fun saveBitmapToGallery(bitmap: Bitmap): Uri? {
        val resolver = requireContext().contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "absen_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/E-Absensi")
        }

        val uri =
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        uri?.let {
            resolver.openOutputStream(it)?.use { os ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, os)
            }
        }

        return uri
    }
    private fun geocodeAddressToLatLng(
        address: String,
        callback: (String?) -> Unit
    ) {
        try {
            val geocoder = Geocoder(requireContext(), Locale("id", "ID"))
            val results = geocoder.getFromLocationName(address, 1)

            if (!results.isNullOrEmpty()) {
                val loc = results[0]
                val latlng = String.format(
                    Locale.US,
                    "%.6f, %.6f",
                    loc.latitude,
                    loc.longitude
                )
                callback(latlng)
            } else {
                callback(null)
            }
        } catch (e: Exception) {
            Log.e("GEOCODE", "Geocode failed", e)
            callback(null)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        historyListener?.remove()
        historyListener = null
    }
    override fun onResume() {
        super.onResume()
        updateUserNameUI()
    }

}
