package com.example.core

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
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
    val musicDuringBreak: Boolean = true,
    val rainLayerEnabled: Boolean = false,
    val activeBriefingSubjectId: String? = null
)

/**
 * Dual-engine offline studio audio player:
 * 1) Primary: Streams bundled 44.1 kHz studio-mastered WAV tracks, rain ambient layer,
 *    chimes, and subject audio briefings from assets/study_vault/audio via MediaPlayer.
 * 2) Fallback: Procedural PCM synthesizer via AudioTrack if AssetFileDescriptor is unavailable.
 */
class AmbientMusicPlayer(
    private val context: Context? = null
) {
    private val appContext: Context? = context?.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var playbackJob: Job? = null
    private var mainMediaPlayer: MediaPlayer? = null
    private var rainMediaPlayer: MediaPlayer? = null
    private var briefingMediaPlayer: MediaPlayer? = null
    private val playerLock = Any()

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
        releaseMainPlayer()
        releaseRainPlayer()
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
        synchronized(playerLock) {
            runCatching { mainMediaPlayer?.setVolume(clamped, clamped) }
            runCatching {
                val rainVol = (clamped * 0.42f).coerceIn(0f, 1f)
                rainMediaPlayer?.setVolume(rainVol, rainVol)
            }
            runCatching { briefingMediaPlayer?.setVolume(clamped, clamped) }
        }
    }

    fun setMusicDuringBreak(enabled: Boolean) {
        _state.value = _state.value.copy(musicDuringBreak = enabled)
    }

    fun toggleAmbientRainLayer() {
        val nextEnabled = !_state.value.rainLayerEnabled
        _state.value = _state.value.copy(rainLayerEnabled = nextEnabled)
        if (nextEnabled) {
            if (_state.value.isPlaying) {
                startRainOverlay()
            } else {
                startRainOverlay()
            }
        } else {
            releaseRainPlayer()
        }
    }

    fun playSubjectBriefing(subjectId: String) {
        if (_state.value.activeBriefingSubjectId == subjectId) {
            stopSubjectBriefing()
            return
        }
        stopSubjectBriefing()
        val ctx = appContext ?: return
        val started = startBundledMediaPlayer(
            ctx = ctx,
            fileName = "briefing_${subjectId}.wav",
            loop = false,
            volScale = 1f,
            onCompletion = {
                _state.value = _state.value.copy(activeBriefingSubjectId = null)
                releaseBriefingPlayer()
            }
        )
        if (started != null) {
            synchronized(playerLock) { briefingMediaPlayer = started }
            _state.value = _state.value.copy(activeBriefingSubjectId = subjectId)
        }
    }

    fun stopSubjectBriefing() {
        releaseBriefingPlayer()
        if (_state.value.activeBriefingSubjectId != null) {
            _state.value = _state.value.copy(activeBriefingSubjectId = null)
        }
    }

    fun playBreakChime() {
        val ctx = appContext
        if (ctx != null) {
            val mp = startBundledMediaPlayer(
                ctx = ctx,
                fileName = "chime_focus_end.wav",
                loop = false,
                volScale = 1f,
                onCompletion = { player ->
                    runCatching {
                        player.stop()
                        player.release()
                    }
                }
            )
            if (mp != null) return
        }

        scope.launch {
            runCatching {
                val sampleRate = 22050
                val durationSamples = sampleRate / 2 // 0.5 sec chime
                val buffer = ShortArray(durationSamples)
                for (i in 0 until durationSamples) {
                    val t = i.toDouble() / sampleRate
                    val env = kotlin.math.exp(-t * 6.0)
                    val tone = sin(2.0 * PI * 523.25 * t) * 0.5 + sin(2.0 * PI * 783.99 * t) * 0.3
                    buffer[i] = (tone * env * currentVolume * 14000.0).toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
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
        releaseMainPlayer()
        _state.value = _state.value.copy(isPlaying = true, currentTrack = musicTrack)

        val ctx = appContext
        if (ctx != null) {
            val mp = startBundledMediaPlayer(
                ctx = ctx,
                fileName = "${musicTrack.id}.wav",
                loop = true,
                volScale = 1f,
                onCompletion = null
            )
            if (mp != null) {
                synchronized(playerLock) { mainMediaPlayer = mp }
                if (_state.value.rainLayerEnabled) {
                    startRainOverlay()
                }
                return
            }
        }

        // Fallback procedural PCM synthesis when asset FD isn't available (e.g., JVM tests)
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

    private fun startRainOverlay() {
        releaseRainPlayer()
        val ctx = appContext ?: return
        val mp = startBundledMediaPlayer(
            ctx = ctx,
            fileName = "track_monsoon_rain.wav",
            loop = true,
            volScale = 0.42f,
            onCompletion = null
        )
        if (mp != null) {
            synchronized(playerLock) { rainMediaPlayer = mp }
        }
    }

    private fun startBundledMediaPlayer(
        ctx: Context,
        fileName: String,
        loop: Boolean,
        volScale: Float,
        onCompletion: ((MediaPlayer) -> Unit)?
    ): MediaPlayer? {
        return runCatching {
            val afd = OfflineVaultManager.openAudioAssetFd(ctx, fileName) ?: return null
            val mp = MediaPlayer()
            afd.use {
                mp.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                mp.setDataSource(it.fileDescriptor, it.startOffset, it.length)
            }
            mp.isLooping = loop
            val v = (currentVolume * volScale).coerceIn(0f, 1f)
            mp.setVolume(v, v)
            if (onCompletion != null) {
                mp.setOnCompletionListener { player -> onCompletion(player) }
            }
            mp.prepare()
            mp.start()
            mp
        }.onFailure { err ->
            Log.d(TAG, "Bundled WAV $fileName fallback: ${err.message}")
        }.getOrNull()
    }

    private fun releaseMainPlayer() {
        synchronized(playerLock) {
            runCatching { mainMediaPlayer?.stop() }
            runCatching { mainMediaPlayer?.release() }
            mainMediaPlayer = null
        }
    }

    private fun releaseRainPlayer() {
        synchronized(playerLock) {
            runCatching { rainMediaPlayer?.stop() }
            runCatching { rainMediaPlayer?.release() }
            rainMediaPlayer = null
        }
    }

    private fun releaseBriefingPlayer() {
        synchronized(playerLock) {
            runCatching { briefingMediaPlayer?.stop() }
            runCatching { briefingMediaPlayer?.release() }
            briefingMediaPlayer = null
        }
    }

    fun release() {
        pause()
        releaseBriefingPlayer()
        scope.cancel()
    }

    companion object {
        private const val TAG = "AmbientMusicPlayer"
    }
}
