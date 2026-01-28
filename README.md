🚀 Afitech E‑Absensi Android

<p align="center">
  <img src="https://raw.githubusercontent.com/Afihacked/E-Absensi/master/app/src/main/res/mipmap-xxhdpi/ic_launcher.webp" alt="AfitechTok Logo" width="180" height="180" />
</p>Aplikasi absensi berbasis foto dengan sistem verifikasi keamanan menggunakan watermark terenkripsi, Photo Code, dan validasi database Firebase.


---

📲 Download Aplikasi

<p align="center">
  <a href="https://github.com/Afihacked/AfitechTok/releases">
    <img src="https://img.shields.io/github/v/release/E-Absensi/E-Absensi?color=brightgreen&label=versi" alt="Release Version">
  </a>
  <a href="https://github.com/Afihacked/E-Absensi/issues">
    <img src="https://img.shields.io/github/issues/Afihacked/E-Absensi?color=yellow" alt="Issues">
  </a>
  <a href="#">
    <img src="https://img.shields.io/github/stars/Afihacked/E-Absensi?color=orange" alt="Stars">
  </a>
  <img src="https://img.shields.io/badge/Made%20with-Kotlin-blue?logo=kotlin" alt="Kotlin Badge">
  <img src="https://img.shields.io/github/license/Afihacked/E-Absensi?color=blue" alt="License Badge">
</p>

<p align="center">
  <a href="https://github.com/Afihacked/E-Absensi/releases/latest/download/E-Absensi_v1.0.0.apk">
    <img src="https://img.shields.io/badge/⬇️_Download-APK-blue?style=for-the-badge&logo=android" alt="Download APK">
  </a>
</p>


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
