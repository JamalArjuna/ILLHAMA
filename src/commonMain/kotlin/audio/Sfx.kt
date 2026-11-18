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
}
