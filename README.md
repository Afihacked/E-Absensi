🚀 Afitech E‑Absensi Android

<p align="center">
  <img src="https://raw.githubusercontent.com/Afihacked/AfitechTok/master/app/src/main/res/mipmap-xxhdpi/ic_launcher.webp" alt="AfitechTok Logo" width="120" height="120" />
</p>Aplikasi absensi berbasis foto dengan sistem verifikasi keamanan menggunakan watermark terenkripsi, Photo Code, dan validasi database Firebase.


---

📲 Download Aplikasi

⬇ Unduh versi terbaru di sini:
👉 Download Latest Release


---

🔥 Fitur Utama

📸 Sistem Absensi

Ambil foto dari kamera atau galeri

Watermark otomatis berisi:

Nama user

Tanggal & waktu

Alamat lokasi

Koordinat (latlong)


Tata letak watermark dinamis & responsif


🔐 Keamanan Foto

Setiap foto memiliki Photo Code unik

Kode disimpan di EXIF metadata

Verifikasi foto langsung ke Firebase Database

Deteksi foto palsu / edit / screenshot

Sistem anti‑duplikat gambar (hash validation)


📍 Sistem Lokasi Pintar

Jika user set alamat custom → koordinat otomatis disesuaikan

Jika tidak → pakai GPS perangkat

Koordinat bisa disalin (long press)


👤 Sistem Akun Profesional

Login & Register Email + Password

Login dengan Google Sign‑In

Sistem session login otomatis

Profil user tersimpan di Firestore

Edit nama profil langsung (inline edit)

Avatar profil (Google + custom lokal)

Logout aman (backstack dibersihkan)


🧾 Riwayat Absensi

Data realtime dari Firestore

Urut berdasarkan waktu terbaru

Tampilan ringkas & detail


🛡 Verifikasi Foto Absensi

Baca Photo Code dari EXIF

Cek ke database

Validasi:

Nama

Lokasi

Waktu

Koordinat

Status foto asli / tidak valid




---

🧠 Arsitektur Aplikasi

com.afitech.absensi
│
├── data
│   ├── firebase → Repository Firebase
│   └── model → Model data (Absensi, UserProfile, UserSettings)
│
├── ui
│   ├── auth → Login, Register, Splash
│   ├── home → Home & History
│   ├── profile → Profil pengguna & avatar
│   ├── settings → Pengaturan watermark
│   └── verify → Verifikasi foto absensi


---

⚙️ Teknologi yang Digunakan

Teknologi	Fungsi

Kotlin	Bahasa utama
Firebase Auth	Sistem login
Cloud Firestore	Database realtime
Firebase Storage	Penyimpanan foto
EXIF Interface	Metadata foto
Material 3	UI modern
Navigation Component	Navigasi fragment
Glide	Loading gambar
Lottie	Animasi modern



---

🧪 Sistem Keamanan yang Dipakai

🔒 Photo Code terenkripsi

🔍 Validasi ke database

🧬 Hash gambar anti manipulasi

🧠 Cegah reuse foto lama

🛡 Firestore Security Rules berbasis UID



---

👨‍💻 Developer

Afitech Team
Sistem absensi modern, aman, dan siap produksi.


---

⭐ Dukungan

Jika proyek ini membantu, beri ⭐ di repository ya!
