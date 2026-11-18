package view

import core.GameState
import core.Metode
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.BmpSlice
import korlibs.image.bitmap.slice
import korlibs.image.color.Colors
import korlibs.korge.view.*
import korlibs.math.geom.RectangleInt

/**
 * HotbarView merender 6 slot alat di tengah bawah layar (20x20 per slot).
 * Slot 1: Cangkul (ikon 0)
 * Slot 2: Penyiram (ikon 1)
 * Slot 3: Kaca Pembesar (ikon 2)
 * Slot 4: Penyemprot (ikon 3, badge N/K)
 * Slot 5: Perangkap (ikon 4, badge stok)
 * Slot 6: Biji (ikon 5, badge stok dan nama biji terpilih)
 */
class HotbarView(
    slotBitmap: Bitmap,
    slotSelectedBitmap: Bitmap,
    ikonBitmap: Bitmap
) : Container() {

    private val slotBgSlice = slotBitmap.slice()
    private val slotSelectedSlice = slotSelectedBitmap.slice()

    // Ikon 64x64 dipotong 4 kolom x 4 baris, 16x16 per ikon
    private val ikonSlices: Array<BmpSlice> = Array(16) { i ->
        val tx = (i % 4) * 16
        val ty = (i / 4) * 16
        ikonBitmap.slice(RectangleInt(tx, ty, 16, 16))
    }

    var selectedSlot: Int = 1
        private set

    private class SlotElement(
        val bgImage: Image,
        val iconImage: Image,
        val badgeText: Text
    )

    private val slots = mutableListOf<SlotElement>()
    private val infoBijiText: Text

    init {
        // Posisikan hotbar di tengah bawah (6 slot x 20 px = 120 px lebar)
        x = (480.0 - 120.0) / 2.0
        y = 248.0

        val toolIcons = listOf(0, 1, 2, 3, 4, 5)

        for (i in 0 until 6) {
            val sx = i * 20.0
            val slotContainer = container {
                this.x = sx
                this.y = 0.0
            }

            val bg = slotContainer.image(if (i == 0) slotSelectedSlice else slotBgSlice) {
                smoothing = false
            }

            val icon = slotContainer.image(ikonSlices[toolIcons[i]]) {
                smoothing = false
                x = 2.0
                y = 2.0
            }

            val badge = slotContainer.text("") {
                textSize = 7.0
                color = Colors.YELLOW
                x = 10.0
                y = 11.0
            }

            slots.add(SlotElement(bg, icon, badge))
        }

        // Teks info kecil di atas slot 6 untuk jenis biji yang sedang dipilih
        infoBijiText = text("") {
            textSize = 7.0
            color = Colors["#ffff99"]
            x = 80.0
            y = -8.0
        }
    }

    fun selectSlot(slot: Int) {
        if (slot in 1..6) {
            selectedSlot = slot
            refreshBgs()
        }
    }

    fun cycleSlot(delta: Int) {
        var next = selectedSlot + delta
        if (next < 1) next = 6
        if (next > 6) next = 1
        selectSlot(next)
    }

    private fun refreshBgs() {
        for (i in slots.indices) {
            val isSelected = (i + 1) == selectedSlot
            slots[i].bgImage.bitmap = if (isSelected) slotSelectedSlice else slotBgSlice
        }
    }

    fun update(state: GameState, selectedCropId: String?) {
        refreshBgs()

        // Slot 4: Penyemprot (mode Nabati / Kimia dan jumlah stok bahan)
        val bahanStok = if (state.modeSemprot == Metode.KIMIA) state.inventory.kimia else state.inventory.nabati
        val modeHuruf = if (state.modeSemprot == Metode.KIMIA) "K" else "N"
        slots[3].badgeText.text = "$modeHuruf:$bahanStok"

        // Slot 5: Perangkap (jumlah stok)
        val perangkapStok = state.inventory.perangkap
        slots[4].badgeText.text = perangkapStok.toString()

        // Slot 6: Biji (jumlah stok biji terpilih)
        if (selectedCropId != null) {
            val bijiStok = state.inventory.biji[selectedCropId] ?: 0
            slots[5].badgeText.text = if (bijiStok > 0) bijiStok.toString() else "0"
            infoBijiText.text = selectedCropId.replaceFirstChar { it.uppercase() }
            infoBijiText.visible = true
        } else {
            slots[5].badgeText.text = "0"
            infoBijiText.text = "-"
            infoBijiText.visible = false
        }
    }
}
