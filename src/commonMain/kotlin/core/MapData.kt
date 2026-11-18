package core

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class MapJsonObjek(
    val id: String,
    val jenis: String,
    val tile: List<Int>,
    val berdiriDi: List<Int>
)

@Serializable
data class MapJsonLahan(
    val originTile: List<Int>,
    val kolom: Int,
    val baris: Int
)

@Serializable
data class MapJsonData(
    val tileSize: Int,
    val kolom: Int,
    val baris: Int,
    val ground: List<List<Int>>,
    val tileTerblokir: List<Int> = listOf(6, 7, 8, 9, 10, 11, 12, 13, 14, 15),
    val lahan: MapJsonLahan,
    val spawn: List<Int>,
    val objek: List<MapJsonObjek> = emptyList()
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun dari(jsonString: String): MapJsonData =
            json.decodeFromString(serializer(), jsonString)
    }

    val tileTerblokirSet: Set<Int> by lazy { tileTerblokir.toSet() }
}
