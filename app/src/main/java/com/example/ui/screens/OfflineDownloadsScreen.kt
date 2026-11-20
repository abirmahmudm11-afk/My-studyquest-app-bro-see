package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.AppLanguage
import com.example.core.OfflineVaultManager
import com.example.core.OfflineVaultStats
import com.example.core.StudyQuestStrings
import com.example.data.local.DownloadPackEntity
import com.example.data.repository.DownloadPackCategory
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StudyQuestCard
import com.example.ui.theme.EditorialSuccess
import com.example.ui.theme.JetBrainsMonoFontFamily
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OfflineDownloadsScreen(
    uiLanguage: AppLanguage,
    isOnline: Boolean,
    classId: String,
    packs: List<DownloadPackEntity>,
    onSeedCatalog: () -> Unit,
    onDownloadPack: (DownloadPackEntity) -> Unit,
    onDownloadClassBundle: () -> Unit,
    onRemovePack: (String) -> Unit,
    onOpenPack: (DownloadPackEntity) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    var vaultStats by remember { mutableStateOf(OfflineVaultStats()) }
    var selectedCategory by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        onSeedCatalog()
    }
    LaunchedEffect(packs) {
        vaultStats = OfflineVaultManager.computeVaultStats(context)
    }

    val filteredPacks = remember(packs, selectedCategory, searchQuery) {
        val q = searchQuery.trim().lowercase(Locale.ROOT)
        packs.filter { p ->
            val catOk = selectedCategory == "all" || p.category == selectedCategory
            val qOk = q.isBlank() ||
                p.titleEn.lowercase(Locale.ROOT).contains(q) ||
                p.titleBn.contains(searchQuery.trim()) ||
                p.subjectId.lowercase(Locale.ROOT).contains(q)
            catOk && qOk
        }
    }
    val installedCount = packs.count { it.status == "installed" }
    val installedSizeMb = packs.filter { it.status == "installed" }.sumOf { it.sizeMb }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = StudyQuestStrings.t(uiLanguage, "Offline Downloads & Vault", "অফলাইন ডাউনলোড ও ভল্ট"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = StudyQuestStrings.t(
                                uiLanguage,
                                if (isOnline) "Online Mode • Remote & Vault Packs Ready" else "Offline Mode • Local Vault Active",
                                if (isOnline) "অনলাইন মোড • সব প্যাক চালু" else "অফলাইন মোড • লোকাল ভল্ট সক্রিয়"
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("downloads_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = StudyQuestStrings.t(uiLanguage, "Back", "ফিরে যাও")
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 980.dp)
                    .fillMaxSize()
                    .testTag("downloads_screen_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    StudyQuestCard(modifier = Modifier.testTag("downloads_summary_card")) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(104.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_vault_offline_vault),
                                    contentDescription = StudyQuestStrings.t(uiLanguage, "Offline Vault Banner", "অফলাইন ভল্ট ব্যানার"),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = StudyQuestStrings.t(
                                            uiLanguage,
                                            "$installedCount / ${packs.size.coerceAtLeast(1)} Packs Ready Offline",
                                            "${StudyQuestStrings.formatDigits(installedCount, uiLanguage)} / ${StudyQuestStrings.formatDigits(packs.size.coerceAtLeast(1), uiLanguage)} প্যাক অফলাইনে প্রস্তুত"
                                        ),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = String.format(
                                            Locale.US,
                                            "Vault %.0f MB  |  Installed %.1f MB",
                                            vaultStats.bundledVaultSizeMb,
                                            installedSizeMb
                                        ),
                                        fontFamily = JetBrainsMonoFontFamily,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (isOnline) EditorialSuccess.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isOnline) Icons.Filled.CloudDone else Icons.Filled.CloudOff,
                                            contentDescription = null,
                                            tint = if (isOnline) EditorialSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = if (isOnline) StudyQuestStrings.t(uiLanguage, "Online", "অনলাইন")
                                            else StudyQuestStrings.t(uiLanguage, "Offline", "অফলাইন"),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Button(
                                onClick = onDownloadClassBundle,
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("downloads_batch_class_btn")
                            ) {
                                Icon(Icons.Filled.DownloadForOffline, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = StudyQuestStrings.t(
                                        uiLanguage,
                                        "Install All ${StudyQuestStrings.classLabel(classId, uiLanguage)} Textbooks & Packs",
                                        "${StudyQuestStrings.classLabel(classId, uiLanguage)}-এর সব বই ও প্যাক ডাউনলোড করো"
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            placeholder = {
                                Text(StudyQuestStrings.t(uiLanguage, "Search books, audio, or lab packs...", "বই, অডিও বা ল্যাব প্যাক খুঁজুন..."))
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("downloads_search_input")
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val categories = listOf(
                                "all" to StudyQuestStrings.t(uiLanguage, "All", "সব"),
                                DownloadPackCategory.TEXTBOOK to StudyQuestStrings.t(uiLanguage, "Textbooks", "পাঠ্যবই"),
                                DownloadPackCategory.AUDIO_BRIEFING to StudyQuestStrings.t(uiLanguage, "Audio Briefs", "অডিও ব্রিফ"),
                                DownloadPackCategory.FOCUS_SOUNDSCAPE to StudyQuestStrings.t(uiLanguage, "Focus Audio", "ফোকাস অডিও"),
                                DownloadPackCategory.STEM_LAB_PACK to StudyQuestStrings.t(uiLanguage, "STEM Labs", "STEM ল্যাব")
                            )
                            categories.forEach { (catId, label) ->
                                FilterChip(
                                    selected = selectedCategory == catId,
                                    onClick = { selectedCategory = catId },
                                    label = { Text(label) },
                                    modifier = Modifier.testTag("downloads_cat_$catId")
                                )
                            }
                        }
                    }
                }

                if (filteredPacks.isEmpty()) {
                    item {
                        EmptyStateView(
                            titleEn = "No packs match your filter",
                            titleBn = "কোনো প্যাক পাওয়া যায়নি",
                            subtitleEn = "Clear your search or choose All categories to view textbooks, audio, and lab packs.",
                            subtitleBn = "সব পাঠ্যবই ও অডিও দেখতে ফিল্টার পরিষ্কার করুন।",
                            language = uiLanguage
                        )
                    }
                } else {
                    items(filteredPacks, key = { it.packId }) { pack ->
                        DownloadPackCard(
                            pack = pack,
                            uiLanguage = uiLanguage,
                            isOnline = isOnline,
                            onDownload = { onDownloadPack(pack) },
                            onOpen = { onOpenPack(pack) },
                            onRemove = { onRemovePack(pack.packId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadPackCard(
    pack: DownloadPackEntity,
    uiLanguage: AppLanguage,
    isOnline: Boolean,
    onDownload: () -> Unit,
    onOpen: () -> Unit,
    onRemove: () -> Unit
) {
    val icon = when (pack.category) {
        DownloadPackCategory.TEXTBOOK -> Icons.AutoMirrored.Filled.MenuBook
        DownloadPackCategory.AUDIO_BRIEFING -> Icons.Filled.Headphones
        DownloadPackCategory.FOCUS_SOUNDSCAPE -> Icons.Filled.LibraryMusic
        else -> Icons.Filled.Science
    }
    val installed = pack.status == "installed"
    val downloading = pack.status == "downloading"
    val requiresNetworkOnly = !pack.remoteUrl.isNullOrBlank() && !isOnline && !installed

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("download_pack_${pack.packId}")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = StudyQuestStrings.t(uiLanguage, pack.titleEn, pack.titleBn),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = StudyQuestStrings.t(uiLanguage, pack.subtitleEn, pack.subtitleBn),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f MB", pack.sizeMb),
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (downloading) {
                LinearProgressIndicator(
                    progress = { (pack.progressPct / 100f).coerceIn(0.05f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(999.dp))
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (installed) {
                    FilledTonalButton(
                        onClick = onOpen,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("download_open_${pack.packId}")
                    ) {
                        Icon(
                            imageVector = if (pack.category == DownloadPackCategory.AUDIO_BRIEFING ||
                                pack.category == DownloadPackCategory.FOCUS_SOUNDSCAPE
                            ) Icons.Filled.PlayArrow else Icons.Filled.DownloadDone,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(StudyQuestStrings.t(uiLanguage, "Open Offline", "অফলাইনে খোলো"))
                    }
                    OutlinedButton(
                        onClick = onRemove,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("download_remove_${pack.packId}")
                    ) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = StudyQuestStrings.t(uiLanguage, "Remove", "মুছুন"), modifier = Modifier.size(18.dp))
                    }
                } else {
                    Button(
                        onClick = onDownload,
                        enabled = !downloading && !requiresNetworkOnly,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("download_install_${pack.packId}")
                    ) {
                        Icon(Icons.Filled.DownloadForOffline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            when {
                                downloading -> StudyQuestStrings.t(uiLanguage, "Installing ${pack.progressPct}%", "ডাউনলোড ${pack.progressPct}%")
                                requiresNetworkOnly -> StudyQuestStrings.t(uiLanguage, "Offline • Online Needed", "অফলাইন • নেট প্রয়োজন")
                                else -> StudyQuestStrings.t(uiLanguage, "Download Offline", "অফলাইনে ডাউনলোড")
                            }
                        )
                    }
                }
            }
        }
    }
}
