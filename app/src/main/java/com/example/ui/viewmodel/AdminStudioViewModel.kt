package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.unwrapFirestoreMessage
import com.example.data.model.AcademicContentDoc
import com.example.data.model.AiProviderSettingsDoc
import com.example.data.model.AiUsageLogDoc
import com.example.data.model.AssignmentDefinitionDoc
import com.example.data.model.AuditLogDoc
import com.example.data.model.BadgeDefinitionDoc
import com.example.data.model.CommunityPostDoc
import com.example.data.model.ModerationCaseDoc
import com.example.data.model.SystemSettingsDoc
import com.example.data.model.TextbookCatalogDoc
import com.example.data.model.TextbookCatalogItem
import com.example.data.model.TextbookChapterSpec
import com.example.data.model.XpRuleConfig
import com.example.data.repository.AdminStudioRepository
import com.example.data.repository.BusinessInsightsSummary
import com.example.data.repository.OpenRouterModelCheckResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminStudioViewModel(
    private val currentUserId: String,
    private val adminRepo: AdminStudioRepository
) : ViewModel() {

    val systemSettings: StateFlow<SystemSettingsDoc> = adminRepo.observeSystemSettings()
        .catch { emit(SystemSettingsDoc()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), SystemSettingsDoc())

    val academicContent: StateFlow<List<AcademicContentDoc>> = adminRepo.observeAllAcademicContent()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val moderationCases: StateFlow<List<ModerationCaseDoc>> = adminRepo.observeModerationCases()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val auditLogs: StateFlow<List<AuditLogDoc>> = adminRepo.observeAuditLogs()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val badgeDefinitions: StateFlow<List<BadgeDefinitionDoc>> = adminRepo.observeBadgeDefinitions()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val allAssignments: StateFlow<List<AssignmentDefinitionDoc>> = adminRepo.observeAllAssignments()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val flaggedPosts: StateFlow<List<CommunityPostDoc>> = adminRepo.observeFlaggedCommunityPosts()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val aiProviderSettings: StateFlow<AiProviderSettingsDoc> = adminRepo.observeAiProviderSettings()
        .catch { emit(AiProviderSettingsDoc()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), AiProviderSettingsDoc())

    val aiUsageLogs: StateFlow<List<AiUsageLogDoc>> = adminRepo.observeAiUsageLogs()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val cloudTextbooks: StateFlow<List<TextbookCatalogDoc>> = adminRepo.observeCloudTextbookCatalog()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val isOpenRouterKeyConfigured: Boolean
        get() = adminRepo.isOpenRouterKeyConfigured()

    private val _modelCheckResult = MutableStateFlow<OpenRouterModelCheckResult?>(null)
    val modelCheckResult: StateFlow<OpenRouterModelCheckResult?> = _modelCheckResult.asStateFlow()

    private val _isVerifyingModels = MutableStateFlow(false)
    val isVerifyingModels: StateFlow<Boolean> = _isVerifyingModels.asStateFlow()

    private val _businessInsights = MutableStateFlow(BusinessInsightsSummary())
    val businessInsights: StateFlow<BusinessInsightsSummary> = _businessInsights.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    fun createDraftQuestion(
        classId: String,
        subjectId: String,
        titleEn: String,
        titleBn: String,
        optionsEn: List<String>,
        optionsBn: List<String>,
        correctIndex: Int,
        explanationEn: String,
        explanationBn: String,
        sourceAttribution: String
    ) {
        viewModelScope.launch {
            val res = adminRepo.createDraftQuestion(
                actorUid = currentUserId,
                classId = classId,
                subjectId = subjectId,
                titleEn = titleEn,
                titleBn = titleBn,
                optionsEn = optionsEn,
                optionsBn = optionsBn,
                correctIndex = correctIndex,
                explanationEn = explanationEn,
                explanationBn = explanationBn,
                sourceAttribution = sourceAttribution
            )
            res.onSuccess { _message.value = "Draft created ($it)" }
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Create failed") }
        }
    }

    fun advanceEditorialStatus(doc: AcademicContentDoc, nextStatus: String) {
        viewModelScope.launch {
            val res = adminRepo.advanceEditorialStatus(currentUserId, doc, nextStatus)
            res.onSuccess { _message.value = "Status updated -> $nextStatus" }
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Transition failed") }
        }
    }

    fun resolveModerationCase(doc: ModerationCaseDoc, status: String, note: String) {
        viewModelScope.launch {
            val res = adminRepo.resolveModerationCase(currentUserId, doc, status, note)
            res.onSuccess { _message.value = "Case marked $status" }
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Moderation update failed") }
        }
    }

    fun updateSystemSettings(
        aiEnabled: Boolean,
        seasonEn: String,
        seasonBn: String,
        billingConfigured: Boolean = systemSettings.value.billingConfigured,
        adMobConfigured: Boolean = systemSettings.value.adMobConfigured,
        externalVideoRendererConfigured: Boolean = systemSettings.value.externalVideoRendererConfigured
    ) {
        viewModelScope.launch {
            val res = adminRepo.updateSystemSettings(
                actorUid = currentUserId,
                aiSolverEnabled = aiEnabled,
                seasonNameEn = seasonEn,
                seasonNameBn = seasonBn,
                billingConfigured = billingConfigured,
                adMobConfigured = adMobConfigured,
                externalVideoRendererConfigured = externalVideoRendererConfigured,
                xpRulesJson = systemSettings.value.xpRulesJson
            )
            res.onSuccess { _message.value = "System settings saved" }
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Settings save failed") }
        }
    }

    fun saveXpRules(rules: XpRuleConfig) {
        viewModelScope.launch {
            val res = adminRepo.saveXpRuleConfig(currentUserId, systemSettings.value, rules)
            res.onSuccess { _message.value = "XP rules & anti-farming caps updated" }
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "XP rules save failed") }
        }
    }

    fun saveBadgeDefinition(
        badgeId: String,
        category: String,
        rarity: String,
        titleEn: String,
        titleBn: String,
        descriptionEn: String,
        target: Long,
        xpBonus: Long,
        enabled: Boolean
    ) {
        viewModelScope.launch {
            val res = adminRepo.saveBadgeDefinition(
                currentUserId, badgeId, category, rarity, titleEn, titleBn, descriptionEn, target, xpBonus, enabled
            )
            res.onSuccess { _message.value = "Badge definition saved ($badgeId)" }
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Badge save failed") }
        }
    }

    fun createAssignment(
        titleEn: String,
        titleBn: String,
        descriptionEn: String,
        assignmentType: String,
        classId: String,
        subjectId: String,
        difficulty: String,
        targetCount: Long,
        xpReward: Long
    ) {
        viewModelScope.launch {
            val res = adminRepo.createAssignmentDefinition(
                currentUserId, titleEn, titleBn, descriptionEn, assignmentType, classId, subjectId, difficulty, targetCount, xpReward
            )
            res.onSuccess { _message.value = "Assignment published ($it)" }
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Assignment failed") }
        }
    }

    fun moderateCommunityPost(postId: String, hidePost: Boolean) {
        viewModelScope.launch {
            val res = adminRepo.moderateCommunityPost(currentUserId, postId, hidePost)
            res.onSuccess { _message.value = if (hidePost) "Post hidden" else "Post approved" }
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Moderation failed") }
        }
    }

    fun createTournament(titleEn: String, titleBn: String, classId: String, subjectId: String) {
        viewModelScope.launch {
            val res = adminRepo.createOfficialTournament(currentUserId, titleEn, titleBn, classId, subjectId)
            res.onSuccess { _message.value = "Tournament launched" }
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Tournament creation failed") }
        }
    }

    fun verifyOpenRouterModels() {
        if (_isVerifyingModels.value) return
        viewModelScope.launch {
            _isVerifyingModels.value = true
            try {
                val res = adminRepo.verifyOpenRouterModels(aiProviderSettings.value)
                _modelCheckResult.value = res
                _message.value = res.message
            } finally {
                _isVerifyingModels.value = false
            }
        }
    }

    fun saveAiProviderSettings(
        openRouterEnabled: Boolean,
        defaultModelId: String,
        reasoningModelId: String,
        advancedModelId: String,
        advancedModelEnabled: Boolean,
        fallbackToFirebaseAi: Boolean,
        globalMonthlyBudgetUsd: Double,
        perUserMonthlyBudgetUsd: Double,
        astraMonthlyBudgetUsd: Double
    ) {
        viewModelScope.launch {
            val res = adminRepo.saveAiProviderSettings(
                actorUid = currentUserId,
                openRouterEnabled = openRouterEnabled,
                defaultModelId = defaultModelId,
                reasoningModelId = reasoningModelId,
                advancedModelId = advancedModelId,
                advancedModelEnabled = advancedModelEnabled,
                fallbackToFirebaseAi = fallbackToFirebaseAi,
                globalMonthlyBudgetUsd = globalMonthlyBudgetUsd,
                perUserMonthlyBudgetUsd = perUserMonthlyBudgetUsd,
                astraMonthlyBudgetUsd = astraMonthlyBudgetUsd
            )
            res.onSuccess { _message.value = "AI Provider & Cost Guard settings saved" }
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Failed to save AI provider config") }
        }
    }

    fun refreshBusinessInsights() {
        viewModelScope.launch {
            _businessInsights.value = adminRepo.computeBusinessInsights(aiUsageLogs.value)
        }
    }

    fun publishTextbook(book: TextbookCatalogItem) {
        viewModelScope.launch {
            val res = adminRepo.publishTextbookCatalogItem(currentUserId, book)
            res.onSuccess { _message.value = "Published NCTB textbook: ${book.titleEn}" }
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Failed to publish textbook") }
        }
    }

    fun publishTextbookChapter(chapter: TextbookChapterSpec) {
        viewModelScope.launch {
            val res = adminRepo.publishTextbookChapter(currentUserId, chapter)
            res.onSuccess { _message.value = "Published chapter: ${chapter.titleEn}" }
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Failed to publish chapter") }
        }
    }
}
