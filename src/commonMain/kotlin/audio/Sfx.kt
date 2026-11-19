package audio

import korlibs.audio.sound.*
import korlibs.io.file.std.resourcesVfs
import kotlin.coroutines.CoroutineContext

/**
 * Manajer efek suara (SFX) dan musik latar (BGM) permainan.
 * Menyediakan dukungan mute (tombol M).
 */
object Sfx {
    private var soundCangkul: Sound? = null
    private var soundSiram: Sound? = null
    private var soundGagal: Sound? = null
    private var soundBeli: Sound? = null
    private var soundPanen: Sound? = null
    private var soundJual: Sound? = null
    private var soundBgm: Sound? = null
    private var bgmChannel: SoundChannel? = null

    var isMuted: Boolean = false
        private set

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

        try {
            soundBgm = resourcesVfs["assets/sfx/bgm_ladang.wav"].readSound()
        } catch (_: Throwable) {}
    }

    fun playBgm(coroutineContext: CoroutineContext) {
        if (bgmChannel != null) return
        try {
            val bgm = soundBgm ?: return
            val vol = if (isMuted) 0.0 else 0.25
            bgmChannel = bgm.play(coroutineContext, PlaybackParameters(times = infinitePlaybackTimes, volume = vol))
        } catch (_: Throwable) {}
    }

    fun stopBgm() {
        try {
            bgmChannel?.stop()
            bgmChannel = null
        } catch (_: Throwable) {}
    }

    fun toggleMute(): Boolean {
        isMuted = !isMuted
        bgmChannel?.volume = if (isMuted) 0.0 else 0.25
        return isMuted
    }

    fun playCangkul() {
        if (isMuted) return
        try {
            soundCangkul?.playNoCancel()
        } catch (_: Throwable) {}
    }

    fun playSiram() {
        if (isMuted) return
        try {
            soundSiram?.playNoCancel()
        } catch (_: Throwable) {}
    }

    fun playGagal() {
        if (isMuted) return
        try {
            soundGagal?.playNoCancel()
        } catch (_: Throwable) {}
    }

    fun playBeli() {
        if (isMuted) return
        try {
            soundBeli?.playNoCancel()
        } catch (_: Throwable) {}
    }

    fun playPanen() {
        if (isMuted) return
        try {
            soundPanen?.playNoCancel()
        } catch (_: Throwable) {}
    }

    fun playJual() {
        if (isMuted) return
        try {
            soundJual?.playNoCancel()
        } catch (_: Throwable) {}
    }
}
