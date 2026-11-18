# SPEC Teknis: Game Edukasi Farming & Pengenalan Hama Indonesia

Dokumen ini melengkapi `docs/PRD.md`. PRD menjawab "apa dan kenapa"; SPEC ini menjawab "persisnya bagaimana".
Bila PRD dan SPEC berbeda, **SPEC yang menang**, kecuali PRD jelas lebih baru. Bila masih ambigu, tanyakan, jangan menebak.

Semua angka permainan ada di `data/*.json`. Jangan menulis angka permainan langsung di kode Kotlin.

---

## 1. Stack dan setup

- Bahasa Kotlin, engine **KorGE**, target **hanya JVM desktop**.
- Informasi versi (diperiksa 18 Nov 2026): rilis resmi terakhir KorGE adalah 6.0.0 dengan plugin `com.soywiz.korge`. Sejak Mei 2026 KorGE pindah namespace ke `org.korge` (`org.korge.engine`, versi 7.0.0-SNAPSHOT), dan itu perubahan yang merusak. Gradle/AGP juga dinaikkan ke versi 9 di repo utama.
- **Aturan:** proyek dibuat dari template resmi (`korlibs/korge-hello-world` atau Starter Kit di KorGE Forge) oleh pemilik proyek dan **harus sudah bisa dijalankan** sebelum agent mulai. Agent **tidak boleh mengubah versi KorGE, Kotlin, Gradle, atau JDK** tanpa izin. Catat versi yang dipakai di `AGENTS.md` bagian "Versi terkunci".
- Template resmi bisa berusia lama dan memakai nama paket lama (`com.soywiz.*` atau `korlibs.*`). Gunakan nama paket yang **sesuai versi yang terpasang**. Rujuk dokumentasi `docs.korge.org` dan kode sumber library yang terunduh, jangan mengandalkan ingatan soal API.
- Jalankan dengan `./gradlew runJvm` (Windows: `gradlew.bat runJvm`). `runJvmAutoreload` boleh dipakai untuk iterasi cepat.
- Folder resource standar KorGE: `src/commonMain/resources/`. Salin `assets/` dan `data/` ke sana (struktur di bagian 3).

## 2. Resolusi dan tampilan

- Resolusi virtual **480 x 272** piksel, tile **16 x 16**, jadi peta **30 x 17** tile pas satu layar. **Tidak ada kamera/scrolling.**
- Ukuran jendela awal 3x (1440 x 816), skala piksel dengan **nearest-neighbor (tanpa smoothing)**. Pertahankan rasio saat jendela diubah.
- Gambar sprite sebagai bitmap dari sheet yang dipotong per tile (slice), jangan memuat ratusan file kecil.

## 3. Struktur proyek

```
src/commonMain/kotlin/
  Main.kt                      // titik masuk, daftar scene
  core/                        // LOGIKA MURNI Kotlin, tanpa import KorGE, bisa diuji unit
    Models.kt                  // data class (bagian 6)
    GameConfig.kt              // memuat constants.json
    Catalog.kt                 // memuat crops/pests/items/texts
    DaySimulator.kt            // pergantian hari (bagian 8)
    Economy.kt                 // harga, kualitas, rating
    Interaction.kt             // aturan alat x petak (bagian 7)
    SaveData.kt                // serialisasi save
  scenes/
    TitleScene.kt  GameScene.kt  EndScene.kt
  view/                        // semua yang menyentuh KorGE
    MapView.kt  PlayerView.kt  PlotsView.kt
    Hud.kt  Hotbar.kt  Toast.kt  ShopPanel.kt  JournalPanel.kt
    SummaryPanel.kt  InspectPanel.kt  ConfirmSleepPanel.kt  TutorialHint.kt
  audio/Sfx.kt
src/commonTest/kotlin/core/    // unit test logika di core/
src/commonMain/resources/
  data/  assets/art/  assets/sfx/  map/
```

Prinsip: **`core/` tidak boleh bergantung pada KorGE.** Tampilan hanya membaca state dari `core/` dan mengirim aksi ke sana. Generator angka acak harus dapat disuntik (`Random` dengan seed) agar logika bisa diuji.

Pemuatan JSON: pilih satu cara yang tersedia di versi KorGE terpasang (parser JSON bawaan korlibs atau `kotlinx.serialization`), pakai konsisten, dan catat pilihannya di `AGENTS.md`.

## 4. Konstanta

Lihat `data/constants.json` (waktu, kecepatan, peluang hama, ambang kualitas, rating, tombol debug).
Lihat `data/crops.json`, `data/pests.json`, `data/items.json`, `data/texts_id.json`.
Seluruh teks yang tampil di layar diambil dari `texts_id.json` (Bahasa Indonesia). Placeholder `{nama}` diganti saat runtime.

## 5. Peta dan tata letak

Sumber kebenaran: `map/map.json` (array `ground[baris][kolom]` berisi id tile 0 sampai 15, sesuai `assets/art/tileset.png` 4 kolom x 4 baris). `map/map.tmx` hanya untuk disunting di Tiled; **render peta dari `map.json` dengan kode sendiri** (loop tile, potong dari tileset) agar tidak bergantung pada API loader Tiled.

Tile terblokir (tidak bisa dilewati): id 6, 7, 8, 9, 10, 11, 12, 13, 14, 15.

| Objek | Tile | Berdiri di | Aksi (tombol E) |
| --- | --- | --- | --- |
| Pintu rumah | (5, 3) | (5, 4) | Dialog konfirmasi tidur |
| Pintu toko | (22, 4) | (22, 5) | Buka panel toko |
| Shipping bin | (13, 7) | (12, 7) | Masukkan semua hasil panen |

- Koordinat tile `(kolom, baris)`, mulai dari (0, 0) di kiri atas.
- Titik muncul pemain: tile (5, 5).
- **Lahan:** 8 x 6 = 48 petak, mulai tile (3, 7) sampai (10, 12). Pagar mengelilingi lahan; gerbang terbuka di tile (6, 6) dan (7, 6).
- Petak lahan digambar ulang sesuai keadaan: id 3 (belum diolah), 4 (diolah, kering), 5 (diolah, basah). Tanaman dan ikon hama digambar di atasnya.
- Area HUD menutupi sebagian baris 0 sampai 1 (kanan atas) dan baris 15 sampai 16 (hotbar di tengah bawah); tidak ada objek penting di sana.

## 6. Model data (Kotlin murni)

```kotlin
enum class Kualitas { BAIK, BIASA, RUSAK }
enum class Metode { MANUAL, NABATI, PERANGKAP, KIMIA }
enum class TanahState { BELUM_DIOLAH, DIOLAH }

data class Petak(
    val x: Int, val y: Int,                    // indeks lahan 0..7, 0..5
    var tanah: TanahState = TanahState.BELUM_DIOLAH,
    var disiramHariIni: Boolean = false,
    var tanaman: Tanaman? = null
)

data class Tanaman(
    val cropId: String,
    var hariTumbuh: Int = 0,                   // jumlah hari ia disiram
    var kesehatan: Int = 100,                  // 0..100
    var hamaId: String? = null,
    var hamaDiperiksa: Boolean = false,
    var penangananTertunda: Metode? = null,    // NABATI atau PERANGKAP, selesai besok pagi
    var pakaiKimia: Boolean = false,
    var mati: Boolean = false
)

data class Inventory(
    val biji: MutableMap<String, Int>,         // cropId -> jumlah
    var nabati: Int = 0, var kimia: Int = 0, var perangkap: Int = 0,
    val hasil: MutableMap<Pair<String, Kualitas>, Int>   // hasil panen
)

data class GameState(
    var hari: Int = 1,                         // 1..14
    var menitHari: Int = 6 * 60,               // menit sejak 00:00, mulai 06:00
    var uang: Int = 500,
    val inventory: Inventory,
    val bin: MutableMap<Pair<String, Kualitas>, Int>,
    val petak: List<Petak>,                    // 48 petak
    val journalTerbuka: MutableSet<String>,    // id hama yang pernah diperiksa
    var tutorialIndex: Int = 0,
    var modeSemprot: Metode = Metode.NABATI    // NABATI atau KIMIA
)
```

Tahap gambar tanaman (indeks kolom di `crops.png`, 0 sampai 3, 4 = layu):

```
jika mati                      -> 4
jika hariTumbuh >= hariTumbuh_crop -> 3   (matang, bisa dipanen)
selain itu                     -> min(2, floor(hariTumbuh * 3 / hariTumbuh_crop))
```

Baris `crops.png` = `spriteBaris` di `crops.json`.

## 7. Aturan interaksi

**Petak sasaran:** petak lahan di tile tepat di depan pemain (arah hadap terakhir). Dihitung dari titik tengah hitbox kaki pemain ditambah satu tile ke arah hadap. Gambar `penanda_petak.png` di petak sasaran bila berada di lahan.

**Spasi = pakai alat terpilih. E = tangan/objek.** Bila tile depan berisi objek (pintu/bin), E menjalankan objek; selain itu E bekerja di petak sasaran.

### Alat (Spasi)

| Alat | Kondisi petak | Hasil |
| --- | --- | --- |
| Cangkul | belum diolah | jadi DIOLAH; suara cangkul |
| Cangkul | sudah diolah | toast `sudahDiolah` |
| Penyiram | belum diolah | toast `tanahBelumDiolah` |
| Penyiram | diolah | `disiramHariIni = true`; suara siram |
| Kaca pembesar | ada tanaman berhama | buka panel pemeriksaan (gejala + nama hama), `hamaDiperiksa = true`, id hama masuk `journalTerbuka`, toast `journalBaru` bila baru |
| Kaca pembesar | ada tanaman sehat | toast `tanamanSehat` dengan nilai kesehatan |
| Biji (slot 6, Tab ganti jenis) | diolah dan kosong | tanam, kurangi 1 biji, kesehatan 100, hariTumbuh 0 |
| Biji | belum diolah | toast `perluDiolah` |
| Biji | terisi | toast `petakTerisi` |
| Penyemprot, mode NABATI (Q ganti mode) | ada hama | habis 1 nabati. Jika `metodeBenar == nabati`: `penangananTertunda = NABATI`, toast `nabatiDisemprot`. Jika salah: toast `metodeSalah`, suara gagal |
| Penyemprot, mode KIMIA | ada hama | habis 1 kimia. Hama hilang **langsung**, `pakaiKimia = true`, toast `kimiaDipakai` |
| Penyemprot | tanpa hama / stok 0 | toast `tidakAdaHama` atau `bahanHabis`, tidak ada bahan terpakai |
| Perangkap | ada hama | habis 1 perangkap. Jika `metodeBenar == perangkap`: `penangananTertunda = PERANGKAP`, toast `perangkapDipasang`. Jika salah: `metodeSalah`, suara gagal |
| Perangkap | tanpa hama / stok 0 | toast `tidakAdaHama` / `bahanHabis` |

Alat tidak berefek pada petak berisi tanaman mati kecuali kaca pembesar (tanaman mati: abaikan).

### Tangan dan objek (E)

| Sasaran | Hasil |
| --- | --- |
| Tanaman matang, hidup | panen: hitung kualitas (bagian 9), tambah ke `inventory.hasil`, petak kembali DIOLAH dan kosong, `disiramHariIni = false`, suara panen, toast `panenBerhasil` |
| Tanaman mati | bersihkan: petak kembali DIOLAH dan kosong, toast `petakDibersihkan` |
| Tanaman berhama, `metodeBenar == manual` | hama hilang langsung, toast `manualBerhasil` |
| Tanaman berhama, metode benar bukan manual | toast `metodeSalah`, suara gagal, tanpa biaya |
| Tanaman belum matang tanpa hama | toast `belumMatang` |
| Pintu toko | buka ShopPanel |
| Pintu rumah | ConfirmSleepPanel; jika "Ya" lakukan pergantian hari |
| Shipping bin | pindahkan semua `inventory.hasil` ke `bin`; toast `masukBin` atau `binKosong` |

Panen boleh dilakukan saat hama masih ada (kualitas mengikuti kesehatan).

### Toko

Daftar sesuai `items.json.urutanDiToko`. Navigasi Naik/Turun, **Enter** beli 1 unit, **Esc** tutup. Beli ditolak (toast `uangKurang`) bila uang kurang. Harga biji dari `crops.json`, harga bahan dari `items.json`. Setiap baris menampilkan ikon, nama, harga, dan jumlah yang dimiliki. Permainan berhenti (waktu tidak berjalan) selama panel terbuka.

## 8. Urutan pergantian hari (wajib persis)

Dipanggil saat pemain tidur (konfirmasi Ya) atau jam mencapai 22:00. Fungsi `DaySimulator.tickHari(state, random)` menjalankan **berurutan**:

1. **Jual isi bin**: `pendapatan = sum(hargaJual(crop) * pengali(kualitas) * jumlah)`, dibulatkan per item (`round`). Tambah ke `uang`, kosongkan `bin`. Simpan `pendapatan` untuk ringkasan.
2. **Selesaikan penanganan tertunda**: setiap tanaman dengan `penangananTertunda != null` kehilangan hama (`hamaId = null`, `hamaDiperiksa = false`, `penangananTertunda = null`).
3. **Pertumbuhan**: setiap tanaman hidup dengan petak `disiramHariIni == true` menambah `hariTumbuh` sebanyak 1.
4. **Keringkan tanah**: semua petak `disiramHariIni = false`.
5. **Dampak hama**: setiap tanaman hidup yang masih berhama kehilangan `kerusakanKesehatanPerHari` (25) kesehatan. Bila kesehatan `<= 0`: `mati = true`, `kesehatan = 0`, hama dihapus.
6. **Penyebaran**: ambil daftar petak berhama **sebelum langkah ini**. Untuk tiap petak itu, dengan peluang `peluangMenyebar` (0,30), pilih satu tetangga empat arah secara acak; hama pindah/menular hanya jika tetangga berisi tanaman hidup **dengan `cropId` sama** dan **tanpa hama**. Tetangga memperoleh jenis hama yang sama.
7. **Hama baru**: setiap tanaman hidup tanpa hama yang tahapnya `>= tahapMinimalMuncul` (1, yaitu tahap gambar kedua ke atas) dengan peluang `peluangMunculHarian` (0,20) mendapat satu hama acak dari `crop.hama`.
8. `hari += 1`, `menitHari = jamMulai * 60`.
9. Bila `hari > jumlahHariMusim` (14): masuk EndScene. Selain itu simpan game (bagian 10) dan tampilkan ringkasan harian.

Tanaman yang baru ditanam pada hari itu mengikuti langkah yang sama. Waktu berjalan: `detikNyataPerHari = 120` untuk rentang 06:00 sampai 22:00 (960 menit game), jadi **8 menit game per detik nyata**. Waktu berhenti saat panel terbuka. Pada 22:00 pergantian hari berjalan otomatis (tanpa hukuman).

Perilaku turunan yang harus benar:
- Tanaman tanpa hama dan terus disiram: tumbuh satu tahap kira-kira tiap hari, matang setelah `hariTumbuh_crop` hari.
- Hama yang tidak ditangani: kesehatan 100 -> 75 -> 50 -> 25 -> 0, jadi **mati pada pergantian hari keempat** setelah hama muncul.
- Penanganan nabati/perangkap yang benar dipasang pada hari H: hama hilang di langkah 2 malam itu, sehingga **tidak merusak** tanaman pada langkah 5.

## 9. Ekonomi, kualitas, rating

- Kualitas saat panen dari `kesehatan`: `>= 75` BAIK, `40..74` BIASA, `< 40` RUSAK. Jika `pakaiKimia == true`, kualitas dibatasi maksimal BIASA.
- Pengali harga: BAIK 1,0; BIASA 0,6; RUSAK 0,3. Harga jual item = `round(hargaJual * pengali)`.
- Uang awal 500. Tidak ada biji awal (pemain harus ke toko, sekaligus tutorial).
- Rating akhir: uang `< 1500` = 0 bintang; `>= 1500` = 1; `>= 3000` = 2; `>= 4500` **dan** journal terbuka 6 dari 6 = 3. Jika `>= 4500` tetapi journal belum penuh, hasilnya 2 bintang.
- Semua ambang ada di `constants.json`.

## 10. Simpan dan muat

- Satu slot, disimpan otomatis di langkah 9 pergantian hari, dan saat memilih "Lanjutkan" dimuat kembali.
- Format JSON, kunci penyimpanan `save_v1`, memakai penyimpanan lokal bawaan KorGE (cari API yang tersedia di versi terpasang). Isi: `versi`, `hari`, `menitHari`, `uang`, inventory, isi bin, 48 petak (tanah, siram, tanaman lengkap), `journalTerbuka`, `tutorialIndex`, `modeSemprot`.
- Tombol "Lanjutkan" di menu nonaktif bila tidak ada save atau musim sudah selesai. Hapus save saat musim tamat.
- Jika berkas save rusak atau versi tidak cocok, abaikan dan mulai baru tanpa crash.

## 11. UI

**Tombol:** WASD/panah gerak; Spasi alat; E tangan/objek; 1 sampai 6 pilih slot; Tab ganti jenis biji; Q ganti mode penyemprot (Nabati/Kimia); J Journal; Esc jeda atau tutup panel; Enter konfirmasi.

**HUD** (di atas peta): kanan atas `Hari x/14`, jam `HH:MM`, uang `Rp n`. Tengah bawah hotbar 6 slot (ikon dari `ikon.png`, slot dari `slot.png` dan `slot_terpilih.png`); slot bahan dan biji menampilkan jumlah; slot penyemprot menampilkan mode (Nabati/Kimia). Toast di atas hotbar selama `durasiToastDetik`.

**Ikon di atas tanaman:** hama ada dan belum diperiksa -> ikon tanda seru (indeks 6 di `ikon.png`); sudah diperiksa -> sprite hama dari `pests.png` (`spriteIndex`).

**Panel** memakai `panel.png` (9-slice, sudut 16 px). Panel: Toko, Journal, Pemeriksaan, Konfirmasi tidur, Ringkasan harian. Journal: daftar 6 hama di kiri; kanan menampilkan ilustrasi `jurnal_*.png`, nama, nama ilmiah, tanaman, ciri, gejala, cara kendali. Entri belum terbuka tampil sebagai siluet (gambar digelapkan) dan teks `journal.terkunci`.

**Layar:** TitleScene (Mulai Baru, Lanjutkan, Keluar) -> GameScene -> EndScene (uang, journal, rating bintang dari `ikon.png` indeks 8 dan 9, tombol main lagi).

**Tutorial hari 1:** urutan `texts_id.json.tutorial`; tampil sebagai kotak petunjuk di atas layar, maju ketika kondisi `selesaiKetika` terpenuhi. Petunjuk kejadian `hama_pertama` dan `matang_pertama` tampil sekali.

**Font:** gunakan font bawaan KorGE dulu; ganti dengan font piksel berlisensi bebas bila sudah ada di `assets/fonts/` (lihat README).

## 12. Alat debug (hanya bila `debug.aktif`)

F1 tambah Rp 1000; F2 lompat ke hari berikutnya (jalankan `tickHari`); F3 paksa hama muncul di semua tanaman tahap >= 1; F4 matangkan semua tanaman. Matikan lewat `constants.json` pada build akhir.

## 13. Manifes aset

| File | Ukuran | Isi |
| --- | --- | --- |
| `art/tileset.png` | 64x64 | 16 tile, id 0 sampai 15 (lihat `map.json.namaTile`) |
| `art/player.png` | 64x64 | baris: bawah, kiri, kanan, atas; 4 frame jalan tiap baris; tiap frame 16x16 |
| `art/crops.png` | 80x48 | baris: padi, jagung, cabai; kolom: tahap 0..3, layu |
| `art/pests.png` | 96x16 | 6 hama berurutan sesuai `spriteIndex` |
| `art/ikon.png` | 64x64 | 16 ikon 16x16: 0 cangkul, 1 penyiram, 2 kaca pembesar, 3 penyemprot, 4 perangkap, 5 biji, 6 tanda seru, 7 koin, 8 bintang, 9 bintang kosong, 10 padi, 11 jagung, 12 cabai, 13 nabati, 14 kimia, 15 gembok |
| `art/panel.png` | 48x48 | panel 9-slice |
| `art/slot.png`, `slot_terpilih.png` | 20x20 | slot hotbar |
| `art/penanda_petak.png` | 16x16 | bingkai petak sasaran |
| `art/jurnal_<id>.png` | 64x64 | ilustrasi Journal tiap hama |
| `sfx/sfx_cangkul/siram/panen/beli/jual/gagal.wav` | | efek suara |
| `sfx/bgm_ladang.wav` | | musik latar (loop) |

Aset ini placeholder buatan skrip (`tools/gen_assets.py`, CC0). Mengganti aset dengan versi yang lebih bagus aman selama **nama file dan ukuran sel (16x16)** dipertahankan.

## 14. Kasus tepi yang harus ditangani

- Pemain tidak boleh keluar peta atau menembus tile terblokir.
- Tidak ada aksi alat bila sasaran di luar lahan.
- Jumlah biji/bahan tidak boleh negatif; Tab hanya bergilir di antara biji yang dimiliki (jumlah > 0).
- Hanya satu panel terbuka pada satu waktu; waktu berhenti selama panel terbuka.
- Hama tidak muncul pada petak kosong atau tanaman mati.
- Panen dan bersihkan mengosongkan seluruh data tanaman termasuk hama.
- Musim berakhir tepat saat hari ke-15 akan dimulai: bin hari ke-14 tetap dijual lebih dulu (langkah 1).

## 15. Pengujian unit wajib (`commonTest`, memakai `core/` dengan Random ber-seed)

1. Tanaman tidak disiram: `hariTumbuh` tidak bertambah setelah `tickHari`.
2. Tanaman disiram: `hariTumbuh` bertambah 1.
3. Hama tidak ditangani: kesehatan 100 menjadi 75, 50, 25, lalu mati pada tick keempat.
4. Nabati benar pada hama bermetode nabati: hama hilang setelah `tickHari` dan kesehatan tidak turun.
5. Nabati pada hama bermetode perangkap: bahan berkurang, hama tetap ada.
6. Kimia: hama hilang langsung; panen kualitas maksimal BIASA.
7. Harga jual: cabai BAIK 160, BIASA 96, RUSAK 48; padi BAIK 60, BIASA 36, RUSAK 18; jagung BAIK 90, BIASA 54, RUSAK 27.
8. Rating: 1499 -> 0 bintang; 1500 -> 1; 3000 -> 2; 4500 dengan journal 5/6 -> 2; 4500 dengan journal 6/6 -> 3.
9. Bin dijual pada tick: uang bertambah sesuai isi dan bin kosong.
10. Hama menyebar hanya ke tetangga bertanaman sama dan tanpa hama.
11. Konversi menit-ke-jam: 120 detik nyata sama dengan 960 menit game.
12. Save lalu load menghasilkan `GameState` yang sama.
