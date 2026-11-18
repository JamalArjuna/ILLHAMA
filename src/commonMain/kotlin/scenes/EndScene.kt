package scenes

import korlibs.event.Key
import korlibs.korge.input.*
import korlibs.korge.scene.*
import korlibs.korge.view.*
import korlibs.image.color.*

/**
 * EndScene: Layar akhir setelah musim selesai.
 * Enter -> TitleScene (untuk iterasi ulang).
 */
class EndScene : Scene() {
    override suspend fun SContainer.sceneMain() {
        solidRect(width, height, Colors["#1a1a3c"])

        text("Musim Selesai") {
            textSize = 24.0
            color = Colors.WHITE
            x = (this@sceneMain.width - this.width) / 2
            y = this@sceneMain.height / 2 - 40
        }

        text("Terima kasih sudah bermain!") {
            textSize = 10.0
            color = Colors["#aaaaff"]
            x = (this@sceneMain.width - this.width) / 2
            y = this@sceneMain.height / 2
        }

        text("Enter: main lagi   Esc: keluar") {
            textSize = 8.0
            color = Colors.YELLOW
            x = (this@sceneMain.width - this.width) / 2
            y = this@sceneMain.height / 2 + 20
        }

        keys.down { key ->
            when (key.key) {
                Key.RETURN, Key.KP_ENTER -> {
                    sceneContainer.changeTo { TitleScene() }
                }
                Key.ESCAPE -> {
                    sceneContainer.changeTo { TitleScene() }
                }
                else -> {}
            }
        }
    }
}
