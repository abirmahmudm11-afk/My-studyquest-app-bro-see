package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AppLanguage
import com.example.core.unwrapFirestoreMessage
import com.example.data.model.AiVideoJobDoc
import com.example.data.model.LearningVideoDoc
import com.example.data.repository.AiVideoRepository
import com.example.data.repository.StoryboardQuizQuestion
import com.example.data.repository.StoryboardScene
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class AiVideoViewModel(
    private val appContext: Context,
    private val currentUserId: String,
    private val aiVideoRepo: AiVideoRepository
) : ViewModel() {

    val jobs: StateFlow<List<AiVideoJobDoc>> = aiVideoRepo.observeAiVideoJobs(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val videos: StateFlow<List<LearningVideoDoc>> = aiVideoRepo.observeLearningVideos(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _activeVideo = MutableStateFlow<LearningVideoDoc?>(null)
    val activeVideo: StateFlow<LearningVideoDoc?> = _activeVideo.asStateFlow()

    private val _scenes = MutableStateFlow<List<StoryboardScene>>(emptyList())
    val scenes: StateFlow<List<StoryboardScene>> = _scenes.asStateFlow()

    private val _quizQuestions = MutableStateFlow<List<StoryboardQuizQuestion>>(emptyList())
    val quizQuestions: StateFlow<List<StoryboardQuizQuestion>> = _quizQuestions.asStateFlow()

    private val _currentSceneIndex = MutableStateFlow(0)
    val currentSceneIndex: StateFlow<Int> = _currentSceneIndex.asStateFlow()

    private val _sceneElapsedSec = MutableStateFlow(0)
    val sceneElapsedSec: StateFlow<Int> = _sceneElapsedSec.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _ttsEnabled = MutableStateFlow(true)
    val ttsEnabled: StateFlow<Boolean> = _ttsEnabled.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var playbackJob: Job? = null

    init {
        runCatching {
            tts = TextToSpeech(appContext.applicationContext) { status ->
                ttsReady = (status == TextToSpeech.SUCCESS)
                if (ttsReady) {
                    val res = tts?.setLanguage(Locale("bn", "BD"))
                    if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.setLanguage(Locale.US)
                    }
                }
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun openVideo(video: LearningVideoDoc, language: AppLanguage = AppLanguage.ENGLISH) {
        val parsedScenes = AiVideoRepository.parseScenes(video.scenesJson)
        val parsedQuiz = AiVideoRepository.parseQuiz(video.quizJson)
        _activeVideo.value = video
        _scenes.value = parsedScenes
        _quizQuestions.value = parsedQuiz
        val startIdx = video.lastWatchedScene.toInt().coerceIn(0, (parsedScenes.size - 1).coerceAtLeast(0))
        _currentSceneIndex.value = startIdx
        _sceneElapsedSec.value = 0
        _isPlaying.value = false
        updateTtsLanguage(language)
    }

    fun closeVideo() {
        pauseStoryboard()
        _activeVideo.value = null
        _scenes.value = emptyList()
        _quizQuestions.value = emptyList()
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pauseStoryboard()
        } else {
            playStoryboard()
        }
    }

    fun playStoryboard() {
        val list = _scenes.value
        if (list.isEmpty()) return
        _isPlaying.value = true
        speakCurrentScene()
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (isActive && _isPlaying.value) {
                delay(1000L)
                val idx = _currentSceneIndex.value
                val currentScene = _scenes.value.getOrNull(idx) ?: break
                val nextSec = _sceneElapsedSec.value + 1
                if (nextSec >= currentScene.durationSec) {
                    if (idx + 1 < _scenes.value.size) {
                        seekToScene(idx + 1, autoSpeak = true)
                    } else {
                        pauseStoryboard()
                        _sceneElapsedSec.value = currentScene.durationSec
                    }
                } else {
                    _sceneElapsedSec.value = nextSec
                }
            }
        }
    }

    fun pauseStoryboard() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
        runCatching { tts?.stop() }
    }

    fun seekToScene(index: Int, autoSpeak: Boolean = _isPlaying.value) {
        val list = _scenes.value
        if (list.isEmpty()) return
        val target = index.coerceIn(0, list.lastIndex)
        _currentSceneIndex.value = target
        _sceneElapsedSec.value = 0
        val vid = _activeVideo.value
        if (vid != null) {
            viewModelScope.launch {
                aiVideoRepo.saveVideoWatchProgress(vid.id, target)
            }
        }
        if (autoSpeak) {
            speakCurrentScene()
        }
    }

    fun toggleTts(enabled: Boolean) {
        _ttsEnabled.value = enabled
        if (!enabled) {
            runCatching { tts?.stop() }
        } else if (_isPlaying.value) {
            speakCurrentScene()
        }
    }

    private fun updateTtsLanguage(language: AppLanguage) {
        if (!ttsReady) return
        val locale = if (language == AppLanguage.BANGLA) Locale("bn", "BD") else Locale.US
        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts?.setLanguage(Locale.US)
        }
    }

    private fun speakCurrentScene() {
        if (!ttsReady || !_ttsEnabled.value) return
        val scene = _scenes.value.getOrNull(_currentSceneIndex.value) ?: return
        val text = scene.narrationText.ifBlank { scene.caption }.take(500)
        if (text.isNotBlank()) {
            runCatching {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "scene_${_currentSceneIndex.value}")
            }
        }
    }

    fun generateFromChapter(
        classId: String,
        subjectId: String,
        chapterId: String,
        language: AppLanguage
    ) {
        if (_isGenerating.value) return
        viewModelScope.launch {
            _isGenerating.value = true
            updateTtsLanguage(language)
            try {
                val res = aiVideoRepo.generateChapterStoryboard(
                    userId = currentUserId,
                    classId = classId,
                    subjectId = subjectId,
                    chapterId = chapterId,
                    language = language
                )
                res.onSuccess { doc ->
                    openVideo(doc, language)
                    _message.value = if (language == AppLanguage.BANGLA) {
                        "এআই স্টোরিবোর্ড লেসন তৈরি হয়েছে!"
                    } else {
                        "AI Learning Storyboard ready!"
                    }
                }.onFailure { err ->
                    _message.value = unwrapFirestoreMessage(err, err.localizedMessage ?: "Storyboard generation failed")
                }
            } catch (e: CancellationException) {
                throw e
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun generateFromDocumentOrNotes(
        classId: String,
        subjectId: String,
        documentUri: Uri?,
        pastedNotes: String,
        language: AppLanguage
    ) {
        if (_isGenerating.value) return
        viewModelScope.launch {
            _isGenerating.value = true
            updateTtsLanguage(language)
            try {
                val res = aiVideoRepo.generateFromUriOrText(
                    context = appContext,
                    userId = currentUserId,
                    classId = classId,
                    subjectId = subjectId,
                    documentUri = documentUri,
                    pastedNotes = pastedNotes,
                    language = language
                )
                res.onSuccess { doc ->
                    openVideo(doc, language)
                    _message.value = if (language == AppLanguage.BANGLA) {
                        "ডকুমেন্ট থেকে এআই স্টোরিবোর্ড তৈরি হয়েছে!"
                    } else {
                        "AI Storyboard generated from your document!"
                    }
                }.onFailure { err ->
                    _message.value = unwrapFirestoreMessage(err, err.localizedMessage ?: "Could not generate storyboard")
                }
            } catch (e: CancellationException) {
                throw e
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun submitVideoCheckQuiz(
        video: LearningVideoDoc,
        correctCount: Int,
        totalQuestions: Int,
        onXpAwarded: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val res = aiVideoRepo.recordAiVideoQuizCompleted(currentUserId, video, correctCount, totalQuestions)
            res.onSuccess { xp ->
                if (xp > 0) {
                    _message.value = "Storyboard quiz checked ($correctCount/$totalQuestions)! +$xp XP"
                    onXpAwarded(xp)
                }
            }.onFailure { err ->
                _message.value = unwrapFirestoreMessage(err, "Could not save quiz progress")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
        runCatching {
            tts?.stop()
            tts?.shutdown()
        }
    }

    companion object {
        private const val TAG = "AiVideoVM"
    }
}
