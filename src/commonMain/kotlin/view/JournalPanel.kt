package view

import core.Catalog
import core.GameState
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
 * JournalPanel menampilkan antarmuka Pest Journal dengan 6 entri hama.
 * Menggunakan panel.png 9-slice dan gambar ilustrasi jurnal_<id>.png.
 */
class JournalPanel(
    panelBitmap: Bitmap,
    pestsBitmap: Bitmap,
    ikonBitmap: Bitmap,
    private val journalBitmaps: Map<String, Bitmap>,
    private val catalog: Catalog
) : Container() {

    private val panelWidth = 430.0
    private val panelHeight = 245.0

    private val pests: List<PestData> = catalog.pests
    private var selectedIndex: Int = 0
    private var currentJournalTerbuka: Set<String> = emptySet()

    // Sprite hama 16x16
    private val pestSprites: Array<BmpSlice> = Array(6) { i ->
        pestsBitmap.slice(RectangleInt(i * 16, 0, 16, 16))
    }

    // Ikon gembok (ikon.png index 15: col 3, row 3)
    private val lockIcon: BmpSlice = ikonBitmap.slice(RectangleInt(3 * 16, 3 * 16, 16, 16))

    private val counterText: Text
    private val listRowContainers = mutableListOf<Container>()
    private val listRowTexts = mutableListOf<Text>()
    private val listRowIcons = mutableListOf<Image>()
    private val highlightRect: SolidRect

    // Komponen Detail Kanan
    private val detailIllustration: Image
    private val detailNamaText: Text
    private val detailIlmiahText: Text
    private val detailTanamanText: Text
    private val detailCiriText: Text
    private val detailGejalaText: Text
    private val detailKendaliText: Text
    private val detailTipText: Text
    private val detailTerkunciDescText: Text

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

        // Header: Judul
        val judul = catalog.texts.journal.judul.ifEmpty { "Pest Journal" }
        text(judul) {
            textSize = 11.0
            color = Colors.YELLOW
            x = 20.0
            y = 12.0
        }

        // Header: Counter Terbuka
        counterText = text("0/6 terbuka") {
            textSize = 7.5
            color = Colors["#aaffaa"]
            x = panelWidth - 95.0
            y = 14.0
        }

        // Garis pemisah kolom kiri dan kanan
        solidRect(1.0, panelHeight - 56.0, Colors["#554433"]) {
            x = 135.0
            y = 30.0
        }

        // Highlight baris daftar kiri
        highlightRect = solidRect(114.0, 20.0, Colors["#ffffff"].withAd(0.18)) {
            x = 16.0
            y = 32.0
        }

        // Daftar 6 entri di sisi kiri
        val startY = 34.0
        val rowHeight = 26.0
        for (i in pests.indices) {
            val pest = pests[i]
            val ry = startY + i * rowHeight

            val rowCont = container {
                this.x = 18.0
                this.y = ry
            }

            val iconImg = rowCont.image(lockIcon) {
                smoothing = false
                x = 0.0
                y = 0.0
            }

            val rowTxt = rowCont.text("???") {
                textSize = 7.5
                color = Colors.WHITE
                x = 20.0
                y = 4.0
            }

            listRowContainers.add(rowCont)
            listRowIcons.add(iconImg)
            listRowTexts.add(rowTxt)
        }

        // Komponen Detail Sisi Kanan (x >= 145)
        val dummyBmp = journalBitmaps.values.firstOrNull() ?: panelBitmap
        detailIllustration = image(dummyBmp.slice()) {
            smoothing = false
            x = panelWidth - 84.0
            y = 32.0
            scale = 1.0
        }

        detailNamaText = text("") {
            textSize = 10.0
            color = Colors.YELLOW
            x = 145.0
            y = 32.0
        }

        detailIlmiahText = text("") {
            textSize = 7.5
            color = Colors.LIGHTGRAY
            x = 145.0
            y = 46.0
        }

        detailTanamanText = text("") {
            textSize = 7.0
            color = Colors["#aaffaa"]
            x = 145.0
            y = 58.0
        }

        detailTerkunciDescText = text("") {
            textSize = 7.5
            color = Colors.LIGHTGRAY
            x = 145.0
            y = 75.0
        }

        detailGejalaText = text("") {
            textSize = 6.8
            color = Colors.WHITE
            x = 145.0
            y = 70.0
        }

        detailCiriText = text("") {
            textSize = 6.8
            color = Colors.WHITE
            x = 145.0
            y = 106.0
        }

        detailKendaliText = text("") {
            textSize = 6.8
            color = Colors.WHITE
            x = 145.0
            y = 144.0
        }

        detailTipText = text("") {
            textSize = 6.8
            color = Colors["#ffea88"]
            x = 145.0
            y = 195.0
        }

        // Footer: Petunjuk navigasi
        val petunjuk = catalog.texts.journal.petunjuk.ifEmpty {
            "Kiri/Kanan atau Atas/Bawah: pilih   J atau Esc: tutup"
        }
        text(petunjuk) {
            textSize = 7.0
            color = Colors.LIGHTGRAY
            x = (panelWidth - this.width) / 2.0
            y = panelHeight - 16.0
        }
    }

    val isOpen: Boolean get() = visible

    fun open(state: GameState, initialPestId: String? = null) {
        currentJournalTerbuka = state.journalTerbuka
        if (initialPestId != null) {
            val idx = pests.indexOfFirst { it.id == initialPestId }
            if (idx >= 0) selectedIndex = idx
        } else if (selectedIndex !in pests.indices) {
            selectedIndex = 0
        }
        visible = true
        refresh()
    }

    fun close() {
        visible = false
    }

    fun handleKeyDown(key: Key): Boolean {
        if (!visible) return false
        when (key) {
            Key.UP, Key.W, Key.LEFT, Key.A -> {
                selectedIndex = (selectedIndex - 1 + pests.size) % pests.size
                refresh()
                return true
            }
            Key.DOWN, Key.S, Key.RIGHT, Key.D -> {
                selectedIndex = (selectedIndex + 1) % pests.size
                refresh()
                return true
            }
            Key.J, Key.ESCAPE -> {
                close()
                return true
            }
            else -> return true // Tahan input lain saat panel terbuka
        }
    }

    private fun refresh() {
        val terbukaCount = currentJournalTerbuka.size
        val totalCount = pests.size
        val counterTemplate = catalog.texts.journal.terbuka.ifEmpty { "{terbuka}/{total} terbuka" }
        counterText.text = counterTemplate
            .replace("{terbuka}", terbukaCount.toString())
            .replace("{total}", totalCount.toString())

        val startY = 34.0
        val rowHeight = 26.0
        highlightRect.y = (startY + selectedIndex * rowHeight) - 3.0

        for (i in pests.indices) {
            val pest = pests[i]
            val isUnlocked = currentJournalTerbuka.contains(pest.id)
            val rowTxt = listRowTexts[i]
            val rowIcon = listRowIcons[i]

            if (isUnlocked) {
                rowTxt.text = pest.nama
                rowIcon.bitmap = pestSprites[pest.spriteIndex.coerceIn(0, 5)]
            } else {
                rowTxt.text = catalog.texts.journal.terkunci.ifEmpty { "???" }
                rowIcon.bitmap = lockIcon
            }

            if (i == selectedIndex) {
                rowTxt.color = Colors.YELLOW
            } else {
                rowTxt.color = if (isUnlocked) Colors.WHITE else Colors.DARKGRAY
            }
        }

        // Tampilkan detail kanan
        val curPest = pests[selectedIndex]
        val isCurUnlocked = currentJournalTerbuka.contains(curPest.id)
        val bmp = journalBitmaps[curPest.id]

        if (bmp != null) {
            detailIllustration.bitmap = bmp.slice()
            detailIllustration.visible = true
        } else {
            detailIllustration.visible = false
        }

        if (isCurUnlocked) {
            detailIllustration.colorMul = Colors.WHITE // warna normal
            detailNamaText.text = curPest.nama
            detailIlmiahText.text = curPest.namaIlmiah
            detailTanamanText.text = "${catalog.texts.journal.label.tanaman}: ${curPest.tanaman.replaceFirstChar { it.uppercase() }}"

            val labelGejala = catalog.texts.journal.label.gejala.ifEmpty { "Gejala" }
            detailGejalaText.text = InspectPanel.wrapText("$labelGejala: ${curPest.gejala}", 42)
            detailGejalaText.visible = true

            val labelCiri = catalog.texts.journal.label.ciri.ifEmpty { "Ciri" }
            detailCiriText.text = InspectPanel.wrapText("$labelCiri: ${curPest.ciri}", 42)
            detailCiriText.visible = true

            val labelKendali = catalog.texts.journal.label.caraKendali.ifEmpty { "Cara kendali" }
            detailKendaliText.text = InspectPanel.wrapText("$labelKendali: ${curPest.caraKendali}", 42)
            detailKendaliText.visible = true

            detailTipText.text = InspectPanel.wrapText(curPest.tipGame, 42)
            detailTipText.visible = true

            detailTerkunciDescText.visible = false
        } else {
            detailIllustration.colorMul = Colors["#222222"] // siluet gelap
            detailNamaText.text = catalog.texts.journal.terkunci.ifEmpty { "???" }
            detailIlmiahText.text = ""
            detailTanamanText.text = ""

            detailGejalaText.visible = false
            detailCiriText.visible = false
            detailKendaliText.visible = false
            detailTipText.visible = false

            val desc = catalog.texts.journal.terkunciDeskripsi.ifEmpty {
                "Periksa hama ini dengan Kaca Pembesar untuk membuka catatannya."
            }
            detailTerkunciDescText.text = InspectPanel.wrapText(desc, 36)
            detailTerkunciDescText.visible = true
        }
    }
}
