🚀 Afitech E-Absensi Android

<p align="center">
  <img src="https://raw.githubusercontent.com/Afihacked/E-Absensi/master/app/src/main/res/mipmap-xxhdpi/ic_launcher.webp" width="180" height="180" />
</p><p align="center">
  <b>Sistem absensi berbasis foto dengan verifikasi keamanan, watermark terenkripsi, Photo Code, dan validasi Firebase.</b>
</p><p align="center">
  <a href="https://github.com/Afihacked/E-Absensi/releases">
    <img src="https://img.shields.io/github/v/release/Afihacked/E-Absensi?color=brightgreen&label=versi" />
  </a>
  <a href="https://github.com/Afihacked/E-Absensi/issues">
    <img src="https://img.shields.io/github/issues/Afihacked/E-Absensi?color=yellow" />
  </a>
  <a href="https://github.com/Afihacked/E-Absensi/stargazers">
    <img src="https://img.shields.io/github/stars/Afihacked/E-Absensi?color=orange" />
  </a>
  <img src="https://img.shields.io/badge/Made%20with-Kotlin-blue?logo=kotlin" />
  <img src="https://img.shields.io/github/license/Afihacked/E-Absensi?color=blue" />
</p><p align="center">
  <a href="https://github.com/Afihacked/E-Absensi/releases/latest">
    <img src="https://img.shields.io/badge/⬇️_Download-APK-blue?style=for-the-badge&logo=android" />
  </a>
</p>
---

🧩 Tentang Aplikasi

Afitech E-Absensi adalah aplikasi Android untuk sistem absensi modern berbasis foto yang dilengkapi watermark dinamis, Photo Code terenkripsi, serta validasi keamanan langsung ke Firebase untuk mencegah manipulasi gambar.

🎯 Fokus utama:

Foto absensi aman

Anti manipulasi

Validasi real-time

UI modern & profesional

Siap dipakai produksi



---

✨ Fitur Utama

Kategori	Deskripsi

📸 Absensi Foto	Ambil foto kamera/galeri dengan watermark otomatis
🕒 Watermark Dinamis	Nama, tanggal, waktu, alamat & koordinat otomatis
🔐 Photo Code Security	Kode unik tersimpan di EXIF metadata
🛡 Validasi Firebase	Cek keaslian foto langsung ke database
🧬 Anti Duplikat	Hash gambar untuk deteksi foto lama
📍 Smart Location	Alamat custom ↔ koordinat otomatis sinkron
👤 Sistem Akun	Email login + Google Sign-In
🧑‍💼 Profil Profesional	Edit nama inline + avatar Google/custom
🧾 Riwayat Absensi	Data realtime, urut terbaru
🔍 Verifikasi Foto	Cek nama, lokasi, waktu, koordinat & status



---

🧱 Arsitektur Proyek

com.afitech.absensi
 ├─ data/
 │   ├─ firebase/      → Repository Firebase
 │   └─ model/         → Absensi, UserProfile, UserSettings
 │
 ├─ ui/
 │   ├─ auth/          → Login, Register, Splash
 │   ├─ home/          → Home & History
 │   ├─ profile/       → Profil pengguna & avatar
 │   ├─ settings/      → Pengaturan watermark
 │   └─ verify/        → Verifikasi foto absensi


---

⚙️ Teknologi yang Digunakan

Komponen	Library

Bahasa	Kotlin
UI	Material 3
Navigasi	Navigation Component
Database	Cloud Firestore
Auth	Firebase Auth
Storage	Firebase Storage
Metadata	EXIF Interface
Gambar	Glide
Animasi	Lottie



---

🔒 Sistem Keamanan

🔐 Photo Code terenkripsi

📷 Metadata EXIF validasi

🧬 Hash gambar anti manipulasi

🛡 Cegah reuse foto lama

🔍 Verifikasi langsung ke Firebase

👤 Firestore rules berbasis UID



---

🧑‍💻 Developer

Afitech Team
Sistem absensi modern, aman, dan siap produksi.


---

⭐ Dukungan

Jika proyek ini membantu, beri ⭐ di repository ya!
