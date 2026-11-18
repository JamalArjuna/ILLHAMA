package view

import audio.Sfx
import core.*
import korlibs.event.Key
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.BmpSlice
import korlibs.image.bitmap.slice
import korlibs.image.color.Colors
import korlibs.korge.view.*
import korlibs.math.geom.RectangleInt
import korlibs.math.geom.Size

/**
 * ShopPanel merender dialog belanja toko menggunakan panel.png (9-slice).
 * Daftar barang sesuai items.json.urutanDiToko.
 * Navigasi: Naik/Turun (W/S), Enter: beli 1 unit, Esc: tutup.
 */
class ShopPanel(
    panelBitmap: Bitmap,
    ikonBitmap: Bitmap,
    private val catalog: Catalog,
    private val onToastNeeded: (String) -> Unit
) : Container() {

    private val panelWidth = 320.0
    private val panelHeight = 210.0

    // Potong 16 ikon dari ikon.png (4 kolom x 4 baris)
    private val ikonSlices: Array<BmpSlice> = Array(16) { i ->
        val tx = (i % 4) * 16
        val ty = (i / 4) * 16
        ikonBitmap.slice(RectangleInt(tx, ty, 16, 16))
    }

    private val items: List<ShopItem> = Shop.daftarItem(catalog)
    private var selectedIndex: Int = 0
    private var currentState: GameState? = null

    private val uangText: Text
    private val highlightRect: SolidRect

    private class RowViews(
        val iconImg: Image,
        val namaText: Text,
        val hargaText: Text,
        val stokText: Text
    )

    private val rowViews = mutableListOf<RowViews>()

    init {
        // Tengahkan di layar virtual (480 x 272)
        x = (480.0 - panelWidth) / 2.0
        y = (272.0 - panelHeight) / 2.0
        visible = false

        // Background 9-patch panel.png (sudut 16 px)
        ninePatch(
            tex = panelBitmap.slice(),
            size = Size(panelWidth.toFloat(), panelHeight.toFloat()),
            left = 16f,
            top = 16f,
            right = 16f,
            bottom = 16f
        ) {
            smoothing = false
        }

        // Judul Toko
        val judul = catalog.texts.toko["judul"] ?: "Toko Tani"
        text(judul) {
            textSize = 12.0
            color = Colors.YELLOW
            x = (panelWidth - this.width) / 2.0
            y = 10.0
        }

        // Uang Pemain
        uangText = text("Uangmu: Rp 0") {
            textSize = 8.0
            color = Colors["#aaffaa"]
            x = 16.0
            y = 26.0
        }

        // Highlight baris terpilih
        highlightRect = solidRect(panelWidth - 24.0, 20.0, Colors["#ffffff"].withAd(0.18)) {
            x = 12.0
            y = 42.0
        }

        // Daftar 6 item
        val startY = 44.0
        val rowHeight = 22.0

        for (i in items.indices) {
            val item = items[i]
            val ry = startY + i * rowHeight

            val icon = image(ikonSlices[item.ikon.coerceIn(0, 15)]) {
                smoothing = false
                x = 16.0
                y = ry
            }

            val nama = text(item.nama) {
                textSize = 8.0
                color = Colors.WHITE
                x = 36.0
                y = ry + 4.0
            }

            val harga = text("Rp ${item.harga}") {
                textSize = 8.0
                color = Colors["#ffea88"]
                x = 180.0
                y = ry + 4.0
            }

            val stok = text("Punya: 0") {
                textSize = 8.0
                color = Colors.LIGHTGRAY
                x = 240.0
                y = ry + 4.0
            }

            rowViews.add(RowViews(icon, nama, harga, stok))
        }

        // Petunjuk navigasi footer
        val petunjuk = catalog.texts.toko["petunjuk"] ?: "Naik/Turun: pilih   Enter: beli   Esc: tutup"
        text(petunjuk) {
            textSize = 7.0
            color = Colors.LIGHTGRAY
            x = (panelWidth - this.width) / 2.0
            y = panelHeight - 18.0
        }
    }

    val isOpen: Boolean get() = visible

    fun open(state: GameState) {
        currentState = state
        selectedIndex = 0
        visible = true
        refresh()
    }

    fun close() {
        visible = false
        currentState = null
    }

    fun handleKeyDown(key: Key): Boolean {
        if (!visible) return false
        val state = currentState ?: return false

        when (key) {
            Key.UP, Key.W -> {
                selectedIndex = (selectedIndex - 1 + items.size) % items.size
                refresh()
                return true
            }
            Key.DOWN, Key.S -> {
                selectedIndex = (selectedIndex + 1) % items.size
                refresh()
                return true
            }
            Key.RETURN, Key.KP_ENTER -> {
                val selectedItem = items[selectedIndex]
                val berhasil = Shop.beli(selectedItem, state)
                if (berhasil) {
                    Sfx.playBeli()
                    refresh()
                } else {
                    Sfx.playGagal()
                    val pesanKurang = catalog.texts.toast["uangKurang"] ?: "Uangmu tidak cukup."
                    onToastNeeded(pesanKurang)
                }
                return true
            }
            Key.ESCAPE -> {
                close()
                return true
            }
            else -> return true // Tahan tombol lain selama toko terbuka
        }
    }

    fun refresh() {
        val state = currentState ?: return

        val uangTemplate = catalog.texts.toko["uang"] ?: "Uangmu: Rp {uang}"
        uangText.text = uangTemplate.replace("{uang}", state.uang.toString())

        val startY = 44.0
        val rowHeight = 22.0
        highlightRect.y = (startY + selectedIndex * rowHeight) - 2.0

        val stokTemplate = catalog.texts.toko["stok"] ?: "Punya: {jumlah}"

        for (i in items.indices) {
            val item = items[i]
            val row = rowViews[i]
            val punya = Shop.jumlahDimiliki(item, state.inventory)
            row.stokText.text = stokTemplate.replace("{jumlah}", punya.toString())

            if (i == selectedIndex) {
                row.namaText.color = Colors.YELLOW
            } else {
                row.namaText.color = Colors.WHITE
            }
        }
    }
}
