package scenes

import korlibs.image.format.*
import korlibs.korge.input.*
import korlibs.korge.scene.*
import korlibs.korge.view.*
import korlibs.image.color.*
import korlibs.io.file.std.*
import view.MapView

/**
 * GameScene: Layar utama permainan.
 * Memuat tileset dan merender peta dari map.json.
 * Tombol debug D -> EndScene.
 */
class GameScene : Scene() {
    override suspend fun SContainer.sceneMain() {
        solidRect(width, height, Colors["#0a1f14"])

        // Muat tileset
        val tilesetBitmap = resourcesVfs["assets/art/tileset.png"].readBitmap()

        // Muat map.json
        val mapJsonString = resourcesVfs["map/map.json"].readString()

        // Render peta
        val mapView = MapView(tilesetBitmap, mapJsonString, tileSize = 16)
        addChild(mapView)

        // Teks debug di sudut kiri bawah
        text("[Debug] Tekan D untuk lanjut ke EndScene") {
            textSize = 7.0
            color = Colors["#ffff00"]
            x = 4.0
            y = this@sceneMain.height - 12
        }

        keys.down { key ->
            when (key.key) {
                korlibs.event.Key.D -> {
                    sceneContainer.changeTo { EndScene() }
                }
                else -> {}
            }
        }
    }
}
