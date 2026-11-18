package core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit test untuk tanam, siram, pertumbuhan, dan tahap gambar (SPEC bagian 15 nomor 1 dan 2).
 */
class FarmingTest {

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

    @Test
    fun `SPEC 15-1 tanaman tidak disiram hariTumbuh tidak bertambah setelah tickHari`() {
        val state = GameState()
        val config = buatConfig()

        val petak = state.petak[0]
        petak.tanah = TanahState.DIOLAH
        petak.disiramHariIni = false
        petak.tanaman = Tanaman(cropId = "padi", hariTumbuh = 0)

        DaySimulator.tickHari(state, config)

        assertEquals(0, petak.tanaman?.hariTumbuh, "Tanaman yang tidak disiram tidak boleh bertambah hariTumbuh")
        assertFalse(petak.disiramHariIni)
    }

    @Test
    fun `SPEC 15-2 tanaman disiram hariTumbuh bertambah 1 setelah tickHari`() {
        val state = GameState()
        val config = buatConfig()

        val petak = state.petak[0]
        petak.tanah = TanahState.DIOLAH
        petak.disiramHariIni = true
        petak.tanaman = Tanaman(cropId = "padi", hariTumbuh = 0)

        DaySimulator.tickHari(state, config)

        assertEquals(1, petak.tanaman?.hariTumbuh, "Tanaman yang disiram harus bertambah hariTumbuh sebanyak 1")
        assertFalse(petak.disiramHariIni, "Tanah harus kering (disiramHariIni = false) setelah tickHari")
    }

    @Test
    fun `tanaman mati tidak bertambah hariTumbuh meskipun disiram`() {
        val state = GameState()
        val config = buatConfig()

        val petak = state.petak[0]
        petak.tanah = TanahState.DIOLAH
        petak.disiramHariIni = true
        petak.tanaman = Tanaman(cropId = "padi", hariTumbuh = 2, mati = true)

        DaySimulator.tickHari(state, config)

        assertEquals(2, petak.tanaman?.hariTumbuh, "Tanaman mati tidak boleh bertumbuh")
    }

    @Test
    fun `rumus tahap gambar tanaman sesuai SPEC bagian 6`() {
        // Padi: hariTumbuh_crop = 5
        val hariTumbuhPadi = 5
        assertEquals(0, hitungTahapGambar(mati = false, hariTumbuh = 0, hariTumbuhCrop = hariTumbuhPadi))
        assertEquals(0, hitungTahapGambar(mati = false, hariTumbuh = 1, hariTumbuhCrop = hariTumbuhPadi))
        assertEquals(1, hitungTahapGambar(mati = false, hariTumbuh = 2, hariTumbuhCrop = hariTumbuhPadi))
        assertEquals(1, hitungTahapGambar(mati = false, hariTumbuh = 3, hariTumbuhCrop = hariTumbuhPadi))
        assertEquals(2, hitungTahapGambar(mati = false, hariTumbuh = 4, hariTumbuhCrop = hariTumbuhPadi))
        // Matang: hariTumbuh >= 5
        assertEquals(3, hitungTahapGambar(mati = false, hariTumbuh = 5, hariTumbuhCrop = hariTumbuhPadi))
        assertEquals(3, hitungTahapGambar(mati = false, hariTumbuh = 6, hariTumbuhCrop = hariTumbuhPadi))
        // Mati: tahap 4 (layu)
        assertEquals(4, hitungTahapGambar(mati = true, hariTumbuh = 2, hariTumbuhCrop = hariTumbuhPadi))

        // Jagung: hariTumbuh_crop = 6
        val hariTumbuhJagung = 6
        assertEquals(0, hitungTahapGambar(mati = false, hariTumbuh = 0, hariTumbuhCrop = hariTumbuhJagung))
        assertEquals(1, hitungTahapGambar(mati = false, hariTumbuh = 2, hariTumbuhCrop = hariTumbuhJagung))
        assertEquals(2, hitungTahapGambar(mati = false, hariTumbuh = 4, hariTumbuhCrop = hariTumbuhJagung))
        assertEquals(3, hitungTahapGambar(mati = false, hariTumbuh = 6, hariTumbuhCrop = hariTumbuhJagung))

        // Cabai: hariTumbuh_crop = 7
        val hariTumbuhCabai = 7
        assertEquals(0, hitungTahapGambar(mati = false, hariTumbuh = 0, hariTumbuhCrop = hariTumbuhCabai))
        assertEquals(1, hitungTahapGambar(mati = false, hariTumbuh = 3, hariTumbuhCrop = hariTumbuhCabai))
        assertEquals(2, hitungTahapGambar(mati = false, hariTumbuh = 5, hariTumbuhCrop = hariTumbuhCabai))
        assertEquals(3, hitungTahapGambar(mati = false, hariTumbuh = 7, hariTumbuhCrop = hariTumbuhCabai))
    }

    @Test
    fun `interaksi cangkul mengubah tanah belum diolah menjadi diolah`() {
        val petak = Petak(0, 0, tanah = TanahState.BELUM_DIOLAH)
        val inventory = Inventory()

        val hasil = Interaction.pakaiAlat(slot = 1, petak = petak, inventory = inventory, selectedCropId = null)
        assertTrue(hasil is InteractionResult.Sukses)
        assertEquals("sfx_cangkul", hasil.sfx)
        assertEquals(TanahState.DIOLAH, petak.tanah)

        // Cangkul lagi di petak yang sudah diolah -> gagal sudahDiolah
        val hasil2 = Interaction.pakaiAlat(slot = 1, petak = petak, inventory = inventory, selectedCropId = null)
        assertTrue(hasil2 is InteractionResult.Gagal)
        assertEquals("sudahDiolah", hasil2.toastKey)
    }

    @Test
    fun `interaksi penyiram menyiram tanah diolah`() {
        val petakBelumDiolah = Petak(0, 0, tanah = TanahState.BELUM_DIOLAH)
        val inventory = Inventory()

        // Siram di tanah belum diolah -> gagal tanahBelumDiolah
        val hasil1 = Interaction.pakaiAlat(slot = 2, petak = petakBelumDiolah, inventory = inventory, selectedCropId = null)
        assertTrue(hasil1 is InteractionResult.Gagal)
        assertEquals("tanahBelumDiolah", hasil1.toastKey)

        // Siram di tanah diolah -> sukses, disiramHariIni = true
        val petakDiolah = Petak(0, 0, tanah = TanahState.DIOLAH)
        val hasil2 = Interaction.pakaiAlat(slot = 2, petak = petakDiolah, inventory = inventory, selectedCropId = null)
        assertTrue(hasil2 is InteractionResult.Sukses)
        assertEquals("sfx_siram", hasil2.sfx)
        assertTrue(petakDiolah.disiramHariIni)
    }

    @Test
    fun `interaksi menanam biji`() {
        val inventory = Inventory()
        inventory.biji["padi"] = 2

        val petakBelumDiolah = Petak(0, 0, tanah = TanahState.BELUM_DIOLAH)
        // Tanam di tanah belum diolah -> gagal perluDiolah
        val hasil1 = Interaction.pakaiAlat(slot = 6, petak = petakBelumDiolah, inventory = inventory, selectedCropId = "padi")
        assertTrue(hasil1 is InteractionResult.Gagal)
        assertEquals("perluDiolah", hasil1.toastKey)

        val petakDiolah = Petak(0, 0, tanah = TanahState.DIOLAH)
        // Tanam berhasil
        val hasil2 = Interaction.pakaiAlat(slot = 6, petak = petakDiolah, inventory = inventory, selectedCropId = "padi")
        assertTrue(hasil2 is InteractionResult.Sukses)
        assertNotNull(petakDiolah.tanaman)
        assertEquals("padi", petakDiolah.tanaman?.cropId)
        assertEquals(0, petakDiolah.tanaman?.hariTumbuh)
        assertEquals(100, petakDiolah.tanaman?.kesehatan)
        assertEquals(1, inventory.biji["padi"])

        // Tanam lagi di petak yang sudah terisi -> gagal petakTerisi
        val hasil3 = Interaction.pakaiAlat(slot = 6, petak = petakDiolah, inventory = inventory, selectedCropId = "padi")
        assertTrue(hasil3 is InteractionResult.Gagal)
        assertEquals("petakTerisi", hasil3.toastKey)

        // Tanam saat biji habis
        inventory.biji["padi"] = 0
        val petakKosong = Petak(1, 0, tanah = TanahState.DIOLAH)
        val hasil4 = Interaction.pakaiAlat(slot = 6, petak = petakKosong, inventory = inventory, selectedCropId = "padi")
        assertTrue(hasil4 is InteractionResult.Gagal)
        assertEquals("bijiHabis", hasil4.toastKey)
        assertNull(petakKosong.tanaman)
    }
}
