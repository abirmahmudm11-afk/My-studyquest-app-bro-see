package com.example.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.model.AiChatMessageDoc
import com.example.data.model.AiConversationDoc
import com.example.data.model.QuestPlanCatalog
import com.example.data.model.UserSubscriptionDoc
import com.example.data.repository.StudyQuestAiToolCatalog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StudyQuestCard
import com.example.ui.theme.LocalStudyQuestColors
import com.example.ui.viewmodel.AiDoubtUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AILabScreen(
    language: AppLanguage,
    classId: String,
    subjectName: String,
    subscription: UserSubscriptionDoc,
    activeToolId: String,
    conversations: List<AiConversationDoc>,
    activeConversationId: String?,
    messages: List<AiChatMessageDoc>,
    aiState: AiDoubtUiState,
    isGuestMode: Boolean,
    onSelectTool: (String) -> Unit,
    onSelectConversation: (String?) -> Unit,
    onCreateConversation: (title: String, toolId: String) -> Unit,
    onDeleteConversation: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onCancelRequest: () -> Unit,
    onRetryRequest: () -> Unit,
    onOpenPlanStore: () -> Unit,
    onRequestGoogleSignIn: () -> Unit,
    onBack: () -> Unit,
    isOnline: Boolean = true
) {
    BackHandler(onBack = onBack)

    var providerTab by remember { mutableIntStateOf(0) } // 0 = StudyQuest AI (OpenRouter), 1 = Puter.js Pay-As-You-Go
    val plan = QuestPlanCatalog.planById(subscription.tier)
    val aiLimit = subscription.monthlyAiLimit.toInt().coerceAtLeast(plan.monthlyAiCredits)
    val aiRemaining = (aiLimit - subscription.aiCreditsUsed.toInt()).coerceAtLeast(0)
    val semantic = LocalStudyQuestColors.current

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("ailab_back_btn")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "STUDYQUEST HELPER",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isOnline) {
                        StudyQuestStrings.t(
                            language,
                            "Cloud Gemini + OpenRouter • $aiRemaining/$aiLimit AI credits left",
                            "ক্লাউড জেমিনি + OpenRouter • $aiRemaining/$aiLimit ক্রেডিট বাকি"
                        )
                    } else {
                        StudyQuestStrings.t(
                            language,
                            "Offline Mode • 6,000-entry Knowledge Atlas active",
                            "অফলাইন মোড • ৬,০০০ গাইডসহ অ্যাটলাস সক্রিয়"
                        )
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (isOnline) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.testTag("ailab_mode_badge")
            ) {
                Text(
                    text = if (isOnline) {
                        StudyQuestStrings.t(language, "Cloud Live", "ক্লাউড লাইভ")
                    } else {
                        StudyQuestStrings.t(language, "Offline Atlas", "অফলাইন অ্যাটলাস")
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            TextButton(onClick = onOpenPlanStore) {
                Icon(Icons.Filled.WorkspacePremium, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("৳150/mo")
            }
        }

        TabRow(selectedTabIndex = providerTab) {
            Tab(
                selected = providerTab == 0,
                onClick = { providerTab = 0 },
                text = { Text(StudyQuestStrings.t(language, "StudyQuest AI (12 Tools)", "StudyQuest AI (১২টি টুল)")) },
                modifier = Modifier.testTag("ai_provider_tab_studyquest")
            )
            Tab(
                selected = providerTab == 1,
                onClick = { providerTab = 1 },
                text = { Text(StudyQuestStrings.t(language, "Puter.js Pay-As-You-Go", "Puter.js পে-অ্যাজ-ইউ-গো")) },
                modifier = Modifier.testTag("ai_provider_tab_puter")
            )
        }

        if (providerTab == 1) {
            if (!isOnline) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    StudyQuestCard(modifier = Modifier.testTag("puter_offline_locked_card")) {
                        Text(
                            text = StudyQuestStrings.t(
                                language,
                                "Puter.js requires an active internet connection. Switch to the StudyQuest AI tab to continue using the Offline Knowledge Atlas (6,000 guides).",
                                "Puter.js ব্যবহার করতে ইন্টারনেট প্রয়োজন। অফলাইন নলেজ অ্যাটলাস ব্যবহার করতে প্রথম ট্যাবে ফিরে যান।"
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                PuterAiWebViewSection(language = language)
            }
        } else if (isGuestMode) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    StudyQuestCard {
                        Text(
                            text = StudyQuestStrings.t(
                                language,
                                "Sign in with Google to unlock StudyQuest AI Helper (20 free credits every month + OpenRouter gpt-6-luna / gpt-6.1-sol tutoring) and save conversation history.",
                                "StudyQuest AI হেল্পার (প্রতি মাসে ২০ ফ্রি ক্রেডিট) ও চ্যাট ইতিহাস সংরক্ষণ করতে গুগল অ্যাকাউন্ট দিয়ে সাইন-ইন করো।"
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = onRequestGoogleSignIn, modifier = Modifier.fillMaxWidth()) {
                            Text(StudyQuestStrings.t(language, "Sign in with Google", "গুগল সাইন-ইন"))
                        }
                        Spacer(Modifier.height(6.dp))
                        OutlinedButton(onClick = { providerTab = 1 }, modifier = Modifier.fillMaxWidth()) {
                            Text(StudyQuestStrings.t(language, "Or try Puter.js Pay-As-You-Go Tab", "অথবা Puter.js ব্যবহার করো"))
                        }
                    }
                }
            }
        } else {
            val activeTool = remember(activeToolId) { StudyQuestAiToolCatalog.byId(activeToolId) }
            var promptInput by remember { mutableStateOf("") }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = StudyQuestStrings.t(language, "12 Specialized Learning Tools", "১২টি বিশেষায়িত শিক্ষণ টুল"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(StudyQuestAiToolCatalog.tools, key = { it.id }) { tool ->
                            FilterChip(
                                selected = tool.id == activeToolId,
                                onClick = { onSelectTool(tool.id) },
                                label = {
                                    Text(StudyQuestStrings.t(language, tool.titleEn, tool.titleBn))
                                },
                                modifier = Modifier.testTag("ai_tool_${tool.id}")
                            )
                        }
                    }
                }

                item {
                    StudyQuestCard(containerColor = MaterialTheme.colorScheme.surface) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = StudyQuestStrings.t(language, activeTool.titleEn, activeTool.titleBn),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = StudyQuestStrings.t(language, activeTool.subtitleEn, activeTool.subtitleBn),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    promptInput = StudyQuestStrings.t(language, activeTool.promptHintEn, activeTool.promptHintBn)
                                }
                            ) {
                                Text(StudyQuestStrings.t(language, "Sample", "উদাহরণ"))
                            }
                        }
                    }
                }

                // Multi-conversation row
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                onCreateConversation(
                                    StudyQuestStrings.t(language, "${activeTool.titleEn} Chat", "${activeTool.titleBn} চ্যাট"),
                                    activeTool.id
                                )
                            },
                            modifier = Modifier.testTag("new_ai_chat_btn")
                        ) {
                            Icon(Icons.Filled.AddComment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(StudyQuestStrings.t(language, "New Chat", "নতুন চ্যাট"))
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                            items(conversations, key = { it.id }) { conv ->
                                FilterChip(
                                    selected = conv.id == activeConversationId,
                                    onClick = { onSelectConversation(conv.id) },
                                    label = { Text(conv.title.take(22)) },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { onDeleteConversation(conv.id) },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete chat", modifier = Modifier.size(14.dp))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                if (messages.isEmpty() && aiState is AiDoubtUiState.Idle) {
                    item {
                        EmptyStateView(
                            titleEn = "Ask StudyQuest Helper anything from $classId",
                            titleBn = "StudyQuest হেল্পারকে যেকোনো প্রশ্ন করো",
                            subtitleEn = "Default routing uses gpt-6-luna for fast tutoring and switches to gpt-6.1-sol automatically for hard math & multi-step proofs.",
                            subtitleBn = "সহজ ব্যাখ্যার জন্য gpt-6-luna এবং জটিল গণিতের জন্য gpt-6.1-sol স্বয়ংক্রিয়ভাবে ব্যবহৃত হয়।",
                            language = language
                        )
                    }
                } else {
                    items(messages, key = { it.id }) { msg ->
                        val isUser = msg.role == "user"
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isUser) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = if (isUser) StudyQuestStrings.t(language, "You", "তুমি") else "STUDYQUEST HELPER",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUser) semantic.success else MaterialTheme.colorScheme.primary
                                    )
                                    if (!isUser && !msg.modelUsed.isNullOrBlank()) {
                                        Text(
                                            text = "${msg.modelUsed} • ${msg.creditsCharged} cr",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(msg.text, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                when (aiState) {
                    AiDoubtUiState.Idle -> Unit
                    AiDoubtUiState.Loading -> {
                        item {
                            StudyQuestCard {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    Text(
                                        text = StudyQuestStrings.t(language, "Routing to StudyQuest Helper...", "StudyQuest হেল্পার উত্তর তৈরি করছে..."),
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = onCancelRequest) {
                                        Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Text(StudyQuestStrings.t(language, "Stop", "থামাও"))
                                    }
                                }
                            }
                        }
                    }
                    is AiDoubtUiState.Streaming -> {
                        item {
                            StudyQuestCard {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = if (aiState.modelUsed.isNotBlank()) "Streaming (${aiState.modelUsed})" else "Streaming...",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    TextButton(onClick = onCancelRequest) {
                                        Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Text(StudyQuestStrings.t(language, "Cancel", "বাতিল"))
                                    }
                                }
                                Text(aiState.text, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    is AiDoubtUiState.Success -> Unit // Persisted directly into messages list
                    is AiDoubtUiState.Error -> {
                        item {
                            StudyQuestCard(containerColor = MaterialTheme.colorScheme.errorContainer) {
                                Text(
                                    text = aiState.message,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(Modifier.height(6.dp))
                                OutlinedButton(onClick = onRetryRequest) {
                                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(StudyQuestStrings.t(language, "Retry (Credits Protected)", "আবার চেষ্টা করো"))
                                }
                            }
                        }
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = { promptInput = it },
                        placeholder = {
                            Text(
                                StudyQuestStrings.t(language, "Ask in Bangla or English...", "বাংলা বা ইংরেজিতে প্রশ্ন লেখো..."),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        maxLines = 3,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ailab_prompt_input")
                    )
                    Button(
                        onClick = {
                            val txt = promptInput.trim()
                            if (txt.isNotEmpty()) {
                                promptInput = ""
                                onSendMessage(txt)
                            }
                        },
                        enabled = promptInput.isNotBlank() && aiState !is AiDoubtUiState.Loading && aiState !is AiDoubtUiState.Streaming,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("ailab_send_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PuterAiWebViewSection(language: AppLanguage) {
    val htmlWrapper = remember {
        """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <script src="https://js.puter.com/v2/"></script>
          <style>
            body { font-family: sans-serif; margin: 0; padding: 14px; background: #F6F7F9; color: #111827; }
            .card { background: #FFFFFF; border: 1px solid #E5E7EB; border-radius: 14px; padding: 14px; margin-bottom: 12px; }
            button { background: #166534; color: #FFFFFF; border: none; border-radius: 10px; padding: 10px 14px; font-weight: 600; }
            textarea { width: 100%; box-sizing: border-box; border: 1px solid #D1D5DB; border-radius: 10px; padding: 10px; min-height: 76px; font-size: 14px; }
            pre { white-space: pre-wrap; word-wrap: break-word; font-family: sans-serif; font-size: 14px; line-height: 1.5; }
          </style>
        </head>
        <body>
          <div class="card">
            <strong>Puter.js Pay-As-You-Go AI</strong>
            <p style="font-size:12px;color:#4B5563;">Sign into your own Puter account below. Puter handles user-funded pay-as-you-go billing directly; no StudyQuest secrets are loaded in this sandbox.</p>
            <button onclick="signInPuter()">Sign in with Puter</button>
          </div>
          <div class="card">
            <textarea id="promptBox" placeholder="Ask any study question in Bangla or English..."></textarea>
            <div style="margin-top:8px;">
              <button onclick="askPuter()">Ask Puter AI</button>
            </div>
          </div>
          <div class="card">
            <pre id="outBox">Ready.</pre>
          </div>
          <script>
            async function signInPuter() {
              const out = document.getElementById('outBox');
              try {
                await puter.auth.signIn();
                out.textContent = 'Signed into Puter account successfully.';
              } catch (e) {
                out.textContent = 'Puter sign-in notice: ' + (e && e.message ? e.message : e);
              }
            }
            async function askPuter() {
              const prompt = document.getElementById('promptBox').value.trim();
              const out = document.getElementById('outBox');
              if (!prompt) return;
              out.textContent = 'Thinking...';
              try {
                const resp = await puter.ai.chat(prompt);
                out.textContent = (resp && resp.message && resp.message.content) ? resp.message.content : String(resp);
              } catch (e) {
                out.textContent = 'Puter AI error: ' + (e && e.message ? e.message : e);
              }
            }
          </script>
        </body>
        </html>
        """.trimIndent()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StudyQuestCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = StudyQuestStrings.t(language, "Puter.js User-Paid Sandbox", "Puter.js পে-অ্যাজ-ইউ-গো স্যান্ডবক্স"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = StudyQuestStrings.t(
                    language,
                    "Students sign in with their own Puter account and pay Puter directly. For younger students (Class 6–10), parent/guardian permission is recommended before third-party account billing.",
                    "শিক্ষার্থী নিজস্ব Puter অ্যাকাউন্ট দিয়ে সরাসরি পে-অ্যাজ-ইউ-গো ব্যবহার করতে পারবে। কম বয়সী শিক্ষার্থীদের জন্য অভিভাবকের অনুমতি নেওয়া উচিত।"
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("puter_webview_container")
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val uri = request?.url ?: return true
                                val scheme = uri.scheme?.lowercase()
                                val host = uri.host?.lowercase().orEmpty()
                                val allowed = scheme == "https" && (
                                    host == "puter.com" || host.endsWith(".puter.com") ||
                                        host.endsWith("puter.site") || host.contains("google")
                                    )
                                return !allowed
                            }
                        }
                        loadDataWithBaseURL("https://puter.com/", htmlWrapper, "text/html", "utf-8", null)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
