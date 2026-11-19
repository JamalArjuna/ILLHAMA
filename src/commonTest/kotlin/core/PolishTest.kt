package core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit test untuk Milestone M5: Rating bintang (SPEC 15-8) dan Save/Load (SPEC 15-12).
 */
class PolishTest {

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
    fun `SPEC 15-8 perhitungan rating bintang akhir`() {
        val config = buatConfig()

        // 1499 -> 0 bintang
        assertEquals(0, Economy.hitungRating(uang = 1499, jumlahJournalTerbuka = 6, config = config))

        // 1500 -> 1 bintang
        assertEquals(1, Economy.hitungRating(uang = 1500, jumlahJournalTerbuka = 0, config = config))
        assertEquals(1, Economy.hitungRating(uang = 2999, jumlahJournalTerbuka = 4, config = config))

        // 3000 -> 2 bintang
        assertEquals(2, Economy.hitungRating(uang = 3000, jumlahJournalTerbuka = 2, config = config))
        assertEquals(2, Economy.hitungRating(uang = 4499, jumlahJournalTerbuka = 6, config = config))

        // 4500 dengan journal 5/6 -> 2 bintang
        assertEquals(2, Economy.hitungRating(uang = 4500, jumlahJournalTerbuka = 5, config = config))

        // 4500 dengan journal 6/6 -> 3 bintang
        assertEquals(3, Economy.hitungRating(uang = 4500, jumlahJournalTerbuka = 6, config = config))
        assertEquals(3, Economy.hitungRating(uang = 6000, jumlahJournalTerbuka = 6, config = config))
    }

    @Test
    fun `SPEC 15-12 save lalu load menghasilkan GameState yang sama`() {
        val original = GameState(
            hari = 4,
            menitHari = 10 * 60 + 30, // 10:30
            uang = 1350,
            tutorialIndex = 7,
            modeSemprot = Metode.KIMIA,
            pendapatanKemarin = 320,
            hamaPertamaMuncul = true,
            matangPertamaMuncul = false
        )

        original.inventory.biji["padi"] = 3
        original.inventory.biji["cabai"] = 1
        original.inventory.nabati = 2
        original.inventory.kimia = 4
        original.inventory.perangkap = 1
        original.inventory.hasil[Pair("padi", Kualitas.BAIK)] = 5
        original.inventory.hasil[Pair("jagung", Kualitas.BIASA)] = 2

        original.bin[Pair("cabai", Kualitas.RUSAK)] = 3

        original.journalTerbuka.addAll(listOf("wereng_cokelat", "tikus_sawah", "kutu_daun"))

        // Set beberapa petak
        val p0 = original.petak[0]
        p0.tanah = TanahState.DIOLAH
        p0.disiramHariIni = true
        p0.tanaman = Tanaman(
            cropId = "padi",
            hariTumbuh = 3,
            kesehatan = 75,
            hamaId = "wereng_cokelat",
            hamaDiperiksa = true,
            penangananTertunda = Metode.NABATI,
            pakaiKimia = false,
            mati = false
        )

        val p1 = original.petak[1]
        p1.tanah = TanahState.DIOLAH
        p1.disiramHariIni = false
        p1.tanaman = Tanaman(
            cropId = "cabai",
            hariTumbuh = 7,
            kesehatan = 100,
            hamaId = null,
            pakaiKimia = true,
            mati = false
        )

        val p2 = original.petak[2]
        p2.tanah = TanahState.DIOLAH
        p2.tanaman = Tanaman(cropId = "jagung", mati = true, kesehatan = 0)

        // Serialize
        val jsonString = SaveData.serialize(original)
        assertTrue(jsonString.isNotBlank(), "JSON save tidak boleh kosong")

        // Deserialize
        val loaded = SaveData.deserialize(jsonString)
        assertNotNull(loaded, "Deserialized GameState tidak boleh null")

        // Verifikasi semua field
        assertEquals(original.hari, loaded.hari)
        assertEquals(original.menitHari, loaded.menitHari)
        assertEquals(original.uang, loaded.uang)
        assertEquals(original.tutorialIndex, loaded.tutorialIndex)
        assertEquals(original.modeSemprot, loaded.modeSemprot)
        assertEquals(original.pendapatanKemarin, loaded.pendapatanKemarin)
        assertEquals(original.hamaPertamaMuncul, loaded.hamaPertamaMuncul)
        assertEquals(original.matangPertamaMuncul, loaded.matangPertamaMuncul)

        assertEquals(original.inventory.biji, loaded.inventory.biji)
        assertEquals(original.inventory.nabati, loaded.inventory.nabati)
        assertEquals(original.inventory.kimia, loaded.inventory.kimia)
        assertEquals(original.inventory.perangkap, loaded.inventory.perangkap)
        assertEquals(original.inventory.hasil, loaded.inventory.hasil)

        assertEquals(original.bin, loaded.bin)
        assertEquals(original.journalTerbuka, loaded.journalTerbuka)

        // Verifikasi petak
        assertEquals(original.petak.size, loaded.petak.size)
        for (i in original.petak.indices) {
            val op = original.petak[i]
            val lp = loaded.petak[i]
            assertEquals(op.x, lp.x)
            assertEquals(op.y, lp.y)
            assertEquals(op.tanah, lp.tanah)
            assertEquals(op.disiramHariIni, lp.disiramHariIni)

            val ot = op.tanaman
            val lt = lp.tanaman
            if (ot == null) {
                assertNull(lt)
            } else {
                assertNotNull(lt)
                assertEquals(ot.cropId, lt.cropId)
                assertEquals(ot.hariTumbuh, lt.hariTumbuh)
                assertEquals(ot.kesehatan, lt.kesehatan)
                assertEquals(ot.hamaId, lt.hamaId)
                assertEquals(ot.hamaDiperiksa, lt.hamaDiperiksa)
                assertEquals(ot.penangananTertunda, lt.penangananTertunda)
                assertEquals(ot.pakaiKimia, lt.pakaiKimia)
                assertEquals(ot.mati, lt.mati)
            }
        }
    }

    @Test
    fun `save rusak atau versi tidak cocok diabaikan tanpa crash`() {
        assertNull(SaveData.deserialize(null))
        assertNull(SaveData.deserialize(""))
        assertNull(SaveData.deserialize("bukan json valid {"))
        assertNull(SaveData.deserialize("""{"versi": 999, "hari": 1}"""))
    }
}
