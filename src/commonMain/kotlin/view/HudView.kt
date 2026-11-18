package view

import core.Catalog
import core.DaySimulator
import core.GameConfig
import core.GameState
import korlibs.image.color.Colors
import korlibs.korge.view.*

/**
 * HudView merender informasi status di pojok kanan atas:
 * - Hari x/14
 * - Jam HH:MM
 * - Uang Rp n
 * Menggunakan teks dari texts_id.json.
 */
class HudView(
    private val config: GameConfig,
    private val catalog: Catalog
) : Container() {

    private val bg = solidRect(115.0, 26.0, Colors["#0f1712"].withAd(0.75)) {
        x = 0.0
        y = 0.0
    }

    private val hariText: Text = text("Hari 1/14") {
        textSize = 8.0
        color = Colors.WHITE
        x = 6.0
        y = 4.0
    }

    private val jamText: Text = text("06:00") {
        textSize = 8.0
        color = Colors.YELLOW
        x = 75.0
        y = 4.0
    }

    private val uangText: Text = text("Rp 500") {
        textSize = 8.0
        color = Colors["#aaffaa"]
        x = 6.0
        y = 14.0
    }

    init {
        // Posisi default di pojok kanan atas layar virtual (480 x 272)
        x = 480.0 - 120.0
        y = 3.0
    }

    fun update(state: GameState) {
        val formatHariTemplate = catalog.texts.hud["hari"] ?: "Hari {hari}/{total}"
        val hariStr = formatHariTemplate
            .replace("{hari}", state.hari.toString())
            .replace("{total}", config.jumlahHariMusim.toString())
        hariText.text = hariStr

        jamText.text = DaySimulator.formatWaktu(state.menitHari)

        val formatUangTemplate = catalog.texts.hud["uang"] ?: "Rp {uang}"
        val uangStr = formatUangTemplate.replace("{uang}", state.uang.toString())
        uangText.text = uangStr
    }
}
