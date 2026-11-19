package scenes

import core.*
import korlibs.event.Key
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.BmpSlice
import korlibs.image.bitmap.slice
import korlibs.image.color.Colors
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.input.keys
import korlibs.korge.scene.Scene
import korlibs.korge.service.storage.storage
import korlibs.korge.view.*
import korlibs.math.geom.RectangleInt
import view.InspectPanel

/**
 * EndScene: Layar akhir musim (SPEC bagian 9, 10, 11).
 * Menampilkan uang akhir, journal terbuka, rating bintang (ikon 8 dan 9), pesan motivasi,
 * serta menghapus save data saat musim tamat.
 */
class EndScene(
    private val uangAkhir: Int = 0,
    private val journalTerbukaCount: Int = 0,
    private val rating: Int = 0
) : Scene() {

    override suspend fun SContainer.sceneMain() {
        // Hapus save saat musim tamat (SPEC bagian 10)
        try {
            views.storage.remove(SaveData.KUNCI_SAVE)
        } catch (_: Throwable) {}

        solidRect(width, height, Colors["#0e1c14"])

        // Muat katalog teks
        val cropsJson = resourcesVfs["data/crops.json"].readString()
        val pestsJson = resourcesVfs["data/pests.json"].readString()
        val itemsJson = resourcesVfs["data/items.json"].readString()
        val textsJson = resourcesVfs["data/texts_id.json"].readString()
        val catalog = Catalog.dari(cropsJson, pestsJson, itemsJson, textsJson)

        val ikonBitmap = resourcesVfs["assets/art/ikon.png"].readBitmap()
        // Ikon 8 (bintang penuh): col 0, row 2 (index 8: 8 % 4 = 0, 8 / 4 = 2)
        // Ikon 9 (bintang kosong): col 1, row 2 (index 9: 9 % 4 = 1, 9 / 4 = 2)
        val bintangPenuh = ikonBitmap.slice(RectangleInt(0 * 16, 2 * 16, 16, 16))
        val bintangKosong = ikonBitmap.slice(RectangleInt(1 * 16, 2 * 16, 16, 16))

        val panelW = 340.0
        val panelH = 220.0
        val px = (width - panelW) / 2.0
        val py = (height - panelH) / 2.0

        val box = container {
            x = px
            y = py
        }

        // Judul
        val judul = catalog.texts.akhir.judul.ifEmpty { "Musim Selesai" }
        box.text(judul) {
            textSize = 16.0
            color = Colors.YELLOW
            x = (panelW - this.width) / 2.0
            y = 16.0
        }

        // Uang Akhir
        val templateUang = catalog.texts.akhir.uang.ifEmpty { "Uang akhir: Rp {uang}" }
        val teksUang = templateUang.replace("{uang}", uangAkhir.toString())
        box.text(teksUang) {
            textSize = 9.0
            color = Colors["#aaffaa"]
            x = (panelW - this.width) / 2.0
            y = 48.0
        }

        // Journal Terbuka
        val templateJournal = catalog.texts.akhir.journal.ifEmpty { "Journal terbuka: {terbuka}/{total}" }
        val teksJournal = templateJournal
            .replace("{terbuka}", journalTerbukaCount.toString())
            .replace("{total}", catalog.pests.size.toString())
        box.text(teksJournal) {
            textSize = 9.0
            color = Colors.WHITE
            x = (panelW - this.width) / 2.0
            y = 66.0
        }

        // Rating Bintang (3 bintang)
        val starContainer = box.container {
            x = (panelW - (3 * 22.0)) / 2.0
            y = 88.0
        }
        for (i in 0 until 3) {
            val isFilled = i < rating
            starContainer.image(if (isFilled) bintangPenuh else bintangKosong) {
                smoothing = false
                scale = 1.2
                x = i * 22.0
                y = 0.0
            }
        }

        // Label rating
        val templateRating = catalog.texts.akhir.rating.ifEmpty { "Rating: {bintang} bintang" }
        val teksRating = templateRating.replace("{bintang}", rating.toString())
        box.text(teksRating) {
            textSize = 8.0
            color = Colors["#ffea88"]
            x = (panelW - this.width) / 2.0
            y = 114.0
        }

        // Pesan motivasi
        val pesan = catalog.texts.akhir.pesan[rating.toString()]
            ?: "Bagus! Kamu sudah menyelesaikan satu musim bertani."
        box.text(InspectPanel.wrapText(pesan, 46)) {
            textSize = 7.5
            color = Colors.LIGHTGRAY
            x = 20.0
            y = 134.0
        }

        // Tombol navigasi
        val petunjukUlang = catalog.texts.akhir.ulang.ifEmpty { "Enter: main lagi   Esc: menu utama" }
        box.text(petunjukUlang) {
            textSize = 7.5
            color = Colors.YELLOW
            x = (panelW - this.width) / 2.0
            y = panelH - 22.0
        }

        keys.down { key ->
            when (key.key) {
                Key.RETURN, Key.KP_ENTER -> {
                    sceneContainer.changeTo { GameScene() }
                }
                Key.ESCAPE -> {
                    sceneContainer.changeTo { TitleScene() }
                }
                else -> {}
            }
        }
    }
}
