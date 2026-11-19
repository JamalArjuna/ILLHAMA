package core

/**
 * Hasil dari penggunaan alat pada petak (Spasi).
 */
sealed class InteractionResult {
    /** Aksi berhasil. sfx: kunci efek suara; toastKey: kunci teks toast (opsional). */
    data class Sukses(val sfx: String? = null, val toastKey: String? = null) : InteractionResult()
    /** Aksi gagal, tampilkan toast. */
    data class Gagal(val toastKey: String) : InteractionResult()
    /** Hasil pemeriksaan kaca pembesar: tanaman berhama. */
    data class PeriksaHama(val pestId: String, val jurnalBaru: Boolean) : InteractionResult()
    /** Hasil pemeriksaan kaca pembesar: tanaman sehat. */
    data class PeriksaSehat(val kesehatan: Int) : InteractionResult()
    object TidakAdaAksi : InteractionResult()
}

/**
 * Hasil interaksi tangan (E) pada petak.
 */
sealed class HandResult {
    data class PanenSukses(val cropId: String, val namaCrop: String, val kualitas: Kualitas) : HandResult()
    object BersihkanSukses : HandResult()
    /** Hama diambil dengan tangan (metodeBenar == manual). */
    object ManualBerhasil : HandResult()
    data class Gagal(val toastKey: String) : HandResult()
    object TidakAdaAksi : HandResult()
}

/**
 * Item yang dijual di toko.
 */
data class ShopItem(
    val id: String,
    val nama: String,
    val harga: Int,
    val ikon: Int,
    val jenis: String,
    val targetId: String
)

/**
 * Logika toko dan pembelian barang.
 */
object Shop {
    fun daftarItem(catalog: Catalog): List<ShopItem> {
        val list = mutableListOf<ShopItem>()
        for (entry in catalog.items.urutanDiToko) {
            val parts = entry.split(":")
            if (parts.size == 2) {
                val jenis = parts[0]
                val targetId = parts[1]
                if (jenis == "biji") {
                    val crop = catalog.cropById[targetId]
                    if (crop != null) {
                        list.add(ShopItem(entry, "Biji ${crop.nama}", crop.hargaBiji, crop.ikonPanen, jenis, targetId))
                    }
                } else if (jenis == "barang") {
                    val barang = catalog.items.barang.find { it.id == targetId }
                    if (barang != null) {
                        list.add(ShopItem(entry, barang.nama, barang.harga, barang.ikon, jenis, targetId))
                    }
                }
            }
        }
        return list
    }

    fun jumlahDimiliki(item: ShopItem, inventory: Inventory): Int {
        return if (item.jenis == "biji") {
            inventory.biji[item.targetId] ?: 0
        } else {
            when (item.targetId) {
                "nabati" -> inventory.nabati
                "perangkap" -> inventory.perangkap
                "kimia" -> inventory.kimia
                else -> 0
            }
        }
    }

    fun beli(item: ShopItem, state: GameState): Boolean {
        if (state.uang < item.harga) return false
        state.uang -= item.harga
        if (item.jenis == "biji") {
            state.inventory.biji[item.targetId] = (state.inventory.biji[item.targetId] ?: 0) + 1
        } else {
            when (item.targetId) {
                "nabati" -> state.inventory.nabati += 1
                "perangkap" -> state.inventory.perangkap += 1
                "kimia" -> state.inventory.kimia += 1
            }
        }
        return true
    }
}

/**
 * Aturan interaksi alat dan tangan pada petak (SPEC bagian 7).
 * Murni logika Kotlin tanpa dependensi KorGE.
 */
object Interaction {

    /**
     * Menggunakan alat terpilih (slot 1–6) pada petak sasaran (Spasi).
     * Parameter M4 (modeSemprot, journalTerbuka, catalog) opsional untuk kompatibilitas mundur.
     */
    fun pakaiAlat(
        slot: Int,
        petak: Petak?,
        inventory: Inventory,
        selectedCropId: String?,
        // Parameter M4:
        modeSemprot: Metode = Metode.NABATI,
        journalTerbuka: MutableSet<String> = mutableSetOf(),
        catalog: Catalog? = null
    ): InteractionResult {
        if (petak == null) return InteractionResult.TidakAdaAksi

        // Alat tidak berefek pada tanaman mati kecuali kaca pembesar (slot 3)
        if (petak.tanaman?.mati == true && slot != 3) {
            return InteractionResult.TidakAdaAksi
        }

        return when (slot) {
            1 -> { // Cangkul
                if (petak.tanah == TanahState.BELUM_DIOLAH) {
                    petak.tanah = TanahState.DIOLAH
                    InteractionResult.Sukses(sfx = "sfx_cangkul")
                } else {
                    InteractionResult.Gagal("sudahDiolah")
                }
            }
            2 -> { // Penyiram
                if (petak.tanah == TanahState.BELUM_DIOLAH) {
                    InteractionResult.Gagal("tanahBelumDiolah")
                } else {
                    petak.disiramHariIni = true
                    InteractionResult.Sukses(sfx = "sfx_siram")
                }
            }
            3 -> { // Kaca pembesar
                val t = petak.tanaman
                if (t == null || t.mati) return InteractionResult.TidakAdaAksi
                val hamaId = t.hamaId
                if (hamaId != null) {
                    val jurnalBaru = !journalTerbuka.contains(hamaId)
                    t.hamaDiperiksa = true
                    journalTerbuka.add(hamaId)
                    InteractionResult.PeriksaHama(hamaId, jurnalBaru)
                } else {
                    InteractionResult.PeriksaSehat(t.kesehatan)
                }
            }
            4 -> { // Penyemprot (nabati atau kimia)
                val t = petak.tanaman
                if (t == null || t.hamaId == null) return InteractionResult.Gagal("tidakAdaHama")
                if (modeSemprot == Metode.NABATI) {
                    // Nabati: selalu habis bahan, lalu cek metode
                    if (inventory.nabati <= 0) return InteractionResult.Gagal("bahanHabis")
                    inventory.nabati -= 1
                    val pest = catalog?.pestById?.get(t.hamaId)
                    if (pest?.metodeBenar == "nabati") {
                        t.penangananTertunda = Metode.NABATI
                        InteractionResult.Sukses(toastKey = "nabatiDisemprot")
                    } else {
                        InteractionResult.Gagal("metodeSalah")
                    }
                } else { // KIMIA: langsung hapus hama
                    if (inventory.kimia <= 0) return InteractionResult.Gagal("bahanHabis")
                    inventory.kimia -= 1
                    t.hamaId = null
                    t.hamaDiperiksa = false
                    t.penangananTertunda = null
                    t.pakaiKimia = true
                    InteractionResult.Sukses(toastKey = "kimiaDipakai")
                }
            }
            5 -> { // Perangkap
                val t = petak.tanaman
                if (t == null || t.hamaId == null) return InteractionResult.Gagal("tidakAdaHama")
                // Perangkap: selalu habis bahan, lalu cek metode
                if (inventory.perangkap <= 0) return InteractionResult.Gagal("bahanHabis")
                inventory.perangkap -= 1
                val pest = catalog?.pestById?.get(t.hamaId)
                if (pest?.metodeBenar == "perangkap") {
                    t.penangananTertunda = Metode.PERANGKAP
                    InteractionResult.Sukses(toastKey = "perangkapDipasang")
                } else {
                    InteractionResult.Gagal("metodeSalah")
                }
            }
            6 -> { // Biji
                if (selectedCropId == null) return InteractionResult.Gagal("bijiHabis")
                val stokBiji = inventory.biji[selectedCropId] ?: 0
                if (stokBiji <= 0) return InteractionResult.Gagal("bijiHabis")
                if (petak.tanah == TanahState.BELUM_DIOLAH) {
                    InteractionResult.Gagal("perluDiolah")
                } else if (petak.tanaman != null) {
                    InteractionResult.Gagal("petakTerisi")
                } else {
                    petak.tanaman = Tanaman(cropId = selectedCropId, hariTumbuh = 0, kesehatan = 100)
                    inventory.biji[selectedCropId] = stokBiji - 1
                    InteractionResult.Sukses()
                }
            }
            else -> InteractionResult.TidakAdaAksi
        }
    }

    /**
     * Interaksi tangan (E) pada petak sasaran (SPEC bagian 7):
     * - Mati: bersihkan
     * - Matang hidup: panen (termasuk saat ada hama)
     * - Berhama dan metodeBenar == manual: hapus hama
     * - Berhama dan metode bukan manual: metodeSalah
     * - Belum matang tanpa hama: belumMatang
     */
    fun interaksiTangan(
        petak: Petak?,
        catalog: Catalog,
        config: GameConfig,
        inventory: Inventory
    ): HandResult {
        if (petak == null) return HandResult.TidakAdaAksi
        val tanaman = petak.tanaman ?: return HandResult.TidakAdaAksi

        if (tanaman.mati) {
            petak.tanaman = null
            petak.tanah = TanahState.DIOLAH
            return HandResult.BersihkanSukses
        }

        val crop = catalog.cropById[tanaman.cropId] ?: return HandResult.TidakAdaAksi

        if (tanaman.hariTumbuh >= crop.hariTumbuh) {
            // Panen (boleh saat ada hama, kualitas mengikuti kesehatan)
            val kualitas = Economy.hitungKualitas(tanaman.kesehatan, tanaman.pakaiKimia, config)
            val kunci = Pair(tanaman.cropId, kualitas)
            inventory.hasil[kunci] = (inventory.hasil[kunci] ?: 0) + 1
            petak.tanaman = null
            petak.tanah = TanahState.DIOLAH
            petak.disiramHariIni = false
            return HandResult.PanenSukses(tanaman.cropId, crop.nama, kualitas)
        }

        // Belum matang — cek hama
        if (tanaman.hamaId != null) {
            val pest = catalog.pestById[tanaman.hamaId]
            return if (pest?.metodeBenar == "manual") {
                tanaman.hamaId = null
                tanaman.hamaDiperiksa = false
                HandResult.ManualBerhasil
            } else {
                HandResult.Gagal("metodeSalah")
            }
        }

        return HandResult.Gagal("belumMatang")
    }

    /**
     * Memindahkan seluruh hasil panen dari inventory ke shipping bin.
     * Mengembalikan jumlah item yang berhasil dimasukkan.
     */
    fun masukkanKeBin(
        inventory: Inventory,
        bin: MutableMap<Pair<String, Kualitas>, Int>
    ): Int {
        if (inventory.hasil.isEmpty()) return 0
        var total = 0
        for ((kunci, jumlah) in inventory.hasil) {
            bin[kunci] = (bin[kunci] ?: 0) + jumlah
            total += jumlah
        }
        inventory.hasil.clear()
        return total
    }
}
