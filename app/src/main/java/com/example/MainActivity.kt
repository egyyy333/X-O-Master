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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
                            FantasyAnimatedBackground(theme = appTheme)

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
    when (activeScreen) {
        ActiveScreen.HOME -> HomeScreen(viewModel = viewModel, onStartClicked = onNavigateToGame)
        ActiveScreen.GAME -> GameScreen(viewModel = viewModel, onBackClicked = onBackToHome)
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModeButton(
                        label = Localizer.get("mode_bot"),
                        icon = Icons.Default.SmartToy,
                        isSelected = activeMode == "VS_BOT",
                        theme = appTheme,
                        modifier = Modifier.weight(1f),
                        onClick = { 
                            triggerClickFeedback()
                            viewModel.setGameMode("VS_BOT") 
                        },
                        testTag = "mode_bot_button"
                    )

                    ModeButton(
                        label = Localizer.get("mode_friend"),
                        icon = Icons.Default.People,
                        isSelected = activeMode == "VS_FRIEND",
                        theme = appTheme,
                        modifier = Modifier.weight(1f),
                        onClick = { 
                            triggerClickFeedback()
                            viewModel.setGameMode("VS_FRIEND") 
                        },
                        testTag = "mode_friend_button"
                    )
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
                    description = Localizer.get("difficulty_easy_desc"),
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
                    description = Localizer.get("difficulty_medium_desc"),
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
                    description = Localizer.get("difficulty_hard_desc"),
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

        // Step 3: Choose Symbol (X or O)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            border = BorderStroke(1.dp, cardBorderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = Localizer.get("symbol_title"),
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SymbolSelectButton(
                        symbol = "X",
                        color = Color(0xFF00E5FF),
                        isSelected = chosenSymbol == "X",
                        theme = appTheme,
                        onClick = { 
                            triggerClickFeedback()
                            viewModel.setPlayerSymbol("X") 
                        },
                        testTag = "symbol_x_select"
                    )

                    SymbolSelectButton(
                        symbol = "O",
                        color = Color(0xFFFF2D55),
                        isSelected = chosenSymbol == "O",
                        theme = appTheme,
                        onClick = { 
                            triggerClickFeedback()
                            viewModel.setPlayerSymbol("O") 
                        },
                        testTag = "symbol_o_select"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                        imageVector = Icons.Default.Refresh,
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
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = Localizer.get("player_x"), color = textColorPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                Icon(
                                    imageVector = if (gameMode == "VS_BOT") Icons.Default.SmartToy else Icons.Default.People,
                                    contentDescription = null,
                                    tint = Color(0xFFFF2D55),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (gameMode == "VS_BOT") Localizer.get("bot_o") else Localizer.get("player_o"),
                            color = textColorPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
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
                        .height(48.dp)
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
                        .height(48.dp)
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
                        .height(48.dp)
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
    val history by viewModel.history.collectAsState()
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
        isWin -> Localizer.get("stats_wins")
        isTie -> Localizer.get("stats_draws")
        else -> Localizer.get("stats_losses")
    }

    val statusColor = when {
        isWin -> Color(0xFF4ADE80)
        isTie -> Color(0xFF94A3B8)
        else -> Color(0xFFFF2D55)
    }

    val modeText = when (record.gameMode) {
        "BOT_EASY" -> Localizer.get("difficulty_easy")
        "BOT_MEDIUM" -> Localizer.get("difficulty_medium")
        "BOT_HARD" -> Localizer.get("difficulty_hard")
        "FRIEND" -> Localizer.get("mode_friend")
        else -> "Battle"
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
    description: String,
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = if (isLight) Color(0xFF475569) else Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
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
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit
) {
    val isLight = theme == "LIGHT"
    val textColorPrimary = if (isLight) Color(0xFF0F172A) else Color.White
    val textColorSecondary = if (isLight) Color(0xFF475569) else Color(0xFF94A3B8)
    val cardBg = if (isLight) Color.White else Color(0xFF121829)
    val cardBorder = if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E293B)
    val accentColor = if (isLight) Color(0xFF8B5CF6) else Color(0xFF00E5FF)

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
                    if (gameMode == "VS_BOT" && winner == "O") Localizer.get("win_bot") else Localizer.get("win_congrats")
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

                Box(modifier = Modifier.size(90.dp * glowScale)) {
                    if (winner == "X") {
                        XSymbol(modifier = Modifier.fillMaxSize(), color = Color(0xFF00E5FF))
                    } else if (winner == "O") {
                        OSymbol(modifier = Modifier.fillMaxSize(), color = Color(0xFFFF2D55))
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

            // Score details card
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
                    Text(
                        text = Localizer.get("round_details"),
                        color = textColorSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ScoreDetailItem(label = Localizer.get("player_x"), score = scoreX, color = Color(0xFF00E5FF))
                        ScoreDetailItem(label = Localizer.get("stats_draws"), score = scoreDraws, color = textColorSecondary)
                        ScoreDetailItem(label = Localizer.get("player_o"), score = scoreO, color = Color(0xFFFF2D55))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons
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

            Spacer(modifier = Modifier.height(16.dp))
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
