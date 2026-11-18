package core

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// DTO untuk deserialisasi constants.json
@Serializable
data class ConstantsTampilan(
    val virtualWidth: Int,
    val virtualHeight: Int,
    val tileSize: Int,
    val skalaJendela: Int,
    val mapKolom: Int,
    val mapBaris: Int
)

@Serializable
data class ConstantsWaktu(
    val detikNyataPerHari: Int,
    val jamMulai: Int,
    val jamTidurOtomatis: Int,
    val jumlahHariMusim: Int
)

@Serializable
data class ConstantsHitbox(
    val lebar: Int,
    val tinggi: Int,
    val offsetX: Int,
    val offsetY: Int
)

@Serializable
data class ConstantsPemain(
    val uangAwal: Int,
    val kecepatanPxPerDetik: Int,
    val hitbox: ConstantsHitbox = ConstantsHitbox(10, 8, 3, 8)
)

@Serializable
data class ConstantsHama(
    val peluangMunculHarian: Double,
    val tahapMinimalMuncul: Int,
    val peluangMenyebar: Double,
    val kerusakanKesehatanPerHari: Int
)

@Serializable
data class ConstantsKualitas(
    val batasBaik: Int,
    val batasBiasa: Int,
    val pengaliBaik: Double,
    val pengaliBiasa: Double,
    val pengaliRusak: Double,
    val kimiaMaksimalKualitas: String
)

@Serializable
data class ConstantsRating(
    val bintang1: Int,
    val bintang2: Int,
    val bintang3: Int,
    val journalPenuhUntukBintang3: Int
)

@Serializable
data class ConstantsUi(
    val durasiToastDetik: Double
)

@Serializable
data class ConstantsDebug(
    val aktif: Boolean
)

@Serializable
data class ConstantsTanaman(
    val jumlahTahapGambar: Int,
    val kesehatanAwal: Int
)

@Serializable
data class ConstantsLahan(
    val kolom: Int,
    val baris: Int,
    val originTile: List<Int>
)

@Serializable
data class ConstantsRoot(
    val tampilan: ConstantsTampilan,
    val waktu: ConstantsWaktu,
    val pemain: ConstantsPemain,
    val lahan: ConstantsLahan,
    val tanaman: ConstantsTanaman,
    val hama: ConstantsHama,
    val kualitas: ConstantsKualitas,
    val rating: ConstantsRating,
    val ui: ConstantsUi,
    val debug: ConstantsDebug
)

/**
 * GameConfig memuat constants.json dan menyediakan akses ke semua konstanta permainan.
 * Tidak bergantung pada KorGE; pemuatan dilakukan lewat string JSON yang dipasok dari luar.
 */
class GameConfig private constructor(val data: ConstantsRoot) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun dari(jsonString: String): GameConfig {
            val parsed = json.decodeFromString(ConstantsRoot.serializer(), jsonString)
            return GameConfig(parsed)
        }
    }

    // Shortcuts
    val virtualWidth get() = data.tampilan.virtualWidth
    val virtualHeight get() = data.tampilan.virtualHeight
    val tileSize get() = data.tampilan.tileSize
    val skalaJendela get() = data.tampilan.skalaJendela
    val mapKolom get() = data.tampilan.mapKolom
    val mapBaris get() = data.tampilan.mapBaris

    val detikNyataPerHari get() = data.waktu.detikNyataPerHari
    val jamMulai get() = data.waktu.jamMulai
    val jamTidurOtomatis get() = data.waktu.jamTidurOtomatis
    val jumlahHariMusim get() = data.waktu.jumlahHariMusim

    val uangAwal get() = data.pemain.uangAwal
    val kecepatanPxPerDetik get() = data.pemain.kecepatanPxPerDetik
    val hitboxLebar get() = data.pemain.hitbox.lebar
    val hitboxTinggi get() = data.pemain.hitbox.tinggi
    val hitboxOffsetX get() = data.pemain.hitbox.offsetX
    val hitboxOffsetY get() = data.pemain.hitbox.offsetY

    val lahanKolom get() = data.lahan.kolom
    val lahanBaris get() = data.lahan.baris
    val lahanOriginTile get() = data.lahan.originTile

    val peluangMunculHarian get() = data.hama.peluangMunculHarian
    val tahapMinimalMuncul get() = data.hama.tahapMinimalMuncul
    val peluangMenyebar get() = data.hama.peluangMenyebar
    val kerusakanKesehatanPerHari get() = data.hama.kerusakanKesehatanPerHari

    val batasBaik get() = data.kualitas.batasBaik
    val batasBiasa get() = data.kualitas.batasBiasa
    val pengaliBaik get() = data.kualitas.pengaliBaik
    val pengaliBiasa get() = data.kualitas.pengaliBiasa
    val pengaliRusak get() = data.kualitas.pengaliRusak

    val bintang1 get() = data.rating.bintang1
    val bintang2 get() = data.rating.bintang2
    val bintang3 get() = data.rating.bintang3
    val journalPenuhUntukBintang3 get() = data.rating.journalPenuhUntukBintang3

    val durasiToastDetik get() = data.ui.durasiToastDetik
    val debugAktif get() = data.debug.aktif
}
