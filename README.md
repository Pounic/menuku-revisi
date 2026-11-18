# MenuKu Revisi

Aplikasi Android untuk mengelola resep, stok dapur, jadwal makan, dan daftar belanja.

## Perubahan

- Stok bahan memakai `PantryViewModel`, halaman geser per lokasi, dan tanggal kedaluwarsa sebenarnya.
- Stok, lokasi simpan, jadwal, daftar belanja, serta pilihan pengingat disimpan pada Cloud Firestore per pengguna.
- Login dan daftar memakai Firebase Authentication. `Ingat Saya` mempertahankan sesi setelah proses aplikasi dimulai ulang; tanpa centang, pengguna masuk kembali pada awal proses berikutnya. Password tidak disimpan dalam preferensi aplikasi.
- Lupa sandi mengirim tautan pemulihan melalui Firebase Authentication; pengguna memeriksa inbox dan spam.
- Pengingat lokal untuk stok (dua hari sebelum tanggal kedaluwarsa, pukul 09.00 waktu perangkat) dan waktu persiapan menu yang dipilih.
- Analisis biaya menggunakan Gemini melalui Firebase AI Logic. Estimasi AI, rumus perkiraan, dan input manual dibedakan. Tidak ada klaim harga pasar real-time.

## Menjalankan di Android Studio

1. Buka folder ini melalui **File > Open**.
2. Gunakan SDK Android 36.1 dan versi Android Studio yang mendukung AGP proyek.
3. Daftarkan aplikasi Android dengan package `com.pounic.menuku` di Firebase milik Anda.
4. Unduh konfigurasi Firebase dan letakkan sebagai `app/google-services.json`. File konfigurasi lokal tidak disertakan dalam Git.
5. Aktifkan **Authentication > Email/Password**.
6. Buat **Cloud Firestore**, lalu terapkan aturan dari `firestore.rules`. Setiap pengguna mengakses dokumen di `users/{uid}` miliknya sendiri.
7. Aktifkan **Firebase AI Logic**, pilih Gemini Developer API, dan konfigurasi App Check sesuai petunjuk Console. Model diatur pada `PRICE_AI_MODEL` dalam `app/build.gradle.kts`; sesuaikan dengan model yang tersedia pada proyek.
   Pada build debug, daftarkan token Debug App Check dari Logcat di Firebase Console > App Check. Token pengujian tidak dimasukkan ke Git. Build rilis memerlukan provider App Check produksi seperti Play Integrity.
8. Lakukan Gradle Sync lalu jalankan `app`. Daftar dengan email milik Anda. Akun contoh pada versi awal tidak disertakan sebagai akun login.
9. Izinkan notifikasi melalui halaman Pengingat Dapur. Pengingat Android dapat terlambat karena penghematan baterai, dan tidak berjalan setelah aplikasi di-*force stop* sampai aplikasi dibuka lagi.

## Struktur data Firestore

- `users/{uid}`: profil dan preferensi pengingat.
- `users/{uid}/inventory/{id}`: stok; `expiryEpochDay` adalah tanggal kalender yang dikodekan dalam jumlah hari sejak 1 Januari 1970 UTC.
- `users/{uid}/storage_locations/{id}`: lokasi simpan tambahan.
- `users/{uid}/schedules/{id}`: tanggal, slot makan, referensi resep, dan waktu pengingat.
- `users/{uid}/shopping_items/{id}`: daftar belanja dan sumber estimasi harga.

Data resep bawaan dipakai sebagai katalog awal. Fitur jurnal dan koleksi yang tidak disebutkan di atas masih mengikuti penyimpanan sementara versi awal dan perlu integrasi terpisah.

## Pengujian

Jalankan dengan JDK yang disertakan Android Studio:

```text
gradlew.bat testDebugUnitTest --tests com.example.StockDatesTest --tests com.example.CloudDocumentsTest --tests com.example.ReminderPlannerTest --tests com.example.PriceAnalysisTest --tests com.example.ReminderSchedulerTest --tests com.example.SessionPreferencesTest
gradlew.bat assembleDebug
```

Tes memeriksa tanggal dan zona waktu, pemetaan dokumen stok/belanja/jadwal, pilihan pengingat, serta validasi rentang harga AI. Pengujian kode lokal tidak menggantikan pengujian layanan Firebase dan perangkat nyata.

Untuk memeriksa integrasi, buat dua akun uji, tambah/edit/hapus stok, mulai ulang aplikasi, periksa pemisahan data akun, uji centang Ingat Saya, dan atur satu pengingat beberapa menit ke depan.
