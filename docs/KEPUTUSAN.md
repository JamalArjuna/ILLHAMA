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
