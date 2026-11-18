package view

import korlibs.image.bitmap.*
import korlibs.korge.view.*
import korlibs.math.geom.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
internal data class MapJsonObjek(
    val id: String,
    val jenis: String,
    val tile: List<Int>,
    val berdiriDi: List<Int>
)

@Serializable
internal data class MapJsonLahan(
    val originTile: List<Int>,
    val kolom: Int,
    val baris: Int
)

@Serializable
internal data class MapJsonData(
    val tileSize: Int,
    val kolom: Int,
    val baris: Int,
    val ground: List<List<Int>>,
    val lahan: MapJsonLahan,
    val spawn: List<Int>,
    val objek: List<MapJsonObjek> = emptyList()
)

/**
 * MapView merender peta dari map.json menggunakan tileset.png.
 * Setiap tile dipotong dari tileset (4 kolom x 4 baris, ukuran 16x16) dan digambar
 * ke posisinya di layar menggunakan nearest-neighbor (smoothing = false).
 *
 * Harus dipanggil setelah bitmap tileset sudah dimuat.
 */
class MapView(
    private val tilesetBitmap: Bitmap,
    private val mapJsonString: String,
    tileSize: Int = 16
) : Container() {

    private val tileW: Int = tileSize
    private val tileH: Int = tileSize
    // Kolom tileset = 64/16 = 4
    private val tilesetKolom = tilesetBitmap.width / tileW

    val spawnTile: Pair<Int, Int>

    init {
        val json = Json { ignoreUnknownKeys = true }
        val mapData = json.decodeFromString(MapJsonData.serializer(), mapJsonString)
        spawnTile = Pair(mapData.spawn[0], mapData.spawn[1])
        renderMap(mapData)
    }

    private fun renderMap(mapData: MapJsonData) {
        removeChildren()

        // Pre-slice 16 tile dari tileset (4 kolom x 4 baris)
        val totalTiles = tilesetKolom * (tilesetBitmap.height / tileH)
        val tileSlices = Array(totalTiles) { tileId ->
            val tx = (tileId % tilesetKolom) * tileW
            val ty = (tileId / tilesetKolom) * tileH
            tilesetBitmap.slice(RectangleInt(tx, ty, tileW, tileH))
        }

        for (baris in mapData.ground.indices) {
            val row = mapData.ground[baris]
            for (kolom in row.indices) {
                val tileId = row[kolom]
                if (tileId in tileSlices.indices) {
                    image(tileSlices[tileId]) {
                        smoothing = false
                        x = (kolom * tileW).toDouble()
                        y = (baris * tileH).toDouble()
                    }
                }
            }
        }
    }
}
