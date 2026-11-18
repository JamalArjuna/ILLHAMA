package view

import core.Direction
import core.PlayerController
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.BmpSlice
import korlibs.image.bitmap.slice
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.image
import korlibs.math.geom.RectangleInt

/**
 * PlayerView merender sprite pemain dari player.png (64x64, 4 frame x 4 baris, tiap frame 16x16).
 * Animasi 4 frame per arah hadap, dengan rendering nearest-neighbor (smoothing = false).
 */
class PlayerView(
    playerBitmap: Bitmap,
    val frameDuration: Double = 0.15
) : Container() {

    private val frameW = 16
    private val frameH = 16
    private val frames: Array<Array<BmpSlice>> = Array(4) { row ->
        Array(4) { col ->
            playerBitmap.slice(RectangleInt(col * frameW, row * frameH, frameW, frameH))
        }
    }

    private val playerImage: Image = image(frames[Direction.DOWN.spriteRow][0]) {
        smoothing = false
    }

    private var animTimer: Double = 0.0

    fun update(controller: PlayerController, dt: Double) {
        x = controller.x
        y = controller.y

        val row = controller.facing.spriteRow
        if (controller.isMoving) {
            animTimer += dt
            val frameIndex = ((animTimer / frameDuration).toInt()) % 4
            playerImage.bitmap = frames[row][frameIndex]
        } else {
            animTimer = 0.0
            playerImage.bitmap = frames[row][0]
        }
    }
}
