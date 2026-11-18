package audio

import korlibs.audio.sound.Sound
import korlibs.audio.sound.readSound
import korlibs.io.file.std.resourcesVfs

/**
 * Manajer efek suara (SFX) permainan.
 */
object Sfx {
    private var soundCangkul: Sound? = null
    private var soundSiram: Sound? = null
    private var soundGagal: Sound? = null
    private var soundBeli: Sound? = null
    private var soundPanen: Sound? = null
    private var soundJual: Sound? = null

    suspend fun load() {
        try {
            soundCangkul = resourcesVfs["assets/sfx/sfx_cangkul.wav"].readSound()
        } catch (_: Throwable) {}

        try {
            soundSiram = resourcesVfs["assets/sfx/sfx_siram.wav"].readSound()
        } catch (_: Throwable) {}

        try {
            soundGagal = resourcesVfs["assets/sfx/sfx_gagal.wav"].readSound()
        } catch (_: Throwable) {}

        try {
            soundBeli = resourcesVfs["assets/sfx/sfx_beli.wav"].readSound()
        } catch (_: Throwable) {}

        try {
            soundPanen = resourcesVfs["assets/sfx/sfx_panen.wav"].readSound()
        } catch (_: Throwable) {}

        try {
            soundJual = resourcesVfs["assets/sfx/sfx_jual.wav"].readSound()
        } catch (_: Throwable) {}
    }

    fun playCangkul() {
        try {
            soundCangkul?.playNoCancel()
        } catch (_: Throwable) {}
    }

    fun playSiram() {
        try {
            soundSiram?.playNoCancel()
        } catch (_: Throwable) {}
    }

    fun playGagal() {
        try {
            soundGagal?.playNoCancel()
        } catch (_: Throwable) {}
    }

    fun playBeli() {
        try {
            soundBeli?.playNoCancel()
        } catch (_: Throwable) {}
    }

    fun playPanen() {
        try {
            soundPanen?.playNoCancel()
        } catch (_: Throwable) {}
    }

    fun playJual() {
        try {
            soundJual?.playNoCancel()
        } catch (_: Throwable) {}
    }
}
