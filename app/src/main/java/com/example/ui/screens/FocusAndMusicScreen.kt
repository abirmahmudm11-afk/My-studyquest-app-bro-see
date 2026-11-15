package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.curriculum.GamificationCatalog
import com.example.ui.components.StatBadgePill
import com.example.ui.components.StudyQuestCard
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalStudyQuestColors
import com.example.ui.theme.TealSecondary
import com.example.ui.viewmodel.FocusTimerViewModel
import java.util.Locale

private data class FocusPreset(val id: String, val focusMin: Int, val breakMin: Int, val labelEn: String, val labelBn: String)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FocusAndMusicScreen(
    uiLanguage: AppLanguage,
    classId: String,
    focusVm: FocusTimerViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val liveDisplay by focusVm.liveDisplay.collectAsStateWithLifecycle()
    val sessions by focusVm.studySessions.collectAsStateWithLifecycle()
    val analytics by focusVm.analytics.collectAsStateWithLifecycle()
    val musicState by focusVm.musicState.collectAsStateWithLifecycle()
    val banner by focusVm.bannerMessage.collectAsStateWithLifecycle()

    val availableSubjects = remember(classId) {
        BangladeshCurriculumCatalog.subjectsForClass(classId)
    }
    var selectedSubjectId by remember { mutableStateOf(availableSubjects.firstOrNull()?.id ?: "math") }
    var customMinutes by remember { mutableIntStateOf(25) }
    var breakMinutes by remember { mutableIntStateOf(5) }
    var autoBreak by remember { mutableStateOf(true) }
    var selectedMode by remember { mutableStateOf("pomodoro") }

    val presets = remember {
        listOf(
            FocusPreset("p10", 10, 2, "10m Sprint", "১০ মি."),
            FocusPreset("p25", 25, 5, "25+5 Pomodoro", "২৫+৫"),
            FocusPreset("p45", 45, 8, "45m Deep", "৪৫ মি."),
            FocusPreset("p50", 50, 10, "50+10 Exam", "৫০+১০")
        )
    }

    val timer = liveDisplay.timer
    val semantic = LocalStudyQuestColors.current
    val remaining = if (timer.active) liveDisplay.remainingPhaseSeconds else customMinutes * 60
    val mm = remaining / 60
    val ss = remaining % 60
    val clockText = String.format(Locale.US, "%02d:%02d", mm, ss)
    val ringProgress = if (timer.active) (1f - liveDisplay.progressFraction).coerceIn(0f, 1f) else 1f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("focus_back_btn")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = StudyQuestStrings.t(uiLanguage, "Focus Timer & Ambient Studio", "ফোকাস টাইমার ও মিউজিক স্টুডিও"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = StudyQuestStrings.t(uiLanguage, "Ad-free verified focus time • Survives navigation", "বিজ্ঞাপনমুক্ত যাচাইকৃত ফোকাস সময়"),
                        style = MaterialTheme.typography.labelSmall,
                        color = semantic.success
                    )
                }
            }
        }

        if (!banner.isNullOrBlank()) {
            item {
                StudyQuestCard(containerColor = semantic.successContainer) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(banner.orEmpty(), style = MaterialTheme.typography.bodySmall, color = semantic.onSuccessContainer, modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = { focusVm.clearBanner() }) { Text("OK") }
                    }
                }
            }
        }

        // Analytics summary row
        item {
            StudyQuestCard(modifier = Modifier.testTag("focus_analytics_summary")) {
                Text(
                    text = StudyQuestStrings.t(uiLanguage, "Verified Focus Summary", "যাচাইকৃত ফোকাস হিসাব"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                val weekDiff = analytics.weeklyMinutes - analytics.previousWeekMinutes
                val diffSign = if (weekDiff >= 0) "+" else ""
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatBadgePill(
                        icon = Icons.Filled.Timer,
                        label = StudyQuestStrings.t(uiLanguage, "Today: ${analytics.todayMinutes}m", "আজ: ${StudyQuestStrings.formatDigits(analytics.todayMinutes, uiLanguage)} মি."),
                        tint = semantic.success,
                        containerColor = semantic.successContainer
                    )
                    StatBadgePill(
                        icon = Icons.Filled.Timer,
                        label = StudyQuestStrings.t(uiLanguage, "Week: ${analytics.weeklyMinutes}m ($diffSign${weekDiff}m)", "সপ্তাহ: ${StudyQuestStrings.formatDigits(analytics.weeklyMinutes, uiLanguage)} মি."),
                        tint = CobaltPrimary,
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    StatBadgePill(
                        icon = Icons.Filled.Timer,
                        label = StudyQuestStrings.t(uiLanguage, "Month: ${analytics.monthlyMinutes}m", "মাস: ${StudyQuestStrings.formatDigits(analytics.monthlyMinutes, uiLanguage)} মি."),
                        tint = TealSecondary,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                }
            }
        }

        // Focus Setup & Active Ring
        item {
            StudyQuestCard {
                if (!timer.active) {
                    Text(
                        text = StudyQuestStrings.t(uiLanguage, "Study Subject", "বিষয় নির্বাচন"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        availableSubjects.take(6).forEach { subj ->
                            FilterChip(
                                selected = selectedSubjectId == subj.id,
                                onClick = { selectedSubjectId = subj.id },
                                label = { Text(StudyQuestStrings.t(uiLanguage, subj.titleEn, subj.titleBn)) },
                                modifier = Modifier.testTag("focus_subj_${subj.id}")
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = StudyQuestStrings.t(uiLanguage, "Session Presets & Custom Duration", "সময় প্রিসেট ও কাস্টম সময়"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        presets.forEach { p ->
                            FilterChip(
                                selected = customMinutes == p.focusMin && breakMinutes == p.breakMin,
                                onClick = {
                                    customMinutes = p.focusMin
                                    breakMinutes = p.breakMin
                                    selectedMode = if (p.breakMin > 0) "pomodoro" else "deep_focus"
                                },
                                label = { Text(StudyQuestStrings.t(uiLanguage, p.labelEn, p.labelBn)) },
                                modifier = Modifier.testTag("focus_preset_${p.id}")
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilledTonalIconButton(
                                onClick = { if (customMinutes > 5) customMinutes -= 5 },
                                modifier = Modifier.minimumInteractiveComponentSize().testTag("focus_dec_min")
                            ) { Icon(Icons.Filled.Remove, contentDescription = "Decrease 5 minutes") }
                            Text(
                                text = StudyQuestStrings.t(uiLanguage, "$customMinutes min", "${StudyQuestStrings.formatDigits(customMinutes, uiLanguage)} মিনিট"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            FilledTonalIconButton(
                                onClick = { if (customMinutes < 120) customMinutes += 5 },
                                modifier = Modifier.minimumInteractiveComponentSize().testTag("focus_inc_min")
                            ) { Icon(Icons.Filled.Add, contentDescription = "Increase 5 minutes") }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(StudyQuestStrings.t(uiLanguage, "Auto-break", "স্বয়ংক্রিয় বিরতি"), style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.width(6.dp))
                            Switch(checked = autoBreak, onCheckedChange = { autoBreak = it })
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val trackColor = MaterialTheme.colorScheme.surfaceVariant
                    val activeRingColor = when {
                        timer.active && timer.phase == "break" -> TealSecondary
                        timer.active && timer.isRunning -> semantic.success
                        else -> CobaltPrimary
                    }
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(204.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke = 14.dp.toPx()
                            drawArc(
                                color = trackColor,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = activeRingColor,
                                startAngle = -90f,
                                sweepAngle = 360f * ringProgress,
                                useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = StudyQuestStrings.formatDigits(clockText, uiLanguage),
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when {
                                    !timer.active -> StudyQuestStrings.t(uiLanguage, "Ready to Focus", "শুরু করতে প্রস্তুত")
                                    timer.phase == "break" -> StudyQuestStrings.t(uiLanguage, "Break Interval", "বিরতির সময়")
                                    timer.isRunning -> StudyQuestStrings.t(uiLanguage, "Deep Focus Active", "গভীর মনোযোগ চলমান")
                                    else -> StudyQuestStrings.t(uiLanguage, "Paused", "বিরতিতে")
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = activeRingColor
                            )
                            if (timer.active) {
                                Text(
                                    text = StudyQuestStrings.t(
                                        uiLanguage,
                                        "Elapsed: ${liveDisplay.elapsedFocusSeconds / 60}m ${liveDisplay.elapsedFocusSeconds % 60}s",
                                        "অতিবাহিত: ${liveDisplay.elapsedFocusSeconds / 60} মি."
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!timer.active) {
                            Button(
                                onClick = {
                                    focusVm.startFocusSession(
                                        subjectId = selectedSubjectId,
                                        mode = selectedMode,
                                        focusMinutes = customMinutes,
                                        breakMinutes = if (autoBreak) breakMinutes else 0,
                                        autoBreak = autoBreak
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = semantic.success),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("focus_start_btn")
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text(StudyQuestStrings.t(uiLanguage, "Start Focus Session", "ফোকাস শুরু করো"))
                            }
                        } else {
                            if (timer.isRunning) {
                                FilledTonalButton(
                                    onClick = { focusVm.pauseSession() },
                                    modifier = Modifier.minimumInteractiveComponentSize().testTag("focus_pause_btn")
                                ) {
                                    Icon(Icons.Filled.Pause, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text(StudyQuestStrings.t(uiLanguage, "Pause", "বিরতি"))
                                }
                            } else {
                                Button(
                                    onClick = { focusVm.resumeSession() },
                                    colors = ButtonDefaults.buttonColors(containerColor = semantic.success),
                                    modifier = Modifier.minimumInteractiveComponentSize().testTag("focus_resume_btn")
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text(StudyQuestStrings.t(uiLanguage, "Resume", "চালিয়ে যাও"))
                                }
                            }

                            if (timer.phase == "break") {
                                FilledTonalButton(
                                    onClick = { focusVm.skipBreakAndFinish() },
                                    modifier = Modifier.minimumInteractiveComponentSize().testTag("focus_skip_break_btn")
                                ) {
                                    Text(StudyQuestStrings.t(uiLanguage, "Skip Break & Finish", "বিরতি বাদ দিয়ে শেষ"))
                                }
                            }

                            Button(
                                onClick = { focusVm.stopAndSaveSession() },
                                modifier = Modifier.minimumInteractiveComponentSize().testTag("focus_stop_save_btn")
                            ) {
                                Icon(Icons.Filled.Stop, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text(StudyQuestStrings.t(uiLanguage, "Stop & Verify XP", "শেষ ও XP সংগ্রহ"))
                            }

                            OutlinedButton(
                                onClick = { focusVm.resetTimer() },
                                modifier = Modifier.minimumInteractiveComponentSize().testTag("focus_reset_btn")
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text(StudyQuestStrings.t(uiLanguage, "Discard", "বাতিল"))
                            }
                        }
                    }
                }
            }
        }

        // Ambient Focus Music Player
        item {
            StudyQuestCard(modifier = Modifier.testTag("ambient_music_card")) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.MusicNote, contentDescription = null, tint = CobaltPrimary)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = StudyQuestStrings.t(uiLanguage, "Ambient Focus Music", "অ্যাম্বিয়েন্ট ফোকাস মিউজিক"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = StudyQuestStrings.t(
                                uiLanguage,
                                musicState.currentTrack.titleEn,
                                musicState.currentTrack.titleBn
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = CobaltPrimary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (musicState.isPlaying) semantic.successContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (musicState.isPlaying) StudyQuestStrings.t(uiLanguage, "Playing", "চলছে")
                            else StudyQuestStrings.t(uiLanguage, "Paused", "বিরতি"),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (musicState.isPlaying) semantic.onSuccessContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "focus" to StudyQuestStrings.t(uiLanguage, "Focus", "ফোকাস"),
                        "relaxing" to StudyQuestStrings.t(uiLanguage, "Relaxing", "প্রশান্ত"),
                        "background" to StudyQuestStrings.t(uiLanguage, "Background", "পটভূমি")
                    ).forEach { (cat, label) ->
                        FilterChip(
                            selected = musicState.selectedCategory == cat,
                            onClick = { focusVm.selectMusicCategory(cat) },
                            label = { Text(label) },
                            modifier = Modifier.testTag("music_cat_$cat")
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))
                val catTracks = remember(musicState.selectedCategory) {
                    GamificationCatalog.focusMusicTracks.filter { it.category == musicState.selectedCategory }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    catTracks.forEach { t ->
                        FilterChip(
                            selected = musicState.currentTrack.id == t.id,
                            onClick = { focusVm.selectMusicTrack(t) },
                            label = { Text(StudyQuestStrings.t(uiLanguage, t.titleEn, t.titleBn)) }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalIconButton(
                            onClick = { focusVm.previousMusicTrack() },
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("music_prev_btn")
                        ) { Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous track") }

                        Button(
                            onClick = { focusVm.toggleMusicPlayPause() },
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("music_play_pause_btn")
                        ) {
                            Icon(
                                if (musicState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(if (musicState.isPlaying) StudyQuestStrings.t(uiLanguage, "Pause", "থামাও") else StudyQuestStrings.t(uiLanguage, "Play", "বাজাও"))
                        }

                        FilledTonalIconButton(
                            onClick = { focusVm.nextMusicTrack() },
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("music_next_btn")
                        ) { Icon(Icons.Filled.SkipNext, contentDescription = "Next track") }
                    }
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    text = StudyQuestStrings.t(uiLanguage, "Volume: ${(musicState.volume * 100).toInt()}%", "ভলিউম: ${(musicState.volume * 100).toInt()}%"),
                    style = MaterialTheme.typography.labelSmall
                )
                Slider(
                    value = musicState.volume,
                    onValueChange = { focusVm.setMusicVolume(it) },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = StudyQuestStrings.t(uiLanguage, "Keep playing during break intervals", "বিরতির সময়ও মিউজিক চালু রাখো"),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Switch(
                        checked = musicState.musicDuringBreak,
                        onCheckedChange = { focusVm.setMusicDuringBreak(it) }
                    )
                }
            }
        }

        // Verified Sessions Log
        if (sessions.isNotEmpty()) {
            item {
                Text(
                    text = StudyQuestStrings.t(uiLanguage, "Verified Focus History", "যাচাইকৃত ফোকাস ইতিহাস"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(sessions.take(10), key = { it.id }) { s ->
                val mins = (s.focusedSeconds / 60L).coerceAtLeast(1L)
                StudyQuestCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${s.subjectId.uppercase(Locale.ROOT)} • ${mins}m (${s.mode})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = StudyQuestStrings.t(uiLanguage, "Status: ${s.status} • Paused: ${s.pausedSeconds}s", "অবস্থা: ${s.status}"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatBadgePill(
                            icon = Icons.Filled.Timer,
                            label = "+${s.xpAwarded} XP",
                            tint = semantic.success,
                            containerColor = semantic.successContainer
                        )
                    }
                }
            }
        }
    }
}
