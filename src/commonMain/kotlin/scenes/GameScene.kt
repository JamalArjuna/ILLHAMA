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
import korlibs.korge.service.storage.storage
import korlibs.korge.view.SContainer
import korlibs.korge.view.addFastUpdater
import korlibs.korge.view.solidRect
import view.*

/**
 * GameScene: Layar utama permainan.
 * Memuat peta, lahan tanam (PlotsView), pemain, penanda petak, jam, HUD, Hotbar,
 * ShopPanel, InspectPanel, JournalPanel, ConfirmSleepPanel, SummaryPanel, TutorialHint, dan toast.
 */
class GameScene(val savedState: GameState? = null) : Scene() {

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

        // 2. Inisialisasi GameState (gunakan savedState jika melanjutkan)
        val gameState = savedState ?: GameState(
            hari = 1,
            menitHari = config.jamMulai * 60,
            uang = config.uangAwal
        )

        val allCropIds = catalog.crops.map { it.id }
        var selectedCropId: String = allCropIds.firstOrNull() ?: "padi"

        // 3. Muat efek suara dan putar BGM berulang
        Sfx.load()
        Sfx.playBgm(coroutineContext)

        // 4. Muat aset grafis
        val tilesetBitmap = resourcesVfs["assets/art/tileset.png"].readBitmap()
        val cropsBitmap = resourcesVfs["assets/art/crops.png"].readBitmap()
        val pestsBitmap = resourcesVfs["assets/art/pests.png"].readBitmap()
        val playerBitmap = resourcesVfs["assets/art/player.png"].readBitmap()
        val penandaBitmap = resourcesVfs["assets/art/penanda_petak.png"].readBitmap()
        val slotBitmap = resourcesVfs["assets/art/slot.png"].readBitmap()
        val slotSelectedBitmap = resourcesVfs["assets/art/slot_terpilih.png"].readBitmap()
        val ikonBitmap = resourcesVfs["assets/art/ikon.png"].readBitmap()
        val panelBitmap = resourcesVfs["assets/art/panel.png"].readBitmap()

        // Muat ilustrasi jurnal untuk tiap hama
        val journalBitmaps = catalog.pests.associate { pest ->
            pest.id to resourcesVfs["assets/art/jurnal_${pest.id}.png"].readBitmap()
        }

        // 5. Render peta dan lahan
        val mapView = MapView(tilesetBitmap, mapData, tileSize = config.tileSize)
        addChild(mapView)

        val plotsView = PlotsView(
            tilesetBitmap = tilesetBitmap,
            cropsBitmap = cropsBitmap,
            pestsBitmap = pestsBitmap,
            ikonBitmap = ikonBitmap,
            originCol = config.lahanOriginTile[0],
            originRow = config.lahanOriginTile[1],
            kolomLahan = config.lahanKolom,
            barisLahan = config.lahanBaris,
            tileSize = config.tileSize
        )
        addChild(plotsView)
        plotsView.update(gameState, catalog)

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
        hudView.update(gameState)

        val hotbarView = HotbarView(slotBitmap, slotSelectedBitmap, ikonBitmap)
        addChild(hotbarView)
        hotbarView.update(gameState, selectedCropId)

        // 9. TutorialHint di atas layar
        val tutorialHint = TutorialHint(catalog)
        addChild(tutorialHint)

        // 10. ToastView
        val toastView = ToastView(config)

        // 11. Dialog Panels
        val shopPanel = ShopPanel(panelBitmap, ikonBitmap, catalog) { pesan ->
            toastView.show(pesan)
        }
        addChild(shopPanel)

        val journalPanel = JournalPanel(panelBitmap, pestsBitmap, ikonBitmap, journalBitmaps, catalog)

        val inspectPanel = InspectPanel(panelBitmap, pestsBitmap, catalog) {
            journalPanel.open(gameState)
        }
        addChild(inspectPanel)
        addChild(journalPanel)

        val summaryPanel = SummaryPanel(panelBitmap, catalog)
        addChild(summaryPanel)

        lateinit var gantiHariFn: () -> Unit

        val confirmSleepPanel = ConfirmSleepPanel(panelBitmap, catalog) {
            gantiHariFn()
        }
        addChild(confirmSleepPanel)

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

        fun simpanGame() {
            try {
                val jsonString = SaveData.serialize(gameState)
                views.storage[SaveData.KUNCI_SAVE] = jsonString
            } catch (_: Throwable) {}
        }

        fun gantiHari() {
            val musimSelesai = DaySimulator.tickHari(gameState, config, catalog)
            plotsView.update(gameState, catalog)
            hudView.update(gameState)
            hotbarView.update(gameState, selectedCropId)

            if (musimSelesai) {
                Sfx.stopBgm()
                val rating = Economy.hitungRating(gameState.uang, gameState.journalTerbuka.size, config)
                launchImmediately {
                    sceneContainer.changeTo { EndScene(gameState.uang, gameState.journalTerbuka.size, rating) }
                }
            } else {
                // Simpan otomatis di langkah 9 pergantian hari
                simpanGame()

                // Buka summary panel
                val summary = gameState.summaryKemarin
                if (summary != null) {
                    summaryPanel.open(summary)
                }

                tutorialHint.onEvent("hari_berganti", gameState)
            }
        }
        gantiHariFn = { gantiHari() }

        fun gunakanAlat() {
            val target = playerController.getTargetTile()
            if (!target.isLahan) return

            val petak = gameState.petakAt(target.lahanCol, target.lahanRow)
            val slot = hotbarView.selectedSlot
            val hasil = Interaction.pakaiAlat(
                slot = slot,
                petak = petak,
                inventory = gameState.inventory,
                selectedCropId = selectedCropId,
                modeSemprot = gameState.modeSemprot,
                journalTerbuka = gameState.journalTerbuka,
                catalog = catalog
            )

            when (hasil) {
                is InteractionResult.PeriksaHama -> {
                    val pest = catalog.pestById[hasil.pestId]
                    if (pest != null) {
                        inspectPanel.open(pest)
                    }
                    if (hasil.jurnalBaru) {
                        val template = catalog.texts.toast["journalBaru"] ?: "Journal baru terbuka: {nama}."
                        val pestNama = pest?.nama ?: hasil.pestId
                        toastView.show(template.replace("{nama}", pestNama))
                    }
                    plotsView.update(gameState, catalog)
                }
                is InteractionResult.PeriksaSehat -> {
                    val template = catalog.texts.toast["tanamanSehat"] ?: "Tanaman ini tampak sehat (kesehatan {kesehatan})."
                    toastView.show(template.replace("{kesehatan}", hasil.kesehatan.toString()))
                }
                is InteractionResult.Sukses -> {
                    if (hasil.sfx == "sfx_cangkul") {
                        Sfx.playCangkul()
                        tutorialHint.onEvent("petak_diolah", gameState)
                    }
                    if (hasil.sfx == "sfx_siram") {
                        Sfx.playSiram()
                        tutorialHint.onEvent("tanaman_disiram", gameState)
                    }
                    if (slot == 6) {
                        tutorialHint.onEvent("tanaman_ditanam", gameState)
                    }
                    if (hasil.toastKey != null) {
                        val pesan = catalog.texts.toast[hasil.toastKey] ?: hasil.toastKey
                        toastView.show(pesan)
                    }
                    plotsView.update(gameState, catalog)
                    hotbarView.update(gameState, selectedCropId)
                }
                is InteractionResult.Gagal -> {
                    val pesan = catalog.texts.toast[hasil.toastKey] ?: hasil.toastKey
                    toastView.show(pesan)
                    Sfx.playGagal()
                    plotsView.update(gameState, catalog)
                    hotbarView.update(gameState, selectedCropId)
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
                        tutorialHint.onEvent("toko_dibuka", gameState)
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
                        confirmSleepPanel.open()
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
                    is HandResult.ManualBerhasil -> {
                        plotsView.update(gameState, catalog)
                        val msg = catalog.texts.toast["manualBerhasil"] ?: "Hama berhasil diambil."
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
                        Sfx.playGagal()
                    }
                    is HandResult.TidakAdaAksi -> {}
                }
            }
        }

        // 12. Event tombol sekali tekan
        keys.down { key ->
            // Panel ConfirmSleepPanel
            if (confirmSleepPanel.isOpen) {
                confirmSleepPanel.handleKeyDown(key.key)
                return@down
            }

            // Panel SummaryPanel
            if (summaryPanel.isOpen) {
                summaryPanel.handleKeyDown(key.key)
                return@down
            }

            // Panel ShopPanel
            if (shopPanel.isOpen) {
                val oldBijiTotal = gameState.inventory.biji.values.sum()
                shopPanel.handleKeyDown(key.key)
                val newBijiTotal = gameState.inventory.biji.values.sum()
                if (newBijiTotal > oldBijiTotal) {
                    tutorialHint.onEvent("biji_dibeli", gameState)
                }
                hudView.update(gameState)
                hotbarView.update(gameState, selectedCropId)
                return@down
            }

            // Panel InspectPanel
            if (inspectPanel.isOpen) {
                inspectPanel.handleKeyDown(key.key)
                return@down
            }

            // Panel JournalPanel
            if (journalPanel.isOpen) {
                journalPanel.handleKeyDown(key.key)
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
                Key.J -> {
                    journalPanel.open(gameState)
                }
                Key.M -> {
                    val muted = Sfx.toggleMute()
                    toastView.show(if (muted) "Suara dimatikan (Mute)" else "Suara diaktifkan")
                }
                Key.SPACE -> gunakanAlat()
                Key.E -> interaksiTanganAtauObjek()
                Key.F1 -> {
                    if (config.debugAktif) {
                        gameState.uang += 1000
                        for (cropId in allCropIds) {
                            gameState.inventory.biji[cropId] = (gameState.inventory.biji[cropId] ?: 0) + 5
                        }
                        gameState.inventory.nabati += 5
                        gameState.inventory.perangkap += 5
                        gameState.inventory.kimia += 5
                        toastView.show("+Rp 1000 & +5 bahan tiap jenis")
                        hudView.update(gameState)
                        hotbarView.update(gameState, selectedCropId)
                    }
                }
                Key.F2 -> {
                    if (config.debugAktif) {
                        gantiHari()
                    }
                }
                Key.F3 -> {
                    if (config.debugAktif) {
                        var munculCount = 0
                        for (petak in gameState.petak) {
                            val t = petak.tanaman
                            if (t != null && !t.mati && t.hamaId == null) {
                                val crop = catalog.cropById[t.cropId]
                                if (crop != null && crop.hama.isNotEmpty()) {
                                    val tahap = t.tahapGambar(crop.hariTumbuh)
                                    if (tahap >= config.tahapMinimalMuncul) {
                                        t.hamaId = crop.hama.first()
                                        t.hamaDiperiksa = false
                                        munculCount++
                                    }
                                }
                            }
                        }
                        plotsView.update(gameState, catalog)
                        toastView.show("Debug: $munculCount tanaman terserang hama")
                    }
                }
                Key.F4 -> {
                    if (config.debugAktif) {
                        var matangCount = 0
                        for (petak in gameState.petak) {
                            val t = petak.tanaman
                            if (t != null && !t.mati) {
                                val crop = catalog.cropById[t.cropId]
                                if (crop != null) {
                                    t.hariTumbuh = crop.hariTumbuh
                                    matangCount++
                                }
                            }
                        }
                        plotsView.update(gameState, catalog)
                        toastView.show("Debug: $matangCount tanaman matang")
                    }
                }
                else -> {}
            }
        }

        // Mouse scroll untuk mengganti slot hotbar
        onScroll { event ->
            val anyPanelOpen = confirmSleepPanel.isOpen || summaryPanel.isOpen || shopPanel.isOpen || inspectPanel.isOpen || journalPanel.isOpen
            if (!anyPanelOpen) {
                if (event.scrollDeltaYLines > 0) hotbarView.cycleSlot(1)
                else if (event.scrollDeltaYLines < 0) hotbarView.cycleSlot(-1)
            }
        }

        // 13. Game Loop updater
        addFastUpdater { fastDuration ->
            val dt = fastDuration.seconds

            // Jika ada panel terbuka, waktu berhenti dan pemain tidak bergerak
            val anyPanelOpen = confirmSleepPanel.isOpen || summaryPanel.isOpen || shopPanel.isOpen || inspectPanel.isOpen || journalPanel.isOpen
            if (anyPanelOpen) {
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

            if (inputX != 0.0 || inputY != 0.0) {
                tutorialHint.onEvent("pemain_bergerak", gameState)
            }

            // Perbarui pemain
            playerController.update(inputX, inputY, dt)
            playerView.update(playerController, dt)

            // Perbarui penanda petak
            val target = playerController.getTargetTile()
            penandaView.update(target)

            // Periksa petunjuk kejadian hama_pertama
            if (!gameState.hamaPertamaMuncul) {
                val adaHama = gameState.petak.any { it.tanaman?.hamaId != null && it.tanaman?.mati == false }
                if (adaHama) {
                    gameState.hamaPertamaMuncul = true
                    val msg = catalog.texts.petunjukKejadian["hama_pertama"]
                    if (msg != null) {
                        tutorialHint.showKejadian(msg)
                    }
                }
            }

            // Periksa petunjuk kejadian matang_pertama
            if (!gameState.matangPertamaMuncul) {
                val adaMatang = gameState.petak.any { p ->
                    val t = p.tanaman
                    if (t != null && !t.mati) {
                        val crop = catalog.cropById[t.cropId]
                        crop != null && t.hariTumbuh >= crop.hariTumbuh
                    } else false
                }
                if (adaMatang) {
                    gameState.matangPertamaMuncul = true
                    val msg = catalog.texts.petunjukKejadian["matang_pertama"]
                    if (msg != null) {
                        tutorialHint.showKejadian(msg)
                    }
                }
            }

            // Perbarui jam & waktu harian
            val tidurOtomatis = DaySimulator.tickWaktu(gameState, config, dt, akumulatorMenit)
            if (tidurOtomatis) {
                gantiHari()
            }

            // Perbarui visual
            hudView.update(gameState)
            hotbarView.update(gameState, selectedCropId)
            tutorialHint.update(dt, gameState)
            toastView.update(dt)
        }
    }
}
