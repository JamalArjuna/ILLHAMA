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
 * PlotsView merender 48 petak lahan (8 kolom x 6 baris).
 * Merender: keadaan tanah, sprite tanaman, dan ikon hama (! atau sprite hama).
 */
class PlotsView(
    val tilesetBitmap: Bitmap,
    val cropsBitmap: Bitmap,
    val pestsBitmap: Bitmap,
    val ikonBitmap: Bitmap,
    val originCol: Int = 3,
    val originRow: Int = 7,
    val kolomLahan: Int = 8,
    val barisLahan: Int = 6,
    val tileSize: Int = 16
) : Container() {

    // Pre-slice tile tanah
    private val tilesetKolom = tilesetBitmap.width / tileSize
    private fun potongTile(id: Int): BmpSlice {
        val tx = (id % tilesetKolom) * tileSize
        val ty = (id / tilesetKolom) * tileSize
        return tilesetBitmap.slice(RectangleInt(tx, ty, tileSize, tileSize))
    }

    private val tileBelumDiolah = potongTile(3)
    private val tileDiolahKering = potongTile(4)
    private val tileDiolahBasah = potongTile(5)

    // Pre-slice crops.png: 3 baris x 5 kolom (tahap 0..3, 4=layu)
    private val cropSlices: Array<Array<BmpSlice>> = Array(3) { row ->
        Array(5) { col ->
            cropsBitmap.slice(RectangleInt(col * tileSize, row * tileSize, tileSize, tileSize))
        }
    }

    // Pre-slice pests.png: 6 sprite hama (96x16, masing-masing 16x16 mendatar)
    private val pestSprites: Array<BmpSlice> = Array(6) { i ->
        pestsBitmap.slice(RectangleInt(i * tileSize, 0, tileSize, tileSize))
    }

    // Ikon tanda seru (ikon.png index 6, 4x4 grid 16x16)
    private val ikonTandaSeru: BmpSlice = run {
        val col = 6 % 4   // = 2
        val row = 6 / 4   // = 1
        ikonBitmap.slice(RectangleInt(col * tileSize, row * tileSize, tileSize, tileSize))
    }

    private class SinglePlotView(
        val groundImage: Image,
        val cropImage: Image,
        val pestIconImage: Image
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

                // Ikon hama ditampilkan di atas sprite tanaman
                val pestIconImg = plotContainer.image(ikonTandaSeru) {
                    smoothing = false
                    visible = false
                }

                plotViews.add(SinglePlotView(groundImg, cropImg, pestIconImg))
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

            // 3. Ikon hama di atas tanaman
            val hamaId = tanaman?.hamaId
            if (hamaId != null && tanaman != null && !tanaman.mati) {
                val pestData = catalog.pestById[hamaId]
                if (tanaman.hamaDiperiksa && pestData != null) {
                    // Sudah diperiksa: tampilkan sprite hama
                    view.pestIconImage.bitmap = pestSprites[pestData.spriteIndex.coerceIn(0, 5)]
                } else {
                    // Belum diperiksa: tampilkan tanda seru
                    view.pestIconImage.bitmap = ikonTandaSeru
                }
                view.pestIconImage.visible = true
            } else {
                view.pestIconImage.visible = false
            }
        }
    }
}
