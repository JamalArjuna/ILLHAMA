package core

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// DTO crops.json
@Serializable
data class CropData(
    val id: String,
    val nama: String,
    val hargaBiji: Int,
    val hariTumbuh: Int,
    val hargaJual: Int,
    val hama: List<String>,
    val spriteBaris: Int,
    val ikonPanen: Int
)

// DTO pests.json
@Serializable
data class PestData(
    val id: String,
    val nama: String,
    val namaIlmiah: String,
    val tanaman: String,
    val metodeBenar: String,
    val spriteIndex: Int,
    val gejala: String,
    val ciri: String,
    val caraKendali: String,
    val tipGame: String
)

// DTO items.json
@Serializable
data class AlatData(val id: String, val nama: String, val slot: Int, val ikon: Int)

@Serializable
data class BarangData(val id: String, val nama: String, val harga: Int, val ikon: Int)

@Serializable
data class ItemsData(
    val alat: List<AlatData>,
    val barang: List<BarangData>,
    val urutanDiToko: List<String>
)

// Teks Journal (nested di texts_id.json)
@Serializable
data class JournalLabelTexts(
    val tanaman: String = "Menyerang",
    val ciri: String = "Ciri",
    val gejala: String = "Gejala",
    val caraKendali: String = "Cara kendali"
)

@Serializable
data class JournalTexts(
    val judul: String = "Pest Journal",
    val terkunci: String = "???",
    val terkunciDeskripsi: String = "Periksa hama ini dengan Kaca Pembesar untuk membuka catatannya.",
    val label: JournalLabelTexts = JournalLabelTexts(),
    val petunjuk: String = "Kiri/Kanan atau Atas/Bawah: pilih   J atau Esc: tutup",
    val terbuka: String = "{terbuka}/{total} terbuka"
)

// Teks panel Pemeriksaan (kaca pembesar)
@Serializable
data class PeriksaTexts(
    val judul: String = "Hasil pemeriksaan",
    val gejala: String = "Gejala: {gejala}",
    val jenis: String = "Hama: {nama} ({ilmiah})",
    val petunjuk: String = "Pilih cara pengendalian yang tepat. Detailnya ada di Journal (tekan J)."
)

@Serializable
data class TutorialStep(
    val id: String,
    val teks: String,
    val selesaiKetika: String
)

@Serializable
data class AkhirTexts(
    val judul: String = "Musim Selesai",
    val uang: String = "Uang akhir: Rp {uang}",
    val journal: String = "Journal terbuka: {terbuka}/{total}",
    val rating: String = "Rating: {bintang} bintang",
    val pesan: Map<String, String> = emptyMap(),
    val ulang: String = "Enter: main lagi   Esc: menu utama"
)

// DTO texts_id.json
@Serializable
data class TextsData(
    val judulGame: String,
    val toast: Map<String, String> = emptyMap(),
    val kualitas: Map<String, String> = emptyMap(),
    val menu: Map<String, String> = emptyMap(),
    val hud: Map<String, String> = emptyMap(),
    val tidur: Map<String, String> = emptyMap(),
    val toko: Map<String, String> = emptyMap(),
    val bin: Map<String, String> = emptyMap(),
    val periksa: PeriksaTexts = PeriksaTexts(),
    val journal: JournalTexts = JournalTexts(),
    val ringkasan: Map<String, String> = emptyMap(),
    val akhir: AkhirTexts = AkhirTexts(),
    val tutorial: List<TutorialStep> = emptyList(),
    val petunjukKejadian: Map<String, String> = emptyMap(),
    val kredit: Map<String, String> = emptyMap()
)

/**
 * Catalog memuat semua data tanaman, hama, item, dan teks.
 * Tidak bergantung pada KorGE; pemuatan dilakukan lewat string JSON yang dipasok dari luar.
 */
class Catalog private constructor(
    val crops: List<CropData>,
    val pests: List<PestData>,
    val items: ItemsData,
    val texts: TextsData
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun dari(
            cropsJson: String,
            pestsJson: String,
            itemsJson: String,
            textsJson: String
        ): Catalog {
            val crops = json.decodeFromString<List<CropData>>(cropsJson)
            val pests = json.decodeFromString<List<PestData>>(pestsJson)
            val items = json.decodeFromString<ItemsData>(itemsJson)
            val texts = json.decodeFromString<TextsData>(textsJson)
            return Catalog(crops, pests, items, texts)
        }
    }

    // Indeks cepat
    val cropById: Map<String, CropData> = crops.associateBy { it.id }
    val pestById: Map<String, PestData> = pests.associateBy { it.id }

    fun cropDariId(id: String): CropData = cropById[id] ?: error("Crop tidak ditemukan: $id")
    fun pestDariId(id: String): PestData = pestById[id] ?: error("Pest tidak ditemukan: $id")

    fun validasiRujukan(): List<String> {
        val errors = mutableListOf<String>()
        for (crop in crops) {
            for (hamaId in crop.hama) {
                if (!pestById.containsKey(hamaId)) {
                    errors += "Crop '${crop.id}' merujuk hama '$hamaId' yang tidak ada di pests.json"
                }
            }
        }
        return errors
    }
}
