package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.sin

class ArcadeAudioEngine(private val context: Context) {

    private val audioScope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 22050
    var soundEnabled: Boolean = true

    // Pre-rendered PCM short buffers for instant zero-latency playback
    private val pcmCache = ConcurrentHashMap<String, ShortArray>()

    init {
        audioScope.launch {
            pcmCache["laser"] = generateLaserPcm()
            pcmCache["heavy_laser"] = generateHeavyLaserPcm()
            pcmCache["explosion"] = generateExplosionPcm()
            pcmCache["boss_explosion"] = generateBossExplosionPcm()
            pcmCache["powerup"] = generatePowerupPcm()
            pcmCache["shield_hit"] = generateShieldHitPcm()
            pcmCache["siren"] = generateSirenPcm()
            pcmCache["bomb"] = generateMegaBombPcm()
        }
    }

    fun playLaser() = playFromCache("laser", 0.6f)
    fun playHeavyLaser() = playFromCache("heavy_laser", 0.7f)
    fun playExplosion() = playFromCache("explosion", 0.8f)
    fun playBossExplosion() = playFromCache("boss_explosion", 1.0f)
    fun playPowerup() = playFromCache("powerup", 0.75f)
    fun playShieldHit() = playFromCache("shield_hit", 0.6f)
    fun playBossSiren() = playFromCache("siren", 0.85f)
    fun playMegaBomb() = playFromCache("bomb", 1.0f)

    private fun playFromCache(key: String, volume: Float) {
        if (!soundEnabled) return
        val samples = pcmCache[key] ?: return
        audioScope.launch {
            try {
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.setVolume(volume)
                audioTrack.write(samples, 0, samples.size)
                audioTrack.play()
                // Sleep until track finishes then release
                val durationMs = (samples.size * 1000L) / sampleRate
                kotlinx.coroutines.delay(durationMs + 50)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
                // Ignore audio hardware transients
            }
        }
    }

    private fun generateLaserPcm(): ShortArray {
        val durationMs = 120
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val freq = 1200f - (800f * progress) // downward chirp
            val t = i.toDouble() / sampleRate
            val sample = sin(2.0 * PI * freq * t)
            val envelope = (1f - progress)
            buffer[i] = (sample * envelope * Short.MAX_VALUE * 0.7f).toInt().toShort()
        }
        return buffer
    }

    private fun generateHeavyLaserPcm(): ShortArray {
        val durationMs = 180
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val freq = 650f - (450f * progress)
            val t = i.toDouble() / sampleRate
            val sample = sin(2.0 * PI * freq * t) + 0.3 * sin(4.0 * PI * freq * t)
            val envelope = (1f - progress)
            buffer[i] = (sample * envelope * Short.MAX_VALUE * 0.5f).toInt().toShort()
        }
        return buffer
    }

    private fun generateExplosionPcm(): ShortArray {
        val durationMs = 380
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        var lastRandom = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val whiteNoise = (Math.random() * 2.0 - 1.0)
            // Low-pass filter for deeper blast sound
            lastRandom = (lastRandom * 0.7) + (whiteNoise * 0.3)
            val rumbleFreq = 80f * (1f - 0.5f * progress)
            val rumble = sin(2.0 * PI * rumbleFreq * (i.toDouble() / sampleRate))
            val combined = (lastRandom * 0.7) + (rumble * 0.5)
            val envelope = Math.exp(-4.5 * progress)
            buffer[i] = (combined * envelope * Short.MAX_VALUE * 0.8f).toInt().toShort()
        }
        return buffer
    }

    private fun generateBossExplosionPcm(): ShortArray {
        val durationMs = 900
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        var lastRandom = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val whiteNoise = (Math.random() * 2.0 - 1.0)
            lastRandom = (lastRandom * 0.8) + (whiteNoise * 0.2)
            val subBass = sin(2.0 * PI * 45.0 * (i.toDouble() / sampleRate))
            val combined = (lastRandom * 0.6) + (subBass * 0.7)
            val envelope = (1f - progress) * (1f - progress)
            buffer[i] = (combined * envelope * Short.MAX_VALUE * 0.9f).toInt().toShort()
        }
        return buffer
    }

    private fun generatePowerupPcm(): ShortArray {
        val durationMs = 280
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        val freqs = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.50f) // C5, E5, G5, C6
        val step = numSamples / 4
        for (i in 0 until numSamples) {
            val noteIdx = minOf(3, i / step)
            val freq = freqs[noteIdx]
            val t = i.toDouble() / sampleRate
            val sample = sin(2.0 * PI * freq * t)
            val noteProgress = (i % step).toFloat() / step
            val envelope = (1f - 0.4f * noteProgress)
            buffer[i] = (sample * envelope * Short.MAX_VALUE * 0.6f).toInt().toShort()
        }
        return buffer
    }

    private fun generateShieldHitPcm(): ShortArray {
        val durationMs = 150
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val freq = 900f + 400f * sin(progress * 15f)
            val t = i.toDouble() / sampleRate
            val sample = sin(2.0 * PI * freq * t)
            val envelope = (1f - progress)
            buffer[i] = (sample * envelope * Short.MAX_VALUE * 0.5f).toInt().toShort()
        }
        return buffer
    }

    private fun generateSirenPcm(): ShortArray {
        val durationMs = 600
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            // Warbling siren tone
            val freq = 480f + 300f * sin(progress * 12.0 * PI).toFloat()
            val t = i.toDouble() / sampleRate
            val sample = sin(2.0 * PI * freq * t)
            buffer[i] = (sample * Short.MAX_VALUE * 0.65f).toInt().toShort()
        }
        return buffer
    }

    private fun generateMegaBombPcm(): ShortArray {
        val durationMs = 800
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        var noise = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val t = i.toDouble() / sampleRate
            val sweep = sin(2.0 * PI * (350f - 250f * progress) * t)
            noise = (noise * 0.75) + ((Math.random() * 2.0 - 1.0) * 0.25)
            val sample = (sweep * 0.6) + (noise * 0.8)
            val envelope = Math.exp(-3.0 * progress)
            buffer[i] = (sample * envelope * Short.MAX_VALUE * 0.9f).toInt().toShort()
        }
        return buffer
    }
}

class GameHaptics(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var hapticsEnabled: Boolean = true

    fun fireHaptic() {
        if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(12)
        }
    }

    fun hitHaptic() {
        if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(45)
        }
    }

    fun explosionHaptic() {
        if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 40, 20, 70),
                    intArrayOf(0, 180, 0, 255),
                    -1
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }
}
