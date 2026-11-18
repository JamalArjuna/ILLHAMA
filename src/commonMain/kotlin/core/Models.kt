package core

enum class Kualitas { BAIK, BIASA, RUSAK }
enum class Metode { MANUAL, NABATI, PERANGKAP, KIMIA }
enum class TanahState { BELUM_DIOLAH, DIOLAH }

/**
 * Rumus tahap gambar tanaman (indeks kolom di crops.png, 0 sampai 3, 4 = layu):
 * jika mati                          -> 4
 * jika hariTumbuh >= hariTumbuhCrop  -> 3 (matang, bisa dipanen)
 * selain itu                         -> min(2, floor(hariTumbuh * 3 / hariTumbuhCrop))
 */
fun hitungTahapGambar(mati: Boolean, hariTumbuh: Int, hariTumbuhCrop: Int): Int {
    if (mati) return 4
    if (hariTumbuh >= hariTumbuhCrop) return 3
    val tahap = (hariTumbuh * 3) / hariTumbuhCrop
    return if (tahap > 2) 2 else tahap
}

data class Petak(
    val x: Int, val y: Int,                    // indeks lahan 0..7, 0..5
    var tanah: TanahState = TanahState.BELUM_DIOLAH,
    var disiramHariIni: Boolean = false,
    var tanaman: Tanaman? = null
)

data class Tanaman(
    val cropId: String,
    var hariTumbuh: Int = 0,                   // jumlah hari ia disiram
    var kesehatan: Int = 100,                  // 0..100
    var hamaId: String? = null,
    var hamaDiperiksa: Boolean = false,
    var penangananTertunda: Metode? = null,    // NABATI atau PERANGKAP, selesai besok pagi
    var pakaiKimia: Boolean = false,
    var mati: Boolean = false
) {
    fun tahapGambar(hariTumbuhCrop: Int): Int =
        hitungTahapGambar(mati, hariTumbuh, hariTumbuhCrop)
}

data class Inventory(
    val biji: MutableMap<String, Int> = mutableMapOf(),         // cropId -> jumlah
    var nabati: Int = 0,
    var kimia: Int = 0,
    var perangkap: Int = 0,
    val hasil: MutableMap<Pair<String, Kualitas>, Int> = mutableMapOf()   // hasil panen
)

data class GameState(
    var hari: Int = 1,                         // 1..14
    var menitHari: Int = 6 * 60,               // menit sejak 00:00, mulai 06:00
    var uang: Int = 500,
    val inventory: Inventory = Inventory(),
    val bin: MutableMap<Pair<String, Kualitas>, Int> = mutableMapOf(),
    val petak: List<Petak> = List(48) { i -> Petak(i % 8, i / 8) },   // 48 petak
    val journalTerbuka: MutableSet<String> = mutableSetOf(),    // id hama yang pernah diperiksa
    var tutorialIndex: Int = 0,
    var modeSemprot: Metode = Metode.NABATI    // NABATI atau KIMIA
) {
    fun petakAt(x: Int, y: Int): Petak? {
        if (x !in 0..7 || y !in 0..5) return null
        return petak[y * 8 + x]
    }
}
