# Keputusan Arsitektur & Teknis ILLHAMA

Dokumen ini mencatat keputusan-keputusan teknis dan resolusi ambiguitas yang diambil selama pengembangan.

---

## Milestone M0 (Setup & Kerangka) - 18/19 November 2026

1. **Namespace Library KorGE:**
   - KorGE versi 6.0.0 menggunakan namespace `korlibs.*` (misalnya `korlibs.korge.*`, `korlibs.image.bitmap.*`, `korlibs.math.geom.*`), bukan `com.soywiz.*` (versi lama) atau `org.korge.*` (versi 7.0.0-SNAPSHOT).

2. **Parser JSON:**
   - Menggunakan `kotlinx.serialization.json.Json` dengan opsi `ignoreUnknownKeys = true` untuk memuat data JSON (`constants.json`, `crops.json`, `pests.json`, `items.json`, `texts_id.json`, `map.json`).

3. **Perpindahan Scene KorGE:**
   - Menggunakan format lambda `sceneContainer.changeTo { NamaScene() }` daripada generic reflection `changeTo<NamaScene>()` untuk menghindari `Injector$NotMappedException` tanpa perlu registrasi dependensi injeksi manual.

4. **Konfigurasi Unit Test Kotlin Multiplatform:**
   - Menambahkan dependensi `kotlin("test")` di `commonTest` dan `kotlin("test-junit")` di `jvmTest`, serta mengatur `kotlin.test.infer.jvm.variant=false` di `gradle.properties` untuk mengatasi konflik capability pada Gradle 8.8 / Kotlin 2.0.20.

5. **Runtime Java / JDK:**
   - Ditetapkan menggunakan Zulu JDK 21 (`C:\Program Files\Zulu\zulu-21`) melalui properti `org.gradle.java.home` di `gradle.properties`.

6. **Mekanisme Keluar Aplikasi (TitleScene):**
   - Menggunakan `views.gameWindow.close()` untuk keluar dari game secara bersih saat tombol Escape ditekan di TitleScene.

---

## Milestone M1 (Gerak Pemain, Interaksi Objek, & Jam) - 19 November 2026

1. **Sistem Tabrakan & Pergerakan Pemain (`PlayerController`):**
   - Logika pergerakan dan tabrakan ditaruh murni di `core/PlayerController.kt` tanpa dependensi KorGE agar dapat diuji unit secara deterministik.
   - Menggunakan teknik pemisahan sumbu (*axis-separated collision*): evaluasi sumbu X dan Y secara terpisah sehingga pergerakan diagonal dapat meluncur mulus (*slide*) menyusuri pagar atau dinding.
   - Hitbox kaki berukuran 10×8 piksel dengan offset (3, 8) dari sudut kiri atas sprite pemain 16×16 piksel, sesuai `constants.json`.

2. **Perhitungan Petak Sasaran (`TargetTile`):**
   - Dihitung dari titik tengah hitbox kaki pemain ditambah 1 tile (16 px) ke arah hadap terakhir (DOWN, LEFT, RIGHT, UP).
   - Penanda sasaran (`penanda_petak.png`) hanya dibuat aktif (*visible*) apabila berada di dalam batas area lahan `(kolom 3..10, baris 7..12)`.

3. **Interaksi Objek (Tombol E):**
   - Objek interaksi (`pintu_rumah`, `pintu_toko`, `shipping_bin`) diperiksa berdasarkan apakah petak sasaran pemain berimpit dengan tile objek tersebut.
   - Pintu dan shipping bin tetap terdaftar dalam daftar `tileTerblokir`, sehingga pemain tidak dapat menembus objek tersebut.
   - Pada M1, interaksi memunculkan pesan sementara via `ToastView` menggunakan teks dari `texts_id.json`.

4. **Simulasi Waktu & Game Loop (`DaySimulator`):**
   - Menggunakan `addFastUpdater` dengan `fastDuration.seconds` di KorGE untuk stabilitas delta time tanpa alokasi obyek berlebih.
   - Rasio waktu: 120 detik nyata = 960 menit game (rentang 06:00 hingga 22:00), menghasilkan 8.0 menit game per detik nyata.
   - Akumulasi pecahan menit dicatat dalam buffer desimal untuk memajukan `GameState.menitHari` secara presisi.
   - Pukul 22:00 memicu pergantian hari otomatis (`tickHari`): hari bertambah 1, jam kembali ke 06:00, dan musim berakhir ke `EndScene` jika hari > 14.
   - Tombol debug F2 memicu pergantian hari langsung untuk keperluan pengujian.

---

## Milestone M2 (Tanam, Siram, & Pertumbuhan Tanaman) - 19 November 2026

1. **Model Data dan Rumus Pertumbuhan (`core/Models.kt`):**
   - Rumus tahap gambar tanaman diimplementasikan murni di `core/Models.kt` (`hitungTahapGambar`):
     - `mati -> 4` (layu)
     - `hariTumbuh >= hariTumbuhCrop -> 3` (matang)
     - `selain itu -> min(2, (hariTumbuh * 3) / hariTumbuhCrop)`
   - Menambahkan helper `GameState.petakAt(x, y)` untuk akses aman ke 48 petak lahan.

2. **Aturan Interaksi Alat x Petak (`core/Interaction.kt`):**
   - Logika interaksi dipisahkan ke `core/Interaction.kt` agar dapat diuji unit tanpa engine rendering.
   - Cangkul mengubah tanah `BELUM_DIOLAH` menjadi `DIOLAH`.
   - Penyiram mengubah petak `DIOLAH` menjadi basah (`disiramHariIni = true`).
   - Biji (slot 6) memeriksa kepemilikan stok biji dan kondisi petak (harus `DIOLAH` dan kosong), lalu menanam `Tanaman` (kesehatan 100, `hariTumbuh = 0`).

3. **Siklus Pergantian Hari (`DaySimulator.tickHari`):**
   - Langkah 3: Menambah `hariTumbuh` sebanyak 1 hanya untuk tanaman hidup yang petaknya `disiramHariIni == true`.
   - Langkah 4: Mengeringkan tanah (`disiramHariIni = false`) untuk seluruh 48 petak.
   - Langkah 8: `hari += 1`, `menitHari = jamMulai * 60`.

4. **Tampilan Lahan dan Hotbar (`PlotsView.kt`, `HotbarView.kt`):**
   - `PlotsView` merender 48 petak lahan (tile 3 belum diolah, tile 4 diolah kering, tile 5 basah) dan sprite tanaman dari `crops.png` (80x48 px, 5 kolom x 3 baris) dengan nearest-neighbor (`smoothing = false`).
   - `HotbarView` merender 6 slot di tengah bawah (`x = 180, y = 248`) dengan ikon dari `ikon.png` dan bingkai `slot.png` / `slot_terpilih.png`. Navigasi menggunakan tombol angka 1-6 dan mouse scroll.
   - Tombol Tab bergilir di antara jenis biji yang dimiliki (`padi`, `jagung`, `cabai`).

5. **Efek Suara (`Sfx.kt`):**
   - Efek suara `sfx_cangkul.wav`, `sfx_siram.wav`, dan `sfx_gagal.wav` dimuat via `readSound()` dan diputar secara non-blocking via `playNoCancel()`.
