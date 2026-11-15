package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.unwrapFirestoreMessage
import com.example.data.model.CompetitionArchiveDoc
import com.example.data.model.LeaderboardEntryDoc
import com.example.data.model.MatchDoc
import com.example.data.model.MatchRequestDoc
import com.example.data.model.MatchSubmissionDoc
import com.example.data.model.PublicProfile
import com.example.data.model.TournamentDoc
import com.example.data.repository.CompetitionRepository
import com.example.data.repository.LeaderboardCategory
import com.example.data.repository.LeaderboardPeriod
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CompeteViewModel(
    private val currentUserId: String,
    private val compRepo: CompetitionRepository
) : ViewModel() {

    val myMatches: StateFlow<List<MatchDoc>> = compRepo.observeMyMatches(currentUserId)
        .catch { error ->
            val fsErr = generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
            if (fsErr == null && error is CancellationException) throw error
            Log.w(TAG, "Matches observe error", fsErr ?: error)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val leaderboard: StateFlow<List<LeaderboardEntryDoc>> = compRepo.observeLeaderboard()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val competitionArchives: StateFlow<List<CompetitionArchiveDoc>> = compRepo.observeCompetitionArchives()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val tournaments: StateFlow<List<TournamentDoc>> = compRepo.observeTournaments()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _selectedCategory = MutableStateFlow(LeaderboardCategory.XP)
    val selectedCategory: StateFlow<LeaderboardCategory> = _selectedCategory.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(LeaderboardPeriod.WEEKLY)
    val selectedPeriod: StateFlow<LeaderboardPeriod> = _selectedPeriod.asStateFlow()

    private val _activeMatchId = MutableStateFlow<String?>(null)
    val activeMatch: StateFlow<MatchDoc?> = _activeMatchId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else compRepo.observeMatch(id).catch { emit(null) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val activeSubmissions: StateFlow<List<MatchSubmissionDoc>> = _activeMatchId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else compRepo.observeSubmissions(id, currentUserId).catch { emit(emptyList()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _activeRequestId = MutableStateFlow<String?>(null)
    val activeMatchRequest: StateFlow<MatchRequestDoc?> = _activeRequestId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else compRepo.observeMatchRequest(id).catch { emit(null) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        viewModelScope.launch {
            activeMatchRequest.collect { req ->
                if (req != null && req.status == "matched" && !req.matchId.isNullOrBlank()) {
                    _activeMatchId.value = req.matchId
                    _activeRequestId.value = null
                }
            }
        }
        viewModelScope.launch {
            leaderboard.collect { entries ->
                if (entries.isNotEmpty()) {
                    compRepo.archivePreviousPeriodIfNeeded(entries, currentUserId)
                }
            }
        }
    }

    fun selectCategory(cat: LeaderboardCategory) {
        _selectedCategory.value = cat
    }

    fun selectPeriod(period: LeaderboardPeriod) {
        _selectedPeriod.value = period
    }

    fun clearMessage() {
        _message.value = null
    }

    fun openMatch(matchId: String) {
        _activeMatchId.value = matchId
    }

    fun closeActiveMatch() {
        _activeMatchId.value = null
    }

    fun enterQuickBattle(profile: PublicProfile, classId: String, subjectId: String) {
        viewModelScope.launch {
            val res = compRepo.enterQuickBattleQueue(profile, classId, subjectId)
            res.onSuccess { (req, match) ->
                if (match != null) {
                    _activeMatchId.value = match.id
                    _activeRequestId.value = null
                } else if (req != null) {
                    _activeRequestId.value = req.id
                }
            }
            res.onFailure {
                _message.value = unwrapFirestoreMessage(it, "Matchmaking failed")
            }
        }
    }

    fun cancelWaitingMatchRequest() {
        val reqId = _activeRequestId.value ?: return
        viewModelScope.launch {
            compRepo.cancelMatchRequest(reqId)
            _activeRequestId.value = null
        }
    }

    fun createPrivateRoom(classId: String, subjectId: String) {
        viewModelScope.launch {
            val res = compRepo.createRoomOrChallenge(
                hostUid = currentUserId,
                mode = "private_room",
                classId = classId,
                subjectId = subjectId
            )
            res.onSuccess { _activeMatchId.value = it.id }
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Could not create room") }
        }
    }

    fun challengeFriendOrGroup(
        friendUids: List<String>,
        isGroup: Boolean,
        classId: String,
        subjectId: String
    ) {
        viewModelScope.launch {
            val res = compRepo.createRoomOrChallenge(
                hostUid = currentUserId,
                mode = if (isGroup) "group_battle" else "friend_challenge",
                classId = classId,
                subjectId = subjectId,
                invitedUids = friendUids
            )
            res.onSuccess { _activeMatchId.value = it.id }
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Challenge failed") }
        }
    }

    fun joinRoomByCode(code: String) {
        viewModelScope.launch {
            val res = compRepo.joinPrivateRoomByCode(code, currentUserId)
            res.onSuccess { _activeMatchId.value = it.id }
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Room code not found") }
        }
    }

    fun joinInvitedMatch(match: MatchDoc) {
        viewModelScope.launch {
            val res = compRepo.joinInvitedMatch(match, currentUserId)
            res.onSuccess { _activeMatchId.value = it.id }
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Could not join match") }
        }
    }

    fun startActiveMatch() {
        val matchId = _activeMatchId.value ?: return
        viewModelScope.launch {
            compRepo.startMatchNow(matchId)
        }
    }

    fun submitAnswer(
        match: MatchDoc,
        questionId: String,
        selectedIndex: Int,
        isCorrect: Boolean,
        responseTimeMs: Long
    ) {
        viewModelScope.launch {
            val res = compRepo.submitMatchAnswer(match, currentUserId, questionId, selectedIndex, isCorrect, responseTimeMs)
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Submission error") }
        }
    }

    fun finalizeMatch(match: MatchDoc, profile: PublicProfile) {
        viewModelScope.launch {
            val res = compRepo.finalizeMatchAndLeaderboard(match, profile)
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Finalization error") }
        }
    }

    fun joinTournament(tournament: TournamentDoc) {
        viewModelScope.launch {
            val res = compRepo.joinTournament(tournament, currentUserId)
            res.onSuccess { _message.value = "Registered for ${tournament.titleEn}" }
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Could not join tournament") }
        }
    }

    fun purchaseCosmetic(profile: PublicProfile, cosmeticId: String, costCoins: Int) {
        viewModelScope.launch {
            val res = compRepo.purchaseCosmetic(profile, cosmeticId, costCoins)
            res.onSuccess { _message.value = "Unlocked!" }
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Purchase failed") }
        }
    }

    companion object {
        private const val TAG = "CompeteVM"
    }
}
