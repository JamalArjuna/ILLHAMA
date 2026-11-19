package scenes

import core.*
import korlibs.event.Key
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.slice
import korlibs.image.color.Colors
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.input.keys
import korlibs.korge.scene.Scene
import korlibs.korge.service.storage.storage
import korlibs.korge.view.*
import korlibs.math.geom.Size

/**
 * TitleScene: Layar judul permainan dengan menu Mulai Baru, Lanjutkan, Kredit, dan Keluar.
 * SPEC bagian 10 & 11.
 */
class TitleScene : Scene() {

    override suspend fun SContainer.sceneMain() {
        solidRect(width, height, Colors["#10281b"])

        val textsJson = resourcesVfs["data/texts_id.json"].readString()
        val cropsJson = resourcesVfs["data/crops.json"].readString()
        val pestsJson = resourcesVfs["data/pests.json"].readString()
        val itemsJson = resourcesVfs["data/items.json"].readString()
        val catalog = Catalog.dari(cropsJson, pestsJson, itemsJson, textsJson)

        val panelBitmap = resourcesVfs["assets/art/panel.png"].readBitmap()

        // Periksa apakah ada save data yang valid
        var savedState: GameState? = null
        try {
            val rawSave = views.storage.getOrNull(SaveData.KUNCI_SAVE)
            savedState = SaveData.deserialize(rawSave)
            if (savedState != null && savedState.hari > 14) {
                // Musim sudah tamat, hapus dan abaikan
                views.storage.remove(SaveData.KUNCI_SAVE)
                savedState = null
            }
        } catch (_: Throwable) {
            savedState = null
        }

        val hasSave = savedState != null

        // Judul Utama
        text(catalog.texts.judulGame.ifEmpty { "ILLHAMA" }) {
            textSize = 28.0
            color = Colors.YELLOW
            x = (this@sceneMain.width - this.width) / 2
            y = 35.0
        }

        text("Game Edukasi Pertanian & Hama Indonesia") {
            textSize = 8.5
            color = Colors["#aaffaa"]
            x = (this@sceneMain.width - this.width) / 2
            y = 70.0
        }

        // Menu Opsi: 0 = Mulai Baru, 1 = Lanjutkan, 2 = Kredit, 3 = Keluar
        val teksMulaiBaru = catalog.texts.menu["mulaiBaru"] ?: "Mulai Baru"
        val teksLanjutkan = (catalog.texts.menu["lanjutkan"] ?: "Lanjutkan") + if (hasSave) " (Hari ${savedState?.hari})" else " (Kosong)"
        val teksKredit = catalog.texts.menu["kredit"] ?: "Kredit"
        val teksKeluar = catalog.texts.menu["keluar"] ?: "Keluar"

        val menuLabels = listOf(teksMulaiBaru, teksLanjutkan, teksKredit, teksKeluar)
        var selectedIndex = if (hasSave) 1 else 0

        val menuContainer = container {
            x = (this@sceneMain.width - 200.0) / 2
            y = 100.0
        }

        val highlightBar = menuContainer.solidRect(200.0, 20.0, Colors["#ffffff"].withAd(0.18)) {
            x = 0.0
            y = 0.0
        }

        val menuTexts = mutableListOf<Text>()
        for (i in menuLabels.indices) {
            val t = menuContainer.text(menuLabels[i]) {
                textSize = 8.5
                color = Colors.WHITE
                x = 24.0
                y = 4.0 + i * 24.0
            }
            menuTexts.add(t)
        }

        fun updateMenuVisual() {
            highlightBar.y = selectedIndex * 24.0
            for (i in menuTexts.indices) {
                val txt = menuTexts[i]
                if (i == 1 && !hasSave) {
                    txt.color = Colors.DARKGRAY
                } else if (i == selectedIndex) {
                    txt.color = Colors.YELLOW
                } else {
                    txt.color = Colors.WHITE
                }
            }
        }
        updateMenuVisual()

        text("Panah / W S: pilih   Enter: konfirmasi   Esc: keluar") {
            textSize = 7.0
            color = Colors.LIGHTGRAY
            x = (this@sceneMain.width - this.width) / 2
            y = this@sceneMain.height - 24.0
        }

        // Dialog Kredit
        val creditPanel = container {
            val cw = 360.0
            val ch = 200.0
            x = (this@sceneMain.width - cw) / 2
            y = (this@sceneMain.height - ch) / 2
            visible = false

            val bw = panelBitmap.width.toFloat()
            val bh = panelBitmap.height.toFloat()
            val borderPx = 16f
            ninePatch(
                tex = panelBitmap.slice(),
                size = Size(cw.toFloat(), ch.toFloat()),
                left = borderPx / bw,
                top = borderPx / bh,
                right = (bw - borderPx) / bw,
                bottom = (bh - borderPx) / bh
            ) {
                smoothing = false
            }

            text(catalog.texts.kredit["judul"] ?: "Kredit") {
                textSize = 12.0
                color = Colors.YELLOW
                x = (cw - this.width) / 2.0
                y = 16.0
            }

            val isiKredit = """
                ILLHAMA: Tani Siaga
                Game Edukasi Farming dan Pengenalan Hama Indonesia
                
                Dibuat dengan Kotlin + KorGE Engine (Desktop JVM).
                Seluruh aset grafis (art), efek suara (sfx),
                musik ladang (bgm), dan peta (map) dibuat khusus
                di bawah lisensi Domain Publik (CC0 1.0).
                
                Terima kasih telah memainkan game ini!
            """.trimIndent()

            text(isiKredit) {
                textSize = 7.5
                color = Colors.WHITE
                x = 24.0
                y = 45.0
            }

            text("Tekan Enter atau Esc untuk kembali") {
                textSize = 7.0
                color = Colors.LIGHTGRAY
                x = (cw - this.width) / 2.0
                y = ch - 22.0
            }
        }

        keys.down { key ->
            if (creditPanel.visible) {
                if (key.key == Key.ESCAPE || key.key == Key.RETURN || key.key == Key.KP_ENTER || key.key == Key.SPACE) {
                    creditPanel.visible = false
                }
                return@down
            }

            when (key.key) {
                Key.UP, Key.W -> {
                    selectedIndex = (selectedIndex - 1 + menuLabels.size) % menuLabels.size
                    if (selectedIndex == 1 && !hasSave) {
                        selectedIndex = 0
                    }
                    updateMenuVisual()
                }
                Key.DOWN, Key.S -> {
                    selectedIndex = (selectedIndex + 1) % menuLabels.size
                    if (selectedIndex == 1 && !hasSave) {
                        selectedIndex = 2
                    }
                    updateMenuVisual()
                }
                Key.RETURN, Key.KP_ENTER, Key.SPACE -> {
                    when (selectedIndex) {
                        0 -> { // Mulai Baru
                            sceneContainer.changeTo { GameScene(savedState = null) }
                        }
                        1 -> { // Lanjutkan
                            if (hasSave && savedState != null) {
                                sceneContainer.changeTo { GameScene(savedState = savedState) }
                            }
                        }
                        2 -> { // Kredit
                            creditPanel.visible = true
                        }
                        3 -> { // Keluar
                            views.gameWindow.close()
                        }
                    }
                }
                Key.ESCAPE -> {
                    views.gameWindow.close()
                }
                else -> {}
            }
        }
    }
}
