# M6: Uji, perbaikan, dan rilis (minggu 6)

Baca: `AGENTS.md`, `docs/PRD.md` bagian Pengujian, `docs/SPEC.md` bagian 14 dan 15.

Tugas:
1. Jalankan seluruh unit test dan perbaiki yang gagal.
2. Telusuri checklist kasus tepi di SPEC bagian 14; tulis dan perbaiki bug yang ditemukan.
3. Main penuh 14 hari minimal 5 kali (pakai F1/F2/F3 untuk mempercepat): tidak boleh ada crash, kondisi macet, uang atau jumlah barang negatif, atau tanaman "hantu" (data tersisa setelah panen/bersihkan).
4. Periksa performa: stabil 60 FPS di laptop spek menengah; perbaiki alokasi berlebihan di loop update bila perlu.
5. Seimbangkan angka bila perlu hanya lewat `data/*.json` (bukan kode), dan catat perubahannya di `docs/KEPUTUSAN.md` beserta alasannya.
6. Matikan alat debug (`debug.aktif = false`) pada build akhir; pastikan tombol F1 sampai F4 tidak lagi berfungsi.
7. Perbarui `README.md` proyek: cara menjalankan dari nol (versi JDK, perintah Gradle), kontrol, dan cara membuat distribusi (`./gradlew` task untuk membangun paket desktop yang tersedia di versi KorGE terpasang; cek dokumentasi).
8. Lengkapi `docs/CREDITS.md` dengan semua aset luar yang dipakai.

Selesai bila: semua kriteria di bagian Pengujian PRD terpenuhi dan orang lain bisa menjalankan game hanya dengan mengikuti README.
