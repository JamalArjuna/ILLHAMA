package scenes

import korlibs.event.Key
import korlibs.korge.input.*
import korlibs.korge.scene.*
import korlibs.korge.view.*
import korlibs.image.color.*

/**
 * TitleScene: Layar judul dengan petunjuk "Tekan Enter untuk mulai".
 * Enter -> GameScene
 */
class TitleScene : Scene() {
    override suspend fun SContainer.sceneMain() {
        solidRect(width, height, Colors["#1a3c2a"])

        text("ILLHAMA") {
            textSize = 32.0
            color = Colors.WHITE
            x = (this@sceneMain.width - this.width) / 2
            y = this@sceneMain.height / 2 - 60
        }

        text("Game Edukasi Hama Tanaman Indonesia") {
            textSize = 10.0
            color = Colors["#aaffaa"]
            x = (this@sceneMain.width - this.width) / 2
            y = this@sceneMain.height / 2 - 20
        }

        text("Tekan Enter untuk Mulai Baru") {
            textSize = 10.0
            color = Colors.YELLOW
            x = (this@sceneMain.width - this.width) / 2
            y = this@sceneMain.height / 2 + 10
        }

        text("Tekan Esc untuk Keluar") {
            textSize = 8.0
            color = Colors.LIGHTGRAY
            x = (this@sceneMain.width - this.width) / 2
            y = this@sceneMain.height / 2 + 30
        }

        keys.down { key ->
            when (key.key) {
                Key.RETURN, Key.KP_ENTER -> {
                    sceneContainer.changeTo { GameScene() }
                }
                Key.ESCAPE -> {
                    views.gameWindow.close()
                }
                else -> {}
            }
        }
    }
}
