package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AnnouncementLog
import com.example.ui.SpeakingClockViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.util.TimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    SpeakingClockScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Helper function to get localized strings based on selected uiLanguage
fun getLocalizedString(key: String, lang: String): String {
    val en = mapOf(
        "app_title" to "Vacha Time",
        "app_subtitle" to "Accessibility Speaking Service",
        "narrator_status" to "Narrator Status",
        "status_active" to "Active & Monitoring",
        "status_inactive" to "Service inactive - Tap to start",
        "wake_talk" to "Wake Talk",
        "wake_desc" to "Speak on power tap",
        "enabled" to "ENABLED",
        "disabled" to "DISABLED",
        "idle_alerts" to "Idle Alerts",
        "idle_desc" to "Periodic intervals",
        "every" to "Every",
        "minutes" to "m",
        "off" to "Off",
        "voice_engine" to "Voice Engine Settings",
        "voice_desc" to "Voice selection & speech rate",
        "voice_selection" to "Hindi Voice Selection",
        "voice_auto" to "Default Female Voice (Auto)",
        "rate" to "Rate",
        "pitch" to "Pitch",
        "speak_now" to "Speak Now",
        "test_speak" to "Test Speak",
        "battery_fix" to "Battery Fix",
        "fix_alerts" to "Fix Alerts",
        "history_logs" to "History Logs",
        "no_history" to "No announcement history yet.",
        "click_test" to "Click 'Speak Now' to hear the time.",
        "screen_wake" to "Screen Wake",
        "time_interval" to "Time Interval",
        "test_play" to "Test Play",
        "spoken_success" to "Successfully spoken",
        "spoken_failure" to "Failed"
    )

    val hi = mapOf(
        "app_title" to "वाचा टाइम",
        "app_subtitle" to "पहुंच-योग्यता स्पीकिंग सर्विस",
        "narrator_status" to "कथावाचक स्थिति",
        "status_active" to "सक्रिय और समय की निगरानी कर रहा है",
        "status_inactive" to "सेवा बंद है - चालू करने के लिए टैप करें",
        "wake_talk" to "वेक टॉक",
        "wake_desc" to "पावर टैप पर बोलें",
        "enabled" to "सक्रिय",
        "disabled" to "निष्क्रिय",
        "idle_alerts" to "अंतराल अलर्ट",
        "idle_desc" to "नियमित अंतराल",
        "every" to "प्रत्येक",
        "minutes" to " मिनट",
        "off" to "बंद",
        "voice_engine" to "आवाज सेटिंग्स",
        "voice_desc" to "आवाज चयन और बोलने की गति",
        "voice_selection" to "हिंदी आवाज का चयन",
        "voice_auto" to "डिफ़ॉल्ट महिला आवाज (ऑटो-डिटेक्ट)",
        "rate" to "गति (Rate)",
        "pitch" to "पिच (Pitch)",
        "speak_now" to "अभी सुनें",
        "test_speak" to "टेस्ट प्ले",
        "battery_fix" to "बैटरी फिक्स",
        "fix_alerts" to "अलर्ट्स ठीक करें",
        "history_logs" to "घोषणा इतिहास",
        "no_history" to "अभी तक कोई इतिहास नहीं है।",
        "click_test" to "समय सुनने के लिए 'अभी सुनें' पर क्लिक करें।",
        "screen_wake" to "स्क्रीन वेक",
        "time_interval" to "समय अंतराल",
        "test_play" to "टेस्ट प्ले",
        "spoken_success" to "सफलतापूर्वक बोला गया",
        "spoken_failure" to "विफलता"
    )

    return if (lang == "hi") hi[key] ?: en[key] ?: key else en[key] ?: key
}

@Composable
fun LanguagePill(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakingClockScreen(
    modifier: Modifier = Modifier,
    viewModel: SpeakingClockViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // Viewmodel States
    val isServiceActive by viewModel.isServiceActive.collectAsStateWithLifecycle()
    val screenWakeEnabled by viewModel.screenWakeEnabled.collectAsStateWithLifecycle()
    val intervalMinutes by viewModel.intervalMinutes.collectAsStateWithLifecycle()
    val speechRate by viewModel.speechRate.collectAsStateWithLifecycle()
    val speechPitch by viewModel.speechPitch.collectAsStateWithLifecycle()
    val selectedVoiceName by viewModel.selectedVoiceName.collectAsStateWithLifecycle()
    val availableVoices by viewModel.availableVoices.collectAsStateWithLifecycle()
    val historyLogs by viewModel.historyLogs.collectAsStateWithLifecycle()
    val uiLanguage by viewModel.uiLanguage.collectAsStateWithLifecycle()

    // Notification Permission Launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.toggleService(context)
            } else {
                val toastMsg = if (uiLanguage == "hi") {
                    "नोटिफिकेशन की अनुमति आवश्यक है ताकि सर्विस बैकग्राउंड में चल सके।"
                } else {
                    "Notification permission required to run service in the background."
                }
                Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show()
            }
        }
    )

    // Ticking Live Time State
    var liveTimeText by remember { mutableStateOf("") }
    var liveHindiTimeText by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        while (isActive) {
            val now = Calendar.getInstance().time
            liveTimeText = timeFormat.format(now).uppercase()
            liveHindiTimeText = TimeFormatter.getCurrentTimeInHindi()
            delay(1000L)
        }
    }

    LazyColumn(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 32.dp, top = 16.dp)
    ) {
        // --- BENTO GRID HEADER SECTION ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = getLocalizedString("app_title", uiLanguage),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = getLocalizedString("app_subtitle", uiLanguage),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Beautiful Bento Pill Language Switcher
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    LanguagePill(selected = uiLanguage == "en", label = "EN") { viewModel.updateUiLanguage("en") }
                    LanguagePill(selected = uiLanguage == "hi", label = "हिं") { viewModel.updateUiLanguage("hi") }
                }
            }
        }

        // --- BENTO GRID TILE 1: Narrator Status & Live Speech Subtitle (col-span-2) ---
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("service_main_card")
                    .clickable {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                            
                            if (hasPermission) {
                                viewModel.toggleService(context)
                            } else {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            viewModel.toggleService(context)
                        }
                    }
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = getLocalizedString("narrator_status", uiLanguage),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (isServiceActive) {
                                    getLocalizedString("status_active", uiLanguage)
                                } else {
                                    getLocalizedString("status_inactive", uiLanguage)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        
                        // Custom toggle widget
                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .height(32.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isServiceActive) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                                )
                                .padding(4.dp),
                            contentAlignment = if (isServiceActive) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }

                    // Ticking Live Clock Preview inside Bento Status block
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color.White.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = liveTimeText.ifEmpty { "00:00:00 --" },
                            style = MaterialTheme.typography.headlineLarge,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "Speaking indicator",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = liveHindiTimeText.ifEmpty {
                                    if (uiLanguage == "hi") "समय का अनुवाद किया जा रहा है..." else "Translating current time..."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // --- BENTO GRID TILE 2 & 3: Screen Wake Talk (1 col) & Idle Alerts (1 col) ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // TILE 2: Wake Talk
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (screenWakeEnabled) MaterialTheme.colorScheme.surfaceVariant
                                        else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(150.dp)
                        .testTag("wake_screen_tile")
                        .clickable { viewModel.updateScreenWakeEnabled(context, !screenWakeEnabled) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TapAndPlay,
                                contentDescription = "Tap to Play",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = getLocalizedString("wake_talk", uiLanguage),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = getLocalizedString("wake_desc", uiLanguage),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (screenWakeEnabled) Color(0xFF4CAF50) else Color.Gray)
                            )
                            Text(
                                text = if (screenWakeEnabled) {
                                    getLocalizedString("enabled", uiLanguage)
                                } else {
                                    getLocalizedString("disabled", uiLanguage)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (screenWakeEnabled) Color(0xFF4CAF50) else Color.Gray,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // TILE 3: Idle Alerts
                val currentIntervalLabel = if (intervalMinutes > 0) {
                    "${getLocalizedString("every", uiLanguage)} ${intervalMinutes}${getLocalizedString("minutes", uiLanguage)}"
                } else {
                    getLocalizedString("off", uiLanguage)
                }
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(150.dp)
                        .testTag("idle_alerts_tile")
                        .clickable {
                            val nextInterval = when (intervalMinutes) {
                                0 -> 10
                                10 -> 15
                                15 -> 30
                                30 -> 60
                                else -> 0
                            }
                            viewModel.updateInterval(context, nextInterval)
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Schedule interval",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = getLocalizedString("idle_alerts", uiLanguage),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = getLocalizedString("idle_desc", uiLanguage),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        // Horizontal alert bar
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = currentIntervalLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            LinearProgressIndicator(
                                progress = {
                                    if (intervalMinutes > 0) (intervalMinutes / 60f).coerceIn(0.1f, 1.0f)
                                    else 0f
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            )
                        }
                    }
                }
            }
        }

        // --- BENTO GRID TILE 4: Hindi Voice Engine Control (col-span-2) ---
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SettingsSuggest,
                                    contentDescription = "Voice parameters",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = getLocalizedString("voice_engine", uiLanguage),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = getLocalizedString("voice_desc", uiLanguage),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    // Voice Picker Dropdown
                    Column {
                        Text(
                            text = getLocalizedString("voice_selection", uiLanguage),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        var voiceDropdownExpanded by remember { mutableStateOf(false) }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { voiceDropdownExpanded = true }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = selectedVoiceName.ifEmpty { getLocalizedString("voice_auto", uiLanguage) },
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Expand Voices"
                                )
                            }

                            DropdownMenu(
                                expanded = voiceDropdownExpanded,
                                onDismissRequest = { voiceDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(getLocalizedString("voice_auto", uiLanguage)) },
                                    onClick = {
                                        viewModel.updateSelectedVoice(context, "")
                                        voiceDropdownExpanded = false
                                    }
                                )
                                availableVoices.forEach { voice ->
                                    DropdownMenuItem(
                                        text = { Text(voice) },
                                        onClick = {
                                            viewModel.updateSelectedVoice(context, voice)
                                            voiceDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Pitch & Speech Rate sliders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = getLocalizedString("rate", uiLanguage),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = String.format("%.1fx", speechRate),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = speechRate,
                                onValueChange = { viewModel.updateSpeechRate(context, it) },
                                valueRange = 0.5f..2.0f,
                                modifier = Modifier.testTag("speech_rate_slider")
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = getLocalizedString("pitch", uiLanguage),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = String.format("%.1fx", speechPitch),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = speechPitch,
                                onValueChange = { viewModel.updateSpeechPitch(context, it) },
                                valueRange = 0.5f..2.0f,
                                modifier = Modifier.testTag("speech_pitch_slider")
                            )
                        }
                    }
                }
            }
        }

        // --- BENTO GRID TILE 5 & 6: Action Buttons ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // TILE 5: Speak Now Action
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .testTag("speak_now_button")
                        .clickable { viewModel.testSpeech(context) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Speak test time",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = getLocalizedString("speak_now", uiLanguage),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = getLocalizedString("test_speak", uiLanguage),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // TILE 6: Battery Optimization Settings
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .clickable {
                            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open battery settings directly", Toast.LENGTH_SHORT).show()
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BatteryChargingFull,
                                contentDescription = "Battery info",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = getLocalizedString("battery_fix", uiLanguage),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = getLocalizedString("fix_alerts", uiLanguage),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }

        // --- BENTO GRID ANNOUNCEMENT HISTORY BLOCK ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getLocalizedString("history_logs", uiLanguage),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (historyLogs.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearLogs() },
                        modifier = Modifier.testTag("clear_logs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Logs",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        if (historyLogs.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = "No History",
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = getLocalizedString("no_history", uiLanguage),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = getLocalizedString("click_test", uiLanguage),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(historyLogs) { log ->
                LogItemRow(log = log, uiLanguage = uiLanguage, onDelete = { viewModel.deleteLog(log.id) })
            }
        }
    }
}

@Composable
fun LogItemRow(
    log: AnnouncementLog,
    uiLanguage: String,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("hh:mm a (dd MMM)", Locale.getDefault())
    val formattedTime = sdf.format(Date(log.timestamp))

    val triggerIcon = when (log.triggerType) {
        "SCREEN_WAKE" -> Icons.Default.LockOpen
        "INTERVAL" -> Icons.Default.Alarm
        else -> Icons.Default.VolumeUp
    }

    val triggerLabel = when (log.triggerType) {
        "SCREEN_WAKE" -> getLocalizedString("screen_wake", uiLanguage)
        "INTERVAL" -> getLocalizedString("time_interval", uiLanguage)
        else -> getLocalizedString("test_play", uiLanguage)
    }

    val statusLabel = if (log.status.startsWith("SUCCESS")) {
        getLocalizedString("spoken_success", uiLanguage)
    } else {
        getLocalizedString("spoken_failure", uiLanguage)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("log_item_${log.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = triggerIcon,
                    contentDescription = triggerLabel,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = triggerLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.textSpoken,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (log.status.startsWith("SUCCESS")) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete Log",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
