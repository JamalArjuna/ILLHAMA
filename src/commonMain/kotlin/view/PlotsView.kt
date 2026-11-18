package view

import core.*
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.BmpSlice
import korlibs.image.bitmap.slice
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.container
import korlibs.korge.view.image
import korlibs.math.geom.RectangleInt

/**
 * PlotsView merender 48 petak lahan (8 kolom x 6 baris) mulai dari origin tile (3, 7).
 * Merender keadaan tanah (tile 3: belum diolah, 4: diolah kering, 5: diolah basah)
 * dan sprite tanaman di atasnya dari crops.png (80x48, 5 kolom x 3 baris) dengan nearest-neighbor.
 */
class PlotsView(
    val tilesetBitmap: Bitmap,
    val cropsBitmap: Bitmap,
    val originCol: Int = 3,
    val originRow: Int = 7,
    val kolomLahan: Int = 8,
    val barisLahan: Int = 6,
    val tileSize: Int = 16
) : Container() {

    // Pre-slice tile tanah: 3 (belum diolah), 4 (diolah), 5 (basah)
    // Tileset berukuran 64x64 (4 kolom x 4 baris)
    private val tilesetKolom = tilesetBitmap.width / tileSize
    private fun potongTile(id: Int): BmpSlice {
        val tx = (id % tilesetKolom) * tileSize
        val ty = (id / tilesetKolom) * tileSize
        return tilesetBitmap.slice(RectangleInt(tx, ty, tileSize, tileSize))
    }

    private val tileBelumDiolah = potongTile(3)
    private val tileDiolahKering = potongTile(4)
    private val tileDiolahBasah = potongTile(5)

    // Pre-slice crops.png: 3 baris (padi, jagung, cabai) x 5 kolom (tahap 0..3, 4=layu)
    private val cropSlices: Array<Array<BmpSlice>> = Array(3) { row ->
        Array(5) { col ->
            cropsBitmap.slice(RectangleInt(col * tileSize, row * tileSize, tileSize, tileSize))
        }
    }

    private class SinglePlotView(
        val groundImage: Image,
        val cropImage: Image
    )

    private val plotViews = mutableListOf<SinglePlotView>()

    init {
        for (y in 0 until barisLahan) {
            for (x in 0 until kolomLahan) {
                val px = (originCol + x) * tileSize
                val py = (originRow + y) * tileSize

                val plotContainer = container {
                    this.x = px.toDouble()
                    this.y = py.toDouble()
                }

                val groundImg = plotContainer.image(tileBelumDiolah) {
                    smoothing = false
                }

                val cropImg = plotContainer.image(cropSlices[0][0]) {
                    smoothing = false
                    visible = false
                }

                plotViews.add(SinglePlotView(groundImg, cropImg))
            }
        }
    }

    fun update(state: GameState, catalog: Catalog) {
        for (i in state.petak.indices) {
            val petak = state.petak[i]
            val view = plotViews[i]

            // 1. Tampilan tanah
            view.groundImage.bitmap = when {
                petak.tanah == TanahState.BELUM_DIOLAH -> tileBelumDiolah
                petak.disiramHariIni -> tileDiolahBasah
                else -> tileDiolahKering
            }

            // 2. Tampilan tanaman
            val tanaman = petak.tanaman
            if (tanaman != null) {
                val cropData = catalog.cropById[tanaman.cropId]
                if (cropData != null) {
                    val stageCol = tanaman.tahapGambar(cropData.hariTumbuh)
                    val row = cropData.spriteBaris.coerceIn(0, 2)
                    view.cropImage.bitmap = cropSlices[row][stageCol.coerceIn(0, 4)]
                    view.cropImage.visible = true
                } else {
                    view.cropImage.visible = false
                }
            } else {
                view.cropImage.visible = false
            }
        }
    }
}
