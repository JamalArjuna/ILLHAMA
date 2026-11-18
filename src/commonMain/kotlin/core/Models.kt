package core

enum class Kualitas { BAIK, BIASA, RUSAK }
enum class Metode { MANUAL, NABATI, PERANGKAP, KIMIA }
enum class TanahState { BELUM_DIOLAH, DIOLAH }

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
)

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
)
