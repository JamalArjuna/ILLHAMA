package core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EconomyTest {

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
          {"id":"padi","nama":"Padi","hargaBiji":20,"hariTumbuh":5,"hargaJual":60,"hama":["wereng_cokelat"],"spriteBaris":0,"ikonPanen":10},
          {"id":"jagung","nama":"Jagung","hargaBiji":30,"hariTumbuh":6,"hargaJual":90,"hama":["ulat_grayak_jagung"],"spriteBaris":1,"ikonPanen":11},
          {"id":"cabai","nama":"Cabai","hargaBiji":50,"hariTumbuh":7,"hargaJual":160,"hama":["kutu_daun"],"spriteBaris":2,"ikonPanen":12}
        ]
        """.trimIndent()
        val pestsJson = "[]"
        val itemsJson = """
        {
          "alat": [],
          "barang": [
            {"id":"nabati","nama":"Pestisida Nabati","harga":15,"ikon":13},
            {"id":"perangkap","nama":"Perangkap","harga":40,"ikon":4},
            {"id":"kimia","nama":"Pestisida Kimia","harga":60,"ikon":14}
          ],
          "urutanDiToko": [
            "biji:padi",
            "biji:jagung",
            "biji:cabai",
            "barang:nabati",
            "barang:perangkap",
            "barang:kimia"
          ]
        }
        """.trimIndent()
        val textsJson = """{"judulGame":"ILLHAMA"}"""
        return Catalog.dari(cropsJson, pestsJson, itemsJson, textsJson)
    }

    @Test
    fun `SPEC 15-7 harga jual tanaman cabai, padi, dan jagung`() {
        val config = buatConfig()

        // Cabai: hargaJual 160
        assertEquals(160, Economy.hitungHargaJual(160, Kualitas.BAIK, config), "Cabai BAIK harus 160")
        assertEquals(96, Economy.hitungHargaJual(160, Kualitas.BIASA, config), "Cabai BIASA harus 96")
        assertEquals(48, Economy.hitungHargaJual(160, Kualitas.RUSAK, config), "Cabai RUSAK harus 48")

        // Padi: hargaJual 60
        assertEquals(60, Economy.hitungHargaJual(60, Kualitas.BAIK, config), "Padi BAIK harus 60")
        assertEquals(36, Economy.hitungHargaJual(60, Kualitas.BIASA, config), "Padi BIASA harus 36")
        assertEquals(18, Economy.hitungHargaJual(60, Kualitas.RUSAK, config), "Padi RUSAK harus 18")

        // Jagung: hargaJual 90
        assertEquals(90, Economy.hitungHargaJual(90, Kualitas.BAIK, config), "Jagung BAIK harus 90")
        assertEquals(54, Economy.hitungHargaJual(90, Kualitas.BIASA, config), "Jagung BIASA harus 54")
        assertEquals(27, Economy.hitungHargaJual(90, Kualitas.RUSAK, config), "Jagung RUSAK harus 27")
    }

    @Test
    fun `penentuan kualitas dari kesehatan dan pembatasan kimia`() {
        val config = buatConfig()

        // Tanpa kimia
        assertEquals(Kualitas.BAIK, Economy.hitungKualitas(100, pakaiKimia = false, config))
        assertEquals(Kualitas.BAIK, Economy.hitungKualitas(75, pakaiKimia = false, config))
        assertEquals(Kualitas.BIASA, Economy.hitungKualitas(74, pakaiKimia = false, config))
        assertEquals(Kualitas.BIASA, Economy.hitungKualitas(40, pakaiKimia = false, config))
        assertEquals(Kualitas.RUSAK, Economy.hitungKualitas(39, pakaiKimia = false, config))
        assertEquals(Kualitas.RUSAK, Economy.hitungKualitas(0, pakaiKimia = false, config))

        // Dengan kimia: kualitas BAIK dibatasi menjadi BIASA
        assertEquals(Kualitas.BIASA, Economy.hitungKualitas(100, pakaiKimia = true, config))
        assertEquals(Kualitas.BIASA, Economy.hitungKualitas(75, pakaiKimia = true, config))
        assertEquals(Kualitas.BIASA, Economy.hitungKualitas(50, pakaiKimia = true, config))
        assertEquals(Kualitas.RUSAK, Economy.hitungKualitas(30, pakaiKimia = true, config))
    }

    @Test
    fun `SPEC 15-9 bin dijual pada tick hari, uang bertambah dan bin kosong`() {
        val config = buatConfig()
        val catalog = buatCatalog()
        val state = GameState(uang = 500)

        // Isi bin: 2 Cabai BAIK (2 * 160 = 320) + 1 Padi BIASA (1 * 36 = 36) = 356
        state.bin[Pair("cabai", Kualitas.BAIK)] = 2
        state.bin[Pair("padi", Kualitas.BIASA)] = 1

        DaySimulator.tickHari(state, config, catalog)

        assertEquals(856, state.uang, "Uang awal 500 + 356 = 856")
        assertEquals(356, state.pendapatanKemarin)
        assertTrue(state.bin.isEmpty(), "Shipping bin harus dikosongkan setelah dijual")
    }

    @Test
    fun `toko menampilkan daftar barang dan proses pembelian`() {
        val catalog = buatCatalog()
        val items = Shop.daftarItem(catalog)

        assertEquals(6, items.size)
        assertEquals("Biji Padi", items[0].nama)
        assertEquals(20, items[0].harga)
        assertEquals("Pestisida Kimia", items[5].nama)
        assertEquals(60, items[5].harga)

        val state = GameState(uang = 50)
        // Beli biji padi (harga 20) -> sukses
        val beli1 = Shop.beli(items[0], state)
        assertTrue(beli1)
        assertEquals(30, state.uang)
        assertEquals(1, state.inventory.biji["padi"])

        // Beli pestisida kimia (harga 60) dengan uang 30 -> ditolak
        val beli2 = Shop.beli(items[5], state)
        assertFalse(beli2)
        assertEquals(30, state.uang)
        assertEquals(0, state.inventory.kimia)
    }

    @Test
    fun `interaksi panen dan shipping bin`() {
        val config = buatConfig()
        val catalog = buatCatalog()
        val inventory = Inventory()

        // Petak dengan padi matang (hariTumbuh = 5)
        val petak = Petak(0, 0, tanah = TanahState.DIOLAH, disiramHariIni = true)
        petak.tanaman = Tanaman(cropId = "padi", hariTumbuh = 5, kesehatan = 100)

        val hasil = Interaction.interaksiTangan(petak, catalog, config, inventory)
        assertTrue(hasil is HandResult.PanenSukses)
        assertEquals("padi", hasil.cropId)
        assertEquals(Kualitas.BAIK, hasil.kualitas)
        assertNull(petak.tanaman, "Petak harus kosong setelah dipanen")
        assertEquals(TanahState.DIOLAH, petak.tanah)
        assertFalse(petak.disiramHariIni)
        assertEquals(1, inventory.hasil[Pair("padi", Kualitas.BAIK)])

        // Masukkan hasil panen ke shipping bin
        val bin = mutableMapOf<Pair<String, Kualitas>, Int>()
        val totalDipindah = Interaction.masukkanKeBin(inventory, bin)
        assertEquals(1, totalDipindah)
        assertTrue(inventory.hasil.isEmpty(), "Inventory hasil harus kosong setelah dipindah ke bin")
        assertEquals(1, bin[Pair("padi", Kualitas.BAIK)])
    }
}
