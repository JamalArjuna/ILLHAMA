package core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlayerMovementTest {

    private fun buatMapData(): MapJsonData {
        // Ground 30x17, default rumput (0)
        // Dinding (9) di pinggir atas
        val ground = List(17) { row ->
            List(30) { col ->
                if (row == 0) 9 // dinding atas terblokir
                else if (row == 3 && col == 5) 11 // pintu rumah (terblokir)
                else 0 // rumput (bisa dilewati)
            }
        }
        return MapJsonData(
            tileSize = 16,
            kolom = 30,
            baris = 17,
            ground = ground,
            tileTerblokir = listOf(6, 7, 8, 9, 10, 11, 12, 13, 14, 15),
            lahan = MapJsonLahan(originTile = listOf(3, 7), kolom = 8, baris = 6),
            spawn = listOf(5, 5),
            objek = listOf(
                MapJsonObjek(id = "pintu_rumah", jenis = "tidur", tile = listOf(5, 3), berdiriDi = listOf(5, 4)),
                MapJsonObjek(id = "pintu_toko", jenis = "toko", tile = listOf(22, 4), berdiriDi = listOf(22, 5)),
                MapJsonObjek(id = "shipping_bin", jenis = "bin", tile = listOf(13, 7), berdiriDi = listOf(12, 7))
            )
        )
    }

    private fun buatController(mapData: MapJsonData): PlayerController {
        return PlayerController(
            x = (mapData.spawn[0] * 16).toDouble(),
            y = (mapData.spawn[1] * 16).toDouble(),
            speed = 80.0,
            hitboxLebar = 10.0,
            hitboxTinggi = 8.0,
            hitboxOffsetX = 3.0,
            hitboxOffsetY = 8.0,
            mapData = mapData
        )
    }

    @Test
    fun `pemain muncul di tile spawn tanpa tabrakan`() {
        val mapData = buatMapData()
        val player = buatController(mapData)

        assertEquals(80.0, player.x)
        assertEquals(80.0, player.y)
        assertTrue(player.canMoveTo(player.x, player.y), "Posisi spawn harus valid")
    }

    @Test
    fun `pemain tidak bisa keluar batas peta`() {
        val mapData = buatMapData()
        val player = buatController(mapData)

        // Coba posisi di luar batas kiri
        assertFalse(player.canMoveTo(-10.0, 80.0), "Tidak boleh keluar peta kiri")

        // Coba posisi di luar batas kanan
        assertFalse(player.canMoveTo(480.0, 80.0), "Tidak boleh keluar peta kanan")

        // Coba posisi di luar batas bawah
        assertFalse(player.canMoveTo(80.0, 272.0), "Tidak boleh keluar peta bawah")
    }

    @Test
    fun `pemain tidak bisa menembus tile terblokir`() {
        val mapData = buatMapData()
        val player = buatController(mapData)

        // Di row 0 ada dinding (9). Jika y mendekati row 0:
        // Hitbox top = y + 8. Jika y = 0, hitbox top = 8 (berada di tile row 0 yang terblokir)
        assertFalse(player.canMoveTo(80.0, 0.0), "Tidak boleh menembus dinding di row 0")
    }

    @Test
    fun `pemain bergerak dan arah hadap diperbarui`() {
        val mapData = buatMapData()
        val player = buatController(mapData)

        // Bergerak ke kanan
        player.update(1.0, 0.0, 0.1)
        assertEquals(Direction.RIGHT, player.facing)
        assertTrue(player.x > 80.0, "x harus bertambah saat gerak ke kanan")

        // Bergerak ke atas
        player.update(0.0, -1.0, 0.1)
        assertEquals(Direction.UP, player.facing)
    }

    @Test
    fun `petak sasaran di depan pemain dan deteksi lahan`() {
        val mapData = buatMapData()
        val player = buatController(mapData)

        // Pemain di (5, 5), menghadap ke bawah (DOWN)
        player.facing = Direction.DOWN
        val target = player.getTargetTile()

        // Hitbox center: x=80+3+5=88, y=80+8+4=92
        // Tile: col = 88/16 = 5. Target y = 92 + 16 = 108 -> row = 108/16 = 6
        assertEquals(5, target.col)
        assertEquals(6, target.row)
        assertFalse(target.isLahan, "Tile (5, 6) bukan area lahan (lahan mulai row 7)")

        // Pindah ke (5, 6) menghadap ke bawah
        player.y = (6 * 16).toDouble()
        val targetLahan = player.getTargetTile()
        assertEquals(5, targetLahan.col)
        assertEquals(7, targetLahan.row)
        assertTrue(targetLahan.isLahan, "Tile (5, 7) harus terdeteksi sebagai lahan")
        assertEquals(2, targetLahan.lahanCol, "5 - 3 = 2")
        assertEquals(0, targetLahan.lahanRow, "7 - 7 = 0")
    }

    @Test
    fun `deteksi interaksi objek di depan pemain`() {
        val mapData = buatMapData()
        val player = buatController(mapData)

        // Berdiri di depan pintu rumah: tile (5, 4) menghadap UP (ke tile 5, 3)
        player.x = (5 * 16).toDouble()
        player.y = (4 * 16).toDouble()
        player.facing = Direction.UP

        val target = player.getTargetTile()
        assertEquals(5, target.col)
        assertEquals(3, target.row)

        val objekId = player.checkObjectInteraction(target)
        assertNotNull(objekId)
        assertEquals("pintu_rumah", objekId)

        // Berdiri menghadap arah lain (misal DOWN) -> tidak ada interaksi objek
        player.facing = Direction.DOWN
        val targetLain = player.getTargetTile()
        assertNull(player.checkObjectInteraction(targetLain))
    }
}
