# M5: Ringkasan, tutorial, save, layar akhir, audio, dan polish (minggu 5) — F-14, F-15, F-16, F-17, F-18

Baca: `AGENTS.md`, `docs/SPEC.md` bagian 8 langkah 9, 9 (rating), 10, 11, 12, 13, 15.

Tugas:
1. Panel tidur: konfirmasi `tidur.konfirmasi` (Ya/Belum) di pintu rumah.
2. Ringkasan harian saat pagi (SummaryPanel): pendapatan kemarin, jumlah hama yang hilang, jumlah tanaman mati; Enter untuk lanjut. Kumpulkan datanya di `tickHari`.
3. Tutorial hari 1 dari `texts_id.json.tutorial` dan petunjuk kejadian `hama_pertama` dan `matang_pertama`. Kondisi `selesaiKetika` dipicu dari kejadian permainan.
4. Simpan dan muat sesuai SPEC bagian 10, tombol Lanjutkan di TitleScene, tangani save rusak tanpa crash. Unit test save/load (nomor 12).
5. Layar akhir (EndScene): uang, journal terbuka, rating bintang (ikon 8 dan 9), pesan sesuai `texts_id.json.akhir.pesan`, tombol main lagi/menu utama. Hapus save saat tamat. Unit test rating (nomor 8).
6. Audio: pasang `sfx_*.wav` (cangkul, siram, panen, beli, jual, gagal) pada aksi terkait dan `bgm_ladang.wav` berulang (loop) di GameScene dengan volume rendah. Tambah pilihan mute (tombol M).
7. Layar Kredit sederhana dari `docs/CREDITS.md`.
8. Polish: fade antar scene bila murah, animasi kecil pada toast, pastikan tidak ada teks terpotong.
9. Jika pemilik proyek sudah menaruh aset pengganti di `assets/`, pakai tanpa mengubah nama berkas.

Selesai bila: seluruh fitur Must dan Should di PRD berfungsi, save/load bekerja, game bisa dimainkan dari menu sampai layar akhir.
