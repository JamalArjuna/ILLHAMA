package scenes

import audio.Sfx
import core.*
import korlibs.event.Key
import korlibs.image.color.Colors
import korlibs.image.format.readBitmap
import korlibs.io.async.launchImmediately
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.input.keys
import korlibs.korge.input.onScroll
import korlibs.korge.scene.Scene
import korlibs.korge.view.SContainer
import korlibs.korge.view.addFastUpdater
import korlibs.korge.view.solidRect
import view.*

/**
 * GameScene: Layar utama permainan.
 * Memuat peta, lahan tanam (PlotsView), pemain, penanda petak, jam, HUD, Hotbar, ShopPanel, dan toast.
 */
class GameScene : Scene() {
    override suspend fun SContainer.sceneMain() {
        solidRect(width, height, Colors["#0a1f14"])

        // 1. Muat data konfigurasi dan katalog
        val constantsJson = resourcesVfs["data/constants.json"].readString()
        val config = GameConfig.dari(constantsJson)

        val cropsJson = resourcesVfs["data/crops.json"].readString()
        val pestsJson = resourcesVfs["data/pests.json"].readString()
        val itemsJson = resourcesVfs["data/items.json"].readString()
        val textsJson = resourcesVfs["data/texts_id.json"].readString()
        val catalog = Catalog.dari(cropsJson, pestsJson, itemsJson, textsJson)

        val mapJsonString = resourcesVfs["map/map.json"].readString()
        val mapData = MapJsonData.dari(mapJsonString)

        // 2. Inisialisasi GameState (uang awal 500, tanpa biji awal sesuai M3)
        val gameState = GameState(
            hari = 1,
            menitHari = config.jamMulai * 60,
            uang = config.uangAwal
        )

        val allCropIds = catalog.crops.map { it.id }
        var selectedCropId: String = allCropIds.firstOrNull() ?: "padi"

        // 3. Muat efek suara (SFX)
        Sfx.load()

        // 4. Muat aset grafis
        val tilesetBitmap = resourcesVfs["assets/art/tileset.png"].readBitmap()
        val cropsBitmap = resourcesVfs["assets/art/crops.png"].readBitmap()
        val playerBitmap = resourcesVfs["assets/art/player.png"].readBitmap()
        val penandaBitmap = resourcesVfs["assets/art/penanda_petak.png"].readBitmap()
        val slotBitmap = resourcesVfs["assets/art/slot.png"].readBitmap()
        val slotSelectedBitmap = resourcesVfs["assets/art/slot_terpilih.png"].readBitmap()
        val ikonBitmap = resourcesVfs["assets/art/ikon.png"].readBitmap()
        val panelBitmap = resourcesVfs["assets/art/panel.png"].readBitmap()

        // 5. Render peta dan lahan
        val mapView = MapView(tilesetBitmap, mapData, tileSize = config.tileSize)
        addChild(mapView)

        val plotsView = PlotsView(
            tilesetBitmap = tilesetBitmap,
            cropsBitmap = cropsBitmap,
            originCol = config.lahanOriginTile[0],
            originRow = config.lahanOriginTile[1],
            kolomLahan = config.lahanKolom,
            barisLahan = config.lahanBaris,
            tileSize = config.tileSize
        )
        addChild(plotsView)

        // 6. Penanda petak sasaran di lahan
        val penandaView = PenandaPetakView(penandaBitmap, tileSize = config.tileSize)
        addChild(penandaView)

        // 7. Pemain dan pengontrol pergerakan
        val spawnX = (mapData.spawn[0] * config.tileSize).toDouble()
        val spawnY = (mapData.spawn[1] * config.tileSize).toDouble()
        val playerController = PlayerController(
            x = spawnX,
            y = spawnY,
            speed = config.kecepatanPxPerDetik.toDouble(),
            hitboxLebar = config.hitboxLebar.toDouble(),
            hitboxTinggi = config.hitboxTinggi.toDouble(),
            hitboxOffsetX = config.hitboxOffsetX.toDouble(),
            hitboxOffsetY = config.hitboxOffsetY.toDouble(),
            mapData = mapData
        )
        val playerView = PlayerView(playerBitmap)
        addChild(playerView)

        // 8. HUD dan Hotbar
        val hudView = HudView(config, catalog)
        addChild(hudView)

        val hotbarView = HotbarView(slotBitmap, slotSelectedBitmap, ikonBitmap)
        addChild(hotbarView)

        // 9. ToastView
        val toastView = ToastView(config)

        // 10. ShopPanel
        val shopPanel = ShopPanel(panelBitmap, ikonBitmap, catalog) { pesan ->
            toastView.show(pesan)
        }
        addChild(shopPanel)

        // Toast selalu di lapisan teratas
        addChild(toastView)

        val akumulatorMenit = doubleArrayOf(0.0)

        fun cycleBiji() {
            val ownedSeeds = allCropIds.filter { (gameState.inventory.biji[it] ?: 0) > 0 }
            if (ownedSeeds.isNotEmpty()) {
                val currentIdx = ownedSeeds.indexOf(selectedCropId)
                selectedCropId = if (currentIdx >= 0) {
                    ownedSeeds[(currentIdx + 1) % ownedSeeds.size]
                } else {
                    ownedSeeds[0]
                }
            } else {
                val currentIdx = allCropIds.indexOf(selectedCropId)
                selectedCropId = allCropIds[(currentIdx + 1) % allCropIds.size]
            }
            hotbarView.update(gameState, selectedCropId)
        }

        fun gantiHari() {
            val musimSelesai = DaySimulator.tickHari(gameState, config, catalog)
            plotsView.update(gameState, catalog)
            hudView.update(gameState)
            hotbarView.update(gameState, selectedCropId)

            if (musimSelesai) {
                launchImmediately {
                    sceneContainer.changeTo { EndScene() }
                }
            } else {
                val formatHariTemplate = catalog.texts.hud["hari"] ?: "Hari {hari}/{total}"
                val infoHari = formatHariTemplate
                    .replace("{hari}", gameState.hari.toString())
                    .replace("{total}", config.jumlahHariMusim.toString())

                if (gameState.pendapatanKemarin > 0) {
                    toastView.show("$infoHari (Hasil bin: +Rp ${gameState.pendapatanKemarin})")
                } else {
                    toastView.show(infoHari)
                }
            }
        }

        fun gunakanAlat() {
            val target = playerController.getTargetTile()
            if (!target.isLahan) return

            val petak = gameState.petakAt(target.lahanCol, target.lahanRow)
            val slot = hotbarView.selectedSlot
            val hasil = Interaction.pakaiAlat(slot, petak, gameState.inventory, selectedCropId)

            when (hasil) {
                is InteractionResult.Sukses -> {
                    if (hasil.sfx == "sfx_cangkul") Sfx.playCangkul()
                    if (hasil.sfx == "sfx_siram") Sfx.playSiram()
                    plotsView.update(gameState, catalog)
                    hotbarView.update(gameState, selectedCropId)
                }
                is InteractionResult.Gagal -> {
                    val pesan = catalog.texts.toast[hasil.toastKey] ?: hasil.toastKey
                    toastView.show(pesan)
                    Sfx.playGagal()
                }
                is InteractionResult.TidakAdaAksi -> {}
            }
        }

        fun interaksiTanganAtauObjek() {
            val target = playerController.getTargetTile()
            val objekId = playerController.checkObjectInteraction(target)

            if (objekId != null) {
                when (objekId) {
                    "pintu_toko" -> {
                        shopPanel.open(gameState)
                    }
                    "shipping_bin" -> {
                        val dipindah = Interaction.masukkanKeBin(gameState.inventory, gameState.bin)
                        if (dipindah > 0) {
                            Sfx.playJual()
                            val msg = (catalog.texts.toast["masukBin"] ?: "{jumlah} hasil panen masuk ke shipping bin.")
                                .replace("{jumlah}", dipindah.toString())
                            toastView.show(msg)
                        } else {
                            val msg = catalog.texts.toast["binKosong"] ?: "Tidak ada hasil panen untuk dimasukkan."
                            toastView.show(msg)
                        }
                    }
                    "pintu_rumah" -> {
                        val msg = catalog.texts.tidur["konfirmasi"] ?: "Tidur dan ganti hari?"
                        toastView.show(msg)
                    }
                    else -> toastView.show(objekId)
                }
            } else if (target.isLahan) {
                val petak = gameState.petakAt(target.lahanCol, target.lahanRow)
                val hasilTangan = Interaction.interaksiTangan(petak, catalog, config, gameState.inventory)

                when (hasilTangan) {
                    is HandResult.PanenSukses -> {
                        Sfx.playPanen()
                        plotsView.update(gameState, catalog)
                        hotbarView.update(gameState, selectedCropId)

                        val kualitasStr = catalog.texts.kualitas[hasilTangan.kualitas.name] ?: hasilTangan.kualitas.name
                        val msg = (catalog.texts.toast["panenBerhasil"] ?: "Panen {nama} ({kualitas})!")
                            .replace("{nama}", hasilTangan.namaCrop)
                            .replace("{kualitas}", kualitasStr)
                        toastView.show(msg)
                    }
                    is HandResult.BersihkanSukses -> {
                        plotsView.update(gameState, catalog)
                        val msg = catalog.texts.toast["petakDibersihkan"] ?: "Petak dibersihkan."
                        toastView.show(msg)
                    }
                    is HandResult.Gagal -> {
                        val msg = catalog.texts.toast[hasilTangan.toastKey] ?: hasilTangan.toastKey
                        toastView.show(msg)
                    }
                    is HandResult.TidakAdaAksi -> {}
                }
            }
        }

        // 11. Event tombol sekali tekan
        keys.down { key ->
            // Jika ShopPanel sedang terbuka, seluruh navigasi dialihkan ke ShopPanel
            if (shopPanel.isOpen) {
                shopPanel.handleKeyDown(key.key)
                hudView.update(gameState)
                hotbarView.update(gameState, selectedCropId)
                return@down
            }

            when (key.key) {
                Key.N1, Key.NUMPAD1 -> hotbarView.selectSlot(1)
                Key.N2, Key.NUMPAD2 -> hotbarView.selectSlot(2)
                Key.N3, Key.NUMPAD3 -> hotbarView.selectSlot(3)
                Key.N4, Key.NUMPAD4 -> hotbarView.selectSlot(4)
                Key.N5, Key.NUMPAD5 -> hotbarView.selectSlot(5)
                Key.N6, Key.NUMPAD6 -> hotbarView.selectSlot(6)
                Key.TAB -> cycleBiji()
                Key.Q -> {
                    gameState.modeSemprot = if (gameState.modeSemprot == Metode.NABATI) Metode.KIMIA else Metode.NABATI
                    hotbarView.update(gameState, selectedCropId)
                }
                Key.SPACE -> gunakanAlat()
                Key.E -> interaksiTanganAtauObjek()
                Key.F1 -> {
                    if (config.debugAktif) {
                        gameState.uang += 1000
                        for (cropId in allCropIds) {
                            gameState.inventory.biji[cropId] = (gameState.inventory.biji[cropId] ?: 0) + 5
                        }
                        toastView.show("+Rp 1000 & +5 biji tiap jenis")
                        hudView.update(gameState)
                        hotbarView.update(gameState, selectedCropId)
                    }
                }
                Key.F2 -> {
                    if (config.debugAktif) {
                        gantiHari()
                    }
                }
                else -> {}
            }
        }

        // Mouse scroll untuk mengganti slot hotbar
        onScroll { event ->
            if (!shopPanel.isOpen) {
                if (event.scrollDeltaYLines > 0) hotbarView.cycleSlot(1)
                else if (event.scrollDeltaYLines < 0) hotbarView.cycleSlot(-1)
            }
        }

        // 12. Game Loop updater
        addFastUpdater { fastDuration ->
            val dt = fastDuration.seconds

            // Jika ShopPanel sedang terbuka, waktu berhenti dan pemain tidak bergerak
            if (shopPanel.isOpen) {
                toastView.update(dt)
                return@addFastUpdater
            }

            var inputX = 0.0
            var inputY = 0.0

            val k = views.input.keys
            if (k[Key.W] || k[Key.UP]) inputY -= 1.0
            if (k[Key.S] || k[Key.DOWN]) inputY += 1.0
            if (k[Key.A] || k[Key.LEFT]) inputX -= 1.0
            if (k[Key.D] || k[Key.RIGHT]) inputX += 1.0

            // Perbarui pemain
            playerController.update(inputX, inputY, dt)
            playerView.update(playerController, dt)

            // Perbarui penanda petak
            val target = playerController.getTargetTile()
            penandaView.update(target)

            // Perbarui jam & waktu harian
            val tidurOtomatis = DaySimulator.tickWaktu(gameState, config, dt, akumulatorMenit)
            if (tidurOtomatis) {
                gantiHari()
            }

            // Perbarui visual
            hudView.update(gameState)
            hotbarView.update(gameState, selectedCropId)
            toastView.update(dt)
        }
    }
}
