# M4: Sistem hama dan Pest Journal (minggu 4) — F-08, F-09, F-10, F-13

Baca: `AGENTS.md`, `docs/SPEC.md` bagian 7 (seluruh tabel alat), 8 (semua langkah), 9, 11 (ikon, panel, Journal), 12, 15, dan `data/pests.json`.

Tugas:
1. Lengkapi `DaySimulator.tickHari` dengan langkah 2, 5, 6, 7 persis sesuai urutan SPEC bagian 8, memakai `Random` yang disuntik.
2. Ikon di atas tanaman: tanda seru bila hama belum diperiksa; sprite hama dari `pests.png` setelah diperiksa.
3. Kaca pembesar: buka panel pemeriksaan (nama hama, nama ilmiah, gejala dari `pests.json`), buka entri Journal, toast `journalBaru` bila baru. Pada tanaman sehat tampilkan kesehatan.
4. Pengendalian: penyemprot (Q ganti mode Nabati/Kimia), perangkap, dan tangan (E) sesuai tabel SPEC bagian 7, termasuk bahan terbuang dan toast `metodeSalah` bila salah metode. Penanganan nabati/perangkap benar bersifat tertunda (selesai di langkah 2 malam itu). Kimia langsung dan membatasi kualitas panen petak itu maksimal BIASA.
5. Panen: perbarui perhitungan kualitas dengan aturan kimia.
6. Pest Journal (tombol J): 6 entri, siluet dan teks `journal.terkunci` bila belum terbuka, detail lengkap dengan ilustrasi `jurnal_<id>.png` bila terbuka, penghitung `terbuka/total`.
7. Tombol debug F3 (paksa hama) dan F4 (matangkan semua) untuk menguji.
8. Unit test: SPEC bagian 15 nomor 3, 4, 5, 6, 10.

Selesai bila: hama muncul, menyebar, merusak, bisa diperiksa, dikendalikan dengan metode benar maupun salah, dan Journal terbuka sesuai pemeriksaan; semua test lulus.
Jangan mengerjakan: ringkasan harian, tutorial, save, layar akhir.
