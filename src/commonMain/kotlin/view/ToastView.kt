package view

import core.GameConfig
import korlibs.image.color.Colors
import korlibs.korge.view.*

/**
 * ToastView menampilkan pesan toast sementara di tengah bawah layar (di atas hotbar).
 * Durasi tampilan mengikuti constants.json (durasiToastDetik).
 */
class ToastView(
    private val config: GameConfig
) : Container() {

    private val bg: SolidRect = solidRect(200.0, 16.0, Colors["#0a0f0d"].withAd(0.85)) {
        x = 0.0
        y = 0.0
    }

    private val label: Text = text("") {
        textSize = 8.0
        color = Colors.WHITE
        x = 6.0
        y = 3.0
    }

    private var sisaWaktu: Double = 0.0

    init {
        visible = false
        y = 222.0
    }

    fun show(pesan: String) {
        label.text = pesan
        val padX = 12.0
        val targetWidth = label.width + padX * 2
        bg.scaledWidth = targetWidth
        // Posisikan horizontal di tengah layar virtual (lebar 480)
        x = (480.0 - targetWidth) / 2.0
        label.x = padX
        sisaWaktu = config.durasiToastDetik
        visible = true
    }

    fun update(dt: Double) {
        if (!visible) return

        sisaWaktu -= dt
        if (sisaWaktu <= 0.0) {
            visible = false
        }
    }
}
