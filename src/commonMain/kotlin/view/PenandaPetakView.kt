package view

import core.TargetTile
import korlibs.image.bitmap.Bitmap
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.image

/**
 * PenandaPetakView merender bingkai target dari penanda_petak.png (16x16)
 * di atas petak sasaran bila berada di area lahan.
 */
class PenandaPetakView(
    penandaBitmap: Bitmap,
    val tileSize: Int = 16
) : Container() {

    private val penandaImage: Image = image(penandaBitmap) {
        smoothing = false
    }

    init {
        visible = false
    }

    fun update(target: TargetTile) {
        if (target.isLahan) {
            visible = true
            x = (target.col * tileSize).toDouble()
            y = (target.row * tileSize).toDouble()
        } else {
            visible = false
        }
    }
}
