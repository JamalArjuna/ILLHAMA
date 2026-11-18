# AGENTS.md (aturan kerja untuk AI agent)

Proyek: game edukasi farming dan pengenalan hama Indonesia, 2D top-down, **Kotlin + KorGE, hanya desktop (JVM)**, seluruh teks Bahasa Indonesia.

## Baca dulu, urutan ini
1. `docs/PRD.md` (apa dan kenapa)
2. `docs/SPEC.md` (aturan persis; menang bila berbeda dengan PRD)
3. `data/*.json` dan `map/map.json` (sumber angka, teks, dan peta)
4. Prompt milestone yang sedang dikerjakan di `prompts/`

## Versi terkunci (isi oleh pemilik proyek)
- KorGE: `6.0.0`  (plugin id: `com.soywiz.korge`)
- Kotlin: `2.0.20`   Gradle: `8.8`   JDK: `21.0.4x`
- Parser JSON yang dipakai: `Kotlinx Serialization JSON`
Jangan mengubah versi apa pun tanpa izin tertulis dari pemilik proyek. Jika build gagal karena versi, **berhenti dan laporkan**.

## Aturan wajib
1. **Satu milestone per sesi.** Jangan mengerjakan fitur milestone lain, jangan menambah fitur di luar PRD (NPC, crafting, ternak, online, dan sebagainya).
2. **Jangan menebak API KorGE.** Cek dokumentasi `docs.korge.org` dan kode library yang terunduh untuk versi terpasang. Bila ragu, tulis percobaan kecil dan jalankan.
3. **Bangun dan jalankan setelah setiap perubahan bermakna** (`./gradlew runJvm`). Perbaiki error kompilasi sebelum lanjut. Jangan menyatakan selesai sebelum game benar-benar dijalankan dan dicoba.
4. **Logika di `core/` tanpa import KorGE**, tampilan di `view/` dan `scenes/`. Random harus bisa disuntik dengan seed.
5. **Tidak ada angka permainan atau teks tampilan yang ditulis langsung di kode.** Semua dari `data/constants.json`, `crops.json`, `pests.json`, `items.json`, `texts_id.json`.
6. Nama identifier kode memakai Bahasa Inggris atau mengikuti `SPEC.md`; teks yang dilihat pemain Bahasa Indonesia.
7. Tulis unit test untuk logika `core/` sesuai `SPEC.md` bagian 15 sejalan dengan milestone yang menyentuhnya.
8. Gunakan nearest-neighbor (tanpa smoothing) untuk semua gambar piksel.
9. Commit kecil dan sering dengan pesan jelas. Satu perubahan logis, satu commit.
10. Bila SPEC ambigu atau bertentangan, **tanyakan**, jangan mengarang. Catat keputusan baru di `docs/KEPUTUSAN.md`.
11. Jangan menghapus atau menimpa file di `assets/` dan `data/` tanpa alasan; aset bisa diganti pemilik proyek (nama file dan ukuran sel harus tetap).
12. Jangan menambah dependensi baru tanpa izin.

## Cara melapor di akhir sesi
- Daftar fitur (ID F-xx) yang selesai dan cara mencobanya (langkah singkat).
- Hasil `./gradlew` build dan test (lulus/gagal).
- Hal yang belum selesai atau ragu-ragu.
- Keputusan yang dibuat (juga dicatat di `docs/KEPUTUSAN.md`).
