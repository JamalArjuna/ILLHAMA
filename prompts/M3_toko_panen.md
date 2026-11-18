# M3: Toko, panen, shipping bin, dan ekonomi (minggu 3) — F-04, F-11, F-12

Baca: `AGENTS.md`, `docs/SPEC.md` bagian 7 (tangan/objek dan toko), 8 langkah 1, 9, 11 (panel), 15.

Tugas:
1. ShopPanel (memakai `panel.png`): daftar dari `items.json.urutanDiToko`, navigasi Naik/Turun, Enter membeli satu unit, Esc menutup. Tampilkan ikon, nama, harga, jumlah dimiliki, dan uang pemain. Pembelian ditolak dengan toast `uangKurang`. Waktu berhenti selama panel terbuka.
2. Hapus biji dummy dari M2; uang awal 500 dan tanpa biji (pemain harus ke toko).
3. Panen (E pada tanaman matang): hitung kualitas dari kesehatan (SPEC bagian 9; kimia belum ada), masukkan ke `inventory.hasil`, petak kembali DIOLAH dan kosong. Bersihkan tanaman mati (E).
4. Shipping bin (E): pindahkan semua hasil ke bin. Pada `tickHari` langkah 1: jual isi bin, uang bertambah, bin dikosongkan; simpan `pendapatan` untuk ringkasan nanti.
5. Hotbar menampilkan jumlah biji dan bahan. HUD menampilkan uang.
6. Unit test: SPEC bagian 15 nomor 7 dan 9.

Selesai bila: alur beli biji -> tanam -> siram -> tunggu -> panen -> masukkan ke bin -> pergantian hari -> uang bertambah bisa dimainkan dari awal sampai akhir tanpa debug tombol; harga sesuai tabel; test lulus.
Jangan mengerjakan: hama, Journal.
