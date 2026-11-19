package core

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class TanamanSave(
    val cropId: String,
    val hariTumbuh: Int,
    val kesehatan: Int,
    val hamaId: String? = null,
    val hamaDiperiksa: Boolean = false,
    val penangananTertunda: String? = null,
    val pakaiKimia: Boolean = false,
    val mati: Boolean = false
)

@Serializable
data class PetakSave(
    val x: Int,
    val y: Int,
    val tanah: String,
    val disiramHariIni: Boolean,
    val tanaman: TanamanSave? = null
)

@Serializable
data class HasilPanenSave(
    val cropId: String,
    val kualitas: String,
    val jumlah: Int
)

@Serializable
data class InventorySave(
    val biji: Map<String, Int>,
    val nabati: Int,
    val kimia: Int,
    val perangkap: Int,
    val hasil: List<HasilPanenSave>
)

@Serializable
data class GameSaveState(
    val versi: Int = 1,
    val hari: Int,
    val menitHari: Int,
    val uang: Int,
    val inventory: InventorySave,
    val bin: List<HasilPanenSave>,
    val petak: List<PetakSave>,
    val journalTerbuka: List<String>,
    val tutorialIndex: Int,
    val modeSemprot: String,
    val pendapatanKemarin: Int = 0,
    val hamaPertamaMuncul: Boolean = false,
    val matangPertamaMuncul: Boolean = false
)

/**
 * Logika murni serialisasi dan deserialisasi save data ke JSON string.
 * SPEC bagian 10.
 */
object SaveData {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    const val KUNCI_SAVE = "save_v1"
    const val VERSI_SAAT_INI = 1

    fun serialize(state: GameState): String {
        val inv = state.inventory
        val invSave = InventorySave(
            biji = inv.biji.toMap(),
            nabati = inv.nabati,
            kimia = inv.kimia,
            perangkap = inv.perangkap,
            hasil = inv.hasil.map { (kunci, jml) ->
                HasilPanenSave(kunci.first, kunci.second.name, jml)
            }
        )

        val binSave = state.bin.map { (kunci, jml) ->
            HasilPanenSave(kunci.first, kunci.second.name, jml)
        }

        val petakSave = state.petak.map { p ->
            val t = p.tanaman
            val tSave = if (t != null) {
                TanamanSave(
                    cropId = t.cropId,
                    hariTumbuh = t.hariTumbuh,
                    kesehatan = t.kesehatan,
                    hamaId = t.hamaId,
                    hamaDiperiksa = t.hamaDiperiksa,
                    penangananTertunda = t.penangananTertunda?.name,
                    pakaiKimia = t.pakaiKimia,
                    mati = t.mati
                )
            } else null

            PetakSave(
                x = p.x,
                y = p.y,
                tanah = p.tanah.name,
                disiramHariIni = p.disiramHariIni,
                tanaman = tSave
            )
        }

        val saveState = GameSaveState(
            versi = VERSI_SAAT_INI,
            hari = state.hari,
            menitHari = state.menitHari,
            uang = state.uang,
            inventory = invSave,
            bin = binSave,
            petak = petakSave,
            journalTerbuka = state.journalTerbuka.toList(),
            tutorialIndex = state.tutorialIndex,
            modeSemprot = state.modeSemprot.name,
            pendapatanKemarin = state.pendapatanKemarin,
            hamaPertamaMuncul = state.hamaPertamaMuncul,
            matangPertamaMuncul = state.matangPertamaMuncul
        )

        return json.encodeToString(saveState)
    }

    fun deserialize(jsonString: String?): GameState? {
        if (jsonString.isNullOrBlank()) return null
        return try {
            val save = json.decodeFromString<GameSaveState>(jsonString)
            if (save.versi != VERSI_SAAT_INI) return null

            val inventory = Inventory(
                biji = save.inventory.biji.toMutableMap(),
                nabati = save.inventory.nabati,
                kimia = save.inventory.kimia,
                perangkap = save.inventory.perangkap,
                hasil = save.inventory.hasil.associate { h ->
                    Pair(h.cropId, Kualitas.valueOf(h.kualitas)) to h.jumlah
                }.toMutableMap()
            )

            val bin = save.bin.associate { h ->
                Pair(h.cropId, Kualitas.valueOf(h.kualitas)) to h.jumlah
            }.toMutableMap()

            val petak = save.petak.map { ps ->
                val tan = ps.tanaman?.let { ts ->
                    Tanaman(
                        cropId = ts.cropId,
                        hariTumbuh = ts.hariTumbuh,
                        kesehatan = ts.kesehatan,
                        hamaId = ts.hamaId,
                        hamaDiperiksa = ts.hamaDiperiksa,
                        penangananTertunda = ts.penangananTertunda?.let { Metode.valueOf(it) },
                        pakaiKimia = ts.pakaiKimia,
                        mati = ts.mati
                    )
                }
                Petak(
                    x = ps.x,
                    y = ps.y,
                    tanah = TanahState.valueOf(ps.tanah),
                    disiramHariIni = ps.disiramHariIni,
                    tanaman = tan
                )
            }

            GameState(
                hari = save.hari,
                menitHari = save.menitHari,
                uang = save.uang,
                inventory = inventory,
                bin = bin,
                petak = petak,
                journalTerbuka = save.journalTerbuka.toMutableSet(),
                tutorialIndex = save.tutorialIndex,
                modeSemprot = Metode.valueOf(save.modeSemprot),
                pendapatanKemarin = save.pendapatanKemarin,
                hamaPertamaMuncul = save.hamaPertamaMuncul,
                matangPertamaMuncul = save.matangPertamaMuncul
            )
        } catch (_: Throwable) {
            // Abaikan save rusak dan kembalikan null tanpa crash
            null
        }
    }
}
