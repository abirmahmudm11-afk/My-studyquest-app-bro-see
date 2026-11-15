package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.local.LocalStudyCircleNoteEntity
import com.example.data.model.PublicProfile
import com.example.ui.components.MascotAvatar
import com.example.ui.components.PulseLiveDot
import com.example.ui.components.StaggeredEntrance
import com.example.ui.components.StudyQuestCard
import com.example.ui.components.StudyQuestGradientHeroBanner
import com.example.ui.theme.CobaltPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GuestStudyCircleContent(
    myProfile: PublicProfile,
    uiLanguage: AppLanguage,
    reducedMotion: Boolean,
    selectedChannelId: String,
    notes: List<LocalStudyCircleNoteEntity>,
    isAiReplying: Boolean,
    onSelectChannel: (String) -> Unit,
    onPostNote: (text: String, askAiBuddy: Boolean) -> Unit,
    onToggleReaction: (LocalStudyCircleNoteEntity, String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onRequestGoogleSignIn: () -> Unit
) {
    var noteInput by remember { mutableStateOf("") }
    var askAiBuddy by remember { mutableStateOf(true) }

    val channels = listOf(
        "ssc_math" to StudyQuestStrings.t(uiLanguage, "Math & Formulas", "গণিত ও সূত্র"),
        "science_lab" to StudyQuestStrings.t(uiLanguage, "Physics & Science", "পদার্থ ও বিজ্ঞান"),
        "english_prep" to StudyQuestStrings.t(uiLanguage, "English & ICT", "ইংরেজি ও আইসিটি")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                StaggeredEntrance(index = 0, reducedMotion = reducedMotion) {
                    StudyQuestGradientHeroBanner(
                        eyebrow = StudyQuestStrings.t(uiLanguage, "STUDY CIRCLE + TITU AI BUDDY", "স্টাডি সার্কেল + টিটু AI বন্ধু"),
                        title = StudyQuestStrings.t(uiLanguage, "Study Notes & AI Circle", "নোট ও AI সহায়তা সার্কেল"),
                        subtitle = StudyQuestStrings.t(
                            uiLanguage,
                            "Save subject formula notes locally and ask Titu AI Buddy for instant step-by-step explanations.",
                            "বিষয়ভিত্তিক নোট সংরক্ষণ করো এবং টিটু AI বন্ধুর কাছ থেকে তাৎক্ষণিক ব্যাখ্যা নাও।"
                        ),
                        reducedMotion = reducedMotion,
                        trailingSlot = { MascotAvatar(size = 52.dp) }
                    )
                }
            }

            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    channels.forEach { (id, label) ->
                        FilterChip(
                            selected = selectedChannelId == id,
                            onClick = { onSelectChannel(id) },
                            label = { Text(label) },
                            modifier = Modifier.testTag("circle_channel_$id")
                        )
                    }
                }
            }

            items(notes, key = { it.id }) { note ->
                val isAi = note.isAiBuddy
                StudyQuestCard(
                    containerColor = if (isAi) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                    else MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isAi) {
                                PulseLiveDot(reducedMotion = reducedMotion)
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = CobaltPrimary, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = note.authorLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isAi) CobaltPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (!isAi) {
                            IconButton(
                                onClick = { onDeleteNote(note.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(text = note.text, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        val activeReactions = note.reactionsCsv.split(",").filter { it.isNotBlank() }.toSet()
                        listOf("📌", "🔥", "💡").forEach { emoji ->
                            val active = emoji in activeReactions
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                TextButton(
                                    onClick = { onToggleReaction(note, emoji) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Text(if (active) "$emoji ✓" else emoji, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }

            if (isAiReplying) {
                item {
                    StudyQuestCard(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Text(
                                text = StudyQuestStrings.t(uiLanguage, "Titu AI Buddy is writing a step-by-step reply...", "টিটু AI বন্ধু উত্তর লিখছে..."),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            item {
                StudyQuestCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = CobaltPrimary)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = StudyQuestStrings.t(uiLanguage, "Want live classmate chat?", "সহপাঠীদের সাথে লাইভ চ্যাট করতে চাও?"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = StudyQuestStrings.t(
                                    uiLanguage,
                                    "Sign in with Google to get your unique StudyQuest ID, add friends & join real Study Groups.",
                                    "গুগল সাইন-ইন করে বন্ধু যুক্ত করো ও স্টাডি গ্রুপে অংশ নাও।"
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Button(onClick = onRequestGoogleSignIn) {
                            Text(StudyQuestStrings.t(uiLanguage, "Sign In", "সাইন-ইন"))
                        }
                    }
                }
            }
        }

        Surface(tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = CobaltPrimary, modifier = Modifier.size(16.dp))
                        Text(
                            text = StudyQuestStrings.t(uiLanguage, "Ask Titu AI Buddy to reply", "টিটু AI বন্ধুর উত্তর নিন"),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Switch(checked = askAiBuddy, onCheckedChange = { askAiBuddy = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        placeholder = {
                            Text(
                                StudyQuestStrings.t(
                                    uiLanguage,
                                    if (askAiBuddy) "Ask a topic doubt or save a note..." else "Save a quick revision note...",
                                    if (askAiBuddy) "পড়ার প্রশ্ন লেখো বা নোট রাখো..." else "একটি নোট লেখো..."
                                )
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("circle_note_input")
                    )
                    Button(
                        onClick = {
                            if (noteInput.isNotBlank()) {
                                onPostNote(noteInput, askAiBuddy)
                                noteInput = ""
                            }
                        },
                        enabled = noteInput.isNotBlank() && !isAiReplying,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("circle_note_send_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}

@Composable
fun GuestSocialFeatureGateCard(
    titleEn: String,
    titleBn: String,
    subtitleEn: String,
    subtitleBn: String,
    uiLanguage: AppLanguage,
    reducedMotion: Boolean,
    onSignInClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        StaggeredEntrance(index = 0, reducedMotion = reducedMotion) {
            StudyQuestGradientHeroBanner(
                eyebrow = StudyQuestStrings.t(uiLanguage, "SAFE STUDENT NETWORK", "নিরাপদ শিক্ষার্থী নেটওয়ার্ক"),
                title = StudyQuestStrings.t(uiLanguage, titleEn, titleBn),
                subtitle = StudyQuestStrings.t(uiLanguage, subtitleEn, subtitleBn),
                reducedMotion = reducedMotion,
                trailingSlot = {
                    Icon(
                        Icons.Filled.Forum,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            )
        }
        StaggeredEntrance(index = 1, reducedMotion = reducedMotion) {
            StudyQuestCard {
                Text(
                    text = StudyQuestStrings.t(
                        uiLanguage,
                        "Real Peer Connections Only — Zero Bots",
                        "শুধুমাত্র প্রকৃত সহপাঠী — কোনো বট নয়"
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = StudyQuestStrings.t(
                        uiLanguage,
                        "To protect student safety, discovering classmates, friend requests, private 1v1 chats, and Study Groups require a verified Google account. Your local Guest XP & notes will automatically merge when you sign in!",
                        "শিক্ষার্থীদের নিরাপত্তার জন্য বন্ধু খোঁজা, ব্যক্তিগত চ্যাট ও স্টাডি গ্রুপে যুক্ত হতে গুগল সাইন-ইন প্রয়োজন। সাইন-ইন করলেই গেস্ট মোডের সব অগ্রগতি স্বয়ংক্রিয়ভাবে সিঙ্ক হবে!"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onSignInClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("guest_social_signin_btn")
                ) {
                    Icon(Icons.Filled.CloudUpload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(StudyQuestStrings.t(uiLanguage, "Sign In with Google & Auto-Merge", "গুগল দিয়ে সাইন-ইন ও সিঙ্ক করুন"))
                }
            }
        }
    }
}
