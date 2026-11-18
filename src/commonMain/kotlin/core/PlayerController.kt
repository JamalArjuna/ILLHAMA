package core

import kotlin.math.floor
import kotlin.math.sqrt

/**
 * Arah hadap pemain.
 * Urutan baris di player.png:
 * Baris 0: bawah (DOWN)
 * Baris 1: kiri (LEFT)
 * Baris 2: kanan (RIGHT)
 * Baris 3: atas (UP)
 */
enum class Direction(val dx: Int, val dy: Int, val spriteRow: Int) {
    DOWN(0, 1, 0),
    LEFT(-1, 0, 1),
    RIGHT(1, 0, 2),
    UP(0, -1, 3)
}

/**
 * Petak sasaran tepat di depan pemain.
 */
data class TargetTile(
    val col: Int,
    val row: Int,
    val isLahan: Boolean,
    val lahanCol: Int = -1,
    val lahanRow: Int = -1
)

/**
 * Pengontrol logika pergerakan pemain, tabrakan, dan petak sasaran.
 * Murni logika Kotlin tanpa dependensi KorGE.
 */
class PlayerController(
    var x: Double,
    var y: Double,
    val speed: Double,
    val hitboxLebar: Double,
    val hitboxTinggi: Double,
    val hitboxOffsetX: Double,
    val hitboxOffsetY: Double,
    val mapData: MapJsonData
) {
    var facing: Direction = Direction.DOWN
    var isMoving: Boolean = false

    val hitboxLeft: Double get() = x + hitboxOffsetX
    val hitboxRight: Double get() = hitboxLeft + hitboxLebar
    val hitboxTop: Double get() = y + hitboxOffsetY
    val hitboxBottom: Double get() = hitboxTop + hitboxTinggi

    val hitboxCenterX: Double get() = x + hitboxOffsetX + hitboxLebar / 2.0
    val hitboxCenterY: Double get() = y + hitboxOffsetY + hitboxTinggi / 2.0

    /**
     * Memperbarui posisi pemain berdasarkan input pergerakan (-1, 0, 1) dan delta waktu (detik).
     * Menggunakan pemisahan sumbu (axis-separated collision) agar pemain dapat meluncur di sepanjang tembok/pagar.
     */
    fun update(inputX: Double, inputY: Double, dt: Double) {
        if (inputX == 0.0 && inputY == 0.0) {
            isMoving = false
            return
        }

        isMoving = true

        // Menentukan arah hadap pemain
        if (inputY > 0.0) facing = Direction.DOWN
        else if (inputY < 0.0) facing = Direction.UP
        else if (inputX < 0.0) facing = Direction.LEFT
        else if (inputX > 0.0) facing = Direction.RIGHT

        // Normalisasi kecepatan (agar gerak diagonal tidak lebih cepat)
        var moveX = inputX
        var moveY = inputY
        val len = sqrt(moveX * moveX + moveY * moveY)
        if (len > 0.0) {
            moveX = (moveX / len) * speed * dt
            moveY = (moveY / len) * speed * dt
        }

        // Cek pergerakan sumbu X
        if (moveX != 0.0) {
            val candidateX = x + moveX
            if (canMoveTo(candidateX, y)) {
                x = candidateX
            }
        }

        // Cek pergerakan sumbu Y
        if (moveY != 0.0) {
            val candidateY = y + moveY
            if (canMoveTo(x, candidateY)) {
                y = candidateY
            }
        }
    }

    /**
     * Memeriksa apakah hitbox pemain valid pada posisi koordinat (candidateX, candidateY).
     * Valid jika tidak keluar batas peta dan tidak bertabrakan dengan tile terblokir.
     */
    fun canMoveTo(candidateX: Double, candidateY: Double): Boolean {
        val left = candidateX + hitboxOffsetX
        val right = left + hitboxLebar
        val top = candidateY + hitboxOffsetY
        val bottom = top + hitboxTinggi

        val mapWidthPx = mapData.kolom * mapData.tileSize
        val mapHeightPx = mapData.baris * mapData.tileSize

        // Batas peta (pemain tidak boleh keluar peta)
        if (left < 0.0 || right > mapWidthPx || top < 0.0 || bottom > mapHeightPx) {
            return false
        }

        // Deteksi tile yang tumpang-tindih dengan hitbox kaki
        val minCol = floor(left / mapData.tileSize).toInt()
        val maxCol = floor((right - 0.001) / mapData.tileSize).toInt()
        val minRow = floor(top / mapData.tileSize).toInt()
        val maxRow = floor((bottom - 0.001) / mapData.tileSize).toInt()

        for (r in minRow..maxRow) {
            if (r !in 0 until mapData.baris) return false
            val rowList = mapData.ground[r]
            for (c in minCol..maxCol) {
                if (c !in 0 until mapData.kolom) return false
                val tileId = rowList[c]
                if (tileId in mapData.tileTerblokirSet) {
                    return false
                }
            }
        }

        return true
    }

    /**
     * Menghitung petak sasaran (1 tile di depan pemain sesuai arah hadap,
     * diukur dari titik tengah hitbox kaki pemain).
     */
    fun getTargetTile(): TargetTile {
        val targetX = hitboxCenterX + facing.dx * mapData.tileSize
        val targetY = hitboxCenterY + facing.dy * mapData.tileSize

        val col = floor(targetX / mapData.tileSize).toInt()
        val row = floor(targetY / mapData.tileSize).toInt()

        val originX = mapData.lahan.originTile[0]
        val originY = mapData.lahan.originTile[1]
        val lahanW = mapData.lahan.kolom
        val lahanH = mapData.lahan.baris

        val lahanCol = col - originX
        val lahanRow = row - originY

        val isLahan = lahanCol in 0 until lahanW && lahanRow in 0 until lahanH

        return TargetTile(
            col = col,
            row = row,
            isLahan = isLahan,
            lahanCol = if (isLahan) lahanCol else -1,
            lahanRow = if (isLahan) lahanRow else -1
        )
    }

    /**
     * Memeriksa apakah petak sasaran berada di depan salah satu objek interaktif:
     * - pintu_rumah (tile [5, 3])
     * - pintu_toko (tile [22, 4])
     * - shipping_bin (tile [13, 7])
     * Mengembalikan id objek jika cocok, atau null jika tidak ada objek.
     */
    fun checkObjectInteraction(target: TargetTile): String? {
        for (obj in mapData.objek) {
            if (target.col == obj.tile[0] && target.row == obj.tile[1]) {
                return obj.id
            }
        }
        return null
    }
}
