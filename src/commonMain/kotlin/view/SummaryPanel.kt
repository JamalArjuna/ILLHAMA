package view

import core.Catalog
import core.DaySummary
import korlibs.event.Key
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.slice
import korlibs.image.color.Colors
import korlibs.korge.view.*
import korlibs.math.geom.Size

/**
 * SummaryPanel menampilkan ringkasan harian saat pagi tiba:
 * pendapatan kemarin, jumlah hama yang hilang, jumlah tanaman mati.
 */
class SummaryPanel(
    panelBitmap: Bitmap,
    private val catalog: Catalog
) : Container() {

    private val panelWidth = 280.0
    private val panelHeight = 160.0

    private val judulText: Text
    private val pendapatanText: Text
    private val hamaText: Text
    private val matiText: Text
    private val lanjutText: Text

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

        val templateJudul = catalog.texts.ringkasan["judul"] ?: "Ringkasan Hari {hari}"
        judulText = text(templateJudul) {
            textSize = 11.0
            color = Colors.YELLOW
            x = (panelWidth - this.width) / 2.0
            y = 16.0
        }

        pendapatanText = text("") {
            textSize = 8.0
            color = Colors["#aaffaa"]
            x = 30.0
            y = 45.0
        }

        hamaText = text("") {
            textSize = 8.0
            color = Colors.WHITE
            x = 30.0
            y = 68.0
        }

        matiText = text("") {
            textSize = 8.0
            color = Colors["#ff8888"]
            x = 30.0
            y = 91.0
        }

        val templateLanjut = catalog.texts.ringkasan["lanjut"] ?: "Tekan Enter untuk lanjut"
        lanjutText = text(templateLanjut) {
            textSize = 7.5
            color = Colors.LIGHTGRAY
            x = (panelWidth - this.width) / 2.0
            y = panelHeight - 22.0
        }
    }

    val isOpen: Boolean get() = visible

    fun open(summary: DaySummary) {
        val templateJudul = catalog.texts.ringkasan["judul"] ?: "Ringkasan Hari {hari}"
        judulText.text = templateJudul.replace("{hari}", summary.hariSebelumnya.toString())
        judulText.x = (panelWidth - judulText.width) / 2.0

        val templatePendapatan = catalog.texts.ringkasan["pendapatan"] ?: "Pendapatan kemarin: Rp {jumlah}"
        pendapatanText.text = templatePendapatan.replace("{jumlah}", summary.pendapatan.toString())

        val templateHama = catalog.texts.ringkasan["hamaDitangani"] ?: "Hama yang hilang: {jumlah}"
        hamaText.text = templateHama.replace("{jumlah}", summary.hamaDitangani.toString())

        val templateMati = catalog.texts.ringkasan["tanamanRusak"] ?: "Tanaman mati: {jumlah}"
        matiText.text = templateMati.replace("{jumlah}", summary.tanamanMati.toString())

        visible = true
    }

    fun close() {
        visible = false
    }

    fun handleKeyDown(key: Key): Boolean {
        if (!visible) return false

        when (key) {
            Key.RETURN, Key.KP_ENTER, Key.SPACE, Key.ESCAPE -> {
                close()
                return true
            }
            else -> return true
        }
    }
}
