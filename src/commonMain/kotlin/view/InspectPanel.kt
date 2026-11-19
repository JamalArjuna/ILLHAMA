package view

import core.Catalog
import core.PestData
import korlibs.event.Key
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.BmpSlice
import korlibs.image.bitmap.slice
import korlibs.image.color.Colors
import korlibs.korge.view.*
import korlibs.math.geom.RectangleInt
import korlibs.math.geom.Size

/**
 * InspectPanel menampilkan dialog hasil pemeriksaan kaca pembesar pada tanaman berhama.
 * Menggunakan panel.png 9-slice.
 */
class InspectPanel(
    panelBitmap: Bitmap,
    pestsBitmap: Bitmap,
    private val catalog: Catalog,
    private val onOpenJournalRequested: (() -> Unit)? = null
) : Container() {

    private val panelWidth = 320.0
    private val panelHeight = 175.0

    private val pestSprites: Array<BmpSlice> = Array(6) { i ->
        pestsBitmap.slice(RectangleInt(i * 16, 0, 16, 16))
    }

    private val judulText: Text
    private val pestImage: Image
    private val jenisText: Text
    private val tanamanText: Text
    private val gejalaText: Text
    private val petunjukText: Text
    private val footerText: Text

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

        // Judul
        val judul = catalog.texts.periksa.judul.ifEmpty { "Hasil pemeriksaan" }
        judulText = text(judul) {
            textSize = 11.0
            color = Colors.YELLOW
            x = (panelWidth - this.width) / 2.0
            y = 12.0
        }

        // Sprite Hama (skala 2x)
        pestImage = image(pestSprites[0]) {
            smoothing = false
            scale = 2.0
            x = 20.0
            y = 30.0
        }

        // Teks Hama (Nama + Nama Ilmiah)
        jenisText = text("") {
            textSize = 9.0
            color = Colors.WHITE
            x = 58.0
            y = 30.0
        }

        // Teks Tanaman yang diserang
        tanamanText = text("") {
            textSize = 7.5
            color = Colors.LIGHTGRAY
            x = 58.0
            y = 44.0
        }

        // Teks Gejala
        gejalaText = text("") {
            textSize = 7.5
            color = Colors.WHITE
            x = 20.0
            y = 66.0
        }

        // Petunjuk Pengendalian
        val petunjuk = catalog.texts.periksa.petunjuk.ifEmpty {
            "Pilih cara pengendalian yang tepat. Detailnya ada di Journal (tekan J)."
        }
        petunjukText = text(wrapText(petunjuk, 48)) {
            textSize = 7.0
            color = Colors["#ffea88"]
            x = 20.0
            y = 124.0
        }

        // Footer navigasi
        footerText = text("Enter / Esc: tutup   J: buka Journal") {
            textSize = 7.0
            color = Colors.LIGHTGRAY
            x = (panelWidth - this.width) / 2.0
            y = panelHeight - 16.0
        }
    }

    val isOpen: Boolean get() = visible

    fun open(pest: PestData) {
        val templateJenis = catalog.texts.periksa.jenis.ifEmpty { "Hama: {nama} ({ilmiah})" }
        jenisText.text = templateJenis
            .replace("{nama}", pest.nama)
            .replace("{ilmiah}", pest.namaIlmiah)

        tanamanText.text = "Menyerang: ${pest.tanaman.replaceFirstChar { it.uppercase() }}"

        val templateGejala = catalog.texts.periksa.gejala.ifEmpty { "Gejala: {gejala}" }
        val teksGejalaLengkap = templateGejala.replace("{gejala}", pest.gejala)
        gejalaText.text = wrapText(teksGejalaLengkap, 48)

        val idx = pest.spriteIndex.coerceIn(0, 5)
        pestImage.bitmap = pestSprites[idx]

        visible = true
    }

    fun close() {
        visible = false
    }

    fun handleKeyDown(key: Key): Boolean {
        if (!visible) return false
        when (key) {
            Key.RETURN, Key.KP_ENTER, Key.ESCAPE, Key.SPACE -> {
                close()
                return true
            }
            Key.J -> {
                close()
                onOpenJournalRequested?.invoke()
                return true
            }
            else -> return true // Tahan input lain saat panel terbuka
        }
    }

    companion object {
        fun wrapText(text: String, maxCharsPerLine: Int): String {
            val words = text.split(" ")
            val sb = StringBuilder()
            var lineLen = 0
            for (word in words) {
                if (lineLen + word.length + 1 > maxCharsPerLine) {
                    sb.append("\n")
                    lineLen = 0
                } else if (lineLen > 0) {
                    sb.append(" ")
                    lineLen += 1
                }
                sb.append(word)
                lineLen += word.length
            }
            return sb.toString()
        }
    }
}
