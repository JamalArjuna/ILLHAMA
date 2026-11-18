package core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit test untuk waktu dan konversi (SPEC bagian 15 nomor 11).
 * "11. Konversi menit-ke-jam: 120 detik nyata sama dengan 960 menit game."
 */
class TimeTest {

    private val detikNyataPerHari = 120
    private val jamMulai = 6
    private val jamTidurOtomatis = 22
    private val jumlahHariMusim = 14

    @Test
    fun `120 detik nyata sama dengan 960 menit game`() {
        // Rentang 06:00 sampai 22:00 adalah 16 jam = 960 menit
        val totalMenit = DaySimulator.detikNyataKeMenitGame(
            120.0,
            detikNyataPerHari,
            jamMulai,
            jamTidurOtomatis
        )
        assertEquals(960.0, totalMenit, 0.001, "120 detik nyata harus tepat 960 menit game")
    }

    @Test
    fun `1 detik nyata sama dengan 8 menit game`() {
        val menitPerDetik = DaySimulator.hitungMenitGamePerDetik(
            detikNyataPerHari,
            jamMulai,
            jamTidurOtomatis
        )
        assertEquals(8.0, menitPerDetik, 0.001, "1 detik nyata harus 8 menit game")
    }

    @Test
    fun `format waktu benar`() {
        assertEquals("06:00", DaySimulator.formatWaktu(6 * 60))
        assertEquals("06:08", DaySimulator.formatWaktu(6 * 60 + 8))
        assertEquals("12:30", DaySimulator.formatWaktu(12 * 60 + 30))
        assertEquals("22:00", DaySimulator.formatWaktu(22 * 60))
    }

    @Test
    fun `tickWaktu memajukan waktu dan mendeteksi jam tidur`() {
        val state = GameState(hari = 1, menitHari = 6 * 60)
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
        val config = GameConfig.dari(constantsJson)

        val akumulator = doubleArrayOf(0.0)

        // 10 detik nyata = 80 menit game -> 06:00 (360) + 80 = 440 (07:20)
        val tidur1 = DaySimulator.tickWaktu(state, config, 10.0, akumulator)
        assertFalse(tidur1, "Belum jam 22:00")
        assertEquals(440, state.menitHari)
        assertEquals("07:20", DaySimulator.formatWaktu(state.menitHari))

        // Majukan sampai 22:00 (1320 menit): butuh 110 detik lagi (total 120 detik)
        val tidur2 = DaySimulator.tickWaktu(state, config, 110.0, akumulator)
        assertTrue(tidur2, "Harus mencapai jam tidur otomatis 22:00")
        assertEquals(1320, state.menitHari)
        assertEquals("22:00", DaySimulator.formatWaktu(state.menitHari))
    }

    @Test
    fun `tickHari menambah hari dan reset jam ke 06 00`() {
        val state = GameState(hari = 1, menitHari = 22 * 60)
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
        val config = GameConfig.dari(constantsJson)

        val selesaiMusim = DaySimulator.tickHari(state, config)
        assertFalse(selesaiMusim)
        assertEquals(2, state.hari)
        assertEquals(6 * 60, state.menitHari)
        assertEquals("06:00", DaySimulator.formatWaktu(state.menitHari))
    }

    @Test
    fun `musim berakhir setelah hari ke-14`() {
        val state = GameState(hari = 14, menitHari = 22 * 60)
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
        val config = GameConfig.dari(constantsJson)

        val selesaiMusim = DaySimulator.tickHari(state, config)
        assertTrue(selesaiMusim, "Hari 15 harus mengakhiri musim")
        assertEquals(15, state.hari)
    }
}
