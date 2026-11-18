package core

/**
 * Pengelola waktu harian dan simulasi pergantian hari.
 * Murni logika Kotlin tanpa dependensi KorGE.
 */
object DaySimulator {

    /**
     * Menghitung rasio menit game per detik nyata.
     * Berdasarkan SPEC: 120 detik nyata = 06:00 sampai 22:00 (16 jam = 960 menit game).
     * 960 / 120 = 8.0 menit game per detik nyata.
     */
    fun hitungMenitGamePerDetik(detikNyataPerHari: Int, jamMulai: Int, jamTidurOtomatis: Int): Double {
        val totalMenitGame = (jamTidurOtomatis - jamMulai) * 60.0
        return totalMenitGame / detikNyataPerHari.toDouble()
    }

    /**
     * Konversi detik nyata ke total menit game untuk suatu durasi.
     */
    fun detikNyataKeMenitGame(
        detikNyata: Double,
        detikNyataPerHari: Int,
        jamMulai: Int,
        jamTidurOtomatis: Int
    ): Double {
        val rasio = hitungMenitGamePerDetik(detikNyataPerHari, jamMulai, jamTidurOtomatis)
        return detikNyata * rasio
    }

    /**
     * Format menit sejak 00:00 menjadi format teks "HH:MM".
     * Contoh: 360 -> "06:00", 750 -> "12:30", 1320 -> "22:00".
     */
    fun formatWaktu(menitHari: Int): String {
        val jam = (menitHari / 60) % 24
        val menit = menitHari % 60
        val jamStr = if (jam < 10) "0$jam" else "$jam"
        val menitStr = if (menit < 10) "0$menit" else "$menit"
        return "$jamStr:$menitStr"
    }

    /**
     * Memajukan waktu game berdasarkan delta detik nyata (dt).
     * Mengembalikan true jika jam telah mencapai atau melewati jamTidurOtomatis (22:00).
     */
    fun tickWaktu(
        state: GameState,
        config: GameConfig,
        dt: Double,
        akumulatorMenit: DoubleArray
    ): Boolean {
        val menitPerDetik = hitungMenitGamePerDetik(
            config.detikNyataPerHari,
            config.jamMulai,
            config.jamTidurOtomatis
        )
        akumulatorMenit[0] += dt * menitPerDetik
        val menitTambah = akumulatorMenit[0].toInt()
        if (menitTambah > 0) {
            state.menitHari += menitTambah
            akumulatorMenit[0] -= menitTambah.toDouble()
        }

        val batasTidur = config.jamTidurOtomatis * 60
        return state.menitHari >= batasTidur
    }

    /**
     * Menjalankan pergantian hari (tickHari).
     * Pada Milestone M1:
     * - Hari bertambah 1
     * - Jam direset ke jamMulai (06:00)
     * Mengembalikan true jika musim telah berakhir (hari > jumlahHariMusim).
     */
    fun tickHari(state: GameState, config: GameConfig): Boolean {
        state.hari += 1
        state.menitHari = config.jamMulai * 60
        return state.hari > config.jumlahHariMusim
    }
}
