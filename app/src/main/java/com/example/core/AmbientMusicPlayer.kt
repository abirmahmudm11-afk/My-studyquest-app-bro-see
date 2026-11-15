package com.example.core

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.example.data.curriculum.GamificationCatalog
import com.example.data.model.FocusMusicTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

data class AmbientMusicUiState(
    val isPlaying: Boolean = false,
    val selectedCategory: String = "focus",
    val currentTrack: FocusMusicTrack = GamificationCatalog.focusMusicTracks.first(),
    val volume: Float = 0.65f,
    val musicDuringBreak: Boolean = true
)

/**
 * Procedural PCM ambient sound synthesizer using Android AudioTrack.
 * Generates genuine offline Focus, Relaxing, and Background audio without broken remote streams.
 */
class AmbientMusicPlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var playbackJob: Job? = null

    private val _state = MutableStateFlow(AmbientMusicUiState())
    val state: StateFlow<AmbientMusicUiState> = _state.asStateFlow()

    @Volatile
    private var currentVolume: Float = 0.65f

    fun tracksForCategory(category: String): List<FocusMusicTrack> =
        GamificationCatalog.focusMusicTracks.filter { it.category == category }
            .ifEmpty { GamificationCatalog.focusMusicTracks }

    fun selectCategory(category: String) {
        val nextList = tracksForCategory(category)
        val firstTrack = nextList.firstOrNull() ?: return
        val wasPlaying = _state.value.isPlaying
        _state.value = _state.value.copy(
            selectedCategory = category,
            currentTrack = firstTrack
        )
        if (wasPlaying) startTrack(firstTrack)
    }

    fun selectTrack(track: FocusMusicTrack) {
        val wasPlaying = _state.value.isPlaying
        _state.value = _state.value.copy(
            selectedCategory = track.category,
            currentTrack = track
        )
        if (wasPlaying) startTrack(track)
    }

    fun togglePlayPause() {
        if (_state.value.isPlaying) {
            pause()
        } else {
            startTrack(_state.value.currentTrack)
        }
    }

    fun play() {
        if (!_state.value.isPlaying) startTrack(_state.value.currentTrack)
    }

    fun pause() {
        playbackJob?.cancel()
        playbackJob = null
        _state.value = _state.value.copy(isPlaying = false)
    }

    fun nextTrack() {
        val all = GamificationCatalog.focusMusicTracks
        val idx = all.indexOfFirst { it.id == _state.value.currentTrack.id }
        val next = all[(idx + 1).coerceAtLeast(0) % all.size]
        _state.value = _state.value.copy(selectedCategory = next.category, currentTrack = next)
        if (_state.value.isPlaying) startTrack(next)
    }

    fun previousTrack() {
        val all = GamificationCatalog.focusMusicTracks
        val idx = all.indexOfFirst { it.id == _state.value.currentTrack.id }
        val prevIdx = if (idx <= 0) all.lastIndex else idx - 1
        val prev = all[prevIdx]
        _state.value = _state.value.copy(selectedCategory = prev.category, currentTrack = prev)
        if (_state.value.isPlaying) startTrack(prev)
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        currentVolume = clamped
        _state.value = _state.value.copy(volume = clamped)
    }

    fun setMusicDuringBreak(enabled: Boolean) {
        _state.value = _state.value.copy(musicDuringBreak = enabled)
    }

    fun playBreakChime() {
        scope.launch {
            runCatching {
                val sampleRate = 22050
                val durationSamples = sampleRate / 2 // 0.5 sec chime
                val buffer = ShortArray(durationSamples)
                for (i in 0 until durationSamples) {
                    val t = i.toDouble() / sampleRate
                    val env = kotlin.math.exp(-t * 6.0)
                    val tone = sin(2.0 * PI * 523.25 * t) * 0.5 + sin(2.0 * PI * 783.99 * t) * 0.3
                    buffer[i] = (tone * env * currentVolume * 14000.0).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(buffer.size * 2)
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(sampleRate)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBuf)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
                track.play()
                track.write(buffer, 0, buffer.size)
                delay(550L)
                track.stop()
                track.release()
            }
        }
    }

    private fun startTrack(musicTrack: FocusMusicTrack) {
        playbackJob?.cancel()
        _state.value = _state.value.copy(isPlaying = true, currentTrack = musicTrack)
        playbackJob = scope.launch {
            val sampleRate = 22050
            val chunkSamples = 2048
            val buffer = ShortArray(chunkSamples)
            var audioTrack: AudioTrack? = null
            try {
                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(chunkSamples * 4)

                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(sampleRate)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBuf)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
                audioTrack.play()

                var sampleIndex = 0L
                var filteredNoise = 0.0
                val rng = Random(musicTrack.id.hashCode())
                while (isActive) {
                    val vol = currentVolume * 0.28f
                    val beatHz = musicTrack.tempoBpm / 60.0
                    for (i in 0 until chunkSamples) {
                        val t = sampleIndex.toDouble() / sampleRate
                        val pulse = 0.72 + 0.28 * sin(2.0 * PI * (beatHz * 0.25) * t)
                        val wave = when (musicTrack.mode) {
                            "rain_chime" -> {
                                val white = rng.nextDouble() * 2.0 - 1.0
                                filteredNoise = filteredNoise * 0.92 + white * 0.08
                                val chimeEnv = (0.5 + 0.5 * sin(2.0 * PI * 0.18 * t))
                                filteredNoise * 0.75 + sin(2.0 * PI * musicTrack.harmonicFreqHz * t) * 0.18 * chimeEnv
                            }
                            "soft_bell" -> {
                                val env = 0.55 + 0.45 * sin(2.0 * PI * 0.22 * t)
                                (sin(2.0 * PI * musicTrack.baseFreqHz * t) * 0.55 +
                                    sin(2.0 * PI * musicTrack.harmonicFreqHz * t) * 0.35) * env
                            }
                            "lofi_drone" -> {
                                (sin(2.0 * PI * musicTrack.baseFreqHz * t) * 0.5 +
                                    sin(2.0 * PI * (musicTrack.baseFreqHz * 1.25) * t) * 0.25 +
                                    sin(2.0 * PI * musicTrack.harmonicFreqHz * t) * 0.2) * pulse
                            }
                            else -> {
                                (sin(2.0 * PI * musicTrack.baseFreqHz * t) * 0.55 +
                                    sin(2.0 * PI * (musicTrack.baseFreqHz + 4.0) * t) * 0.25 +
                                    sin(2.0 * PI * musicTrack.harmonicFreqHz * t) * 0.2) * pulse
                            }
                        }
                        val pcm = (wave * vol * 24000.0).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        buffer[i] = pcm.toShort()
                        sampleIndex++
                    }
                    audioTrack.write(buffer, 0, buffer.size)
                }
            } catch (e: Throwable) {
                Log.d(TAG, "AudioTrack unavailable or stopped: ${e.message}")
            } finally {
                runCatching {
                    audioTrack?.stop()
                    audioTrack?.release()
                }
            }
        }
    }

    fun release() {
        pause()
        scope.cancel()
    }

    companion object {
        private const val TAG = "AmbientMusicPlayer"
    }
}
