package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.GameRecord
import com.example.data.GameRecordRepository
import com.example.sound.GameSoundPlayer
import com.example.ui.GameViewModel
import com.example.ui.GameViewModelFactory
import com.example.ui.Localizer
import com.example.ui.FantasyAnimatedBackground
import com.example.ui.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Navigation Screen enum with Settings included
enum class ActiveScreen {
    HOME,
    GAME,
    STATS,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Setup Database and Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = GameRecordRepository(database.gameRecordDao())
        val viewModelFactory = GameViewModelFactory(application, repository)

        setContent {
            MyApplicationTheme {
                val database = AppDatabase.getDatabase(LocalContext.current)
                val repository = GameRecordRepository(database.gameRecordDao())
                val viewModelFactory = GameViewModelFactory(application, repository)
                val viewModel: GameViewModel = viewModel(factory = viewModelFactory)

                val appLanguage by viewModel.appLanguage.collectAsState()
                val appTheme by viewModel.appTheme.collectAsState()
                val backgroundTheme by viewModel.backgroundTheme.collectAsState()

                // Respond to language updates to force the correct RTL direction
                val layoutDirection = if (appLanguage == "AR") LayoutDirection.Rtl else LayoutDirection.Ltr

                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    var activeScreen by remember { mutableStateOf(ActiveScreen.HOME) }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        bottomBar = {
                            FloatingNavigationBar(
                                activeScreen = activeScreen,
                                theme = appTheme,
                                onTabSelected = { 
                                    activeScreen = it 
                                }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            // High performance animated canvas background matching selected theme
                            FantasyAnimatedBackground(theme = backgroundTheme)

                            CrossfadeScreen(
                                activeScreen = activeScreen,
                                viewModel = viewModel,
                                onNavigateToGame = { activeScreen = ActiveScreen.GAME },
                                onBackToHome = { activeScreen = ActiveScreen.HOME }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingNavigationBar(
    activeScreen: ActiveScreen,
    theme: String,
    onTabSelected: (ActiveScreen) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val isLight = theme == "LIGHT"
    val navBgColor = if (isLight) Color.White.copy(alpha = 0.95f) else Color(0xFF121829).copy(alpha = 0.95f)
    val navBorderColor = if (isLight) Color(0xFFCBD5E1) else Color(0xFF1E293B)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth()
                .height(68.dp)
                .background(navBgColor, RoundedCornerShape(32.dp))
                .border(1.dp, navBorderColor, RoundedCornerShape(32.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            NavBarItem(
                icon = Icons.Default.Home,
                label = Localizer.get("tab_home"),
                isSelected = activeScreen == ActiveScreen.HOME,
                theme = theme,
                onClick = { 
                    GameSoundPlayer.playClick()
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTabSelected(ActiveScreen.HOME) 
                },
                testTag = "nav_home_tab"
            )
            NavBarItem(
                icon = Icons.Default.Gamepad,
                label = Localizer.get("tab_game"),
                isSelected = activeScreen == ActiveScreen.GAME,
                theme = theme,
                onClick = { 
                    GameSoundPlayer.playClick()
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTabSelected(ActiveScreen.GAME) 
                },
                testTag = "nav_game_tab"
            )
            NavBarItem(
                icon = Icons.Default.BarChart,
                label = Localizer.get("tab_stats"),
                isSelected = activeScreen == ActiveScreen.STATS,
                theme = theme,
                onClick = { 
                    GameSoundPlayer.playClick()
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTabSelected(ActiveScreen.STATS) 
                },
                testTag = "nav_stats_tab"
            )
            NavBarItem(
                icon = Icons.Default.Settings,
                label = Localizer.get("tab_settings"),
                isSelected = activeScreen == ActiveScreen.SETTINGS,
                theme = theme,
                onClick = { 
                    GameSoundPlayer.playClick()
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTabSelected(ActiveScreen.SETTINGS) 
                },
                testTag = "nav_settings_tab"
            )
        }
    }
}

@Composable
fun NavBarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    theme: String,
    onClick: () -> Unit,
    testTag: String
) {
    val isLight = theme == "LIGHT"
    val activeColor = if (isLight) Color(0xFF8B5CF6) else Color(0xFF00E5FF)
    val inactiveColor = if (isLight) Color(0xFF94A3B8) else Color(0xFF64748B)

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isSelected) activeColor else inactiveColor,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun CrossfadeScreen(
    activeScreen: ActiveScreen,
    viewModel: GameViewModel,
    onNavigateToGame: () -> Unit,
    onBackToHome: () -> Unit
) {
    val gameMode by viewModel.gameMode.collectAsState()
    val activeId by viewModel.activeTournamentId.collectAsState()
    val matches by viewModel.tournamentMatches.collectAsState()
    val currentIndex by viewModel.currentTournamentMatchIndex.collectAsState()

    when (activeScreen) {
        ActiveScreen.HOME -> HomeScreen(viewModel = viewModel, onStartClicked = onNavigateToGame)
        ActiveScreen.GAME -> {
            if (gameMode == "TOURNAMENT") {
                if (activeId == null) {
                    TournamentSetupScreen(viewModel = viewModel, onBack = onBackToHome)
                } else if (currentIndex in matches.indices) {
                    var showDashboard by remember { mutableStateOf(true) }
                    
                    if (showDashboard) {
                        TournamentDashboardScreen(
                            viewModel = viewModel,
                            onPlayMatch = { showDashboard = false },
                            onBack = onBackToHome
                        )
                    } else {
                        GameScreen(viewModel = viewModel, onBackClicked = { showDashboard = true })
                    }
                } else {
                    var showLeaderboardPostMatch by remember { mutableStateOf(false) }
                    if (showLeaderboardPostMatch) {
                        TournamentDashboardScreen(
                            viewModel = viewModel,
                            onPlayMatch = {},
                            onBack = { showLeaderboardPostMatch = false }
                        )
                    } else {
                        TournamentChampionScreen(
                            viewModel = viewModel,
                            onFinish = {
                                viewModel.clearActiveTournament()
                                onBackToHome()
                            },
                            onViewLeaderboard = {
                                showLeaderboardPostMatch = true
                            }
                        )
                    }
                }
            } else {
                GameScreen(viewModel = viewModel, onBackClicked = onBackToHome)
            }
        }
        ActiveScreen.STATS -> StatsScreen(viewModel = viewModel)
        ActiveScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
    }
}

// 1. HOME SCREEN
@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    onStartClicked: () -> Unit
) {
    val activeMode by viewModel.gameMode.collectAsState()
    val activeDifficulty by viewModel.botDifficulty.collectAsState()
    val chosenSymbol by viewModel.playerSymbol.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    val isHapticEnabled by viewModel.isHapticEnabled.collectAsState()

    val haptic = LocalHapticFeedback.current

    val isLight = appTheme == "LIGHT"
    val textColorPrimary = if (isLight) Color(0xFF0F172A) else Color.White
    val textColorSecondary = if (isLight) Color(0xFF475569) else Color(0xFF94A3B8)
    val cardBgColor = if (isLight) Color.White else Color(0xFF121829)
    val cardBorderColor = if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E293B)
    val accentColor = if (isLight) Color(0xFF8B5CF6) else Color(0xFF00E5FF)

    fun triggerClickFeedback() {
        GameSoundPlayer.playClick()
        if (isHapticEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Neon Pedestal Logo (re-adapted layout)
        NeonPedestalLogo(theme = appTheme)

        Text(
            text = Localizer.get("app_name"),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColorPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )

        Text(
            text = "By Sheikh Ahmed Al-Nems",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = accentColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
        )

        Text(
            text = Localizer.get("app_subtitle"),
            fontSize = 14.sp,
            color = textColorSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Step 1: Select Game Mode
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            border = BorderStroke(1.dp, cardBorderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = Localizer.get("mode_title"),
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                val isAr = viewModel.appLanguage.collectAsState().value == "AR"
                val activeBg = if (isLight) Color(0xFF8B5CF6).copy(alpha = 0.08f) else Color(0xFF1A2642)
                val inactiveBg = if (isLight) Color(0xFFF8FAFC) else Color(0xFF0F1422)
                val activeBorder = accentColor
                val inactiveBorder = if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E293B)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Vs Bot
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (activeMode == "VS_BOT") activeBg else inactiveBg,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                BorderStroke(
                                    if (activeMode == "VS_BOT") 2.dp else 1.dp,
                                    if (activeMode == "VS_BOT") activeBorder else inactiveBorder
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                triggerClickFeedback()
                                viewModel.setGameMode("VS_BOT")
                            }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🤖", fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isAr) "ضد البوت" else "Vs Bot",
                                color = if (activeMode == "VS_BOT") accentColor else textColorPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }

                    // Vs Local Player
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (activeMode == "VS_FRIEND") activeBg else inactiveBg,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                BorderStroke(
                                    if (activeMode == "VS_FRIEND") 2.dp else 1.dp,
                                    if (activeMode == "VS_FRIEND") activeBorder else inactiveBorder
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                triggerClickFeedback()
                                viewModel.setGameMode("VS_FRIEND")
                            }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "👥", fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isAr) "ضد لاعب" else "Vs Player",
                                color = if (activeMode == "VS_FRIEND") accentColor else textColorPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }

                    // Tournament
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (activeMode == "TOURNAMENT") activeBg else inactiveBg,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                BorderStroke(
                                    if (activeMode == "TOURNAMENT") 2.dp else 1.dp,
                                    if (activeMode == "TOURNAMENT") activeBorder else inactiveBorder
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                triggerClickFeedback()
                                viewModel.setGameMode("TOURNAMENT")
                            }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🏆", fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isAr) "البطولة الأسطورية" else "Tournament",
                                color = if (activeMode == "TOURNAMENT") accentColor else textColorPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Dual Player Setup (Visible only if VS_FRIEND)
        AnimatedVisibility(
            visible = activeMode == "VS_FRIEND",
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            val p1Name by viewModel.friendP1Name.collectAsState()
            val p1Title by viewModel.friendP1Title.collectAsState()
            val p2Name by viewModel.friendP2Name.collectAsState()
            val p2Title by viewModel.friendP2Title.collectAsState()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (viewModel.appLanguage.value == "AR") "إعدادات اللاعبين 👥" else "Players Setup 👥",
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Player 1 Name input
                    Text(
                        text = if (viewModel.appLanguage.value == "AR") "اللاعب الأول (رمز X):" else "Player 1 (Symbol X):",
                        color = textColorPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CustomTextField(
                        value = p1Name,
                        onValueChange = { viewModel.setFriendP1Info(it, p1Title) },
                        placeholder = if (viewModel.appLanguage.value == "AR") "أدخل اسم اللاعب الأول" else "Enter Player 1 Name",
                        theme = appTheme
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Player 1 selectable titles
                    // الملك ، الأمير ، القائد ، المدمر ، الفارس
                    val p1Titles = listOf("الملك", "الأمير", "القائد", "المدمر", "الفارس")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        p1Titles.forEach { title ->
                            val isSelected = p1Title == title
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) accentColor.copy(alpha = 0.15f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            if (isSelected) accentColor else cardBorderColor
                                        ),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        triggerClickFeedback()
                                        viewModel.setFriendP1Info(p1Name, title)
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) accentColor else textColorSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Player 2 Name input
                    Text(
                        text = if (viewModel.appLanguage.value == "AR") "اللاعب الثاني (رمز O):" else "Player 2 (Symbol O):",
                        color = textColorPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CustomTextField(
                        value = p2Name,
                        onValueChange = { viewModel.setFriendP2Info(it, p2Title) },
                        placeholder = if (viewModel.appLanguage.value == "AR") "أدخل اسم اللاعب الثاني" else "Enter Player 2 Name",
                        theme = appTheme
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Player 2 selectable titles
                    // العبقري ، الشبح ، الداهية ، المهندس ، الفيلسوف
                    val p2Titles = listOf("العبقري", "الشبح", "الداهية", "المهندس", "الفيلسوف")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        p2Titles.forEach { title ->
                            val isSelected = p2Title == title
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) accentColor.copy(alpha = 0.15f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            if (isSelected) accentColor else cardBorderColor
                                        ),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        triggerClickFeedback()
                                        viewModel.setFriendP2Info(p2Name, title)
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) accentColor else textColorSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Single Player Setup (Visible only if VS_BOT)
        AnimatedVisibility(
            visible = activeMode == "VS_BOT",
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            val botPName by viewModel.botPlayerName.collectAsState()
            val botPTitle by viewModel.botPlayerTitle.collectAsState()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (viewModel.appLanguage.value == "AR") "إعداد اسم اللاعب ضد البوت 👤" else "Player vs Bot Setup 👤",
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (viewModel.appLanguage.value == "AR") "اسمك الكريم:" else "Your Name:",
                        color = textColorPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CustomTextField(
                        value = botPName,
                        onValueChange = { viewModel.setBotPlayerInfo(it, botPTitle) },
                        placeholder = if (viewModel.appLanguage.value == "AR") "أدخل اسمك ضد البوت" else "Enter your name vs Bot",
                        theme = appTheme
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Player selectable titles for Bot Mode
                    val pBotTitles = listOf("البطل", "الأسطورة", "الذكي", "المتحدي", "العبقري")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        pBotTitles.forEach { title ->
                            val isSelected = botPTitle == title
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) accentColor.copy(alpha = 0.15f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            if (isSelected) accentColor else cardBorderColor
                                        ),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        triggerClickFeedback()
                                        viewModel.setBotPlayerInfo(botPName, title)
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) accentColor else textColorSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Step 2: Select Difficulty (Visible only if VS_BOT)
        AnimatedVisibility(
            visible = activeMode == "VS_BOT",
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = Localizer.get("difficulty_title"),
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                )

                RobotDifficultyCard(
                    difficulty = "EASY",
                    title = Localizer.get("difficulty_easy"),
                    color = Color(0xFF4ADE80),
                    isSelected = activeDifficulty == "EASY",
                    theme = appTheme,
                    onClick = { 
                        triggerClickFeedback()
                        viewModel.setBotDifficulty("EASY") 
                    }
                )

                RobotDifficultyCard(
                    difficulty = "MEDIUM",
                    title = Localizer.get("difficulty_medium"),
                    color = Color(0xFFFBBF24),
                    isSelected = activeDifficulty == "MEDIUM",
                    theme = appTheme,
                    onClick = { 
                        triggerClickFeedback()
                        viewModel.setBotDifficulty("MEDIUM") 
                    }
                )

                RobotDifficultyCard(
                    difficulty = "HARD",
                    title = Localizer.get("difficulty_hard"),
                    color = Color(0xFFEF4444),
                    isSelected = activeDifficulty == "HARD",
                    theme = appTheme,
                    onClick = { 
                        triggerClickFeedback()
                        viewModel.setBotDifficulty("HARD") 
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Start Adventure Play Button
        Button(
            onClick = {
                triggerClickFeedback()
                onStartClicked()
            },
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 350.dp)
                .height(56.dp)
                .testTag("start_game_button"),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = if (isLight) Color.White else Color(0xFF090C15)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(imageVector = Icons.Default.Gamepad, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = Localizer.get("start_battle"), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(110.dp)) // Padding for bottom floating navbar
    }
}

@Composable
fun ModeButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    theme: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    testTag: String
) {
    val isLight = theme == "LIGHT"
    val accentColor = if (isLight) Color(0xFF8B5CF6) else Color(0xFF00E5FF)
    
    val activeBorder = accentColor
    val inactiveBorder = if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E293B)
    
    val activeBg = if (isLight) Color(0xFF8B5CF6).copy(alpha = 0.08f) else Color(0xFF1A2642)
    val inactiveBg = if (isLight) Color(0xFFF8FAFC) else Color(0xFF0F1422)

    Box(
        modifier = modifier
            .height(96.dp)
            .background(if (isSelected) activeBg else inactiveBg, RoundedCornerShape(16.dp))
            .border(
                BorderStroke(1.dp, if (isSelected) activeBorder else inactiveBorder),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .testTag(testTag)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) accentColor else Color(0xFF64748B),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                color = if (isSelected) (if (isLight) Color(0xFF0F172A) else Color.White) else Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DetailedModeCard(
    title: String,
    desc: String,
    isSelected: Boolean,
    theme: String,
    onClick: () -> Unit
) {
    val isLight = theme == "LIGHT"
    val accentColor = if (isLight) Color(0xFF8B5CF6) else Color(0xFF00E5FF)
    
    val activeBorder = accentColor
    val inactiveBorder = if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E293B)
    
    val activeBg = if (isLight) Color(0xFF8B5CF6).copy(alpha = 0.08f) else Color(0xFF1A2642)
    val inactiveBg = if (isLight) Color(0xFFF8FAFC) else Color(0xFF0F1422)

    val textColorPrimary = if (isLight) Color(0xFF0F172A) else Color.White
    val textColorSecondary = if (isLight) Color(0xFF475569) else Color(0xFF94A3B8)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) activeBg else inactiveBg, RoundedCornerShape(16.dp))
            .border(
                BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) activeBorder else inactiveBorder),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                color = if (isSelected) accentColor else textColorPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = desc,
                color = textColorSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun SymbolSelectButton(
    symbol: String,
    color: Color,
    isSelected: Boolean,
    theme: String,
    onClick: () -> Unit,
    testTag: String
) {
    val isLight = theme == "LIGHT"
    val inactiveBorder = if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E293B)
    val bg = if (isSelected) color.copy(alpha = 0.15f) else (if (isLight) Color(0xFFF8FAFC) else Color(0xFF0F1422))

    Box(
        modifier = Modifier
            .size(72.dp)
            .background(bg, RoundedCornerShape(16.dp))
            .border(
                BorderStroke(2.dp, if (isSelected) color else inactiveBorder),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (symbol == "X") {
            XSymbol(modifier = Modifier.size(44.dp), color = color)
        } else {
            OSymbol(modifier = Modifier.size(44.dp), color = color)
        }
    }
}


// 2. GAME SCREEN (ARENA)
@Composable
fun GameScreen(viewModel: GameViewModel, onBackClicked: () -> Unit) {
    val board by viewModel.board.collectAsState()
    val currentTurn by viewModel.currentTurn.collectAsState()
    val winner by viewModel.winner.collectAsState()
    val winningLine by viewModel.winningLine.collectAsState()
    val isGameOver by viewModel.isGameOver.collectAsState()
    val gameMode by viewModel.gameMode.collectAsState()
    val botDifficulty by viewModel.botDifficulty.collectAsState()
    val isBotThinking by viewModel.isBotThinking.collectAsState()

    val scoreX by viewModel.scoreX.collectAsState()
    val scoreO by viewModel.scoreO.collectAsState()
    val scoreDraws by viewModel.scoreDraws.collectAsState()

    val highlightedCell by viewModel.highlightedCell.collectAsState()
    val hintCount by viewModel.hintCount.collectAsState()

    val appTheme by viewModel.appTheme.collectAsState()
    val isHapticEnabled by viewModel.isHapticEnabled.collectAsState()

    val haptic = LocalHapticFeedback.current

    val isLight = appTheme == "LIGHT"
    val textColorPrimary = if (isLight) Color(0xFF0F172A) else Color.White
    val textColorSecondary = if (isLight) Color(0xFF475569) else Color(0xFF94A3B8)
    val cardBgColor = if (isLight) Color.White else Color(0xFF121829)
    val cardBorderColor = if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E293B)
    val accentColor = if (isLight) Color(0xFF8B5CF6) else Color(0xFF00E5FF)

    fun triggerMoveFeedback() {
        if (isHapticEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    DisposableEffect(Unit) {
        viewModel.startStopwatch()
        onDispose {
            viewModel.stopStopwatch()
        }
    }

    // Real-time counting match timer
    var secondsElapsed by remember { mutableStateOf(0) }
    var isTimerActive by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = isGameOver, key2 = board) {
        secondsElapsed = 0
        isTimerActive = !isGameOver
    }

    LaunchedEffect(key1 = secondsElapsed, key2 = isTimerActive) {
        if (isTimerActive) {
            delay(1000)
            secondsElapsed += 1
        }
    }

    val minutesStr = (secondsElapsed / 60).toString().padStart(2, '0')
    val secondsStr = (secondsElapsed % 60).toString().padStart(2, '0')
    val formattedTime = "$minutesStr:$secondsStr"

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. E-Sports Title Header Row (Back, VS Avatars, Info)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                IconButton(
                    onClick = {
                        GameSoundPlayer.playClick()
                        triggerMoveFeedback()
                        onBackClicked()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(cardBgColor, CircleShape)
                        .border(1.dp, cardBorderColor, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = Localizer.get("cancel"),
                        tint = textColorPrimary
                    )
                }

                // VS Avatars Frame
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Player X
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val activeX = currentTurn == "X" && !isGameOver
                        val glowAnim = rememberInfiniteTransition(label = "glowX").animateFloat(
                            initialValue = 1f,
                            targetValue = 1.25f,
                            animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
                            label = "glow"
                        )
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(cardBgColor, CircleShape)
                                .border(
                                    BorderStroke(
                                        width = if (activeX) 2.dp else 1.dp,
                                        color = if (activeX) Color(0xFF00E5FF) else cardBorderColor
                                    ),
                                    CircleShape
                                )
                                .padding(if (activeX) (2 * glowAnim.value).dp else 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF00E5FF).copy(alpha = if (activeX) 0.15f else 0.05f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "X",
                                    color = Color(0xFF00E5FF),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = viewModel.getPlayer1DisplayName(),
                            color = textColorPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }

                    // VS Circle
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(if (isLight) Color(0xFFCBD5E1) else Color(0xFF1B223C), CircleShape)
                            .border(1.dp, if (isLight) Color(0xFF94A3B8) else Color(0xFF2E3B5E), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "VS",
                            color = Color(0xFFFFD700),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Player O / Bot
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val activeO = currentTurn == "O" && !isGameOver
                        val glowAnim = rememberInfiniteTransition(label = "glowO").animateFloat(
                            initialValue = 1f,
                            targetValue = 1.25f,
                            animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
                            label = "glow"
                        )
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(cardBgColor, CircleShape)
                                .border(
                                    BorderStroke(
                                        width = if (activeO) 2.dp else 1.dp,
                                        color = if (activeO) Color(0xFFFF2D55) else cardBorderColor
                                    ),
                                    CircleShape
                                )
                                .padding(if (activeO) (2 * glowAnim.value).dp else 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFFF2D55).copy(alpha = if (activeO) 0.15f else 0.05f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (gameMode == "VS_BOT") {
                                    Icon(
                                        imageVector = Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = Color(0xFFFF2D55),
                                        modifier = Modifier.size(22.dp)
                                    )
                                } else {
                                    Text(
                                        text = "O",
                                        color = Color(0xFFFF2D55),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 20.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = viewModel.getPlayer2DisplayName(),
                            color = textColorPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Info Icon
                IconButton(
                    onClick = { GameSoundPlayer.playClick() },
                    modifier = Modifier
                        .size(44.dp)
                        .background(cardBgColor, CircleShape)
                        .border(1.dp, cardBorderColor, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = textColorSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Active Turn Banner with Live Timer
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val pulseDotAnim = rememberInfiniteTransition(label = "pulseDot").animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse),
                            label = "dot"
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    if (currentTurn == "X") Color(0xFF00E5FF).copy(alpha = pulseDotAnim.value)
                                    else Color(0xFFFF2D55).copy(alpha = pulseDotAnim.value),
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        val bannerMsg = if (isGameOver) Localizer.get("game_over") else {
                            if (isBotThinking) Localizer.get("turn_bot") else {
                                if (gameMode == "VS_BOT" && currentTurn == "X") Localizer.get("turn_your")
                                else if (gameMode == "VS_BOT") Localizer.get("turn_bot")
                                else String.format(Localizer.get("turn_player"), currentTurn)
                            }
                        }
                        Text(
                            text = bannerMsg,
                            color = textColorPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Timer Box
                    Box(
                        modifier = Modifier
                            .background(if (isLight) Color(0xFFF1F5F9) else Color(0xFF090C15), RoundedCornerShape(8.dp))
                            .border(1.dp, cardBorderColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = formattedTime,
                            color = Color(0xFFFFD700),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. 3x3 Grid Board Card (Dynamic, Neon Win lines, Glow cells)
            Box(
                modifier = Modifier
                    .size(310.dp)
                    .background(if (isLight) Color.White else Color(0xFF0F1424), RoundedCornerShape(24.dp))
                    .border(2.dp, cardBorderColor, RoundedCornerShape(24.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                // Draw grid lines
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val size = this.size.width
                    val step = size / 3
                    val strokeW = 4f
                    val strokeColor = cardBorderColor

                    // Verticals
                    drawLine(strokeColor, Offset(step, 8f), Offset(step, size - 8f), strokeW)
                    drawLine(strokeColor, Offset(step * 2, 8f), Offset(step * 2, size - 8f), strokeW)

                    // Horizontals
                    drawLine(strokeColor, Offset(8f, step), Offset(size - 8f, step), strokeW)
                    drawLine(strokeColor, Offset(8f, step * 2), Offset(size - 8f, step * 2), strokeW)
                }

                // Interactive Cell Grid
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (row in 0..2) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (col in 0..2) {
                                val cellIndex = row * 3 + col
                                val cellSymbol = board[cellIndex]
                                val isWinningCell = winningLine?.contains(cellIndex) == true
                                val isHintCell = highlightedCell == cellIndex

                                val hintPulse = if (isHintCell) {
                                    rememberInfiniteTransition(label = "hintGlow").animateFloat(
                                        initialValue = 0.3f,
                                        targetValue = 1.0f,
                                        animationSpec = infiniteRepeatable(tween(450, easing = LinearEasing), RepeatMode.Reverse),
                                        label = "glow"
                                    ).value
                                } else 0f

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isWinningCell) Color(0xFFFFD700).copy(alpha = 0.15f)
                                            else if (isHintCell) Color(0xFFFFD700).copy(alpha = 0.1f * hintPulse)
                                            else Color.Transparent
                                        )
                                        .border(
                                            BorderStroke(
                                                width = if (isWinningCell) 2.5.dp else if (isHintCell) 2.dp else 0.dp,
                                                color = if (isWinningCell) Color(0xFFFFD700)
                                                else if (isHintCell) Color(0xFFFFD700).copy(alpha = hintPulse)
                                                else Color.Transparent
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            triggerMoveFeedback()
                                            viewModel.onCellClick(cellIndex)
                                        }
                                        .testTag("cell_$cellIndex"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (cellSymbol != null) {
                                        if (cellSymbol == "X") {
                                            XSymbol(modifier = Modifier.size(52.dp), color = Color(0xFF00E5FF))
                                        } else if (cellSymbol == "O") {
                                            OSymbol(modifier = Modifier.size(52.dp), color = Color(0xFFFF2D55))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Game Arena Control Row (Restart, Undo, Hint)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Restart Button
                Button(
                    onClick = {
                        GameSoundPlayer.playClick()
                        triggerMoveFeedback()
                        viewModel.resetGame()
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(54.dp)
                        .testTag("reset_game_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E293B),
                        contentColor = textColorPrimary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = Localizer.get("re_round"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Undo Button
                Button(
                    onClick = {
                        triggerMoveFeedback()
                        viewModel.undoLastMove()
                    },
                    modifier = Modifier
                        .weight(1.1f)
                        .height(54.dp)
                        .testTag("undo_game_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLight) Color(0xFFF1F5F9) else Color(0xFF14192B),
                        contentColor = textColorSecondary
                    ),
                    border = BorderStroke(1.dp, cardBorderColor)
                ) {
                    Text(text = "${Localizer.get("undo")} ↩", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Hint Button
                Button(
                    onClick = {
                        triggerMoveFeedback()
                        viewModel.requestHint()
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(54.dp)
                        .testTag("hint_game_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hintCount > 0) Color(0xFF0F2B3E) else (if (isLight) Color(0xFFF1F5F9) else Color(0xFF101426)),
                        contentColor = if (hintCount > 0) Color(0xFFFFD700) else Color(0xFF64748B)
                    ),
                    border = BorderStroke(1.dp, if (hintCount > 0) Color(0xFFFFD700).copy(alpha = 0.4f) else cardBorderColor)
                ) {
                    Text(text = "${Localizer.get("hint")} ✨ ($hintCount)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(110.dp)) // Padding for BottomBar
        }

        // 5. Celebration Victory Overlay
        if (isGameOver) {
            VictoryOverlay(
                winner = winner,
                gameMode = gameMode,
                scoreX = scoreX,
                scoreO = scoreO,
                scoreDraws = scoreDraws,
                theme = appTheme,
                viewModel = viewModel,
                onPlayAgain = {
                    triggerMoveFeedback()
                    viewModel.resetGame()
                },
                onMainMenu = {
                    triggerMoveFeedback()
                    viewModel.resetGame()
                    onBackClicked()
                }
            )
        }
    }
}


// 3. STATS SCREEN (With Battle history & Achievements system)
@Composable
fun StatsScreen(viewModel: GameViewModel) {
    val historyRaw by viewModel.history.collectAsState()
    val history = remember(historyRaw) {
        historyRaw.filter { it.gameMode != "TOURNAMENT" }
    }
    val appTheme by viewModel.appTheme.collectAsState()
    val isHapticEnabled by viewModel.isHapticEnabled.collectAsState()

    val haptic = LocalHapticFeedback.current

    val isLight = appTheme == "LIGHT"
    val textColorPrimary = if (isLight) Color(0xFF0F172A) else Color.White
    val textColorSecondary = if (isLight) Color(0xFF475569) else Color(0xFF94A3B8)
    val cardBgColor = if (isLight) Color.White else Color(0xFF121829)
    val cardBorderColor = if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E293B)
    val accentColor = if (isLight) Color(0xFF8B5CF6) else Color(0xFF00E5FF)

    var activeStatsTab by remember { mutableStateOf(0) } // 0: History, 1: Achievements

    fun triggerTabFeedback() {
        GameSoundPlayer.playClick()
        if (isHapticEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = Localizer.get("stats_title"),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = textColorPrimary,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = Localizer.get("stats_subtitle"),
            fontSize = 12.sp,
            color = textColorSecondary,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        // Custom Double-Sliding Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .height(48.dp)
                .background(cardBgColor, RoundedCornerShape(24.dp))
                .border(1.dp, cardBorderColor, RoundedCornerShape(24.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (activeStatsTab == 0) accentColor else Color.Transparent)
                    .clickable {
                        triggerTabFeedback()
                        activeStatsTab = 0
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Localizer.get("history_log"),
                    color = if (activeStatsTab == 0) (if (isLight) Color.White else Color(0xFF090C15)) else textColorSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (activeStatsTab == 1) accentColor else Color.Transparent)
                    .clickable {
                        triggerTabFeedback()
                        activeStatsTab = 1
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Localizer.get("achievements"),
                    color = if (activeStatsTab == 1) (if (isLight) Color.White else Color(0xFF090C15)) else textColorSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val totalGames = history.size
        val wins = history.count { it.winnerSymbol != null && it.winnerSymbol == it.playerSymbol }
        val ties = history.count { it.winnerSymbol == null }
        val losses = totalGames - wins - ties

        if (activeStatsTab == 0) {
            // VIEW TABS 1: HISTORY LOGS & SUMMARY
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    StatsRingChart(wins = wins, losses = losses, ties = ties, theme = appTheme)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${Localizer.get("history_log")} ($totalGames)",
                    fontSize = 14.sp,
                    color = textColorSecondary,
                    fontWeight = FontWeight.Bold
                )

                if (totalGames > 0) {
                    IconButton(
                        onClick = {
                            triggerTabFeedback()
                            viewModel.clearHistory()
                        },
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = Localizer.get("clear_history"),
                            tint = Color(0xFFEF4444)
                        )
                    }
                }
            }

            if (totalGames == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = Localizer.get("no_records"),
                        color = textColorSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(history) { record ->
                        HistoryItemCard(record = record, theme = appTheme)
                    }
                    item {
                        Spacer(modifier = Modifier.height(110.dp)) // padding for bottom bar
                    }
                }
            }
        } else {
            // VIEW TABS 2: ACHIEVEMENT PROGRESS
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Calculation of achievements
                val firstWinUnlocked = history.any { it.winnerSymbol != null && it.winnerSymbol == it.playerSymbol }
                val hardConquerorUnlocked = history.any { it.gameMode == "BOT_HARD" && (it.winnerSymbol == it.playerSymbol || it.winnerSymbol == null) }
                val legendaryUnlocked = totalGames >= 10

                var maxStreak = 0
                var currentStreak = 0
                val chronological = history.reversed()
                chronological.forEach { rec ->
                    val won = rec.winnerSymbol != null && rec.winnerSymbol == rec.playerSymbol
                    if (won) {
                        currentStreak++
                        maxStreak = maxOf(maxStreak, currentStreak)
                    } else {
                        currentStreak = 0
                    }
                }
                val streakUnlocked = maxStreak >= 3
                val hintAchievementUnlocked = viewModel.didUseAllThreeHints()

                item {
                    AchievementBadgeRow(
                        name = Localizer.get("ach_first_win"),
                        desc = Localizer.get("ach_first_win_desc"),
                        isUnlocked = firstWinUnlocked,
                        theme = appTheme
                    )
                }

                item {
                    AchievementBadgeRow(
                        name = Localizer.get("ach_hard_draw"),
                        desc = Localizer.get("ach_hard_draw_desc"),
                        isUnlocked = hardConquerorUnlocked,
                        theme = appTheme
                    )
                }

                item {
                    AchievementBadgeRow(
                        name = Localizer.get("ach_streak"),
                        desc = Localizer.get("ach_streak_desc"),
                        isUnlocked = streakUnlocked,
                        theme = appTheme
                    )
                }

                item {
                    AchievementBadgeRow(
                        name = Localizer.get("ach_hint"),
                        desc = Localizer.get("ach_hint_desc"),
                        isUnlocked = hintAchievementUnlocked,
                        theme = appTheme
                    )
                }

                item {
                    AchievementBadgeRow(
                        name = Localizer.get("ach_legend"),
                        desc = Localizer.get("ach_legend_desc"),
                        isUnlocked = legendaryUnlocked,
                        theme = appTheme
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(110.dp)) // Bottom padding
                }
            }
        }
    }
}

@Composable
fun AchievementBadgeRow(
    name: String,
    desc: String,
    isUnlocked: Boolean,
    theme: String
) {
    val isLight = theme == "LIGHT"
    val cardBg = if (isLight) Color.White else Color(0xFF101426)
    val cardBorder = if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E293B)
    val textColorPrimary = if (isLight) Color(0xFF0F172A) else Color.White
    val textColorSecondary = if (isLight) Color(0xFF475569) else Color(0xFF94A3B8)

    val badgeColor = if (isUnlocked) Color(0xFFFFD700) else Color(0xFF64748B)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 500.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(badgeColor.copy(alpha = 0.12f), CircleShape)
                    .border(1.dp, badgeColor.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColorPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = textColorSecondary
                )
            }

            Box(
                modifier = Modifier
                    .background(
                        if (isUnlocked) Color(0xFF4ADE80).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isUnlocked) Localizer.get("unlocked") else Localizer.get("locked"),
                    color = if (isUnlocked) Color(0xFF4ADE80) else Color(0xFFEF4444),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StatsRingChart(wins: Int, losses: Int, ties: Int, theme: String) {
    val total = wins + losses + ties
    val isLight = theme == "LIGHT"
    val textColorPrimary = if (isLight) Color(0xFF0F172A) else Color.White
    val textColorSecondary = if (isLight) Color(0xFF475569) else Color(0xFF94A3B8)

    if (total == 0) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = Localizer.get("no_records"),
                color = textColorSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val winSweep = (wins.toFloat() / total) * 360f
    val lossSweep = (losses.toFloat() / total) * 360f
    val tieSweep = (ties.toFloat() / total) * 360f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(100.dp)) {
                val strokeWidth = 24f
                var startAngle = -90f

                // Wins (Neon Blue)
                if (wins > 0) {
                    drawArc(
                        color = Color(0xFF00E5FF),
                        startAngle = startAngle,
                        sweepAngle = winSweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += winSweep
                }

                // Ties (Yellow Gray)
                if (ties > 0) {
                    drawArc(
                        color = Color(0xFF64748B),
                        startAngle = startAngle,
                        sweepAngle = tieSweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += tieSweep
                }

                // Losses (Neon Pink)
                if (losses > 0) {
                    drawArc(
                        color = Color(0xFFFF2D55),
                        startAngle = startAngle,
                        sweepAngle = lossSweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }
            Text(
                text = "$total\n${Localizer.get("stats_played")}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColorPrimary,
                textAlign = TextAlign.Center
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LegendItem(color = Color(0xFF00E5FF), label = Localizer.get("stats_wins"), count = wins, percent = (wins * 100 / total), theme = theme)
            LegendItem(color = Color(0xFF64748B), label = Localizer.get("stats_draws"), count = ties, percent = (ties * 100 / total), theme = theme)
            LegendItem(color = Color(0xFFFF2D55), label = Localizer.get("stats_losses"), count = losses, percent = (losses * 100 / total), theme = theme)
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, count: Int, percent: Int, theme: String) {
    val isLight = theme == "LIGHT"
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: $count ($percent%)",
            color = if (isLight) Color(0xFF0F172A) else Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun HistoryItemCard(record: GameRecord, theme: String) {
    val isLight = theme == "LIGHT"
    val isWin = record.winnerSymbol != null && record.winnerSymbol == record.playerSymbol
    val isTie = record.winnerSymbol == null

    val statusText = when {
        record.gameMode == "FRIEND" -> {
            if (record.winnerSymbol != null) {
                record.winnerName ?: ""
            } else {
                if (Localizer.currentLanguage == "AR") "تعادل عادل" else "Fair Draw"
            }
        }
        isWin -> Localizer.get("stats_wins")
        isTie -> Localizer.get("stats_draws")
        else -> Localizer.get("stats_losses")
    }

    val statusColor = when {
        record.gameMode == "FRIEND" && record.winnerSymbol != null -> Color(0xFF4ADE80)
        isWin -> Color(0xFF4ADE80)
        isTie -> Color(0xFF94A3B8)
        else -> Color(0xFFFF2D55)
    }

    val modeText = when (record.gameMode) {
        "BOT_EASY" -> Localizer.get("difficulty_easy")
        "BOT_MEDIUM" -> Localizer.get("difficulty_medium")
        "BOT_HARD" -> Localizer.get("difficulty_hard")
        "FRIEND" -> Localizer.get("mode_friend")
        else -> if (Localizer.currentLanguage == "AR") "مباراة بطولة 🏆" else "Tournament Match 🏆"
    }

    val formattedDate = remember(record.timestamp) {
        try {
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            sdf.format(Date(record.timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 500.dp),
        colors = CardDefaults.cardColors(containerColor = if (isLight) Color.White else Color(0xFF0F1422)),
        border = BorderStroke(1.dp, if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E293B).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = modeText,
                    color = if (isLight) Color(0xFF0F172A) else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedDate,
                    color = if (isLight) Color(0xFF64748B) else Color(0xFF475569),
                    fontSize = 11.sp
                )
            }

            Box(
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .border(BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun XSymbol(modifier: Modifier = Modifier, color: Color = Color(0xFF00E5FF)) {
    val scaleAnim = rememberInfiniteTransition(label = "XGlow").animateFloat(
        initialValue = 0.9f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "XGlowScale"
    )
    Canvas(modifier = modifier) {
        val size = this.size.width
        val strokeWidth = 14f
        val padding = 8f
        val glowRadius = strokeWidth * 1.5f * scaleAnim.value

        drawLine(
            color = color.copy(alpha = 0.35f),
            start = Offset(padding, padding),
            end = Offset(size - padding, size - padding),
            strokeWidth = strokeWidth + glowRadius,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color.copy(alpha = 0.35f),
            start = Offset(size - padding, padding),
            end = Offset(padding, size - padding),
            strokeWidth = strokeWidth + glowRadius,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(padding, padding),
            end = Offset(size - padding, size - padding),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size - padding, padding),
            end = Offset(padding, size - padding),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun OSymbol(modifier: Modifier = Modifier, color: Color = Color(0xFFFF2D55)) {
    val scaleAnim = rememberInfiniteTransition(label = "OGlow").animateFloat(
        initialValue = 0.9f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OGlowScale"
    )
    Canvas(modifier = modifier) {
        val size = this.size.width
        val radius = size / 2 - 14f
        val strokeWidth = 14f
        val glowRadius = strokeWidth * 1.5f * scaleAnim.value

        drawCircle(
            color = color.copy(alpha = 0.35f),
            radius = radius,
            center = Offset(size / 2, size / 2),
            style = Stroke(width = strokeWidth + glowRadius, cap = StrokeCap.Round)
        )

        drawCircle(
            color = color,
            radius = radius,
            center = Offset(size / 2, size / 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

// Robot face graphics
@Composable
fun RobotFaceIcon(difficulty: String, color: Color) {
    Canvas(modifier = Modifier.size(54.dp)) {
        val w = size.width
        val h = size.height
        val strokeW = 3f
        
        drawLine(
            color = color,
            start = Offset(w / 2, h * 0.25f),
            end = Offset(w / 2, h * 0.1f),
            strokeWidth = strokeW * 1.5f,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = color,
            radius = 4f,
            center = Offset(w / 2, h * 0.1f)
        )
        
        if (difficulty == "HARD") {
            drawRect(
                color = color,
                topLeft = Offset(w * 0.1f, h * 0.45f),
                size = androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.2f)
            )
            drawRect(
                color = color,
                topLeft = Offset(w * 0.82f, h * 0.45f),
                size = androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.2f)
            )
        } else {
            drawCircle(color = color, radius = 4f, center = Offset(w * 0.15f, h * 0.55f))
            drawCircle(color = color, radius = 4f, center = Offset(w * 0.85f, h * 0.55f))
        }

        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.2f, h * 0.25f),
            size = androidx.compose.ui.geometry.Size(w * 0.6f, h * 0.55f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
            style = Stroke(width = strokeW)
        )
        
        when (difficulty) {
            "EASY" -> {
                drawArc(
                    color = color,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.32f, h * 0.42f),
                    size = androidx.compose.ui.geometry.Size(w * 0.12f, h * 0.1f),
                    style = Stroke(width = strokeW * 1.5f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = color,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.56f, h * 0.42f),
                    size = androidx.compose.ui.geometry.Size(w * 0.12f, h * 0.1f),
                    style = Stroke(width = strokeW * 1.5f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = color,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.42f, h * 0.52f),
                    size = androidx.compose.ui.geometry.Size(w * 0.16f, h * 0.14f),
                    style = Stroke(width = strokeW * 1.5f, cap = StrokeCap.Round)
                )
            }
            "MEDIUM" -> {
                drawCircle(color = color, radius = 5f, center = Offset(w * 0.38f, h * 0.45f))
                drawCircle(color = color, radius = 5f, center = Offset(w * 0.62f, h * 0.45f))
                drawLine(
                    color = color,
                    start = Offset(w * 0.42f, h * 0.62f),
                    end = Offset(w * 0.58f, h * 0.62f),
                    strokeWidth = strokeW * 1.5f,
                    cap = StrokeCap.Round
                )
            }
            "HARD" -> {
                drawLine(
                    color = color,
                    start = Offset(w * 0.3f, h * 0.42f),
                    end = Offset(w * 0.44f, h * 0.48f),
                    strokeWidth = strokeW * 2f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(w * 0.7f, h * 0.42f),
                    end = Offset(w * 0.56f, h * 0.48f),
                    strokeWidth = strokeW * 2f,
                    cap = StrokeCap.Round
                )
                drawCircle(color = color, radius = 3f, center = Offset(w * 0.37f, h * 0.47f))
                drawCircle(color = color, radius = 3f, center = Offset(w * 0.63f, h * 0.47f))
                drawArc(
                    color = color,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.44f, h * 0.6f),
                    size = androidx.compose.ui.geometry.Size(w * 0.12f, h * 0.1f),
                    style = Stroke(width = strokeW * 1.5f, cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
fun CustomNeonArrow(color: Color) {
    Canvas(modifier = Modifier.size(16.dp)) {
        val w = size.width
        val h = size.height
        val stroke = 4f
        drawLine(
            color = color,
            start = Offset(w * 0.15f, h * 0.5f),
            end = Offset(w * 0.85f, h * 0.5f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.15f, h * 0.5f),
            end = Offset(w * 0.45f, h * 0.2f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.15f, h * 0.5f),
            end = Offset(w * 0.45f, h * 0.8f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun RobotDifficultyCard(
    difficulty: String,
    title: String,
    color: Color,
    isSelected: Boolean,
    theme: String,
    onClick: () -> Unit
) {
    val isLight = theme == "LIGHT"
    val cardBg = if (isSelected) color.copy(alpha = 0.08f) else (if (isLight) Color.White else Color(0xFF101426))
    val cardBorder = if (isSelected) color else (if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E293B))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RobotFaceIcon(difficulty = difficulty, color = color)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = color,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (isSelected) color.copy(alpha = 0.15f) else (if (isLight) Color(0xFFCBD5E1) else Color(0xFF1B223C)),
                        CircleShape
                    )
                    .border(
                        BorderStroke(1.dp, if (isSelected) color else (if (isLight) Color(0xFF94A3B8) else Color(0xFF2E3B5E))),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                CustomNeonArrow(color = if (isSelected) color else Color(0xFF64748B))
            }
        }
    }
}

@Composable
fun NeonPedestalLogo(theme: String) {
    val isLight = theme == "LIGHT"
    val infiniteTransition = rememberInfiniteTransition(label = "pedestalGlow")
    val bounceY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier.size(width = 240.dp, height = 150.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                
                if (!isLight) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.18f * glowAlpha), Color.Transparent),
                            center = Offset(w * 0.35f, h * 0.5f),
                            radius = w * 0.4f
                        ),
                        center = Offset(w * 0.35f, h * 0.5f),
                        radius = w * 0.4f
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFF2D55).copy(alpha = 0.18f * glowAlpha), Color.Transparent),
                            center = Offset(w * 0.65f, h * 0.5f),
                            radius = w * 0.4f
                        ),
                        center = Offset(w * 0.65f, h * 0.5f),
                        radius = w * 0.4f
                    )
                }
            }

            // The Pedestal Platform
            Canvas(
                modifier = Modifier
                    .width(180.dp)
                    .height(60.dp)
                    .align(Alignment.BottomCenter)
            ) {
                val w = size.width
                val h = size.height
                
                drawOval(
                    color = (if (isLight) Color(0xFFCBD5E1) else Color(0xFF00E5FF)).copy(alpha = 0.25f * glowAlpha),
                    topLeft = Offset(w * 0.1f, h * 0.4f),
                    size = androidx.compose.ui.geometry.Size(w * 0.8f, h * 0.5f)
                )
                drawOval(
                    color = if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E293B),
                    topLeft = Offset(w * 0.12f, h * 0.35f),
                    size = androidx.compose.ui.geometry.Size(w * 0.76f, h * 0.45f)
                )
                drawOval(
                    color = if (isLight) Color.White else Color(0xFF0D1527),
                    topLeft = Offset(w * 0.12f, h * 0.3f),
                    size = androidx.compose.ui.geometry.Size(w * 0.76f, h * 0.45f)
                )
                drawOval(
                    color = if (isLight) Color(0xFF8B5CF6) else Color(0xFF00E5FF),
                    topLeft = Offset(w * 0.12f, h * 0.3f),
                    size = androidx.compose.ui.geometry.Size(w * 0.76f, h * 0.45f),
                    style = Stroke(width = 3f)
                )
            }

            // Floating X and O symbols
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 15.dp)
                    .offset(y = bounceY.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                XSymbol(modifier = Modifier.size(64.dp), color = Color(0xFF00E5FF))
                OSymbol(modifier = Modifier.size(64.dp), color = Color(0xFFFF2D55))
            }
        }
    }
}

@Composable
fun VictoryOverlay(
    winner: String?,
    gameMode: String,
    scoreX: Int,
    scoreO: Int,
    scoreDraws: Int,
    theme: String,
    viewModel: GameViewModel,
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit
) {
    val isLight = theme == "LIGHT"
    val textColorPrimary = if (isLight) Color(0xFF0F172A) else Color.White
    val textColorSecondary = if (isLight) Color(0xFF475569) else Color(0xFF94A3B8)
    val cardBg = if (isLight) Color.White else Color(0xFF121829)
    val cardBorder = if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E293B)
    val accentColor = if (isLight) Color(0xFF8B5CF6) else Color(0xFF00E5FF)

    val p1Name = viewModel.getPlayer1DisplayName()
    val p1Moves by viewModel.p1MovesCount.collectAsState()
    val p1Seconds by viewModel.p1ThinkingSeconds.collectAsState()
    val p1HintCount by viewModel.p1HintCount.collectAsState()
    val p1HintsUsed = if (gameMode == "VS_BOT") 3 - p1HintCount else 2 - p1HintCount

    val p2Name = viewModel.getPlayer2DisplayName()
    val p2Moves by viewModel.p2MovesCount.collectAsState()
    val p2Seconds by viewModel.p2ThinkingSeconds.collectAsState()
    val p2HintCount by viewModel.p2HintCount.collectAsState()
    val p2HintsUsed = if (gameMode == "VS_BOT") 0 else 2 - p2HintCount

    val infiniteTransition = rememberInfiniteTransition(label = "victoryGlow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background((if (isLight) Color(0xFFF1F5F9) else Color(0xFF090C15)).copy(alpha = 0.96f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        if (winner != null) {
            ConfettiRain()
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val colors = listOf(Color(0xFF00E5FF), Color(0xFFFF2D55), Color(0xFFFFD700), Color(0xFF4ADE80))
            val randPoints = listOf(
                Offset(w * 0.15f, h * 0.2f), Offset(w * 0.85f, h * 0.25f),
                Offset(w * 0.2f, h * 0.7f), Offset(w * 0.8f, h * 0.65f),
                Offset(w * 0.3f, h * 0.15f), Offset(w * 0.7f, h * 0.12f),
                Offset(w * 0.08f, h * 0.45f), Offset(w * 0.92f, h * 0.5f)
            )
            randPoints.forEachIndexed { idx, pt ->
                val sizeVal = 8f + (idx % 3) * 4f
                val color = colors[idx % colors.size]
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(pt.x, pt.y - sizeVal)
                    lineTo(pt.x + sizeVal / 2, pt.y - sizeVal / 4)
                    lineTo(pt.x + sizeVal, pt.y)
                    lineTo(pt.x + sizeVal / 2, pt.y + sizeVal / 4)
                    lineTo(pt.x, pt.y + sizeVal)
                    lineTo(pt.x - sizeVal / 2, pt.y + sizeVal / 4)
                    lineTo(pt.x - sizeVal, pt.y)
                    lineTo(pt.x - sizeVal / 2, pt.y - sizeVal / 4)
                    close()
                }
                drawPath(path, color)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            val titleText = when {
                winner != null -> {
                    val wName = if (winner == "X") viewModel.getPlayer1DisplayName() else viewModel.getPlayer2DisplayName()
                    if (viewModel.appLanguage.value == "AR") "فوز $wName 🎉" else "Victory for $wName 🎉"
                }
                else -> Localizer.get("draw_title")
            }
            val titleColor = when {
                winner == "X" -> Color(0xFF00E5FF)
                winner == "O" -> Color(0xFFFF2D55)
                else -> Color(0xFFFFD700)
            }

            Text(
                text = titleText,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = titleColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = if (winner != null) Localizer.get("win_desc") else Localizer.get("draw_desc"),
                fontSize = 14.sp,
                color = textColorSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Giant Laurel wreath and symbol in center
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val wreathColor = titleColor.copy(alpha = 0.4f)
                    
                    drawArc(
                        color = wreathColor,
                        startAngle = 100f,
                        sweepAngle = 160f,
                        useCenter = false,
                        topLeft = Offset(w * 0.1f, h * 0.1f),
                        size = androidx.compose.ui.geometry.Size(w * 0.8f, h * 0.8f),
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )
                    
                    drawArc(
                        color = wreathColor,
                        startAngle = -80f,
                        sweepAngle = 160f,
                        useCenter = false,
                        topLeft = Offset(w * 0.1f, h * 0.1f),
                        size = androidx.compose.ui.geometry.Size(w * 0.8f, h * 0.8f),
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )
                    
                    val leafCount = 6
                    for (i in 0 until leafCount) {
                        val angleLeft = 110f + i * 22f
                        val angleRight = -70f - i * 22f
                        
                        val radLeft = Math.toRadians(angleLeft.toDouble())
                        val lx = w / 2 + (w * 0.4f) * Math.cos(radLeft)
                        val ly = h / 2 + (h * 0.4f) * Math.sin(radLeft)
                        drawCircle(color = wreathColor, radius = 5f, center = Offset(lx.toFloat(), ly.toFloat()))

                        val radRight = Math.toRadians(angleRight.toDouble())
                        val rx = w / 2 + (w * 0.4f) * Math.cos(radRight)
                        val ry = h / 2 + (h * 0.4f) * Math.sin(radRight)
                        drawCircle(color = wreathColor, radius = 5f, center = Offset(rx.toFloat(), ry.toFloat()))
                    }
                }

                Box(
                    modifier = Modifier.size(140.dp * glowScale),
                    contentAlignment = Alignment.Center
                ) {
                    if (winner != null) {
                        val wName = if (winner == "X") viewModel.getPlayer1DisplayName() else viewModel.getPlayer2DisplayName()
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(titleColor.copy(alpha = 0.08f), androidx.compose.foundation.shape.CircleShape)
                                .border(BorderStroke(2.dp, titleColor), androidx.compose.foundation.shape.CircleShape)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "🏆",
                                fontSize = 38.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = wName,
                                color = textColorPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            drawCircle(
                                color = Color(0xFF00E5FF).copy(alpha = 0.5f),
                                radius = w * 0.25f,
                                center = Offset(w * 0.4f, h * 0.5f),
                                style = Stroke(width = 8f)
                            )
                            drawCircle(
                                color = Color(0xFFFF2D55).copy(alpha = 0.5f),
                                radius = w * 0.25f,
                                center = Offset(w * 0.6f, h * 0.5f),
                                style = Stroke(width = 8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rich comparison card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val isAr = viewModel.appLanguage.value == "AR"
                    Text(
                        text = if (isAr) "إحصائيات المواجهة الحالية 📊" else "Current Battle Stats 📊",
                        color = accentColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Competitor 1 (X)
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = p1Name,
                                color = Color(0xFF00E5FF),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            ComparisonStatRow(
                                label = if (isAr) "عدد النقرات" else "Total Clicks",
                                value = "$p1Moves",
                                color = textColorPrimary
                            )
                            ComparisonStatRow(
                                label = if (isAr) "وقت التفكير" else "Thinking Time",
                                value = "$p1Seconds s",
                                color = textColorPrimary
                            )
                            ComparisonStatRow(
                                label = if (isAr) "التلميحات" else "Hints Used",
                                value = "$p1HintsUsed",
                                color = textColorPrimary
                            )
                        }

                        // Vertical Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(120.dp)
                                .background(cardBorder.copy(alpha = 0.5f))
                        )

                        // Competitor 2 (O)
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = p2Name,
                                color = Color(0xFFFF2D55),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            ComparisonStatRow(
                                label = if (isAr) "عدد النقرات" else "Total Clicks",
                                value = "$p2Moves",
                                color = textColorPrimary
                            )
                            ComparisonStatRow(
                                label = if (isAr) "وقت التفكير" else "Thinking Time",
                                value = "$p2Seconds s",
                                color = textColorPrimary
                            )
                            ComparisonStatRow(
                                label = if (isAr) "التلميحات" else "Hints Used",
                                value = "$p2HintsUsed",
                                color = textColorPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons
            val isArLanguage = viewModel.appLanguage.collectAsState().value == "AR"
            if (gameMode == "TOURNAMENT") {
                val matches by viewModel.tournamentMatches.collectAsState()
                val currentIndex by viewModel.currentTournamentMatchIndex.collectAsState()
                val isReplay by viewModel.isTournamentReplay.collectAsState()
                
                if (isReplay) {
                    // Tie break replay is active
                    Button(
                        onClick = onPlayAgain,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = if (isLight) Color.White else Color(0xFF090C15)
                        )
                    ) {
                        Text(
                            text = if (isArLanguage) "إعادة المباراة لحسم التعادل ⚔️" else "Replay Match to Break Tie ⚔️",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onMainMenu,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = textColorPrimary
                        ),
                        border = BorderStroke(1.dp, cardBorder)
                    ) {
                        Text(
                            text = if (isArLanguage) "العودة للقائمة الرئيسية" else "Back to Main Menu",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // Match completed
                    val isLastMatch = currentIndex == matches.size - 1
                    Button(
                        onClick = {
                            viewModel.proceedToNextTournamentMatch()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = if (isLight) Color.White else Color(0xFF090C15)
                        )
                    ) {
                        Text(
                            text = if (isLastMatch) {
                                if (isArLanguage) "تتويج بطل البطولة وإعلانه 🏆" else "Announce & Crown Champion 🏆"
                            } else {
                                if (isArLanguage) "الانتقال للمباراة التالية ⚔️" else "Proceed to Next Match ⚔️"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.proceedToNextTournamentMatch()
                            onMainMenu() // Sets showDashboard = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = textColorPrimary
                        ),
                        border = BorderStroke(1.dp, cardBorder)
                    ) {
                        Text(
                            text = if (isArLanguage) "لوحة صدارة البطولة 📊" else "Tournament Standings 📊",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                Button(
                    onClick = onPlayAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = if (isLight) Color.White else Color(0xFF090C15)
                    )
                ) {
                    Text(text = Localizer.get("play_again"), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onMainMenu,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = textColorPrimary
                    ),
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Text(text = Localizer.get("main_menu"), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ConfettiRain() {
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "animProgress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w == 0f || h == 0f) return@Canvas

        val colors = listOf(
            Color(0xFF00E5FF), Color(0xFFFF2D55), Color(0xFFFFD700),
            Color(0xFF4ADE80), Color(0xFFA855F7), Color(0xFFF97316)
        )

        for (i in 0 until 50) {
            val seed = i * 17
            val startX = (seed % 100) / 100f * w
            val speedY = 0.5f + ((seed % 6) / 10f)
            val swingAmplitude = 10.dp.toPx() + (seed % 4) * 4.dp.toPx()
            val swingSpeed = 3f + (seed % 3) * 2f

            val currentY = (speedY * animProgress * h * 1.15f - 15.dp.toPx()) % (h + 30.dp.toPx())
            val currentX = startX + Math.sin((animProgress * swingSpeed + i).toDouble()).toFloat() * swingAmplitude

            val sizeW = 6.dp.toPx() + (seed % 3) * 2.dp.toPx()
            val sizeH = 3.dp.toPx() + (seed % 2) * 2.dp.toPx()
            val color = colors[i % colors.size]

            val angle = (animProgress * 360f * (1f + (seed % 2))) + seed

            rotate(degrees = angle, pivot = Offset(currentX, currentY)) {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(currentX - sizeW / 2, currentY - sizeH / 2),
                    size = androidx.compose.ui.geometry.Size(sizeW, sizeH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun ScoreDetailItem(label: String, score: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color(0xFF64748B), fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = score.toString(), color = color, fontSize = 22.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun ComparisonStatRow(label: String, value: String, color: Color) {
    Column(
        modifier = Modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF64748B)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    theme: String
) {
    val isLight = theme == "LIGHT"
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = placeholder, color = if (isLight) Color(0xFF64748B) else Color(0xFF94A3B8), fontSize = 14.sp) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = if (isLight) Color(0xFF0F172A) else Color.White,
            unfocusedTextColor = if (isLight) Color(0xFF0F172A) else Color.White,
            focusedBorderColor = if (isLight) Color(0xFF8B5CF6) else Color(0xFF00E5FF),
            unfocusedBorderColor = if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E293B),
            focusedContainerColor = if (isLight) Color.White else Color(0xFF0C1020),
            unfocusedContainerColor = if (isLight) Color.White else Color(0xFF0C1020)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

data class StandingRow(
    val playerIdx: Int,
    val name: String,
    val title: String,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val points: Int
)

@Composable
fun StandingsList(
    standings: List<StandingRow>,
    isAr: Boolean,
    textColorPrimary: Color,
    textColorSecondary: Color,
    accentColor: Color
) {
    Column {
        standings.forEachIndexed { index, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(2f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${index + 1}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Column {
                        Text(text = row.title, fontSize = 10.sp, color = accentColor, fontWeight = FontWeight.Bold)
                        Text(text = row.name, fontSize = 13.sp, color = textColorPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Text(text = "${row.wins}", color = textColorPrimary, fontSize = 13.sp, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                Text(text = "${row.draws}", color = textColorPrimary, fontSize = 13.sp, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                Text(text = "${row.losses}", color = textColorPrimary, fontSize = 13.sp, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                Text(text = "${row.points}", color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
            }
        }
    }
}

@Composable
fun TournamentDetailsDialog(
    tournament: com.example.data.Tournament,
    viewModel: com.example.ui.GameViewModel,
    onDismiss: () -> Unit,
    onResume: () -> Unit
) {
    val isLight = viewModel.appTheme.collectAsState().value == "LIGHT"
    val textColorPrimary = if (isLight) Color(0xFF0F172A) else Color.White
    val textColorSecondary = if (isLight) Color(0xFF475569) else Color(0xFF94A3B8)
    val cardBg = if (isLight) Color.White else Color(0xFF121829)
    val cardBorder = if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E293B)
    val accentColor = if (isLight) Color(0xFF8B5CF6) else Color(0xFF00E5FF)
    val isAr = viewModel.appLanguage.collectAsState().value == "AR"

    val players = remember(tournament) { com.example.ui.deserializePlayers(tournament.playersData) }
    val matches = remember(tournament) { com.example.ui.deserializeMatches(tournament.matchesData) }

    val standings = remember(players, matches) {
        players.indices.map { idx ->
            var wins = 0
            var draws = 0
            var losses = 0
            var points = 0
            matches.forEach { m ->
                val p1Actual = getActualPlayerIndexForMatchStatic(m.player1Idx, matches, players.size)
                val p2Actual = getActualPlayerIndexForMatchStatic(m.player2Idx, matches, players.size)
                if (m.winnerIdx != -1) {
                    if (p1Actual == idx) {
                        when (m.winnerIdx) {
                            0 -> { wins++; points += 3 }
                            2 -> { draws++; points += 1 }
                            1 -> { losses++ }
                        }
                    } else if (p2Actual == idx) {
                        when (m.winnerIdx) {
                            1 -> { wins++; points += 3 }
                            2 -> { draws++; points += 1 }
                            0 -> { losses++ }
                        }
                    }
                }
            }
            StandingRow(
                playerIdx = idx,
                name = players[idx].first,
                title = players[idx].second,
                wins = wins,
                draws = draws,
                losses = losses,
                points = points
            )
        }.sortedByDescending { it.points }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, cardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (isAr) "تفاصيل بطولة: ${tournament.name}" else "Tournament: ${tournament.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColorPrimary
                )
                Text(
                    text = if (isAr) "نوع البطولة: ${if (tournament.type == "KNOCKOUT") "خروج المغلوب ⚔️" else "دوري نقاط 📊"}" else "Format: ${tournament.type}",
                    fontSize = 12.sp,
                    color = accentColor,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .heightIn(max = 240.dp)
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        // Display Standings Table
                        Text(
                            text = if (isAr) "جدول الترتيب الحالي 🏆" else "Current Standings 🏆",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = textColorPrimary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        standings.forEachIndexed { rank, row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(if (rank % 2 == 0) accentColor.copy(alpha = 0.04f) else Color.Transparent, RoundedCornerShape(4.dp))
                                    .padding(6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${rank + 1}. [${row.title}] ${row.name}",
                                    color = textColorPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1.5f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Pts: ${row.points} (${row.wins}W - ${row.draws}D)",
                                    color = textColorSecondary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Display Matches
                        Text(
                            text = if (isAr) "نتائج المباريات ⚔️" else "Matches Log ⚔️",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = textColorPrimary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        matches.forEachIndexed { mIdx, m ->
                            val p1 = players[m.player1Idx]
                            val p2 = players[m.player2Idx]
                            val resultText = when (m.winnerIdx) {
                                0 -> if (isAr) "فوز ${p1.first}" else "${p1.first} Won"
                                1 -> if (isAr) "فوز ${p2.first}" else "${p2.first} Won"
                                2 -> if (isAr) "تعادل" else "Draw"
                                else -> if (isAr) "قيد الانتظار ⏳" else "Pending ⏳"
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .border(BorderStroke(1.dp, cardBorder.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "M${mIdx + 1}: ${p1.first} vs ${p2.first}",
                                    color = textColorPrimary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1.5f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = resultText,
                                    color = if (m.winnerIdx != -1) accentColor else textColorSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = cardBorder, contentColor = textColorPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = if (isAr) "إغلاق" else "Close")
                    }

                    if (tournament.winnerName == null) {
                        Button(
                            onClick = {
                                onResume()
                                onDismiss()
                            },
                            modifier = Modifier.weight(1.5f),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = if (isLight) Color.White else Color(0xFF090C15)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = if (isAr) "استكمال البطولة ⚔️" else "Resume ⚔️")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TournamentHistoryItemCard(
    tournament: com.example.data.Tournament,
    theme: String,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val isLight = theme == "LIGHT"
    val isAr = Localizer.currentLanguage == "AR"

    val statusText = if (tournament.winnerName != null) {
        tournament.winnerName
    } else {
        if (isAr) "غير مكتملة" else "In Progress"
    }

    val statusColor = if (tournament.winnerName != null) {
        Color(0xFF4ADE80)
    } else {
        Color(0xFF94A3B8)
    }

    val formattedDate = remember(tournament.timestamp) {
        try {
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            sdf.format(Date(tournament.timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 500.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = if (isLight) Color.White else Color(0xFF0F1422)),
        border = BorderStroke(1.dp, if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E293B).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isAr) "بطولة: ${tournament.name}" else "Tournament: ${tournament.name}",
                    color = if (isLight) Color(0xFF0F172A) else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${if (tournament.type == "KNOCKOUT") (if (isAr) "خروج المغلوب" else "Knockout") else (if (isAr) "دوري نقاط" else "Round Robin")} | $formattedDate",
                    color = if (isLight) Color(0xFF64748B) else Color(0xFF475569),
                    fontSize = 11.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .border(BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = statusText ?: "",
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFFF2D55)
                    )
                }
            }
        }
    }
}

@Composable
fun TournamentSetupScreen(
    viewModel: com.example.ui.GameViewModel,
    onBack: () -> Unit
) {
    val isLight = viewModel.appTheme.collectAsState().value == "LIGHT"
    val accentColor = if (isLight) Color(0xFF8B5CF6) else Color(0xFF00E5FF)
    val cardBgColor = if (isLight) Color.White else Color(0xFF0F1426)
    val cardBorderColor = if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E293B)
    val textColorPrimary = if (isLight) Color(0xFF0F172A) else Color.White
    val textColorSecondary = if (isLight) Color(0xFF475569) else Color(0xFF94A3B8)
    val isAr = viewModel.appLanguage.collectAsState().value == "AR"

    var isHistoryView by remember { mutableStateOf(false) }
    var selectedTournament by remember { mutableStateOf<com.example.data.Tournament?>(null) }
    val savedTournaments by viewModel.tournamentsList.collectAsState()

    var tournamentName by remember { mutableStateOf(if (isAr) "بطولة النخبة 🏆" else "Elite Championship 🏆") }
    var playerCount by remember { mutableStateOf(4) }
    var tournamentType by remember { mutableStateOf("KNOCKOUT") }

    val defaultTitles = listOf("الملك", "العبقري", "الشبح", "القائد", "المدمر", "الفارس", "الداهية", "المهندس", "الفيلسوف", "الأمير")
    var players by remember {
        mutableStateOf(
            List(8) { idx -> 
                Pair(if (isAr) "اللاعب ${idx + 1}" else "Player ${idx + 1}", defaultTitles[idx % defaultTitles.size]) 
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Back",
                    tint = textColorPrimary
                )
            }
            Text(
                text = if (isAr) "إعداد البطولة 🏆" else "Tournament Setup 🏆",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColorPrimary
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs Toggle: New Tournament vs History
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (!isHistoryView) accentColor.copy(alpha = 0.15f) else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        BorderStroke(
                            if (!isHistoryView) 2.dp else 1.dp,
                            if (!isHistoryView) accentColor else cardBorderColor
                        ),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { isHistoryView = false }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isAr) "بطولة جديدة ⚔️" else "New Tournament ⚔️",
                    fontWeight = FontWeight.Bold,
                    color = if (!isHistoryView) accentColor else textColorSecondary
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (isHistoryView) accentColor.copy(alpha = 0.15f) else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        BorderStroke(
                            if (isHistoryView) 2.dp else 1.dp,
                            if (isHistoryView) accentColor else cardBorderColor
                        ),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { isHistoryView = true }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isAr) "سجل البطولات 📜" else "Tournaments History 📜",
                    fontWeight = FontWeight.Bold,
                    color = if (isHistoryView) accentColor else textColorSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isHistoryView) {
            // Tournaments History View
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAr) "سجل البطولات التاريخية 📜" else "Championships History 📜",
                    color = textColorPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                if (savedTournaments.isNotEmpty()) {
                    Button(
                        onClick = { viewModel.clearAllTournaments() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2D55)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = if (isAr) "الحذف الكامل 🗑️" else "Delete All 🗑️",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (savedTournaments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isAr) "لا توجد بطولات مسجلة حالياً. أنشئ بطولة جديدة للبدء!" else "No saved tournaments found. Launch a new one to start!",
                        color = textColorSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    savedTournaments.forEach { t ->
                        TournamentHistoryItemCard(
                            tournament = t,
                            theme = viewModel.appTheme.value,
                            onDelete = { viewModel.deleteTournamentById(t.id) },
                            onClick = { 
                                viewModel.resumeTournament(t)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        } else {
            // Creation View
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isAr) "اسم البطولة:" else "Tournament Name:",
                        color = textColorPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CustomTextField(
                        value = tournamentName,
                        onValueChange = { tournamentName = it },
                        placeholder = if (isAr) "أدخل اسم البطولة" else "Enter Tournament Name",
                        theme = viewModel.appTheme.value
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isAr) "عدد اللاعبين المتنافسين:" else "Number of Competitors:",
                        color = textColorPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf(4, 8).forEach { count ->
                            val isSelected = playerCount == count
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) accentColor.copy(alpha = 0.15f) else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        BorderStroke(
                                            if (isSelected) 2.dp else 1.dp,
                                            if (isSelected) accentColor else cardBorderColor
                                        ),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { playerCount = count }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isAr) "$count لاعبين" else "$count Players",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) accentColor else textColorSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isAr) "نظام البطولة:" else "Tournament Format:",
                        color = textColorPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("KNOCKOUT", "LEAGUE").forEach { type ->
                            val isSelected = tournamentType == type
                            val label = if (type == "KNOCKOUT") {
                                if (isAr) "خروج المغلوب ⚔️" else "Knockout ⚔️"
                            } else {
                                if (isAr) "دوري النقاط 📊" else "Round Robin 📊"
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) accentColor.copy(alpha = 0.15f) else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        BorderStroke(
                                            if (isSelected) 2.dp else 1.dp,
                                            if (isSelected) accentColor else cardBorderColor
                                        ),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { tournamentType = type }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) accentColor else textColorSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isAr) "أسماء اللاعبين وألقابهم ⚔️" else "Player Names & Titles ⚔️",
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isAr) "اضغط على اللقب لتغييره عشوائياً" else "Click the title to cycle it",
                        color = textColorSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    for (i in 0 until playerCount) {
                        val p = players[i]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(accentColor.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "${i + 1}", color = accentColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedTextField(
                                value = p.first,
                                onValueChange = { newName ->
                                    players = players.toMutableList().apply {
                                        this[i] = Pair(newName, p.second)
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textColorPrimary,
                                    unfocusedTextColor = textColorPrimary,
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = cardBorderColor,
                                    focusedContainerColor = if (isLight) Color.White else Color(0xFF0C1020),
                                    unfocusedContainerColor = if (isLight) Color.White else Color(0xFF0C1020)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .border(BorderStroke(1.dp, accentColor), RoundedCornerShape(8.dp))
                                    .clickable {
                                        val currentIdx = defaultTitles.indexOf(p.second)
                                        val nextIdx = (if (currentIdx == -1) 0 else currentIdx + 1) % defaultTitles.size
                                        players = players.toMutableList().apply {
                                            this[i] = Pair(p.first, defaultTitles[nextIdx])
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = p.second,
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val finalPlayers = players.take(playerCount)
                    viewModel.startNewTournament(tournamentName, tournamentType, finalPlayers)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = if (isLight) Color.White else Color(0xFF090C15))
            ) {
                Text(
                    text = if (isAr) "بدء المواجهات الحماسية ⚔️" else "Launch Current Matches ⚔️",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    // Detail modal display
    if (selectedTournament != null) {
        TournamentDetailsDialog(
            tournament = selectedTournament!!,
            viewModel = viewModel,
            onDismiss = { selectedTournament = null },
            onResume = {
                viewModel.resumeTournament(selectedTournament!!)
            }
        )
    }
}

private fun getActualPlayerIndexForMatchStatic(idx: Int, matches: List<com.example.ui.TournamentMatch>, playerCount: Int): Int {
    if (idx < playerCount) {
        return idx
    }
    val matchIdx = idx - playerCount
    if (matchIdx >= 0 && matchIdx < matches.size) {
        val m = matches[matchIdx]
        if (m.winnerIdx == 0) {
            return getActualPlayerIndexForMatchStatic(m.player1Idx, matches, playerCount)
        } else if (m.winnerIdx == 1) {
            return getActualPlayerIndexForMatchStatic(m.player2Idx, matches, playerCount)
        }
    }
    return -1
}

@Composable
fun KnockoutBracketView(
    players: List<Pair<String, String>>,
    matches: List<com.example.ui.TournamentMatch>,
    currentIndex: Int,
    isAr: Boolean,
    textColorPrimary: Color,
    textColorSecondary: Color,
    accentColor: Color,
    cardBgColor: Color,
    cardBorderColor: Color
) {
    val is8Players = players.size == 8
    val rounds = if (is8Players) {
        listOf(
            (if (isAr) "ربع النهائي ⚔️" else "Quarter-Finals ⚔️") to matches.subList(0, 4),
            (if (isAr) "نصف النهائي 🏆" else "Semi-Finals 🏆") to matches.subList(4, 6),
            (if (isAr) "النهائي الكبير 👑" else "Grand Final 👑") to matches.subList(6, 7)
        )
    } else {
        listOf(
            (if (isAr) "نصف النهائي 🏆" else "Semi-Finals 🏆") to matches.subList(0, 2),
            (if (isAr) "النهائي الكبير 👑" else "Grand Final 👑") to matches.subList(2, 3)
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        for ((roundTitle, roundMatches) in rounds) {
            Text(
                text = roundTitle,
                color = accentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (match in roundMatches) {
                    val p1IdxResolved = getActualPlayerIndexForMatchStatic(match.player1Idx, matches, players.size)
                    val p2IdxResolved = getActualPlayerIndexForMatchStatic(match.player2Idx, matches, players.size)
                    
                    val p1Name = if (p1IdxResolved in players.indices) players[p1IdxResolved].first else "?"
                    val p2Name = if (p2IdxResolved in players.indices) players[p2IdxResolved].first else "?"
                    
                    val p1Title = if (p1IdxResolved in players.indices) players[p1IdxResolved].second else ""
                    val p2Title = if (p2IdxResolved in players.indices) players[p2IdxResolved].second else ""
                    
                    val isMatchPlayed = match.winnerIdx != -1
                    val matchGlobalIndex = matches.indexOf(match)
                    val isCurrentPlaying = matchGlobalIndex == currentIndex

                    val borderCol = if (isCurrentPlaying) accentColor else if (isMatchPlayed) cardBorderColor.copy(alpha = 0.5f) else cardBorderColor

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(BorderStroke(if (isCurrentPlaying) 2.dp else 1.dp, borderCol), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrentPlaying) accentColor.copy(alpha = 0.05f) else cardBgColor
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Player 1 Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    if (p1Title.isNotEmpty()) {
                                        Text(text = p1Title, fontSize = 8.sp, color = accentColor, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                    }
                                    Text(
                                        text = p1Name,
                                        fontSize = 11.sp,
                                        color = if (isMatchPlayed && match.winnerIdx == 0) Color(0xFF00E5FF) else textColorPrimary,
                                        fontWeight = if (isMatchPlayed && match.winnerIdx == 0) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                                if (isMatchPlayed && match.winnerIdx == 0) {
                                    Text(text = "✓", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "vs", fontSize = 9.sp, color = textColorSecondary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))

                            // Player 2 Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    if (p2Title.isNotEmpty()) {
                                        Text(text = p2Title, fontSize = 8.sp, color = accentColor, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                    }
                                    Text(
                                        text = p2Name,
                                        fontSize = 11.sp,
                                        color = if (isMatchPlayed && match.winnerIdx == 1) Color(0xFFFF2D55) else textColorPrimary,
                                        fontWeight = if (isMatchPlayed && match.winnerIdx == 1) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                                if (isMatchPlayed && match.winnerIdx == 1) {
                                    Text(text = "✓", color = Color(0xFFFF2D55), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TournamentDashboardScreen(
    viewModel: com.example.ui.GameViewModel,
    onPlayMatch: () -> Unit,
    onBack: () -> Unit
) {
    val isLight = viewModel.appTheme.collectAsState().value == "LIGHT"
    val accentColor = if (isLight) Color(0xFF8B5CF6) else Color(0xFF00E5FF)
    val cardBgColor = if (isLight) Color.White else Color(0xFF0F1426)
    val cardBorderColor = if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E293B)
    val textColorPrimary = if (isLight) Color(0xFF0F172A) else Color.White
    val textColorSecondary = if (isLight) Color(0xFF475569) else Color(0xFF94A3B8)
    val isAr = viewModel.appLanguage.collectAsState().value == "AR"

    val players by viewModel.tournamentPlayers.collectAsState()
    val matches by viewModel.tournamentMatches.collectAsState()
    val currentIndex by viewModel.currentTournamentMatchIndex.collectAsState()

    val standings = remember(players, matches) {
        players.indices.map { idx ->
            var wins = 0
            var draws = 0
            var losses = 0
            var points = 0
            matches.forEach { m ->
                val p1Actual = getActualPlayerIndexForMatchStatic(m.player1Idx, matches, players.size)
                val p2Actual = getActualPlayerIndexForMatchStatic(m.player2Idx, matches, players.size)
                if (m.winnerIdx != -1) {
                    if (p1Actual == idx) {
                        when (m.winnerIdx) {
                            0 -> { wins++; points += 3 }
                            2 -> { draws++; points += 1 }
                            1 -> { losses++ }
                        }
                    } else if (p2Actual == idx) {
                        when (m.winnerIdx) {
                            1 -> { wins++; points += 3 }
                            2 -> { draws++; points += 1 }
                            0 -> { losses++ }
                        }
                    }
                }
            }
            StandingRow(
                playerIdx = idx,
                name = players[idx].first,
                title = players[idx].second,
                wins = wins,
                draws = draws,
                losses = losses,
                points = points
            )
        }.sortedByDescending { it.points }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = textColorPrimary
                )
            }
            Text(
                text = if (isAr) "لوحة صدارة البطولة 🏆" else "Championship Dashboard 🏆",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColorPrimary
            )
            IconButton(
                onClick = {
                    viewModel.clearActiveTournament()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = if (isAr) "إلغاء البطولة" else "Cancel Tournament",
                    tint = Color(0xFFFF2D55)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (currentIndex in matches.indices) {
            val currentMatch = matches[currentIndex]
            val p1IdxActual = getActualPlayerIndexForMatchStatic(currentMatch.player1Idx, matches, players.size)
            val p2IdxActual = getActualPlayerIndexForMatchStatic(currentMatch.player2Idx, matches, players.size)
            val p1 = if (p1IdxActual in players.indices) players[p1IdxActual] else Pair("?", "")
            val p2 = if (p2IdxActual in players.indices) players[p2IdxActual] else Pair("?", "")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.08f)),
                border = BorderStroke(2.dp, accentColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isAr) "المواجهة القادمة حان وقتها! ⚔️" else "Next Match is Live! ⚔️",
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isAr) "الجولة رقم ${currentIndex + 1} من أصل ${matches.size}" else "Round ${currentIndex + 1} of ${matches.size}",
                        color = textColorSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = p1.second, color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = p1.first, color = textColorPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "X", color = Color(0xFF00E5FF), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        }

                        Text(text = "VS", color = textColorSecondary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = p2.second, color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = p2.first, color = textColorPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "O", color = Color(0xFFFF2D55), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onPlayMatch,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = if (isLight) Color.White else Color(0xFF090C15))
                    ) {
                        Text(
                            text = if (isAr) "دخول ساحة المعركة ⚔️" else "Enter Arena ⚔️",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val tournamentType by viewModel.tournamentType.collectAsState()

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            border = BorderStroke(1.dp, cardBorderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (tournamentType == "KNOCKOUT") {
                    Text(
                        text = if (isAr) "شجرة مواجهات البطولة (خروج المغلوب) 🏆" else "Tournament Bracket Tree (Knockout) 🏆",
                        color = textColorPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    KnockoutBracketView(
                        players = players,
                        matches = matches,
                        currentIndex = currentIndex,
                        isAr = isAr,
                        textColorPrimary = textColorPrimary,
                        textColorSecondary = textColorSecondary,
                        accentColor = accentColor,
                        cardBgColor = cardBgColor,
                        cardBorderColor = cardBorderColor
                    )
                } else {
                    Text(
                        text = if (isAr) "ترتيب المتنافسين 🏆" else "Standings 🏆",
                        color = textColorPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = if (isAr) "اللاعب" else "Player", color = textColorSecondary, fontSize = 12.sp, modifier = Modifier.weight(2f))
                        Text(text = if (isAr) "فوز" else "W", color = textColorSecondary, fontSize = 12.sp, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                        Text(text = if (isAr) "تعادل" else "D", color = textColorSecondary, fontSize = 12.sp, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                        Text(text = if (isAr) "خسارة" else "L", color = textColorSecondary, fontSize = 12.sp, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                        Text(text = if (isAr) "نقاط" else "Pts", color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
                    }

                    StandingsList(standings = standings, isAr = isAr, textColorPrimary = textColorPrimary, textColorSecondary = textColorSecondary, accentColor = accentColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            border = BorderStroke(1.dp, cardBorderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isAr) "سجل المواجهات" else "Match History",
                    color = textColorPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                matches.forEachIndexed { idx, m ->
                    val p1IdxActual = getActualPlayerIndexForMatchStatic(m.player1Idx, matches, players.size)
                    val p2IdxActual = getActualPlayerIndexForMatchStatic(m.player2Idx, matches, players.size)
                    val p1 = if (p1IdxActual in players.indices) players[p1IdxActual] else Pair("?", "")
                    val p2 = if (p2IdxActual in players.indices) players[p2IdxActual] else Pair("?", "")
                    val isPlayed = m.winnerIdx != -1
                    val statusText = if (isPlayed) {
                        if (m.winnerIdx == 2) {
                            if (isAr) "تعادل" else "Draw"
                        } else {
                            val winnerName = if (m.winnerIdx == 0) p1.first else p2.first
                            if (isAr) "فاز $winnerName" else "$winnerName Won"
                        }
                    } else {
                        if (isAr) "قيد الانتظار" else "Pending"
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${idx + 1}. ${p1.first} vs ${p2.first}",
                            color = textColorPrimary,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(2f)
                        )
                        Text(
                            text = statusText,
                            color = if (isPlayed) accentColor else textColorSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isPlayed) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun TournamentChampionScreen(
    viewModel: com.example.ui.GameViewModel,
    onFinish: () -> Unit,
    onViewLeaderboard: () -> Unit
) {
    val isLight = viewModel.appTheme.collectAsState().value == "LIGHT"
    val accentColor = if (isLight) Color(0xFF8B5CF6) else Color(0xFF00E5FF)
    val textColorPrimary = if (isLight) Color(0xFF0F172A) else Color.White
    val textColorSecondary = if (isLight) Color(0xFF475569) else Color(0xFF94A3B8)
    val isAr = viewModel.appLanguage.collectAsState().value == "AR"

    val winnerName by viewModel.tournamentWinnerName.collectAsState()

    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnimation = true
    }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1200, easing = LinearOutSlowInEasing),
        label = "alpha"
    )
    val translateYAnim by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 40f,
        animationSpec = tween(1200, easing = LinearOutSlowInEasing),
        label = "translateY"
    )

    // Crown floating & pulsing animations
    val infiniteTransition = rememberInfiniteTransition(label = "champion_crown")
    
    val crownScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "crownScale"
    )

    val crownRotation by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "crownRotation"
    )

    val rayRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rayRotation"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Glorious continuous confetti rain falling in background
        ConfettiRain()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
                .graphicsLayer(
                    alpha = alphaAnim,
                    translationY = translateYAnim
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                // Majestic spinning rays behind the crown
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(rotationZ = rayRotation)
                ) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.width / 2
                    
                    val rayCount = 8
                    val angleStep = 360f / rayCount
                    for (i in 0 until rayCount) {
                        val path = Path().apply {
                            moveTo(center.x, center.y)
                            val angle1 = Math.toRadians((i * angleStep - 15).toDouble())
                            val angle2 = Math.toRadians((i * angleStep + 15).toDouble())
                            lineTo(
                                (center.x + radius * Math.cos(angle1)).toFloat(),
                                (center.y + radius * Math.sin(angle1)).toFloat()
                            )
                            lineTo(
                                (center.x + radius * Math.cos(angle2)).toFloat(),
                                (center.y + radius * Math.sin(angle2)).toFloat()
                            )
                            close()
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFFFFD700).copy(alpha = 0.15f)
                        )
                    }
                    
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFD700).copy(alpha = 0.35f), Color.Transparent),
                            center = center,
                            radius = radius
                        )
                    )
                }
                
                // Pulsing, floating crown
                Text(
                    text = "👑",
                    fontSize = 84.sp,
                    modifier = Modifier.graphicsLayer(
                        scaleX = crownScale,
                        scaleY = crownScale,
                        rotationZ = crownRotation
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isAr) "بطل البطولة الأسطوري! 🎉" else "Legendary Champion! 🎉",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFFD700),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = winnerName ?: (if (isAr) "البطل المجهول" else "Unknown Champion"),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = textColorPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isAr) {
                    "تهانينا الحارة للبطل المغوار على سحق جميع الخصوم في هذه المنافسة الملحمية!"
                } else {
                    "Warm congratulations to the legendary fighter for defeating all competitors in this epic battle!"
                },
                fontSize = 14.sp,
                color = textColorSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Button 1: Back to Main Menu
            Button(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = if (isLight) Color.White else Color(0xFF090C15)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = if (isAr) "العودة للقائمة الرئيسية 🏠" else "Back to Main Menu 🏠",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Button 2: Return to Standings / Leaderboard
            OutlinedButton(
                onClick = onViewLeaderboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(2.dp, accentColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = accentColor,
                    containerColor = Color.Transparent
                )
            ) {
                Text(
                    text = if (isAr) "عرض لوحة الصدارة وجدول المواجهات 📊" else "View Standings & Match History 📊",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
