package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AmbientMusicPlayer
import com.example.core.AmbientMusicUiState
import com.example.core.PersistedFocusTimerState
import com.example.core.StudyActivityMonitor
import com.example.core.StudyQuestPreferences
import com.example.core.unwrapFirestoreMessage
import com.example.data.model.FocusMusicTrack
import com.example.data.model.StudySessionDoc
import com.example.data.repository.FocusAnalyticsSummary
import com.example.data.repository.FocusSessionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class LiveTimerDisplayState(
    val timer: PersistedFocusTimerState = PersistedFocusTimerState(),
    val elapsedFocusSeconds: Int = 0,
    val elapsedBreakSeconds: Int = 0,
    val remainingPhaseSeconds: Int = 0,
    val progressFraction: Float = 0f
)

class FocusTimerViewModel(
    private val currentUserId: String,
    private val focusRepo: FocusSessionRepository,
    private val preferences: StudyQuestPreferences,
    private val musicPlayer: AmbientMusicPlayer = AmbientMusicPlayer()
) : ViewModel() {

    val timerState: StateFlow<PersistedFocusTimerState> = focusRepo.observeTimerState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), PersistedFocusTimerState())

    val studySessions: StateFlow<List<StudySessionDoc>> = focusRepo.observeStudySessions(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val analytics: StateFlow<FocusAnalyticsSummary> = studySessions
        .map { FocusSessionRepository.computeFocusAnalytics(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), FocusAnalyticsSummary())

    val musicState: StateFlow<AmbientMusicUiState> = musicPlayer.state

    private val _nowMs = MutableStateFlow(System.currentTimeMillis())

    val liveDisplay: StateFlow<LiveTimerDisplayState> = combine(timerState, _nowMs) { state, now ->
        if (!state.active) {
            LiveTimerDisplayState(timer = state)
        } else {
            val focusSec = (FocusSessionRepository.computeLiveFocusElapsedMs(state, now) / 1000L).toInt()
            val breakSec = (FocusSessionRepository.computeLiveBreakElapsedMs(state, now) / 1000L).toInt()
            val targetSec = if (state.phase == "break") state.targetBreakSeconds.coerceAtLeast(1) else state.targetFocusSeconds.coerceAtLeast(1)
            val elapsedInPhase = if (state.phase == "break") breakSec else focusSec
            val remaining = (targetSec - elapsedInPhase).coerceAtLeast(0)
            val progress = (elapsedInPhase.toFloat() / targetSec.toFloat()).coerceIn(0f, 1f)
            LiveTimerDisplayState(
                timer = state,
                elapsedFocusSeconds = focusSec,
                elapsedBreakSeconds = breakSec,
                remainingPhaseSeconds = remaining,
                progressFraction = progress
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), LiveTimerDisplayState())

    private val _bannerMessage = MutableStateFlow<String?>(null)
    val bannerMessage: StateFlow<String?> = _bannerMessage.asStateFlow()

    private var tickerJob: Job? = null
    private var autoTransitionLock = false

    init {
        viewModelScope.launch {
            runCatching {
                val prefs = preferences.preferencesFlow.first()
                musicPlayer.setVolume(prefs.focusMusicVolume)
                musicPlayer.setMusicDuringBreak(prefs.musicDuringBreak)
            }
        }
        viewModelScope.launch {
            timerState.collect { state ->
                StudyActivityMonitor.isFocusActive.value = state.active && state.isRunning
                if (state.active && state.isRunning) {
                    startTicker()
                } else {
                    stopTicker()
                    _nowMs.value = System.currentTimeMillis()
                }
            }
        }
    }

    private fun startTicker() {
        if (tickerJob?.isActive == true) return
        tickerJob = viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                _nowMs.value = now
                val st = timerState.value
                if (st.active && st.isRunning && !autoTransitionLock) {
                    if (st.phase == "focus" && st.targetFocusSeconds > 0) {
                        val focusSec = (FocusSessionRepository.computeLiveFocusElapsedMs(st, now) / 1000L).toInt()
                        if (focusSec >= st.targetFocusSeconds) {
                            autoTransitionLock = true
                            try {
                                if (st.autoBreak && st.targetBreakSeconds > 0) {
                                    focusRepo.transitionToBreakPhase()
                                    musicPlayer.playBreakChime()
                                    if (!musicPlayer.state.value.musicDuringBreak && musicPlayer.state.value.isPlaying) {
                                        musicPlayer.pause()
                                    }
                                    _bannerMessage.value = "Focus interval complete! Break started."
                                } else {
                                    completeSessionInternal(completedTarget = true)
                                }
                            } finally {
                                autoTransitionLock = false
                            }
                        }
                    } else if (st.phase == "break" && st.targetBreakSeconds > 0) {
                        val breakSec = (FocusSessionRepository.computeLiveBreakElapsedMs(st, now) / 1000L).toInt()
                        if (breakSec >= st.targetBreakSeconds) {
                            autoTransitionLock = true
                            try {
                                musicPlayer.playBreakChime()
                                completeSessionInternal(completedTarget = true)
                            } finally {
                                autoTransitionLock = false
                            }
                        }
                    }
                }
                delay(500L)
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    fun clearBanner() {
        _bannerMessage.value = null
    }

    fun startFocusSession(
        subjectId: String,
        mode: String,
        focusMinutes: Int,
        breakMinutes: Int,
        autoBreak: Boolean
    ) {
        viewModelScope.launch {
            focusRepo.startSession(
                subjectId = subjectId,
                mode = mode,
                targetFocusMinutes = focusMinutes,
                targetBreakMinutes = breakMinutes,
                autoBreak = autoBreak
            )
            _nowMs.value = System.currentTimeMillis()
        }
    }

    fun pauseSession() {
        viewModelScope.launch {
            focusRepo.pauseSession()
            _nowMs.value = System.currentTimeMillis()
        }
    }

    fun resumeSession() {
        viewModelScope.launch {
            focusRepo.resumeSession()
            _nowMs.value = System.currentTimeMillis()
        }
    }

    fun skipBreakAndFinish() {
        viewModelScope.launch {
            completeSessionInternal(completedTarget = true)
        }
    }

    fun stopAndSaveSession() {
        viewModelScope.launch {
            completeSessionInternal(completedTarget = false)
        }
    }

    fun resetTimer() {
        viewModelScope.launch {
            focusRepo.resetTimer()
            if (musicPlayer.state.value.isPlaying) {
                musicPlayer.pause()
            }
            _nowMs.value = System.currentTimeMillis()
        }
    }

    private suspend fun completeSessionInternal(completedTarget: Boolean) {
        try {
            val res = focusRepo.stopAndVerifySession(currentUserId, completedTarget = completedTarget)
            res.onSuccess { xp ->
                _bannerMessage.value = if (xp > 0) {
                    "Verified study session saved! Streak & badge progress updated."
                } else {
                    "Verified study session saved."
                }
            }.onFailure { err ->
                _bannerMessage.value = unwrapFirestoreMessage(err, err.localizedMessage ?: "Focus session not recorded")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Stop focus warning", e)
            _bannerMessage.value = e.localizedMessage
        }
    }

    fun selectMusicCategory(category: String) = musicPlayer.selectCategory(category)

    fun selectMusicTrack(track: FocusMusicTrack) = musicPlayer.selectTrack(track)

    fun toggleMusicPlayPause() = musicPlayer.togglePlayPause()

    fun nextMusicTrack() = musicPlayer.nextTrack()

    fun previousMusicTrack() = musicPlayer.previousTrack()

    fun setMusicVolume(volume: Float) {
        musicPlayer.setVolume(volume)
        viewModelScope.launch {
            preferences.setMusicPreferences(volume, musicPlayer.state.value.musicDuringBreak)
        }
    }

    fun setMusicDuringBreak(enabled: Boolean) {
        musicPlayer.setMusicDuringBreak(enabled)
        viewModelScope.launch {
            preferences.setMusicPreferences(musicPlayer.state.value.volume, enabled)
        }
    }

    fun toggleAmbientRainLayer() = musicPlayer.toggleAmbientRainLayer()

    fun playSubjectBriefing(subjectId: String) = musicPlayer.playSubjectBriefing(subjectId)

    fun stopSubjectBriefing() = musicPlayer.stopSubjectBriefing()

    override fun onCleared() {
        super.onCleared()
        stopTicker()
        StudyActivityMonitor.isFocusActive.value = false
        musicPlayer.release()
    }

    companion object {
        private const val TAG = "FocusTimerVM"
    }
}
