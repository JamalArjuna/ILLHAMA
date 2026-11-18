package core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit test M0: memastikan data termuat dengan benar.
 * Test ini dijalankan di commonTest dan hanya bergantung pada core/ (tanpa KorGE).
 */
class CatalogTest {

    // JSON minimal untuk test — menggunakan data sebenarnya dari file
    private val cropsJson = """
    [
      {"id":"padi","nama":"Padi","hargaBiji":20,"hariTumbuh":5,"hargaJual":60,
       "hama":["wereng_cokelat","tikus_sawah"],"spriteBaris":0,"ikonPanen":10},
      {"id":"jagung","nama":"Jagung","hargaBiji":30,"hariTumbuh":6,"hargaJual":90,
       "hama":["ulat_grayak_jagung","belalang"],"spriteBaris":1,"ikonPanen":11},
      {"id":"cabai","nama":"Cabai","hargaBiji":50,"hariTumbuh":7,"hargaJual":160,
       "hama":["kutu_daun","lalat_buah"],"spriteBaris":2,"ikonPanen":12}
    ]
    """.trimIndent()

    private val pestsJson = """
    [
      {"id":"wereng_cokelat","nama":"Wereng Cokelat","namaIlmiah":"Nilaparvata lugens",
       "tanaman":"padi","metodeBenar":"nabati","spriteIndex":0,
       "gejala":"Daun menguning","ciri":"Serangga","caraKendali":"Semprot","tipGame":"nabati"},
      {"id":"tikus_sawah","nama":"Tikus Sawah","namaIlmiah":"Rattus argentiventer",
       "tanaman":"padi","metodeBenar":"perangkap","spriteIndex":1,
       "gejala":"Batang terpotong","ciri":"Hewan","caraKendali":"Perangkap","tipGame":"perangkap"},
      {"id":"ulat_grayak_jagung","nama":"Ulat Grayak Jagung","namaIlmiah":"Spodoptera frugiperda",
       "tanaman":"jagung","metodeBenar":"manual","spriteIndex":2,
       "gejala":"Daun berlubang","ciri":"Ulat","caraKendali":"Ambil","tipGame":"manual"},
      {"id":"belalang","nama":"Belalang","namaIlmiah":"Locusta migratoria",
       "tanaman":"jagung","metodeBenar":"manual","spriteIndex":3,
       "gejala":"Tepi daun habis","ciri":"Serangga","caraKendali":"Tangkap","tipGame":"manual"},
      {"id":"kutu_daun","nama":"Kutu Daun","namaIlmiah":"Aphis gossypii",
       "tanaman":"cabai","metodeBenar":"nabati","spriteIndex":4,
       "gejala":"Daun keriting","ciri":"Serangga kecil","caraKendali":"Semprot","tipGame":"nabati"},
      {"id":"lalat_buah","nama":"Lalat Buah","namaIlmiah":"Bactrocera dorsalis",
       "tanaman":"cabai","metodeBenar":"perangkap","spriteIndex":5,
       "gejala":"Buah busuk","ciri":"Lalat","caraKendali":"Perangkap","tipGame":"perangkap"}
    ]
    """.trimIndent()

    private val itemsJson = """
    {"alat":[],"barang":[],"urutanDiToko":[]}
    """.trimIndent()

    private val textsJson = """
    {"judulGame":"Tani Siaga"}
    """.trimIndent()

    private fun buatCatalog() = Catalog.dari(cropsJson, pestsJson, itemsJson, textsJson)

    @Test
    fun `tiga tanaman termuat`() {
        val catalog = buatCatalog()
        assertEquals(3, catalog.crops.size, "Harus ada tepat 3 tanaman")
        assertTrue(catalog.cropById.containsKey("padi"), "padi harus ada")
        assertTrue(catalog.cropById.containsKey("jagung"), "jagung harus ada")
        assertTrue(catalog.cropById.containsKey("cabai"), "cabai harus ada")
    }

    @Test
    fun `enam hama termuat`() {
        val catalog = buatCatalog()
        assertEquals(6, catalog.pests.size, "Harus ada tepat 6 hama")
    }

    @Test
    fun `rujukan hama di tiap tanaman valid`() {
        val catalog = buatCatalog()
        val errors = catalog.validasiRujukan()
        assertTrue(errors.isEmpty(), "Semua rujukan hama harus valid. Error: $errors")
    }

    @Test
    fun `data tanaman benar`() {
        val catalog = buatCatalog()
        val padi = catalog.cropDariId("padi")
        assertEquals(20, padi.hargaBiji)
        assertEquals(5, padi.hariTumbuh)
        assertEquals(60, padi.hargaJual)
        assertEquals(2, padi.hama.size)

        val cabai = catalog.cropDariId("cabai")
        assertEquals(50, cabai.hargaBiji)
        assertEquals(7, cabai.hariTumbuh)
        assertEquals(160, cabai.hargaJual)
    }

    @Test
    fun `data hama benar`() {
        val catalog = buatCatalog()
        val wereng = catalog.pestDariId("wereng_cokelat")
        assertEquals("nabati", wereng.metodeBenar)
        assertEquals("padi", wereng.tanaman)

        val tikus = catalog.pestDariId("tikus_sawah")
        assertEquals("perangkap", tikus.metodeBenar)

        val ulat = catalog.pestDariId("ulat_grayak_jagung")
        assertEquals("manual", ulat.metodeBenar)
    }

    @Test
    fun `game config memuat konstanta`() {
        val constantsJson = """
        {
          "tampilan": {
            "virtualWidth": 480,
            "virtualHeight": 272,
            "tileSize": 16,
            "skalaJendela": 3,
            "mapKolom": 30,
            "mapBaris": 17
          },
          "waktu": {
            "detikNyataPerHari": 120,
            "jamMulai": 6,
            "jamTidurOtomatis": 22,
            "jumlahHariMusim": 14
          },
          "pemain": {
            "uangAwal": 500,
            "kecepatanPxPerDetik": 80,
            "hitbox": { "lebar": 10, "tinggi": 8, "offsetX": 3, "offsetY": 8 }
          },
          "lahan": {
            "kolom": 8,
            "baris": 6,
            "originTile": [3, 7]
          },
          "tanaman": {
            "jumlahTahapGambar": 4,
            "kesehatanAwal": 100
          },
          "hama": {
            "peluangMunculHarian": 0.2,
            "tahapMinimalMuncul": 1,
            "peluangMenyebar": 0.3,
            "kerusakanKesehatanPerHari": 25
          },
          "kualitas": {
            "batasBaik": 75,
            "batasBiasa": 40,
            "pengaliBaik": 1.0,
            "pengaliBiasa": 0.6,
            "pengaliRusak": 0.3,
            "kimiaMaksimalKualitas": "BIASA"
          },
          "rating": {
            "bintang1": 1500,
            "bintang2": 3000,
            "bintang3": 4500,
            "journalPenuhUntukBintang3": 6
          },
          "ui": {
            "durasiToastDetik": 2.5
          },
          "debug": {
            "aktif": true,
            "tombolTambahUang": "F1",
            "tombolLompatHari": "F2",
            "tombolPaksaHama": "F3",
            "tombolMatangkanSemua": "F4"
          }
        }
        """.trimIndent()

        val config = GameConfig.dari(constantsJson)
        assertEquals(480, config.virtualWidth)
        assertEquals(272, config.virtualHeight)
        assertEquals(16, config.tileSize)
        assertEquals(500, config.uangAwal)
        assertEquals(14, config.jumlahHariMusim)
        assertEquals(8, config.lahanKolom)
        assertEquals(6, config.lahanBaris)
    }
}
