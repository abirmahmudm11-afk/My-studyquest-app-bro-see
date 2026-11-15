package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AppLanguage
import com.example.core.ConnectivityObserver
import com.example.core.LocalUserPreferences
import com.example.core.StudyQuestPreferences
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.local.LocalSoloMatchEntity
import com.example.data.local.LocalStudyCircleNoteEntity
import com.example.data.model.LeaderboardEntryDoc
import com.example.data.model.PublicProfile
import com.example.data.model.QuestionItem
import com.example.data.model.QuestionType
import com.example.data.model.TournamentDoc
import com.example.data.model.UserNotificationDoc
import com.example.data.model.UserQuestDoc
import com.example.data.repository.AiDoubtSolverRepository
import com.example.data.repository.GuestDataRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class ActiveSoloSprintState(
    val id: String,
    val mode: String, // "solo_sprint" or "solo_tournament"
    val title: String,
    val subjectId: String,
    val classId: String,
    val questions: List<QuestionItem>,
    val currentIndex: Int = 0,
    val correctCount: Int = 0,
    val scorePoints: Long = 0L,
    val finished: Boolean = false,
    val earnedXp: Long = 0L,
    val earnedCoins: Long = 0L
)

@OptIn(ExperimentalCoroutinesApi::class)
class GuestSessionViewModel(
    private val preferences: StudyQuestPreferences,
    private val guestRepo: GuestDataRepository,
    private val aiRepo: AiDoubtSolverRepository,
    connectivityObserver: ConnectivityObserver
) : ViewModel() {

    val localPreferences: StateFlow<LocalUserPreferences> = preferences.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), LocalUserPreferences(isGuestMode = true))

    val isOnline: StateFlow<Boolean> = connectivityObserver.observeOnline()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), true)

    val profile: StateFlow<PublicProfile> = localPreferences
        .map { guestRepo.buildGuestProfile(it) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            guestRepo.buildGuestProfile(LocalUserPreferences(isGuestMode = true))
        )

    val unlockedCosmetics: StateFlow<Set<String>> = localPreferences
        .map { prefs ->
            prefs.guestUnlockedCosmeticsCsv.split(",").filter { it.isNotBlank() }.toSet() + "owl_classic"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), setOf("owl_classic"))

    val quests: StateFlow<List<UserQuestDoc>> = guestRepo.observeGuestQuests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val soloMatches: StateFlow<List<LocalSoloMatchEntity>> = guestRepo.observeSoloMatches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val leaderboard: StateFlow<List<LeaderboardEntryDoc>> = combine(profile, soloMatches) { prof, matches ->
        guestRepo.buildGuestLeaderboard(prof, matches)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val tournaments: StateFlow<List<TournamentDoc>> = profile
        .map { guestRepo.buildLocalTournaments(it.classId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val notifications: StateFlow<List<UserNotificationDoc>> = localPreferences
        .map { guestRepo.guestWelcomeNotifications(it.uiLanguage, it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _selectedCircleChannel = MutableStateFlow("ssc_math")
    val selectedCircleChannel: StateFlow<String> = _selectedCircleChannel.asStateFlow()

    val studyCircleNotes: StateFlow<List<LocalStudyCircleNoteEntity>> = _selectedCircleChannel
        .flatMapLatest { channel -> guestRepo.observeStudyCircleNotes(channel) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _isAiReplyingInCircle = MutableStateFlow(false)
    val isAiReplyingInCircle: StateFlow<Boolean> = _isAiReplyingInCircle.asStateFlow()

    private val _activeSoloSprint = MutableStateFlow<ActiveSoloSprintState?>(null)
    val activeSoloSprint: StateFlow<ActiveSoloSprintState?> = _activeSoloSprint.asStateFlow()

    private val _bannerMessage = MutableStateFlow<String?>(null)
    val bannerMessage: StateFlow<String?> = _bannerMessage.asStateFlow()

    private val _exportedJson = MutableStateFlow<String?>(null)
    val exportedJson: StateFlow<String?> = _exportedJson.asStateFlow()

    private val _celebrationPulse = MutableStateFlow(0)
    val celebrationPulse: StateFlow<Int> = _celebrationPulse.asStateFlow()

    init {
        viewModelScope.launch {
            guestRepo.ensureGuestQuestsSeeded()
            guestRepo.ensureStudyCircleSeeded(localPreferences.value.uiLanguage)
        }
    }

    fun clearBanner() {
        _bannerMessage.value = null
    }

    fun clearExportedJson() {
        _exportedJson.value = null
    }

    fun selectCircleChannel(channelId: String) {
        _selectedCircleChannel.value = channelId
    }

    fun postStudyCircleNote(text: String, askAiBuddy: Boolean = false) {
        val channel = _selectedCircleChannel.value
        val lang = localPreferences.value.uiLanguage
        val author = profile.value.displayName
        val classId = profile.value.classId
        viewModelScope.launch {
            guestRepo.postStudyCircleNote(channel, author, text, isAiBuddy = false)
            if (askAiBuddy) {
                _isAiReplyingInCircle.value = true
                try {
                    val subjectLabel = when (channel) {
                        "ssc_math" -> "Mathematics"
                        "science_lab" -> "Physics & Science"
                        else -> "English & ICT"
                    }
                    var latest = ""
                    aiRepo.streamDoubtExplanation(
                        questionPrompt = "Study Circle ($subjectLabel)",
                        studentQuestion = text,
                        classId = classId,
                        subjectName = subjectLabel,
                        language = lang
                    ).collect { chunk -> latest = chunk }
                    if (latest.isNotBlank()) {
                        guestRepo.postStudyCircleNote(channel, "Titu AI Buddy", latest, isAiBuddy = true)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w(TAG, "AI circle reply failed", e)
                    guestRepo.postStudyCircleNote(
                        channel,
                        "Titu AI Buddy",
                        StudyQuestStrings.t(
                            lang,
                            "Saved your note locally! Connect to the internet for live AI Buddy explanations.",
                            "তোমার নোট সংরক্ষিত হয়েছে! লাইভ AI টিটুর উত্তর পেতে ইন্টারনেটে যুক্ত হও।"
                        ),
                        isAiBuddy = true
                    )
                } finally {
                    _isAiReplyingInCircle.value = false
                }
            }
        }
    }

    fun toggleCircleNoteReaction(note: LocalStudyCircleNoteEntity, emoji: String) {
        viewModelScope.launch {
            guestRepo.toggleNoteReaction(note, emoji)
        }
    }

    fun deleteCircleNote(noteId: String) {
        viewModelScope.launch {
            guestRepo.deleteNote(noteId)
        }
    }

    fun startSoloSprint(
        classId: String,
        subjectId: String,
        mode: String = "solo_sprint",
        customTitle: String? = null
    ) {
        val playable = BangladeshCurriculumCatalog.questions.filter {
            it.type in listOf(QuestionType.SINGLE_MCQ, QuestionType.TRUE_FALSE)
        }
        val pool = playable.filter { it.subjectId == subjectId && it.classId == classId }
            .ifEmpty { playable.filter { it.subjectId == subjectId } }
            .ifEmpty { playable }
        val picked = (pool.shuffled() + playable).distinctBy { it.id }.take(5)
        val subj = BangladeshCurriculumCatalog.subjects.find { it.id == subjectId }
        val lang = localPreferences.value.uiLanguage
        val title = customTitle ?: StudyQuestStrings.t(
            lang,
            "${subj?.titleEn ?: subjectId} Speed Sprint",
            "${subj?.titleBn ?: subjectId} স্পিড স্প্রিন্ট"
        )
        _activeSoloSprint.value = ActiveSoloSprintState(
            id = UUID.randomUUID().toString().take(8),
            mode = mode,
            title = title,
            subjectId = subjectId,
            classId = classId,
            questions = picked
        )
    }

    fun answerSoloSprintQuestion(isCorrect: Boolean, responseTimeMs: Long) {
        val cur = _activeSoloSprint.value ?: return
        if (cur.finished) return
        val pts = if (isCorrect) {
            20L + ((15_000L - responseTimeMs.coerceIn(0L, 15_000L)) / 3000L).coerceIn(0L, 5L)
        } else 0L
        val nextIdx = cur.currentIndex + 1
        val nextCorrect = cur.correctCount + if (isCorrect) 1 else 0
        val nextScore = cur.scorePoints + pts
        if (nextIdx >= cur.questions.size) {
            viewModelScope.launch {
                val (xp, coins, _) = guestRepo.recordSoloArenaResult(
                    mode = cur.mode,
                    classId = cur.classId,
                    subjectId = cur.subjectId,
                    title = cur.title,
                    correctCount = nextCorrect,
                    totalQuestions = cur.questions.size,
                    scorePoints = nextScore
                )
                _activeSoloSprint.value = cur.copy(
                    correctCount = nextCorrect,
                    scorePoints = nextScore,
                    finished = true,
                    earnedXp = xp,
                    earnedCoins = coins
                )
                _celebrationPulse.value += 1
            }
        } else {
            _activeSoloSprint.value = cur.copy(
                currentIndex = nextIdx,
                correctCount = nextCorrect,
                scorePoints = nextScore
            )
        }
    }

    fun closeSoloSprint() {
        _activeSoloSprint.value = null
    }

    fun claimQuest(quest: UserQuestDoc, onRewarded: (Long, Long) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val res = guestRepo.claimGuestQuest(quest.questId)
            res.onSuccess { (xp, coins) ->
                _celebrationPulse.value += 1
                onRewarded(xp, coins)
            }
            res.onFailure { _bannerMessage.value = it.localizedMessage }
        }
    }

    fun purchaseCosmetic(cosmeticId: String, costCoins: Int) {
        viewModelScope.launch {
            val ok = preferences.purchaseGuestCosmetic(cosmeticId, costCoins)
            _bannerMessage.value = if (ok) {
                _celebrationPulse.value += 1
                "Unlocked!"
            } else {
                "Not enough Coins yet — practice or claim quests!"
            }
        }
    }

    fun updateProfileCustomization(
        displayName: String? = null,
        avatarId: String? = null,
        frameId: String? = null,
        titleBadge: String? = null,
        classId: String? = null,
        curriculumVersion: String? = null
    ) {
        viewModelScope.launch {
            preferences.updateGuestCustomization(
                displayName = displayName,
                avatarId = avatarId,
                frameId = frameId,
                titleBadge = titleBadge,
                classId = classId,
                curriculumVersion = curriculumVersion
            )
        }
    }

    fun updateUiLanguage(lang: AppLanguage) {
        viewModelScope.launch { preferences.setUiLanguage(lang) }
    }

    fun updateQuestionLanguage(lang: AppLanguage) {
        viewModelScope.launch { preferences.setQuestionLanguage(lang) }
    }

    fun updateDarkMode(enabled: Boolean) {
        viewModelScope.launch { preferences.setDarkMode(enabled) }
    }

    fun updateReducedMotion(enabled: Boolean) {
        viewModelScope.launch { preferences.setReducedMotion(enabled) }
    }

    fun updateSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setSoundEnabled(enabled) }
    }

    fun updateQuietHours(enabled: Boolean) {
        viewModelScope.launch { preferences.setQuietHoursEnabled(enabled) }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setNotificationsEnabled(enabled) }
    }

    fun exportGuestData() {
        viewModelScope.launch {
            _exportedJson.value = guestRepo.exportGuestDataJson(localPreferences.value)
        }
    }

    fun exitGuestModeToSignIn() {
        viewModelScope.launch {
            preferences.exitGuestMode()
        }
    }

    companion object {
        private const val TAG = "GuestSessionVM"
    }
}
