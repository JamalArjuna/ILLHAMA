package core

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit test untuk sistem hama dan pengendalian (SPEC bagian 15 nomor 3, 4, 5, 6, 10).
 */
class PestTest {

    private fun buatConfig(): GameConfig {
        val constantsJson = """
        {
          "tampilan": { "virtualWidth": 480, "virtualHeight": 272, "tileSize": 16, "skalaJendela": 3, "mapKolom": 30, "mapBaris": 17 },
          "waktu": { "detikNyataPerHari": 120, "jamMulai": 6, "jamTidurOtomatis": 22, "jumlahHariMusim": 14 },
          "pemain": { "uangAwal": 500, "kecepatanPxPerDetik": 80 },
          "lahan": { "kolom": 8, "baris": 6, "originTile": [3, 7] },
          "tanaman": { "jumlahTahapGambar": 4, "kesehatanAwal": 100 },
          "hama": { "peluangMunculHarian": 0.2, "tahapMinimalMuncul": 1, "peluangMenyebar": 0.3, "kerusakanKesehatanPerHari": 25 },
          "kualitas": { "batasBaik": 75, "batasBiasa": 40, "pengaliBaik": 1.0, "pengaliBiasa": 0.6, "pengaliRusak": 0.3, "kimiaMaksimalKualitas": "BIASA" },
          "rating": { "bintang1": 1500, "bintang2": 3000, "bintang3": 4500, "journalPenuhUntukBintang3": 6 },
          "ui": { "durasiToastDetik": 2.5 },
          "debug": { "aktif": true }
        }
        """.trimIndent()
        return GameConfig.dari(constantsJson)
    }

    private fun buatCatalog(): Catalog {
        val cropsJson = """
        [
          { "id": "padi", "nama": "Padi", "hargaBiji": 20, "hariTumbuh": 5, "hargaJual": 60, "hama": ["wereng_cokelat", "tikus_sawah"], "spriteBaris": 0, "ikonPanen": 10 },
          { "id": "jagung", "nama": "Jagung", "hargaBiji": 30, "hariTumbuh": 6, "hargaJual": 90, "hama": ["ulat_grayak_jagung", "belalang"], "spriteBaris": 1, "ikonPanen": 11 },
          { "id": "cabai", "nama": "Cabai", "hargaBiji": 50, "hariTumbuh": 7, "hargaJual": 160, "hama": ["kutu_daun", "lalat_buah"], "spriteBaris": 2, "ikonPanen": 12 }
        ]
        """.trimIndent()

        val pestsJson = """
        [
          { "id": "wereng_cokelat", "nama": "Wereng Cokelat", "namaIlmiah": "Nilaparvata lugens", "tanaman": "padi", "metodeBenar": "nabati", "spriteIndex": 0, "gejala": "Daun menguning", "ciri": "Serangga kecil cokelat", "caraKendali": "Semprot nabati", "tipGame": "Semprot nabati" },
          { "id": "tikus_sawah", "nama": "Tikus Sawah", "namaIlmiah": "Rattus argentiventer", "tanaman": "padi", "metodeBenar": "perangkap", "spriteIndex": 1, "gejala": "Batang terpotong", "ciri": "Pengerat", "caraKendali": "Pasang perangkap", "tipGame": "Pasang perangkap" },
          { "id": "ulat_grayak_jagung", "nama": "Ulat Grayak Jagung", "namaIlmiah": "Spodoptera frugiperda", "tanaman": "jagung", "metodeBenar": "manual", "spriteIndex": 2, "gejala": "Daun berlubang", "ciri": "Ulat hijau", "caraKendali": "Ambil tangan", "tipGame": "Ambil tangan" }
        ]
        """.trimIndent()

        val itemsJson = """
        {
          "alat": [],
          "barang": [
            { "id": "nabati", "nama": "Pestisida Nabati", "harga": 15, "ikon": 13 },
            { "id": "perangkap", "nama": "Perangkap", "harga": 40, "ikon": 4 },
            { "id": "kimia", "nama": "Pestisida Kimia", "harga": 60, "ikon": 14 }
          ],
          "urutanDiToko": ["barang:nabati", "barang:perangkap", "barang:kimia"]
        }
        """.trimIndent()

        val textsJson = """
        {
          "judulGame": "Tani Siaga",
          "toast": {
            "metodeSalah": "Metode kurang tepat.",
            "nabatiDisemprot": "Disemprot.",
            "perangkapDipasang": "Perangkap dipasang.",
            "manualBerhasil": "Hama berhasil diambil.",
            "kimiaDipakai": "Kimia dipakai."
          }
        }
        """.trimIndent()

        return Catalog.dari(cropsJson, pestsJson, itemsJson, textsJson)
    }

    @Test
    fun `SPEC 15-3 hama tidak ditangani kesehatan turun 25 per hari dan mati pada tick keempat`() {
        val state = GameState()
        val config = buatConfig()

        val petak = state.petak[0]
        petak.tanah = TanahState.DIOLAH
        petak.tanaman = Tanaman(cropId = "padi", hariTumbuh = 2, kesehatan = 100, hamaId = "wereng_cokelat")

        // Hari 1 -> tick 1
        DaySimulator.tickHari(state, config)
        assertEquals(75, petak.tanaman?.kesehatan, "Kesehatan harus 75 setelah tick 1")
        assertFalse(petak.tanaman?.mati ?: true)

        // Hari 2 -> tick 2
        DaySimulator.tickHari(state, config)
        assertEquals(50, petak.tanaman?.kesehatan, "Kesehatan harus 50 setelah tick 2")
        assertFalse(petak.tanaman?.mati ?: true)

        // Hari 3 -> tick 3
        DaySimulator.tickHari(state, config)
        assertEquals(25, petak.tanaman?.kesehatan, "Kesehatan harus 25 setelah tick 3")
        assertFalse(petak.tanaman?.mati ?: true)

        // Hari 4 -> tick 4 (mati)
        DaySimulator.tickHari(state, config)
        assertEquals(0, petak.tanaman?.kesehatan, "Kesehatan harus 0 setelah tick 4")
        assertTrue(petak.tanaman?.mati ?: false, "Tanaman harus berstatus mati setelah tick 4")
        assertNull(petak.tanaman?.hamaId, "Hama harus dihapus saat tanaman mati")
    }

    @Test
    fun `SPEC 15-4 nabati benar pada hama bermetode nabati hama hilang setelah tickHari dan kesehatan tidak turun`() {
        val state = GameState()
        val config = buatConfig()
        val catalog = buatCatalog()

        state.inventory.nabati = 1
        val petak = state.petak[0]
        petak.tanah = TanahState.DIOLAH
        petak.tanaman = Tanaman(cropId = "padi", hariTumbuh = 2, kesehatan = 100, hamaId = "wereng_cokelat")

        // Pakai penyemprot mode NABATI (slot 4)
        val hasil = Interaction.pakaiAlat(
            slot = 4,
            petak = petak,
            inventory = state.inventory,
            selectedCropId = null,
            modeSemprot = Metode.NABATI,
            catalog = catalog
        )

        assertTrue(hasil is InteractionResult.Sukses, "Penyemprotan nabati benar harus sukses")
        assertEquals(0, state.inventory.nabati, "Bahan nabati harus berkurang 1")
        assertEquals(Metode.NABATI, petak.tanaman?.penangananTertunda, "Penanganan tertunda harus NABATI")

        // Jalankan pergantian hari (tickHari)
        DaySimulator.tickHari(state, config)

        assertNull(petak.tanaman?.hamaId, "Hama harus hilang setelah pergantian hari")
        assertNull(petak.tanaman?.penangananTertunda, "Penanganan tertunda harus reset")
        assertEquals(100, petak.tanaman?.kesehatan, "Kesehatan tidak boleh turun (tetap 100) karena hama hilang di langkah 2")
    }

    @Test
    fun `SPEC 15-5 nabati pada hama bermetode perangkap bahan berkurang dan hama tetap ada`() {
        val state = GameState()
        val config = buatConfig()
        val catalog = buatCatalog()

        state.inventory.nabati = 2
        val petak = state.petak[0]
        petak.tanah = TanahState.DIOLAH
        // tikus_sawah metodeBenar = perangkap
        petak.tanaman = Tanaman(cropId = "padi", hariTumbuh = 2, kesehatan = 100, hamaId = "tikus_sawah")

        val hasil = Interaction.pakaiAlat(
            slot = 4,
            petak = petak,
            inventory = state.inventory,
            selectedCropId = null,
            modeSemprot = Metode.NABATI,
            catalog = catalog
        )

        assertTrue(hasil is InteractionResult.Gagal, "Harus gagal karena metode salah")
        assertEquals("metodeSalah", (hasil as InteractionResult.Gagal).toastKey)
        assertEquals(1, state.inventory.nabati, "Bahan nabati harus tetap berkurang 1 meskipun salah")
        assertNull(petak.tanaman?.penangananTertunda, "Tidak boleh ada penanganan tertunda")
        assertEquals("tikus_sawah", petak.tanaman?.hamaId, "Hama harus tetap ada")

        // Pergantian hari -> hama merusak tanaman
        DaySimulator.tickHari(state, config)
        assertEquals(75, petak.tanaman?.kesehatan, "Kesehatan harus turun jadi 75")
        assertEquals("tikus_sawah", petak.tanaman?.hamaId, "Hama masih ada")
    }

    @Test
    fun `SPEC 15-6 kimia hama hilang langsung dan panen kualitas maksimal BIASA`() {
        val state = GameState()
        val config = buatConfig()
        val catalog = buatCatalog()

        state.inventory.kimia = 1
        val petak = state.petak[0]
        petak.tanah = TanahState.DIOLAH
        petak.tanaman = Tanaman(cropId = "padi", hariTumbuh = 5, kesehatan = 100, hamaId = "wereng_cokelat")

        // Pakai penyemprot mode KIMIA
        val hasil = Interaction.pakaiAlat(
            slot = 4,
            petak = petak,
            inventory = state.inventory,
            selectedCropId = null,
            modeSemprot = Metode.KIMIA,
            catalog = catalog
        )

        assertTrue(hasil is InteractionResult.Sukses, "Penyemprotan kimia harus sukses")
        assertEquals(0, state.inventory.kimia, "Bahan kimia harus berkurang 1")
        assertNull(petak.tanaman?.hamaId, "Hama harus hilang langsung seketika")
        assertTrue(petak.tanaman?.pakaiKimia ?: false, "Tanaman harus ditandai pakaiKimia = true")

        // Panen tanaman dengan kesehatan 100 yang pernah pakai kimia
        val hasilPanen = Interaction.interaksiTangan(petak, catalog, config, state.inventory)
        assertTrue(hasilPanen is HandResult.PanenSukses)
        assertEquals(Kualitas.BIASA, (hasilPanen as HandResult.PanenSukses).kualitas, "Kualitas panen maksimal BIASA meskipun kesehatan 100")
    }

    @Test
    fun `SPEC 15-10 hama menyebar hanya ke tetangga bertanaman sama dan tanpa hama`() {
        val state = GameState()
        val config = buatConfig()

        // Petak (0, 0) terserang wereng_cokelat pada padi
        val p00 = state.petakAt(0, 0)!!
        p00.tanaman = Tanaman(cropId = "padi", hariTumbuh = 2, kesehatan = 100, hamaId = "wereng_cokelat")

        // Tetangga kanan (1, 0): tanaman jagung (crop berbeda) -> TIDAK boleh tertular
        val p10 = state.petakAt(1, 0)!!
        p10.tanaman = Tanaman(cropId = "jagung", hariTumbuh = 2, kesehatan = 100, hamaId = null)

        // Tetangga bawah (0, 1): tanaman padi tanpa hama -> BOLEH tertular
        val p01 = state.petakAt(0, 1)!!
        p01.tanaman = Tanaman(cropId = "padi", hariTumbuh = 2, kesehatan = 100, hamaId = null)

        // Buat mock/fixed Random yang:
        // 1. peluangMenyebar: nextDouble() return 0.1 (< 0.3) -> menyebar
        // 2. arah: nextInt(4) return 2 (yaitu arah (0, 1) ke bawah)
        class FixedRandom : Random() {
            private var doubleCount = 0
            override fun nextBits(bitCount: Int): Int = 0
            override fun nextDouble(): Double {
                doubleCount++
                return 0.1 // selalu tembus peluangMenyebar
            }
            override fun nextInt(until: Int): Int {
                return 2 // arah (0, 1) ke bawah
            }
        }

        DaySimulator.tickHari(state, config, random = FixedRandom())

        // p01 harus tertular wereng_cokelat
        assertEquals("wereng_cokelat", p01.tanaman?.hamaId, "Tetangga bertanaman padi sama harus tertular wereng_cokelat")

        // p10 (jagung) tidak boleh tertular
        assertNull(p10.tanaman?.hamaId, "Tetangga bertanaman jagung berbeda tidak boleh tertular")
    }
}
