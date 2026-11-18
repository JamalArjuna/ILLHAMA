# M1: Gerak pemain, interaksi objek, dan jam (minggu 1) — F-01, F-02, F-03

Baca: `AGENTS.md`, `docs/SPEC.md` bagian 2, 5, 7 (bagian objek), 8 (aturan waktu), 11 (tombol, HUD).

Tugas:
1. Pemain (`player.png`) bergerak 4 arah dengan WASD/panah, kecepatan dari `constants.json`, animasi jalan 4 frame per arah, menghadap arah terakhir. Tabrakan memakai hitbox kaki dan tile terblokir dari `map.json`. Tidak bisa keluar peta.
2. Pemain muncul di tile spawn. Hitung "petak sasaran" (tile di depan pemain) dan tampilkan `penanda_petak.png` bila berada di area lahan.
3. Objek interaksi: tekan E di depan pintu rumah, pintu toko, dan shipping bin menampilkan toast sementara (isi sungguhan menyusul di milestone lain). Pintu dan bin tetap terblokir.
4. Jam game: 120 detik nyata = 06:00 sampai 22:00. HUD kanan atas menampilkan `Hari x/14` dan `HH:MM`. Pada 22:00 panggil fungsi pergantian hari kosong (hari naik, jam kembali 06:00) dan akhiri musim setelah hari ke-14.
5. Toast dan HUD memakai teks dari `texts_id.json`. Tambah tombol debug F2 (lompat hari).
6. Unit test: konversi waktu (SPEC bagian 15 nomor 11).

Selesai bila: pemain berjalan mulus tanpa menembus bangunan/pagar/air/pohon, penanda petak muncul di lahan, jam berjalan dan hari berganti, HUD tampil. Tunjukkan hasilnya dengan menjalankan game.
Jangan mengerjakan: tanam, toko, hama.
