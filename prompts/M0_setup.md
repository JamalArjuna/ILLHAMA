# M0: Verifikasi proyek dan kerangka (sebelum minggu 1)

Konteks: baca `AGENTS.md`, `docs/PRD.md`, dan `docs/SPEC.md` bagian 1 sampai 5 dan 13.

Prasyarat (dikerjakan pemilik proyek): proyek KorGE dari template resmi sudah bisa dijalankan dengan `./gradlew runJvm`, dan folder `assets/`, `data/`, `map/` dari paket starter sudah disalin ke `src/commonMain/resources/`.

Tugas:
1. Jalankan proyek apa adanya dan pastikan tampil. Catat versi KorGE, Kotlin, Gradle, JDK, dan nama paket yang dipakai ke bagian "Versi terkunci" di `AGENTS.md`. **Jangan mengubah versi.**
2. Atur resolusi virtual 480x272, jendela awal 1440x816, skala piksel nearest-neighbor.
3. Buat struktur paket sesuai `SPEC.md` bagian 3 (folder kosong dengan file placeholder boleh).
4. Muat `constants.json`, `crops.json`, `pests.json`, `items.json`, `texts_id.json` ke objek Kotlin di `core/` (`GameConfig`, `Catalog`). Tulis satu unit test sederhana yang memastikan data termuat (3 tanaman, 6 hama, rujukan hama di tiap tanaman valid).
5. Buat tiga scene kosong: TitleScene, GameScene, EndScene, dengan perpindahan Title -> Game lewat Enter dan Game -> End lewat tombol debug.
6. Render peta dari `map/map.json` memakai `tileset.png` di GameScene (belum ada pemain). Peta harus tampak sama dengan `map/pratinjau_peta.png`.

Selesai bila: game berjalan, peta tampil benar dan tajam, data termuat, test lulus.
Jangan mengerjakan: pemain, tanaman, UI, suara.
