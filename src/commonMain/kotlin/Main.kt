import korlibs.korge.*
import korlibs.korge.scene.*
import korlibs.math.geom.*
import scenes.*

/**
 * Titik masuk ILLHAMA.
 * Resolusi virtual: 480 x 272 (30 tile × 17 tile, ukuran 16×16).
 * Jendela awal: 1440 × 816 (skala 3×).
 * Rendering: nearest-neighbor (smoothing dimatikan di tiap ImageView).
 */
suspend fun main() = Korge(
    windowSize = Size(1440, 816),
    virtualSize = Size(480, 272),
    title = "ILLHAMA – Tani Siaga"
) {
    val sceneContainer = sceneContainer()
    sceneContainer.changeTo { TitleScene() }
}
