package core

import kotlin.math.round

/**
 * Logika ekonomi, penetapan kualitas hasil panen, harga jual, dan penjualan shipping bin.
 * Murni logika Kotlin tanpa dependensi KorGE.
 */
object Economy {

    /**
     * Menghitung kualitas hasil panen berdasarkan nilai kesehatan tanaman (0..100)
     * dan status penggunaan pestisida kimia (SPEC bagian 9).
     * - >= 75 : BAIK
     * - 40..74: BIASA
     * - < 40  : RUSAK
     * Jika pakaiKimia == true, kualitas dibatasi maksimal BIASA.
     */
    fun hitungKualitas(kesehatan: Int, pakaiKimia: Boolean, config: GameConfig): Kualitas {
        val kualitasAwal = when {
            kesehatan >= config.batasBaik -> Kualitas.BAIK
            kesehatan >= config.batasBiasa -> Kualitas.BIASA
            else -> Kualitas.RUSAK
        }
        return if (pakaiKimia && kualitasAwal == Kualitas.BAIK) Kualitas.BIASA else kualitasAwal
    }

    /**
     * Mengambil pengali harga berdasarkan kualitas (SPEC bagian 9).
     * - BAIK : 1.0
     * - BIASA: 0.6
     * - RUSAK: 0.3
     */
    fun pengaliKualitas(kualitas: Kualitas, config: GameConfig): Double {
        return when (kualitas) {
            Kualitas.BAIK -> config.pengaliBaik
            Kualitas.BIASA -> config.pengaliBiasa
            Kualitas.RUSAK -> config.pengaliRusak
        }
    }

    /**
     * Menghitung harga jual per item: round(hargaDasar * pengali).
     */
    fun hitungHargaJual(hargaDasar: Int, kualitas: Kualitas, config: GameConfig): Int {
        val pengali = pengaliKualitas(kualitas, config)
        return round(hargaDasar * pengali).toInt()
    }

    /**
     * Menjual seluruh isi shipping bin (SPEC bagian 8 langkah 1).
     * Mengembalikan total pendapatan, menambah uang pemain, dan mengosongkan bin.
     */
    fun jualIsiBin(
        bin: MutableMap<Pair<String, Kualitas>, Int>,
        catalog: Catalog,
        config: GameConfig
    ): Int {
        var totalPendapatan = 0
        for ((kunci, jumlah) in bin) {
            val (cropId, kualitas) = kunci
            val crop = catalog.cropById[cropId] ?: continue
            val hargaSatuan = hitungHargaJual(crop.hargaJual, kualitas, config)
            totalPendapatan += hargaSatuan * jumlah
        }
        bin.clear()
        return totalPendapatan
    }
}
