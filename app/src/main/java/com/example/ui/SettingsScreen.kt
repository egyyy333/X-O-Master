package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sound.GameSoundPlayer

@Composable
fun SettingsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()
    val isHapticEnabled by viewModel.isHapticEnabled.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()

    val haptic = LocalHapticFeedback.current

    var showResetDialog by remember { mutableStateOf(false) }

    fun triggerVibration() {
        if (isHapticEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (appTheme == "LIGHT") Color(0xFFE2E8F0) else Color(0xFF1E293B),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = if (appTheme == "LIGHT") Color(0xFF0F172A) else Color(0xFF00E5FF)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = Localizer.get("tab_settings"),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (appTheme == "LIGHT") Color(0xFF0F172A) else Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val cardBgColor = if (appTheme == "LIGHT") Color.White else Color(0xFF121829)
        val cardBorderColor = if (appTheme == "LIGHT") Color(0xFFE2E8F0) else Color(0xFF1E293B)
        val textColorPrimary = if (appTheme == "LIGHT") Color(0xFF0F172A) else Color.White
        val textColorSecondary = if (appTheme == "LIGHT") Color(0xFF475569) else Color(0xFF94A3B8)

        // 1. SOUND SETTINGS CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            border = BorderStroke(1.dp, cardBorderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = if (isSoundEnabled) Color(0xFF00E5FF) else Color(0xFF64748B),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = Localizer.get("settings_sound"),
                            color = textColorPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = Localizer.get("settings_sound_desc"),
                            color = textColorSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = isSoundEnabled,
                    onCheckedChange = {
                        viewModel.setSoundEnabled(it)
                        GameSoundPlayer.playTone(880.0, 50, "PLUCK")
                        triggerVibration()
                    },
                    modifier = Modifier.testTag("sound_toggle_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00E5FF),
                        checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.35f),
                        uncheckedThumbColor = Color(0xFF64748B),
                        uncheckedTrackColor = Color(0xFF1E293B)
                    )
                )
            }
        }

        // 2. HAPTIC FEEDBACK CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            border = BorderStroke(1.dp, cardBorderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = null,
                        tint = if (isHapticEnabled) Color(0xFFFF2D55) else Color(0xFF64748B),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = Localizer.get("settings_haptic"),
                            color = textColorPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = Localizer.get("settings_haptic_desc"),
                            color = textColorSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = isHapticEnabled,
                    onCheckedChange = {
                        viewModel.setHapticEnabled(it)
                        GameSoundPlayer.playClick()
                        if (it) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    },
                    modifier = Modifier.testTag("haptic_toggle_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFFF2D55),
                        checkedTrackColor = Color(0xFFFF2D55).copy(alpha = 0.35f),
                        uncheckedThumbColor = Color(0xFF64748B),
                        uncheckedTrackColor = Color(0xFF1E293B)
                    )
                )
            }
        }

        // 3. THEME SELECTION CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            border = BorderStroke(1.dp, cardBorderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = Localizer.get("settings_theme"),
                        color = textColorPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOptionButton(
                        label = Localizer.get("settings_theme_fantasy"),
                        isSelected = appTheme == "COSMIC_FANTASY",
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setAppTheme("COSMIC_FANTASY")
                            GameSoundPlayer.playClick()
                            triggerVibration()
                        }
                    )
                    ThemeOptionButton(
                        label = Localizer.get("settings_theme_dark"),
                        isSelected = appTheme == "DARK",
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setAppTheme("DARK")
                            GameSoundPlayer.playClick()
                            triggerVibration()
                        }
                    )
                    ThemeOptionButton(
                        label = Localizer.get("settings_theme_light"),
                        isSelected = appTheme == "LIGHT",
                        color = Color(0xFF64748B),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setAppTheme("LIGHT")
                            GameSoundPlayer.playClick()
                            triggerVibration()
                        }
                    )
                }
            }
        }

        // 4. LANGUAGE SELECTION CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            border = BorderStroke(1.dp, cardBorderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = Color(0xFF4ADE80),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = Localizer.get("settings_lang"),
                        color = textColorPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LangOptionButton(
                        label = "العربية",
                        isSelected = appLanguage == "AR",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setAppLanguage("AR")
                            GameSoundPlayer.playClick()
                            triggerVibration()
                        }
                    )
                    LangOptionButton(
                        label = "English",
                        isSelected = appLanguage == "EN",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setAppLanguage("EN")
                            GameSoundPlayer.playClick()
                            triggerVibration()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. WIPE DATA CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            border = BorderStroke(1.dp, cardBorderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1.2f)) {
                    Text(
                        text = Localizer.get("settings_reset"),
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = Localizer.get("settings_reset_desc"),
                        color = textColorSecondary,
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = {
                        GameSoundPlayer.playTone(329.63, 100, "SWEEP")
                        triggerVibration()
                        showResetDialog = true
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFFEF4444).copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f), CircleShape)
                        .testTag("deep_wipe_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = Color(0xFFEF4444)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(120.dp)) // Floating Bottom navigation padding

        // RESET CONFIRMATION DIALOG
        AnimatedVisibility(
            visible = showResetDialog,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .clickable { showResetDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .widthIn(max = 350.dp)
                        .padding(24.dp)
                        .clickable(enabled = false) {}, // prevent click-through
                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                    border = BorderStroke(1.dp, cardBorderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = Localizer.get("confirm_reset_title"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = Localizer.get("confirm_reset_desc"),
                            fontSize = 13.sp,
                            color = textColorSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    showResetDialog = false
                                    GameSoundPlayer.playClick()
                                    triggerVibration()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (appTheme == "LIGHT") Color(0xFFE2E8F0) else Color(0xFF1E293B),
                                    contentColor = textColorPrimary
                                )
                            ) {
                                Text(
                                    text = Localizer.get("cancel"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    showResetDialog = false
                                    viewModel.wipeAllData()
                                    GameSoundPlayer.playTone(261.63, 200, "SWEEP")
                                    triggerVibration()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEF4444),
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = Localizer.get("confirm"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeOptionButton(
    label: String,
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent
            )
            .border(
                BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) color else Color(0xFF2D3748)
                ),
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) color else Color(0xFF718096),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LangOptionButton(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) Color(0xFF4ADE80).copy(alpha = 0.15f) else Color.Transparent
            )
            .border(
                BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) Color(0xFF4ADE80) else Color(0xFF2D3748)
                ),
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color(0xFF4ADE80) else Color(0xFF718096),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
