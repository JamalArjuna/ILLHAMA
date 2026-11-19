package core

/**
 * Pengelola waktu harian dan simulasi pergantian hari.
 * Murni logika Kotlin tanpa dependensi KorGE.
 */
object DaySimulator {

    fun hitungMenitGamePerDetik(detikNyataPerHari: Int, jamMulai: Int, jamTidurOtomatis: Int): Double {
        val totalMenitGame = (jamTidurOtomatis - jamMulai) * 60.0
        return totalMenitGame / detikNyataPerHari.toDouble()
    }

    fun detikNyataKeMenitGame(
        detikNyata: Double,
        detikNyataPerHari: Int,
        jamMulai: Int,
        jamTidurOtomatis: Int
    ): Double {
        val rasio = hitungMenitGamePerDetik(detikNyataPerHari, jamMulai, jamTidurOtomatis)
        return detikNyata * rasio
    }

    fun formatWaktu(menitHari: Int): String {
        val jam = (menitHari / 60) % 24
        val menit = menitHari % 60
        val jamStr = if (jam < 10) "0$jam" else "$jam"
        val menitStr = if (menit < 10) "0$menit" else "$menit"
        return "$jamStr:$menitStr"
    }

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
     * Urutan sesuai SPEC bagian 8 (Langkah 1–9).
     * @param random Dapat disuntik seed untuk unit test deterministik.
     */
    fun tickHari(
        state: GameState,
        config: GameConfig,
        catalog: Catalog? = null,
        random: kotlin.random.Random = kotlin.random.Random
    ): Boolean {
        val hariSebelumnya = state.hari

        // Langkah 1: Jual isi bin
        var pendapatan = 0
        if (catalog != null) {
            pendapatan = Economy.jualIsiBin(state.bin, catalog, config)
            state.uang += pendapatan
            state.pendapatanKemarin = pendapatan
        }

        // Langkah 2: Selesaikan penanganan tertunda (nabati / perangkap)
        var hamaDitangani = 0
        for (petak in state.petak) {
            val t = petak.tanaman ?: continue
            if (t.penangananTertunda != null) {
                t.hamaId = null
                t.hamaDiperiksa = false
                t.penangananTertunda = null
                hamaDitangani += 1
            }
        }

        // Langkah 3: Pertumbuhan
        for (petak in state.petak) {
            val t = petak.tanaman ?: continue
            if (!t.mati && petak.disiramHariIni) {
                t.hariTumbuh += 1
            }
        }

        // Langkah 4: Keringkan tanah
        for (petak in state.petak) {
            petak.disiramHariIni = false
        }

        // Langkah 5: Dampak hama (kurangi kesehatan, matikan bila <= 0)
        var tanamanMati = 0
        for (petak in state.petak) {
            val t = petak.tanaman ?: continue
            if (!t.mati && t.hamaId != null) {
                t.kesehatan -= config.kerusakanKesehatanPerHari
                if (t.kesehatan <= 0) {
                    t.kesehatan = 0
                    t.mati = true
                    t.hamaId = null
                    t.hamaDiperiksa = false
                    tanamanMati += 1
                }
            }
        }

        // Simpan data ringkasan harian
        state.summaryKemarin = DaySummary(
            hariSebelumnya = hariSebelumnya,
            pendapatan = pendapatan,
            hamaDitangani = hamaDitangani,
            tanamanMati = tanamanMati
        )

        // Langkah 6: Penyebaran hama
        // Ambil snapshot SEBELUM langkah ini agar yang baru tertular tidak ikut menyebar
        val petakBerhamaSnapshot = state.petak.filter { p ->
            p.tanaman?.let { !it.mati && it.hamaId != null } == true
        }
        val arahEmpat = listOf(Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1))
        for (petak in petakBerhamaSnapshot) {
            if (random.nextDouble() < config.peluangMenyebar) {
                val t = petak.tanaman ?: continue
                val hamaId = t.hamaId ?: continue
                val arahDipilih = arahEmpat[random.nextInt(4)]
                val nx = petak.x + arahDipilih.first
                val ny = petak.y + arahDipilih.second
                val tetangga = state.petakAt(nx, ny)
                if (tetangga != null) {
                    val tt = tetangga.tanaman
                    if (tt != null && !tt.mati && tt.hamaId == null && tt.cropId == t.cropId) {
                        tt.hamaId = hamaId
                        tt.hamaDiperiksa = false
                    }
                }
            }
        }

        // Langkah 7: Hama baru muncul (peluang 20%, hanya tahap >= tahapMinimalMuncul)
        if (catalog != null) {
            for (petak in state.petak) {
                val t = petak.tanaman ?: continue
                if (!t.mati && t.hamaId == null) {
                    val crop = catalog.cropById[t.cropId] ?: continue
                    if (crop.hama.isEmpty()) continue
                    val tahap = hitungTahapGambar(false, t.hariTumbuh, crop.hariTumbuh)
                    if (tahap >= config.tahapMinimalMuncul) {
                        if (random.nextDouble() < config.peluangMunculHarian) {
                            t.hamaId = crop.hama[random.nextInt(crop.hama.size)]
                            t.hamaDiperiksa = false
                        }
                    }
                }
            }
        }

        // Langkah 8: Hari naik, jam reset
        state.hari += 1
        state.menitHari = config.jamMulai * 60

        // Langkah 9: Akhir musim
        return state.hari > config.jumlahHariMusim
    }
}
