package view

import core.Catalog
import core.GameState
import korlibs.image.color.Colors
import korlibs.korge.view.*

/**
 * TutorialHint menampilkan kotak petunjuk tutorial hari 1 dan petunjuk kejadian di atas layar.
 */
class TutorialHint(
    private val catalog: Catalog
) : Container() {

    private val bgRect: SolidRect
    private val hintText: Text

    private var durasiKejadian: Double = 0.0
    private var teksKejadian: String? = null

    init {
        x = 10.0
        y = 4.0
        visible = false

        bgRect = solidRect(460.0, 16.0, Colors["#000000"].withAd(0.75)) {
            x = 0.0
            y = 0.0
        }

        hintText = text("") {
            textSize = 7.0
            color = Colors.YELLOW
            x = 6.0
            y = 3.0
        }
    }

    fun showKejadian(pesan: String, durasi: Double = 5.0) {
        teksKejadian = pesan
        durasiKejadian = durasi
        hintText.text = pesan
        hintText.color = Colors["#ffea88"]
        visible = true
    }

    fun update(dt: Double, state: GameState) {
        if (durasiKejadian > 0.0) {
            durasiKejadian -= dt
            if (durasiKejadian <= 0.0) {
                teksKejadian = null
            } else {
                return
            }
        }

        // Jika hari 1 dan masih ada tutorial
        if (state.hari == 1 && state.tutorialIndex < catalog.texts.tutorial.size) {
            val step = catalog.texts.tutorial[state.tutorialIndex]
            hintText.text = "Petunjuk: ${step.teks}"
            hintText.color = Colors.YELLOW
            visible = true
        } else {
            visible = false
        }
    }

    fun onEvent(kondisi: String, state: GameState) {
        if (state.hari == 1 && state.tutorialIndex < catalog.texts.tutorial.size) {
            val step = catalog.texts.tutorial[state.tutorialIndex]
            if (step.selesaiKetika == kondisi) {
                state.tutorialIndex += 1
            }
        }
    }
}
