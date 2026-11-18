package scenes

import core.*
import korlibs.event.Key
import korlibs.image.color.Colors
import korlibs.image.format.readBitmap
import korlibs.io.async.launchImmediately
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.input.keys
import korlibs.korge.scene.Scene
import korlibs.korge.view.SContainer
import korlibs.korge.view.addFastUpdater
import korlibs.korge.view.solidRect
import view.*

/**
 * GameScene: Layar utama permainan.
 * Memuat peta, pemain, waktu, HUD, dan toast.
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

        // 2. Inisialisasi GameState
        val gameState = GameState(
            hari = 1,
            menitHari = config.jamMulai * 60,
            uang = config.uangAwal
        )

        // 3. Muat aset grafis
        val tilesetBitmap = resourcesVfs["assets/art/tileset.png"].readBitmap()
        val playerBitmap = resourcesVfs["assets/art/player.png"].readBitmap()
        val penandaBitmap = resourcesVfs["assets/art/penanda_petak.png"].readBitmap()

        // 4. Render peta
        val mapView = MapView(tilesetBitmap, mapData, tileSize = config.tileSize)
        addChild(mapView)

        // 5. Penanda petak sasaran di lahan
        val penandaView = PenandaPetakView(penandaBitmap, tileSize = config.tileSize)
        addChild(penandaView)

        // 6. Pemain dan pengontrol pergerakan
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

        // 7. HUD dan Toast
        val hudView = HudView(config, catalog)
        addChild(hudView)

        val toastView = ToastView(config)
        addChild(toastView)

        val akumulatorMenit = doubleArrayOf(0.0)

        fun gantiHari() {
            val musimSelesai = DaySimulator.tickHari(gameState, config)
            if (musimSelesai) {
                launchImmediately {
                    sceneContainer.changeTo { EndScene() }
                }
            } else {
                val formatHariTemplate = catalog.texts.hud["hari"] ?: "Hari {hari}/{total}"
                val infoHari = formatHariTemplate
                    .replace("{hari}", gameState.hari.toString())
                    .replace("{total}", config.jumlahHariMusim.toString())
                toastView.show(infoHari)
            }
        }

        // 8. Event tombol sekali tekan (E untuk objek, F2 untuk debug lompat hari)
        keys.down { key ->
            when (key.key) {
                Key.E -> {
                    val target = playerController.getTargetTile()
                    val objekId = playerController.checkObjectInteraction(target)
                    if (objekId != null) {
                        val pesanToast = when (objekId) {
                            "pintu_rumah" -> catalog.texts.tidur["konfirmasi"] ?: "Tidur dan ganti hari?"
                            "pintu_toko" -> catalog.texts.toko["judul"] ?: "Toko Tani"
                            "shipping_bin" -> catalog.texts.bin["petunjuk"] ?: "Shipping bin"
                            else -> objekId
                        }
                        toastView.show(pesanToast)
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

        // 9. Game Loop updater menggunakan addFastUpdater
        addFastUpdater { fastDuration ->
            val dt = fastDuration.seconds

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

            // Perbarui HUD dan Toast
            hudView.update(gameState)
            toastView.update(dt)
        }
    }
}
