# M2: Tanam, siram, dan pertumbuhan (minggu 2) — F-05, F-06, F-07

Baca: `AGENTS.md`, `docs/SPEC.md` bagian 6, 7 (alat), 8 langkah 3 dan 4, 11 (hotbar), 15.

Tugas:
1. Buat model data `core/Models.kt` sesuai SPEC bagian 6 dan `GameState` awal (48 petak, uang awal).
2. Inventory dan hotbar 6 slot (ikon dari `ikon.png`): pilih dengan tombol 1 sampai 6 atau scroll. Slot 6 (biji) bergilir dengan Tab di antara biji yang dimiliki. Sementara belum ada toko, beri 5 biji tiap jenis lewat tombol debug F1 (tambah uang tetap jalan, tambahkan juga biji dummy bila `debug.aktif`).
3. Cangkul: petak jadi DIOLAH (tile 4). Penyiram: petak jadi basah (tile 5, `disiramHariIni`). Biji: tanam di petak diolah dan kosong.
4. Gambar tanaman dari `crops.png` sesuai rumus tahap gambar (SPEC bagian 6). Gambar ulang petak sesuai keadaan.
5. Implementasikan di `DaySimulator.tickHari` hanya langkah 3 (pertumbuhan), 4 (keringkan tanah), dan 8 (hari naik, jam reset). Hubungkan ke pergantian hari milestone sebelumnya. Tanaman hanya tumbuh bila disiram pada hari itu.
6. Suara cangkul dan siram dari `sfx/` (boleh ditunda ke M5, tetapi pasang bila mudah).
7. Unit test: SPEC bagian 15 nomor 1 dan 2, ditambah test rumus tahap gambar.

Selesai bila: cangkul, tanam, siram bekerja; tanaman berpindah tahap tiap hari bila disiram dan diam bila tidak; tanaman matang tampil dengan gambar tahap 3; test lulus.
Jangan mengerjakan: panen, toko, hama.
