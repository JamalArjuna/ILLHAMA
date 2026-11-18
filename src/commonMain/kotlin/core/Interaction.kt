package core

/**
 * Hasil dari penggunaan alat pada petak.
 */
sealed class InteractionResult {
    data class Sukses(val sfx: String? = null) : InteractionResult()
    data class Gagal(val toastKey: String) : InteractionResult()
    object TidakAdaAksi : InteractionResult()
}

/**
 * Aturan interaksi alat x petak (SPEC bagian 7).
 * Murni logika Kotlin tanpa dependensi KorGE.
 */
object Interaction {

    /**
     * Menggunakan alat terpilih (slot 1 sampai 6) pada petak sasaran.
     */
    fun pakaiAlat(
        slot: Int,
        petak: Petak?,
        inventory: Inventory,
        selectedCropId: String?
    ): InteractionResult {
        if (petak == null) return InteractionResult.TidakAdaAksi

        // Alat tidak berefek pada petak berisi tanaman mati kecuali kaca pembesar
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
            6 -> { // Biji
                if (selectedCropId == null) {
                    return InteractionResult.Gagal("bijiHabis")
                }
                val stokBiji = inventory.biji[selectedCropId] ?: 0
                if (stokBiji <= 0) {
                    return InteractionResult.Gagal("bijiHabis")
                }

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
}
