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
