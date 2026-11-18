package view

import core.MapJsonData
import korlibs.image.bitmap.*
import korlibs.korge.view.*
import korlibs.math.geom.*

/**
 * MapView merender peta dari map.json menggunakan tileset.png.
 * Setiap tile dipotong dari tileset (4 kolom x 4 baris, ukuran 16x16) dan digambar
 * ke posisinya di layar menggunakan nearest-neighbor (smoothing = false).
 */
class MapView(
    private val tilesetBitmap: Bitmap,
    val mapData: MapJsonData,
    tileSize: Int = 16
) : Container() {

    constructor(tilesetBitmap: Bitmap, mapJsonString: String, tileSize: Int = 16) :
        this(tilesetBitmap, MapJsonData.dari(mapJsonString), tileSize)

    private val tileW: Int = tileSize
    private val tileH: Int = tileSize
    // Kolom tileset = 64/16 = 4
    private val tilesetKolom = tilesetBitmap.width / tileW

    val spawnTile: Pair<Int, Int> = Pair(mapData.spawn[0], mapData.spawn[1])

    init {
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
