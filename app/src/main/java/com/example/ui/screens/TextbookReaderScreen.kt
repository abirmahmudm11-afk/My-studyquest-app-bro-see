package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.AmbientMusicPlayer
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.local.BookChapterEntity
import com.example.data.local.BookPageNoteEntity
import com.example.data.local.CachedBookEntity
import com.example.data.model.TextbookCatalogItem
import com.example.data.model.TextbookDownloadStatus
import com.example.ui.components.StudyQuestCard
import com.example.ui.theme.SkyCyan
import com.example.ui.theme.VividEmerald
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TextbookReaderScreen(
    book: TextbookCatalogItem,
    cachedBook: CachedBookEntity?,
    chapters: List<BookChapterEntity>,
    notes: List<BookPageNoteEntity>,
    uiLanguage: AppLanguage,
    onBack: () -> Unit,
    onInstallOffline: (forceRedownload: Boolean) -> Unit,
    onImportLocalPdf: (Uri) -> Unit,
    onSaveLastReadPage: (Int) -> Unit,
    onMarkChapterCompleted: (String) -> Unit,
    onSavePageNote: (page: Int, chapterId: String?, title: String, noteText: String, isBookmark: Boolean) -> Unit,
    onDeletePageNote: (String) -> Unit,
    onAskAiAboutPage: (contextPrompt: String) -> Unit,
    onPracticeBookQuestions: () -> Unit,
    onReportBookIssue: (reason: String, details: String) -> Unit,
    onSwitchBook: () -> Unit,
    onOpenStemLab: ((String) -> Unit)? = null
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val briefingPlayer = remember(context) { AmbientMusicPlayer(context) }
    val audioState by briefingPlayer.state.collectAsStateWithLifecycle()
    val isBriefingPlaying = audioState.activeBriefingSubjectId == book.subjectId
    DisposableEffect(briefingPlayer) {
        onDispose { briefingPlayer.release() }
    }

    val uriHandler = LocalUriHandler.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Reader, 1=Chapters, 2=Search & Notes
    var currentPage by remember(book.id) { mutableIntStateOf((cachedBook?.lastReadPage ?: 0).coerceAtLeast(0)) }
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var searchQuery by remember { mutableStateOf("") }
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteTitleInput by remember { mutableStateOf("") }
    var noteBodyInput by remember { mutableStateOf("") }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportDetails by remember { mutableStateOf("") }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportLocalPdf(uri)
        }
    }

    val localPdfFile = remember(cachedBook?.localPdfPath) {
        cachedBook?.localPdfPath?.takeIf { it.isNotBlank() }?.let(::File)
            ?.takeIf { it.exists() && it.length() > 0L }
    }

    val pdfHolder = remember(localPdfFile?.absolutePath) {
        localPdfFile?.let { file ->
            runCatching {
                val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                pfd to renderer
            }.getOrNull()
        }
    }

    DisposableEffect(pdfHolder) {
        onDispose {
            runCatching { pdfHolder?.second?.close() }
            runCatching { pdfHolder?.first?.close() }
        }
    }

    val pageCount = pdfHolder?.second?.pageCount?.coerceAtLeast(1)
        ?: (cachedBook?.totalPages?.takeIf { it > 0 } ?: book.estimatedPages.coerceAtLeast(1))
    val safePage = currentPage.coerceIn(0, (pageCount - 1).coerceAtLeast(0))

    LaunchedEffect(book.id, safePage) {
        if (safePage != currentPage) currentPage = safePage
        onSaveLastReadPage(safePage)
    }

    val renderedBitmap by produceState<ImageBitmap?>(
        initialValue = null,
        key1 = pdfHolder,
        key2 = safePage
    ) {
        val renderer = pdfHolder?.second
        value = if (renderer == null) {
            null
        } else {
            withContext(Dispatchers.IO) {
                runCatching {
                    synchronized(renderer) {
                        renderer.openPage(safePage).use { page ->
                            val targetW = (page.width * 2).coerceIn(720, 1600)
                            val targetH = ((targetW.toFloat() / page.width.coerceAtLeast(1)) * page.height)
                                .toInt()
                                .coerceIn(900, 2400)
                            val bmp = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
                            bmp.eraseColor(android.graphics.Color.WHITE)
                            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            bmp.asImageBitmap()
                        }
                    }
                }.getOrNull()
            }
        }
    }

    val activeChapter = remember(chapters, safePage) {
        chapters.firstOrNull { (safePage + 1) in it.startPage..it.endPage } ?: chapters.firstOrNull()
    }
    val pageNotesForCurrentPage = remember(notes, safePage) {
        notes.filter { it.pageNumber == safePage + 1 }
    }
    val isCurrentPageBookmarked = pageNotesForCurrentPage.any { it.isBookmark }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = StudyQuestStrings.t(uiLanguage, book.titleEn, book.titleBn),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${book.bookCode} • ${book.academicYear}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("reader_back_btn")) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = StudyQuestStrings.t(uiLanguage, "Back", "ফিরে যাও")
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                        modifier = Modifier.testTag("reader_open_pdf_picker")
                    ) {
                        Icon(
                            Icons.Filled.AttachFile,
                            contentDescription = StudyQuestStrings.t(uiLanguage, "Attach local PDF", "লোকাল PDF সংযুক্ত")
                        )
                    }
                    IconButton(
                        onClick = onSwitchBook,
                        modifier = Modifier.testTag("reader_switch_book")
                    ) {
                        Icon(
                            Icons.Filled.SwapHoriz,
                            contentDescription = StudyQuestStrings.t(uiLanguage, "Switch active book", "সক্রিয় বই পরিবর্তন")
                        )
                    }
                    IconButton(
                        onClick = { showReportDialog = true },
                        modifier = Modifier.testTag("reader_report_book")
                    ) {
                        Icon(
                            Icons.Filled.Flag,
                            contentDescription = StudyQuestStrings.t(uiLanguage, "Report issue", "ত্রুটি জানান")
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val statusEnum = TextbookDownloadStatus.entries.firstOrNull {
                it.id == cachedBook?.downloadStatus
            } ?: TextbookDownloadStatus.NOT_DOWNLOADED

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (localPdfFile != null || statusEnum == TextbookDownloadStatus.INSTALLED) {
                                    Icons.Filled.CloudDone
                                } else {
                                    Icons.Filled.Lock
                                },
                                contentDescription = null,
                                tint = if (localPdfFile != null || statusEnum == TextbookDownloadStatus.INSTALLED) {
                                    VividEmerald
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = when {
                                    localPdfFile != null -> StudyQuestStrings.t(
                                        uiLanguage,
                                        "Offline PDF Installed (${cachedBook?.totalPages ?: pageCount} pages)",
                                        "অফলাইন PDF সংরক্ষিত (${cachedBook?.totalPages ?: pageCount} পৃষ্ঠা)"
                                    )
                                    statusEnum == TextbookDownloadStatus.INSTALLED -> StudyQuestStrings.t(
                                        uiLanguage,
                                        "Offline Chapter Pack Ready • Attach PDF anytime",
                                        "অফলাইন অধ্যায় প্যাক প্রস্তুত • চাইলে PDF যুক্ত করো"
                                    )
                                    statusEnum == TextbookDownloadStatus.DOWNLOADING -> StudyQuestStrings.t(
                                        uiLanguage,
                                        "Installing offline pack...",
                                        "অফলাইন প্যাক ইনস্টল হচ্ছে..."
                                    )
                                    else -> StudyQuestStrings.t(
                                        uiLanguage,
                                        "Locked Active Book • Install for offline reading",
                                        "লককৃত সক্রিয় বই • অফলাইন পড়ার জন্য ইনস্টল করো"
                                    )
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilledTonalButton(
                                onClick = { onInstallOffline(statusEnum == TextbookDownloadStatus.INSTALLED) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("reader_install_offline_btn"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = StudyQuestStrings.t(
                                        uiLanguage,
                                        if (statusEnum == TextbookDownloadStatus.INSTALLED) "Refresh" else "Install",
                                        if (statusEnum == TextbookDownloadStatus.INSTALLED) "রিফ্রেশ" else "ইনস্টল"
                                    ),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                    if (statusEnum == TextbookDownloadStatus.DOWNLOADING) {
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { ((cachedBook?.downloadProgress ?: 25).coerceIn(0, 100)) / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(StudyQuestStrings.t(uiLanguage, "Reader", "পড়ার ভিউ")) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(StudyQuestStrings.t(uiLanguage, "Chapters (${chapters.size})", "অধ্যায় (${chapters.size})")) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(StudyQuestStrings.t(uiLanguage, "Notes & Search", "নোট ও অনুসন্ধান")) }
                )
            }

            when (selectedTab) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            if (renderedBitmap != null) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    tonalElevation = 2.dp,
                                    color = Color.White,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(0.72f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .pointerInput(safePage) {
                                            detectTransformGestures { _, _, zoom, _ ->
                                                zoomScale = (zoomScale * zoom).coerceIn(1f, 3f)
                                            }
                                        }
                                ) {
                                    Image(
                                        bitmap = renderedBitmap!!,
                                        contentDescription = StudyQuestStrings.t(
                                            uiLanguage,
                                            "Textbook Page ${safePage + 1}",
                                            "পাঠ্যবই পৃষ্ঠা ${safePage + 1}"
                                        ),
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer(
                                                scaleX = zoomScale,
                                                scaleY = zoomScale
                                            )
                                    )
                                }
                            } else {
                                // Structured offline reader page view when PDF binary is not attached yet
                                StudyQuestCard {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = StudyQuestStrings.t(
                                                    uiLanguage,
                                                    "Page ${safePage + 1} of $pageCount",
                                                    "পৃষ্ঠা ${StudyQuestStrings.formatDigits(safePage + 1, uiLanguage)} / ${StudyQuestStrings.formatDigits(pageCount, uiLanguage)}"
                                                ),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = SkyCyan.copy(alpha = 0.14f)
                                        ) {
                                            Text(
                                                text = if (book.is2027EditionReady) "2027 NCTB Track" else "2026 NCTB Official",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SkyCyan,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    Text(
                                        text = activeChapter?.let {
                                            StudyQuestStrings.t(uiLanguage, it.titleEn, it.titleBn)
                                        } ?: StudyQuestStrings.t(uiLanguage, book.titleEn, book.titleBn),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = activeChapter?.let { ch ->
                                            StudyQuestStrings.t(uiLanguage, ch.studyNotesEn, ch.studyNotesBn)
                                                .ifBlank { StudyQuestStrings.t(uiLanguage, ch.summaryEn, ch.summaryBn) }
                                        } ?: StudyQuestStrings.t(uiLanguage, book.editionLabelEn, book.editionLabelBn),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!activeChapter?.formulasCsv.isNullOrBlank()) {
                                        Spacer(Modifier.height(10.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = VividEmerald.copy(alpha = 0.10f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                    text = StudyQuestStrings.t(uiLanguage, "Key Formulas", "গুরুত্বপূর্ণ সূত্র"),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = VividEmerald
                                                )
                                                Spacer(Modifier.height(4.dp))
                                                activeChapter!!.formulasCsv.split("||").filter { it.isNotBlank() }.forEach { f ->
                                                    Text("• $f", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                                }
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Icon(Icons.Filled.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text(StudyQuestStrings.t(uiLanguage, "Attach Official NCTB PDF", "অফিসিয়াল NCTB PDF যুক্ত করো"))
                                        }
                                        OutlinedButton(
                                            onClick = { runCatching { uriHandler.openUri(book.officialSourceUrl) } },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Icon(Icons.Filled.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text(StudyQuestStrings.t(uiLanguage, "NCTB Portal", "এনসিটিবি পোর্টাল"))
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            StudyQuestCard(accentColor = SkyCyan) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    FilledTonalButton(
                                        onClick = { if (safePage > 0) currentPage = safePage - 1 },
                                        enabled = safePage > 0,
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("reader_prev_page")
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = null)
                                        Text(StudyQuestStrings.t(uiLanguage, "Prev", "আগে"))
                                    }
                                    Text(
                                        text = "${safePage + 1} / $pageCount",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    FilledTonalButton(
                                        onClick = { if (safePage < pageCount - 1) currentPage = safePage + 1 },
                                        enabled = safePage < pageCount - 1,
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("reader_next_page")
                                    ) {
                                        Text(StudyQuestStrings.t(uiLanguage, "Next", "পরে"))
                                        Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = null)
                                    }
                                }
                                if (pageCount > 1) {
                                    Slider(
                                        value = safePage.toFloat(),
                                        onValueChange = { currentPage = it.toInt() },
                                        valueRange = 0f..(pageCount - 1).toFloat()
                                    )
                                }
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (renderedBitmap != null) {
                                        OutlinedButton(
                                            onClick = {
                                                zoomScale = when {
                                                    zoomScale < 1.4f -> 1.5f
                                                    zoomScale < 1.9f -> 2.0f
                                                    else -> 1f
                                                }
                                            },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Icon(Icons.Filled.ZoomIn, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("${"%.1f".format(zoomScale)}x")
                                        }
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            onSavePageNote(
                                                safePage + 1,
                                                activeChapter?.id,
                                                StudyQuestStrings.t(uiLanguage, "Bookmark p.${safePage + 1}", "বুকমার্ক পৃষ্ঠা ${safePage + 1}"),
                                                activeChapter?.let { StudyQuestStrings.t(uiLanguage, it.titleEn, it.titleBn) } ?: book.titleEn,
                                                true
                                            )
                                        },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("reader_bookmark_page")
                                    ) {
                                        Icon(
                                            if (isCurrentPageBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(StudyQuestStrings.t(uiLanguage, "Bookmark", "বুকমার্ক"))
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            noteTitleInput = StudyQuestStrings.t(
                                                uiLanguage,
                                                "Note on Page ${safePage + 1}",
                                                "পৃষ্ঠা ${safePage + 1} নোট"
                                            )
                                            noteBodyInput = ""
                                            showNoteDialog = true
                                        },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(Icons.Filled.NoteAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(StudyQuestStrings.t(uiLanguage, "Add Note", "নোট লেখো"))
                                    }
                                    OutlinedButton(
                                        onClick = { briefingPlayer.playSubjectBriefing(book.subjectId) },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("reader_audio_brief_btn")
                                    ) {
                                        Icon(
                                            imageVector = if (isBriefingPlaying) Icons.Filled.Stop else Icons.Filled.GraphicEq,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            StudyQuestStrings.t(
                                                uiLanguage,
                                                if (isBriefingPlaying) "Stop Audio" else "Audio Brief",
                                                if (isBriefingPlaying) "অডিও থামাও" else "অডিও সারসংক্ষেপ"
                                            )
                                        )
                                    }
                                    if (onOpenStemLab != null && book.subjectId in listOf("math", "physics", "chemistry", "biology", "gen_science")) {
                                        val labId = when (book.subjectId) {
                                            "math" -> "trig"
                                            "physics" -> "physics"
                                            "chemistry" -> "chem"
                                            else -> "bio"
                                        }
                                        OutlinedButton(
                                            onClick = { onOpenStemLab(labId) },
                                            modifier = Modifier
                                                .minimumInteractiveComponentSize()
                                                .testTag("reader_stem_lab_btn")
                                        ) {
                                            Icon(Icons.Filled.Science, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(StudyQuestStrings.t(uiLanguage, "STEM Lab", "STEM ল্যাব"))
                                        }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val chName = activeChapter?.titleEn ?: book.titleEn
                                            onAskAiAboutPage(
                                                "Explain page ${safePage + 1} from ${book.titleEn} ($chName) clearly step by step."
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .minimumInteractiveComponentSize()
                                            .testTag("reader_ask_ai")
                                    ) {
                                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            StudyQuestStrings.t(uiLanguage, "Ask AI Tutor", "AI সাহায্য"),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Button(
                                        onClick = onPracticeBookQuestions,
                                        colors = ButtonDefaults.buttonColors(containerColor = VividEmerald, contentColor = Color.White),
                                        modifier = Modifier
                                            .weight(1f)
                                            .minimumInteractiveComponentSize()
                                            .testTag("reader_practice_book")
                                    ) {
                                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            StudyQuestStrings.t(uiLanguage, "Practice Book", "বই অনুশীলন"),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(chapters, key = { it.id }) { ch ->
                            StudyQuestCard(
                                accentColor = if (ch.completed) VividEmerald else null
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = StudyQuestStrings.t(uiLanguage, ch.titleEn, ch.titleBn),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = StudyQuestStrings.t(
                                                uiLanguage,
                                                "Pages ${ch.startPage}–${ch.endPage}",
                                                "পৃষ্ঠা ${StudyQuestStrings.formatDigits(ch.startPage, uiLanguage)}–${StudyQuestStrings.formatDigits(ch.endPage, uiLanguage)}"
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (ch.completed) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = VividEmerald)
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = StudyQuestStrings.t(uiLanguage, ch.summaryEn, ch.summaryBn),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilledTonalButton(
                                        onClick = {
                                            currentPage = (ch.startPage - 1).coerceAtLeast(0)
                                            selectedTab = 0
                                        },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(Icons.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(StudyQuestStrings.t(uiLanguage, "Read Unit", "পড়ো"))
                                    }
                                    if (!ch.completed) {
                                        OutlinedButton(
                                            onClick = { onMarkChapterCompleted(ch.id) },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(StudyQuestStrings.t(uiLanguage, "Mark Done", "সম্পন্ন"))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    val filteredChapters = remember(chapters, searchQuery) {
                        if (searchQuery.isBlank()) emptyList()
                        else chapters.filter {
                            it.titleEn.contains(searchQuery, ignoreCase = true) ||
                                it.titleBn.contains(searchQuery, ignoreCase = true) ||
                                it.studyNotesEn.contains(searchQuery, ignoreCase = true) ||
                                it.studyNotesBn.contains(searchQuery, ignoreCase = true)
                        }
                    }
                    val filteredNotes = remember(notes, searchQuery) {
                        if (searchQuery.isBlank()) notes
                        else notes.filter {
                            it.title.contains(searchQuery, ignoreCase = true) ||
                                it.noteText.contains(searchQuery, ignoreCase = true)
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text(StudyQuestStrings.t(uiLanguage, "Search chapters & notes", "অধ্যায় ও নোট খুঁজো")) },
                                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (filteredChapters.isNotEmpty()) {
                            item {
                                Text(
                                    text = StudyQuestStrings.t(uiLanguage, "Matching Chapters", "মিলে যাওয়া অধ্যায়"),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            items(filteredChapters, key = { "search_${it.id}" }) { ch ->
                                StudyQuestCard {
                                    Text(
                                        text = StudyQuestStrings.t(uiLanguage, ch.titleEn, ch.titleBn),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = StudyQuestStrings.t(uiLanguage, ch.summaryEn, ch.summaryBn),
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    TextButton(onClick = {
                                        currentPage = (ch.startPage - 1).coerceAtLeast(0)
                                        selectedTab = 0
                                    }) {
                                        Text(StudyQuestStrings.t(uiLanguage, "Jump to page ${ch.startPage}", "পৃষ্ঠা ${ch.startPage}-এ যাও"))
                                    }
                                }
                            }
                        }
                        item {
                            Text(
                                text = StudyQuestStrings.t(uiLanguage, "Bookmarks & Study Notes (${filteredNotes.size})", "বুকমার্ক ও নোট (${filteredNotes.size})"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (filteredNotes.isEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = StudyQuestStrings.t(
                                            uiLanguage,
                                            "No bookmarks or page notes yet. Use Bookmark or Add Note while reading.",
                                            "এখনো কোনো বুকমার্ক বা নোট নেই। পড়ার সময় বুকমার্ক বা নোট যোগ করো।"
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        } else {
                            items(filteredNotes, key = { it.id }) { note ->
                                StudyQuestCard {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(note.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = StudyQuestStrings.t(uiLanguage, "Page ${note.pageNumber}", "পৃষ্ঠা ${note.pageNumber}"),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(onClick = { onDeletePageNote(note.id) }) {
                                            Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete note")
                                        }
                                    }
                                    if (note.noteText.isNotBlank()) {
                                        Spacer(Modifier.height(4.dp))
                                        Text(note.noteText, style = MaterialTheme.typography.bodySmall)
                                    }
                                    TextButton(onClick = {
                                        currentPage = (note.pageNumber - 1).coerceAtLeast(0)
                                        selectedTab = 0
                                    }) {
                                        Text(StudyQuestStrings.t(uiLanguage, "Open Page", "পৃষ্ঠা খোলো"))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text(StudyQuestStrings.t(uiLanguage, "Add Page Note", "পৃষ্ঠায় নোট যোগ")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = noteTitleInput,
                        onValueChange = { noteTitleInput = it },
                        label = { Text(StudyQuestStrings.t(uiLanguage, "Title", "শিরোনাম")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noteBodyInput,
                        onValueChange = { noteBodyInput = it },
                        label = { Text(StudyQuestStrings.t(uiLanguage, "Study Note", "নোট")) },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onSavePageNote(
                        safePage + 1,
                        activeChapter?.id,
                        noteTitleInput.ifBlank { "Page ${safePage + 1}" },
                        noteBodyInput,
                        false
                    )
                    showNoteDialog = false
                }) {
                    Text(StudyQuestStrings.t(uiLanguage, "Save", "সংরক্ষণ"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) {
                    Text(StudyQuestStrings.t(uiLanguage, "Cancel", "বাতিল"))
                }
            }
        )
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text(StudyQuestStrings.t(uiLanguage, "Report Textbook Issue", "পাঠ্যবই ত্রুটি জানান")) },
            text = {
                OutlinedTextField(
                    value = reportDetails,
                    onValueChange = { reportDetails = it },
                    label = { Text(StudyQuestStrings.t(uiLanguage, "Describe issue (edition, missing page, typo)", "সমস্যার বিবরণ লেখো")) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    onReportBookIssue("textbook_feedback", reportDetails.ifBlank { "Book issue on ${book.id}" })
                    reportDetails = ""
                    showReportDialog = false
                }) {
                    Text(StudyQuestStrings.t(uiLanguage, "Submit", "পাঠাও"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text(StudyQuestStrings.t(uiLanguage, "Cancel", "বাতিল"))
                }
            }
        )
    }
}
