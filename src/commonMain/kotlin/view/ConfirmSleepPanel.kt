package view

import core.Catalog
import korlibs.event.Key
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.slice
import korlibs.image.color.Colors
import korlibs.korge.view.*
import korlibs.math.geom.Size

/**
 * ConfirmSleepPanel menampilkan dialog konfirmasi tidur saat pemain berinteraksi dengan pintu rumah.
 */
class ConfirmSleepPanel(
    panelBitmap: Bitmap,
    private val catalog: Catalog,
    private val onConfirmTidur: () -> Unit
) : Container() {

    private val panelWidth = 240.0
    private val panelHeight = 110.0

    private var selectedOption: Int = 0 // 0 = Ya, 1 = Belum
    private val optionYaText: Text
    private val optionTidakText: Text
    private val highlightRect: SolidRect

    init {
        x = (480.0 - panelWidth) / 2.0
        y = (272.0 - panelHeight) / 2.0
        visible = false

        // Background 9-patch panel.png (48x48, border 16px)
        val bw = panelBitmap.width.toFloat()
        val bh = panelBitmap.height.toFloat()
        val borderPx = 16f
        ninePatch(
            tex = panelBitmap.slice(),
            size = Size(panelWidth.toFloat(), panelHeight.toFloat()),
            left = borderPx / bw,
            top = borderPx / bh,
            right = (bw - borderPx) / bw,
            bottom = (bh - borderPx) / bh
        ) {
            smoothing = false
        }

        // Teks Konfirmasi
        val pesanKonfirmasi = catalog.texts.tidur["konfirmasi"] ?: "Tidur dan ganti hari?"
        text(pesanKonfirmasi) {
            textSize = 9.0
            color = Colors.WHITE
            x = (panelWidth - this.width) / 2.0
            y = 24.0
        }

        // Highlight pilihan
        highlightRect = solidRect(60.0, 22.0, Colors["#ffffff"].withAd(0.18)) {
            x = 45.0
            y = 55.0
        }

        val teksYa = catalog.texts.tidur["ya"] ?: "Ya"
        val teksTidak = catalog.texts.tidur["tidak"] ?: "Belum"

        optionYaText = text(teksYa) {
            textSize = 9.0
            color = Colors.YELLOW
            x = 60.0
            y = 60.0
        }

        optionTidakText = text(teksTidak) {
            textSize = 9.0
            color = Colors.LIGHTGRAY
            x = 145.0
            y = 60.0
        }

        text("Kiri/Kanan: pilih   Enter: ya   Esc: tutup") {
            textSize = 6.5
            color = Colors.DARKGRAY
            x = (panelWidth - this.width) / 2.0
            y = panelHeight - 16.0
        }
    }

    val isOpen: Boolean get() = visible

    fun open() {
        selectedOption = 0
        visible = true
        refresh()
    }

    fun close() {
        visible = false
    }

    fun handleKeyDown(key: Key): Boolean {
        if (!visible) return false

        when (key) {
            Key.LEFT, Key.A, Key.RIGHT, Key.D -> {
                selectedOption = 1 - selectedOption
                refresh()
                return true
            }
            Key.RETURN, Key.KP_ENTER -> {
                close()
                if (selectedOption == 0) {
                    onConfirmTidur()
                }
                return true
            }
            Key.ESCAPE -> {
                close()
                return true
            }
            else -> return true
        }
    }

    private fun refresh() {
        if (selectedOption == 0) {
            highlightRect.x = 42.0
            optionYaText.color = Colors.YELLOW
            optionTidakText.color = Colors.LIGHTGRAY
        } else {
            highlightRect.x = 132.0
            optionYaText.color = Colors.LIGHTGRAY
            optionTidakText.color = Colors.YELLOW
        }
    }
}
