# PRD – Game Edukasi Farming & Pengenalan Hama Indonesia

Nov 18, 2026 · @jamal

## Ringkasan Produk

Game 2D top-down bergaya Stardew Valley, di mana pemain bertani sambil belajar mengenali dan mengendalikan hama tanaman di Indonesia. Alur utamanya: beli biji → tanam → siram, rawat, dan basmi hama → panen → taruh ke shipping bin. Game dibuat sendiri dengan KorGE (Kotlin) dan sengaja dibuat kecil agar selesai tepat waktu.

| Item | Keterangan |
| --- | --- |
| Genre | Farming sim edukasi, single-player |
| Sudut pandang | 2D top-down, berbasis tile |
| Engine & bahasa | KorGE, Kotlin |
| Platform target | Desktop (JVM) saja, tanpa Android atau web |
| Fokus edukasi | Mengenali hama, gejala, dan cara pengendaliannya |
| Durasi satu sesi | 1 musim = 14 hari game, sekitar 28 menit nyata |
| Status dokumen | Draf 1, 18 November 2026 |

## Latar Belakang & Masalah

Hama adalah ancaman utama bagi hasil panen di Indonesia, dan salah mengenali hama dapat berujung pada salah penanganan. Materi pengenalan hama biasanya berupa buku atau poster yang statis, sehingga kurang menarik bagi pelajar.

Game farming cocok sebagai medianya karena siklus tanam-panen sudah familier dan konsekuensinya langsung terasa: hama yang dibiarkan merusak panen dan mengurangi uang. Pemain belajar lewat tindakan, yaitu memeriksa gejala, mengenali hama, lalu memilih cara pengendalian.

Penanganan di game mengikuti prinsip Pengendalian Hama Terpadu (PHT): pantau rutin, utamakan cara fisik dan hayati, pakai pestisida sebagai pilihan terakhir. Prinsip ini menjadi dasar penilaian "metode yang benar" di game.

## Tujuan & Metrik Keberhasilan

Game dianggap berhasil jika pemain bisa menamatkan satu musim dan mampu mengenali keenam hama setelah bermain. Angka target di bawah adalah usulan awal.

| Tujuan | Ukuran keberhasilan | Cara ukur |
| --- | --- | --- |
| Alur inti berjalan | Siklus beli → jual selesai tanpa bug yang menghentikan permainan | Uji manual 5 kali main penuh |
| Edukasi hama | ≥ 70% playtester menjawab benar ≥ 4 dari 6 pertanyaan pengenalan hama | Kuis singkat di luar game setelah bermain |
| Mudah dipahami | ≥ 80% playtester menyelesaikan siklus tanam pertama tanpa bantuan | Observasi saat playtest |
| Stabil | ≥ 60 FPS dan tidak crash selama 14 hari game | Uji di laptop spek menengah |
| Selesai tepat waktu | Semua fitur prioritas Must selesai sebelum tahap polishing | Checklist milestone |

## Target Pengguna

Pengguna utama adalah pelajar dan mahasiswa berusia 13 tahun ke atas yang belum mengenal hama tanaman, serta penguji atau dosen yang menilai luaran.

| Persona | Kebutuhan | Implikasi desain |
| --- | --- | --- |
| Pelajar SMP/SMA (13–18 tahun) | Belajar santai tanpa teks panjang | Info hama singkat dan visual yang jelas |
| Mahasiswa non-pertanian | Pengantar cepat tentang hama umum di Indonesia | Journal ringkas dengan nama lokal dan nama ilmiah |
| Penguji / dosen | Melihat konsep, alur, dan kualitas implementasi | Alur inti jelas, build mudah dijalankan |

## Ruang Lingkup

MVP dibatasi pada satu peta, tiga tanaman, dan enam hama; fitur lain sengaja ditunda.

**Masuk cakupan**

- 1 peta lahan: area tanam 8×6 petak, toko biji, shipping bin, dan tempat tidur
- 3 tanaman: padi, jagung, cabai
- 6 hama, dua untuk tiap tanaman
- 5 alat: cangkul, penyiram, kaca pembesar, penyemprot, perangkap (ditambah tangan kosong untuk panen dan mengambil hama)
- Toko (hanya beli: biji, pestisida nabati, perangkap, dan pestisida kimia), shipping bin (hanya jual)
- Pest Journal, ringkasan harian, tutorial singkat, simpan dan muat

**Di luar cakupan**

- NPC, dialog, romansa, dan quest
- Crafting, ternak, mancing, mining
- Cuaca dinamis dan pergantian musim
- Multiplayer atau fitur online
- Kuis di dalam game, cutscene, monetisasi

## Game Loop & Alur Utama

Pemain menjalani siklus beli biji, tanam, rawat, panen, lalu jual; hama muncul di tahap rawat dan harus diperiksa sebelum dibasmi.

&#91;embedded content: alur utama · 5 tahap, perawatan harian dirinci\]

Pemain tidak tahu jenis hama sebelum memeriksa gejalanya, dan metode yang salah hanya membuang bahan. Uang dari shipping bin dipakai membeli biji untuk siklus berikutnya.

## Kebutuhan Fungsional

MVP terdiri dari 18 fitur; 13 di antaranya prioritas Must dan wajib selesai. Must = wajib untuk MVP, Should = dikerjakan bila waktu ada, Could = opsional.

| ID | Kebutuhan | Prioritas | Kriteria penerimaan |
| --- | --- | --- | --- |
| F-01 | Gerak pemain 4 arah dengan tabrakan tile | Must | Pemain tidak menembus pagar, bangunan, atau air; gerak halus di 60 FPS |
| F-02 | Peta lahan 8×6 petak, toko, shipping bin, tempat tidur | Must | Semua area terjangkau dan berfungsi |
| F-03 | Jam dan hari game | Must | 1 hari = 2 menit nyata; pukul 22:00 pemain otomatis tidur dan hari berganti; bisa tidur lebih awal |
| F-04 | Toko biji | Must | Bisa beli 3 jenis biji; uang berkurang; pembelian ditolak bila uang kurang |
| F-05 | Inventory dan hotbar | Must | Item dipilih dengan tombol angka atau scroll; jumlah item akurat |
| F-06 | Mencangkul dan menanam | Must | Petak diolah dulu baru bisa ditanami; tanaman tampil di tahap 1 |
| F-07 | Menyiram dan pertumbuhan | Must | Tanaman maju satu tahap per hari hanya jika disiram pada hari itu |
| F-08 | Kemunculan hama harian | Must | Tiap pagi tanaman berpeluang terkena hama yang sesuai jenisnya |
| F-09 | Memeriksa hama | Must | Kaca pembesar pada tanaman berhama menampilkan gejala dan membuka entri di Journal |
| F-10 | Pengendalian hama | Must | Metode benar menghilangkan hama; metode salah tidak berefek dan memberi petunjuk |
| F-11 | Panen dan kualitas | Must | Panen di tahap akhir; kualitas Baik/Biasa/Rusak mengikuti kesehatan tanaman |
| F-12 | Shipping bin | Must | Isi bin terjual saat hari berganti dan uang bertambah |
| F-13 | Pest Journal | Must | 6 entri, terkunci sampai hama pernah diperiksa; berisi ciri, gejala, dan cara kendali |
| F-14 | Ringkasan harian | Should | Tiap pagi menampilkan pendapatan kemarin, hama yang ditangani, dan tanaman rusak |
| F-15 | Tutorial singkat | Should | Petunjuk hari 1 sampai pemain menanam dan menyiram pertama kali |
| F-16 | Simpan dan muat | Should | Progres tersimpan saat tidur dan terbaca saat game dibuka |
| F-17 | Layar akhir | Should | Setelah hari 14 menampilkan uang, Journal terisi, dan rating |
| F-18 | Musik dan efek suara | Could | Minimal efek untuk cangkul, siram, panen, dan beli |

## Sistem Hama & Konten Edukasi

Enam hama dipilih agar tiap tanaman punya dua hama dengan gejala dan cara kendali yang berbeda, sehingga pemain harus memeriksa dulu sebelum bertindak. Isi tabel ditulis dari pengetahuan umum dan disederhanakan untuk gameplay; verifikasi ke sumber resmi (mis. Balitbangtan/Kementan, BPTP, atau jurnal entomologi) sebelum dipakai final.

| Tanaman | Hama | Nama ilmiah | Gejala yang ditampilkan | Metode benar di game |
| --- | --- | --- | --- | --- |
| Padi | Wereng cokelat | Nilaparvata lugens | Daun menguning dan mengering dari pangkal, rumpun tampak terbakar | Pestisida nabati |
| Padi | Tikus sawah | Rattus argentiventer | Batang terpotong rapi, ada lubang sarang di pematang | Perangkap |
| Jagung | Ulat grayak jagung | Spodoptera frugiperda | Daun berlubang, ada serbuk seperti kotoran di pucuk | Ambil manual |
| Jagung | Belalang | Locusta migratoria | Tepi daun habis dimakan, daun compang-camping | Ambil manual |
| Cabai | Kutu daun | Aphis gossypii | Daun keriting, ada lapisan lengket dan semut | Pestisida nabati |
| Cabai | Lalat buah | Bactrocera dorsalis | Buah busuk dan berbelatung, buah mudah rontok | Perangkap |

**Aturan kemunculan dan dampak** (angka awal, di-tuning saat playtest)

- Tiap pagi, tanaman tahap 2 ke atas punya peluang 20% terkena salah satu hama sesuai jenis tanamannya.
- Hama yang dibiarkan menyebar ke satu petak tetangga dengan peluang 30% per hari.
- Kesehatan tanaman 100; turun 25 per hari selama hama belum ditangani; tanaman mati di 0.
- Kualitas panen: kesehatan ≥ 75 = Baik (×1,0), 40–74 = Biasa (×0,6), di bawah 40 = Rusak (×0,3).

**Metode pengendalian**

| Metode | Alat | Biaya | Hasil bila benar | Hasil bila salah |
| --- | --- | --- | --- | --- |
| Ambil manual | Tangan kosong | Gratis | Hama hilang saat itu juga | Tidak berhasil, pemain mendapat petunjuk |
| Pestisida nabati | Penyemprot | Rp 15 per semprot | Hama hilang keesokan pagi | Bahan terbuang |
| Perangkap | Perangkap dipasang di petak | Rp 40 per unit | Hama tertangkap keesokan pagi | Perangkap terbuang |
| Pestisida kimia | Penyemprot | Rp 60 per semprot | Hama hilang langsung pada semua jenis | Kualitas panen petak itu maksimal Biasa |

Pestisida kimia selalu berhasil tetapi mahal dan menurunkan kualitas, sehingga pemain terdorong mengenali hama dan memilih cara yang tepat lebih dulu, sesuai prinsip PHT.

## Ekonomi & Balancing

Pemain mulai dengan Rp 500 dan punya 14 hari untuk mengumpulkan uang sebanyak mungkin. Semua angka di bawah adalah nilai awal yang akan disesuaikan setelah playtest.

| Tanaman | Harga biji | Lama tumbuh | Harga jual (kualitas Baik) |
| --- | --- | --- | --- |
| Padi | Rp 20 | 5 hari | Rp 60 |
| Jagung | Rp 30 | 6 hari | Rp 90 |
| Cabai | Rp 50 | 7 hari | Rp 160 |

Cabai paling menguntungkan tetapi tumbuh paling lama, dan kedua hamanya butuh bahan atau perangkap berbayar; hama jagung bisa diatasi gratis dengan tangan. Dengan begitu untung dan biaya perawatan seimbang.

Rating akhir: 1 bintang bila uang ≥ Rp 1.500, 2 bintang ≥ Rp 3.000, 3 bintang ≥ Rp 4.500 dan seluruh 6 entri Journal terbuka.

## UI/UX & Kontrol

Antarmuka dibuat seminimal mungkin agar pemain fokus pada lahan, dengan semua informasi hama disampaikan lewat ikon dan satu panel Journal.

**HUD di layar game**

- Pojok kanan atas: hari, jam, dan uang
- Bawah tengah: hotbar 6 slot (cangkul, penyiram, kaca pembesar, penyemprot, perangkap, biji terpilih)
- Di atas tanaman: ikon tanda seru kecil bila ada hama, tanpa menyebut jenisnya sampai diperiksa
- Notifikasi singkat (toast) untuk umpan balik, misalnya "Metode kurang tepat, periksa gejalanya lagi"

**Kontrol (keyboard, desktop)**

| Aksi | Tombol |
| --- | --- |
| Bergerak | W A S D atau tombol panah |
| Pakai alat yang dipilih / tanam | Spasi |
| Tangan: panen, ambil hama, toko, tidur, shipping bin | E |
| Ganti slot hotbar | Angka 1–6 atau scroll mouse |
| Buka Pest Journal | J |
| Jeda dan menu | Esc |

**Pest Journal**

Panel dua kolom: daftar enam hama di kiri, detail di kanan (gambar, nama lokal dan ilmiah, tanaman yang diserang, gejala, cara kendali). Entri terkunci dan tampil sebagai siluet sampai hama pertama kali diperiksa dengan kaca pembesar.

**Alur layar**

1. Layar judul (Mulai, Lanjutkan, Keluar)
2. Layar game, dengan Journal dan menu jeda sebagai panel di atasnya
3. Ringkasan harian saat pagi tiba
4. Layar akhir setelah hari ke-14

## Arsitektur Teknis (KorGE/Kotlin)

Game dibangun sebagai proyek KorGE dengan Kotlin, dibagi menjadi empat lapis supaya logika, tampilan, dan data tidak saling bercampur.

&#91;embedded content: arsitektur · 4 lapis\]

Semua lapis membaca data yang sama, sehingga harga, lama tumbuh, dan peluang hama bisa diubah tanpa menyentuh kode sistem.

**Catatan teknis**

- Target hanya JVM desktop, jadi tidak perlu mengurus target JS atau Android.
- Tiap layar adalah satu Scene, dan perpindahan layar lewat SceneContainer.
- Peta satu layar (30×17 tile, tanpa kamera) disimpan di map.json dan digambar dengan kode sendiri; file Tiled (TMX) hanya untuk menyunting. Tabrakan dan jarak interaksi dihitung per petak, bukan dengan fisika.
- Data tanaman, hama, dan harga disimpan terpisah dari kode (file JSON atau objek Kotlin).
- Progres disimpan sebagai JSON ke penyimpanan lokal bawaan KorGE.
- Nama paket dan beberapa API KorGE berbeda antarversi; cek dokumentasi versi yang dipakai sebelum mulai.

## Aset & Audio

Semua aset berskala pixel art dengan tile 16×16 yang diperbesar 3× tanpa penghalusan agar tetap tajam. Semua aset memakai paket gratis berlisensi yang membolehkan pemakaian ulang (mis. CC0 atau CC-BY), dicari di itch.io, OpenGameArt, atau Kenney; nama pembuat, sumber, dan lisensi dicatat di README dan layar kredit. Sprite hama spesifik (wereng, lalat buah) jarang tersedia, jadi pakai serangga generik atau gambar sederhana buatan sendiri, lalu tampilkan nama hama lewat Journal.

| Kategori | Kebutuhan | Jumlah |
| --- | --- | --- |
| Tileset | Rumput, tanah kering, tanah basah, jalan, pagar, air, bangunan | 1 tileset, peta dibuat di Tiled |
| Pemain | Berjalan 4 arah, animasi pakai alat | 4 arah × 4 frame |
| Tanaman | Padi, jagung, cabai, masing-masing 4 tahap tumbuh ditambah layu | 15 sprite |
| Hama | Sprite kecil di atas tanaman, ditambah ilustrasi untuk Journal | 6 sprite + 6 ilustrasi |
| UI | Panel, hotbar, ikon alat dan item, ikon peringatan | sekitar 20 elemen |
| Audio | Efek cangkul, siram, panen, beli, jual, gagal; satu musik latar | 6 efek + 1 musik |

## Rencana Pengembangan

Pengembangan dibagi enam minggu per kelompok fitur, dengan semua fitur Must selesai di akhir minggu ke-4. Tenggat sebenarnya belum diketahui, jadi jadwal ini bisa digeser sesuai tanggal penyerahan.

&#91;embedded content: jadwal · 6 minggu, 1 milestone\]

Fitur Should dan Could dikerjakan di minggu ke-5, dan minggu ke-6 dicadangkan untuk uji dan perbaikan.

## Pengujian & Kriteria Penerimaan

Game dinyatakan selesai bila seluruh fitur Must lulus uji, tidak ada crash dalam satu musim penuh, dan konten hama sudah diverifikasi.

| Jenis uji | Cara | Lulus bila |
| --- | --- | --- |
| Fungsional | Checklist F-01 sampai F-18 | Semua fitur Must lulus |
| Alur penuh | Main 14 hari sebanyak 5 kali | Tidak ada crash atau kondisi macet |
| Playtest | 5 orang yang belum pernah melihat game | Sesuai target metrik di bagian Tujuan |
| Performa | Laptop spek menengah | ≥ 60 FPS stabil |
| Konten | Periksa tabel hama dengan sumber resmi atau dosen | Semua gejala dan metode terverifikasi |

Build dianggap siap diserahkan bila bisa dijalankan dari petunjuk di README oleh orang lain.

## Risiko, Asumsi & Pertanyaan Terbuka

Risiko terbesar adalah cakupan yang melebar dan akurasi konten hama.

| Risiko | Dampak | Mitigasi |
| --- | --- | --- |
| Cakupan melebar | Game tidak selesai tepat waktu | Kunci cakupan di PRD ini; fitur baru masuk daftar "nanti" |
| Konten hama keliru | Edukasi menyesatkan | Verifikasi ke sumber resmi atau dosen sebelum final |
| Aset terlambat | Tahap polishing mundur | Mulai dengan placeholder berbentuk kotak, ganti aset belakangan |
| Kurva belajar KorGE | Fitur inti lambat dibangun | Bangun prototipe gerak dan tile di minggu pertama |
| Ekonomi tidak seimbang | Terlalu mudah atau terlalu sulit | Simpan angka di satu file data agar mudah diubah |

**Asumsi**

- Platform hanya desktop (JVM); tidak ada Android atau web
- Tile 16×16, satu hari game = 2 menit nyata, satu musim = 14 hari; seluruh teks game berbahasa Indonesia
- Semua angka ekonomi dan peluang hama bersifat sementara

**Pertanyaan terbuka**

- [ ] Berapa tenggat luaran, dan apakah yang dinilai game saja atau juga laporan?
- [ ] Adakah dosen atau penyuluh pertanian yang bisa memverifikasi konten hama?
