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
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Navigation Screen enum
enum class ActiveScreen {
    HOME,
    GAME,
    STATS
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
                // Force Right-to-Left layout for native Arabic feeling
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val viewModel: GameViewModel = viewModel(factory = viewModelFactory)
                    
                    var activeScreen by remember { mutableStateOf(ActiveScreen.HOME) }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        bottomBar = {
                            FloatingNavigationBar(
                                activeScreen = activeScreen,
                                onTabSelected = { 
                                    activeScreen = it 
                                    GameSoundPlayer.playClick()
                                }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF090C15)) // Rich dark space theme
                                .drawBehind {
                                    // Custom visual decoration: Subtle glowing grid of futuristic cyber dots
                                    val columns = 12
                                    val rows = 24
                                    val cellW = size.width / columns
                                    val cellH = size.height / rows
                                    for (i in 0..columns) {
                                        for (j in 0..rows) {
                                            drawCircle(
                                                color = Color(0xFF1E293B).copy(alpha = 0.25f),
                                                radius = 2f,
                                                center = Offset(i * cellW, j * cellH)
                                            )
                                        }
                                    }
                                }
                                .padding(innerPadding)
                        ) {
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
    onTabSelected: (ActiveScreen) -> Unit
) {
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
                .background(Color(0xFF121829).copy(alpha = 0.95f), RoundedCornerShape(32.dp))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(32.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            NavBarItem(
                icon = Icons.Default.Home,
                label = "الرئيسية",
                isSelected = activeScreen == ActiveScreen.HOME,
                onClick = { onTabSelected(ActiveScreen.HOME) },
                testTag = "nav_home_tab"
            )
            NavBarItem(
                icon = Icons.Default.Gamepad,
                label = "الملعب",
                isSelected = activeScreen == ActiveScreen.GAME,
                onClick = { onTabSelected(ActiveScreen.GAME) },
                testTag = "nav_game_tab"
            )
            NavBarItem(
                icon = Icons.Default.BarChart,
                label = "سجل الإحصائيات",
                isSelected = activeScreen == ActiveScreen.STATS,
                onClick = { onTabSelected(ActiveScreen.STATS) },
                testTag = "nav_stats_tab"
            )
        }
    }
}

@Composable
fun NavBarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val activeColor = Color(0xFF00E5FF) // Cyber Neon Cyan
    val inactiveColor = Color(0xFF64748B)

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Custom Neon Pedestal Logo
        NeonPedestalLogo()

        Text(
            text = "إكس أو المحترف",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )

        Text(
            text = "تحدّ الذكاء الاصطناعي بمستويات مذهلة أو العب مع أصدقائك بنقرة سريعة",
            fontSize = 14.sp,
            color = Color(0xFF94A3B8),
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
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121829)),
            border = BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "١. نمط اللعبة",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModeButton(
                        label = "ضد الكمبيوتر (البوت)",
                        icon = Icons.Default.SmartToy,
                        isSelected = activeMode == "VS_BOT",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setGameMode("VS_BOT") },
                        testTag = "mode_bot_button"
                    )

                    ModeButton(
                        label = "ضد لاعب (ثنائي)",
                        icon = Icons.Default.People,
                        isSelected = activeMode == "VS_FRIEND",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setGameMode("VS_FRIEND") },
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
                    text = "٢. اختر مستوى الصعوبة للذكاء الاصطناعي",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                )

                RobotDifficultyCard(
                    difficulty = "EASY",
                    title = "مستوى سهل 🤖",
                    description = "الكمبيوتر يرتكب أخطاء عشوائية وهو مناسب للتدريب والتعلم السريع لجميع الأعمار.",
                    color = Color(0xFF4ADE80),
                    isSelected = activeDifficulty == "EASY",
                    onClick = { viewModel.setBotDifficulty("EASY") }
                )

                RobotDifficultyCard(
                    difficulty = "MEDIUM",
                    title = "مستوى متوسط 🧠",
                    description = "الكمبيوتر يلعب بنقلات أذكى ويبادر بالدفاع المنظم مع بعض الأخطاء التكتيكية.",
                    color = Color(0xFFFBBF24),
                    isSelected = activeDifficulty == "MEDIUM",
                    onClick = { viewModel.setBotDifficulty("MEDIUM") }
                )

                RobotDifficultyCard(
                    difficulty = "HARD",
                    title = "مستوى مستحيل 🔥",
                    description = "الذكاء الاصطناعي الكامل (مينيمكس). لا يخطئ أبداً، جرب أن تتحداه وتحصل على تعادل!",
                    color = Color(0xFFEF4444),
                    isSelected = activeDifficulty == "HARD",
                    onClick = { viewModel.setBotDifficulty("HARD") }
                )
            }
        }

        // Step 3: Choose Symbol (X or O)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121829)),
            border = BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "٣. اختر رمزك الأساسي",
                    color = Color(0xFF00E5FF),
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
                        onClick = { viewModel.setPlayerSymbol("X") },
                        testTag = "symbol_x_select"
                    )

                    SymbolSelectButton(
                        symbol = "O",
                        color = Color(0xFFFF2D55),
                        isSelected = chosenSymbol == "O",
                        onClick = { viewModel.setPlayerSymbol("O") },
                        testTag = "symbol_o_select"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Start Adventure Play Button
        Button(
            onClick = {
                GameSoundPlayer.playClick()
                onStartClicked()
            },
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 350.dp)
                .height(56.dp)
                .testTag("start_game_button"),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00E5FF),
                contentColor = Color(0xFF090C15)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(imageVector = Icons.Default.Gamepad, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "ابدأ المعركة الحماسية", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(110.dp)) // Extra padding so it doesn't overlap the floating navbar
    }
}

@Composable
fun ModeButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    testTag: String
) {
    val activeBorder = Color(0xFF00E5FF)
    val inactiveBorder = Color(0xFF1E293B)
    val activeBg = Color(0xFF1A2642)
    val inactiveBg = Color(0xFF0F1422)

    Box(
        modifier = modifier
            .height(96.dp)
            .background(if (isSelected) activeBg else inactiveBg, RoundedCornerShape(16.dp))
            .border(
                BorderStroke(1.dp, if (isSelected) activeBorder else inactiveBorder),
                RoundedCornerShape(16.dp)
            )
            .clickable {
                GameSoundPlayer.playClick()
                onClick()
            }
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
                tint = if (isSelected) Color(0xFF00E5FF) else Color(0xFF64748B),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DifficultyChip(
    label: String,
    color: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .background(
                if (isSelected) color.copy(alpha = 0.2f) else Color(0xFF0F1422),
                RoundedCornerShape(12.dp)
            )
            .border(
                BorderStroke(1.dp, if (isSelected) color else Color(0xFF1E293B)),
                RoundedCornerShape(12.dp)
            )
            .clickable {
                GameSoundPlayer.playClick()
                onClick()
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) color else Color(0xFF64748B),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SymbolSelectButton(
    symbol: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .background(
                if (isSelected) color.copy(alpha = 0.15f) else Color(0xFF0F1422),
                RoundedCornerShape(16.dp)
            )
            .border(
                BorderStroke(2.dp, if (isSelected) color else Color(0xFF1E293B)),
                RoundedCornerShape(16.dp)
            )
            .clickable {
                GameSoundPlayer.playClick()
                onClick()
            }
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
                        onBackClicked()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF121829), CircleShape)
                        .border(1.dp, Color(0xFF1E293B), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh, // Rotate for back-like feel or simple refresh
                        contentDescription = "الرجوع",
                        tint = Color.White,
                        modifier = Modifier.drawBehind {
                            // Rotate the icon to point left/right in RTL
                        }
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
                                .background(Color(0xFF121829), CircleShape)
                                .border(
                                    BorderStroke(
                                        width = if (activeX) 2.dp else 1.dp,
                                        color = if (activeX) Color(0xFF00E5FF) else Color(0xFF1E293B)
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
                        Text(text = "اللاعب X", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // VS Circle
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF1B223C), CircleShape)
                            .border(1.dp, Color(0xFF2E3B5E), CircleShape),
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
                                .background(Color(0xFF121829), CircleShape)
                                .border(
                                    BorderStroke(
                                        width = if (activeO) 2.dp else 1.dp,
                                        color = if (activeO) Color(0xFFFF2D55) else Color(0xFF1E293B)
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
                            text = if (gameMode == "VS_BOT") "البوت O" else "اللاعب O",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Info Button
                IconButton(
                    onClick = {
                        GameSoundPlayer.playClick()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF121829), CircleShape)
                        .border(1.dp, Color(0xFF1E293B), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "قواعد اللعبة",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Active Turn Banner with Live Timer
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121829)),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Turn Title message
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Pulse dot indicating turn
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
                        
                        val bannerMsg = if (isGameOver) "الجولة انتهت!" else {
                            if (isBotThinking) "جاري تفكير البوت..." else {
                                if (gameMode == "VS_BOT" && currentTurn == "X") "دورك لتباغت الخصم!"
                                else if (gameMode == "VS_BOT") "دور البوت الذكي..."
                                else "دور اللاعب $currentTurn الآن"
                            }
                        }
                        Text(
                            text = bannerMsg,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Live E-Sports Match Timer
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF090C15), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formattedTime,
                                color = Color(0xFFFFD700),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. 3x3 Grid Board Card
            Box(
                modifier = Modifier
                    .size(310.dp)
                    .background(Color(0xFF0F1424), RoundedCornerShape(24.dp))
                    .border(2.dp, Color(0xFF1E293B), RoundedCornerShape(24.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                // Draw grid lines
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val size = this.size.width
                    val step = size / 3
                    val strokeW = 4f
                    val strokeColor = Color(0xFF1E293B)

                    // Verticals
                    drawLine(strokeColor, Offset(step, 8f), Offset(step, size - 8f), strokeW)
                    drawLine(strokeColor, Offset(step * 2, 8f), Offset(step * 2, size - 8f), strokeW)

                    // Horizontals
                    drawLine(strokeColor, Offset(8f, step), Offset(size - 8f, step), strokeW)
                    drawLine(strokeColor, Offset(8f, step * 2), Offset(size - 8f, step * 2), strokeW)
                }

                // Interactive Cell Grid overlay
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
                                            if (isWinningCell) Color(0xFF1E2942)
                                            else if (isHintCell) Color(0xFFFFD700).copy(alpha = 0.1f * hintPulse)
                                            else Color.Transparent
                                        )
                                        .border(
                                            BorderStroke(
                                                width = if (isWinningCell) 2.dp else if (isHintCell) 2.5.dp else 0.dp,
                                                color = if (isWinningCell) Color(0xFFFFD700)
                                                else if (isHintCell) Color(0xFFFFD700).copy(alpha = hintPulse)
                                                else Color.Transparent
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            viewModel.onCellClick(cellIndex)
                                        }
                                        .testTag("cell_$cellIndex"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (cellSymbol != null) {
                                        if (cellSymbol == "X") {
                                            XSymbol(
                                                modifier = Modifier.size(52.dp),
                                                color = Color(0xFF00E5FF)
                                            )
                                        } else if (cellSymbol == "O") {
                                            OSymbol(
                                                modifier = Modifier.size(52.dp),
                                                color = Color(0xFFFF2D55)
                                            )
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
                        viewModel.resetGame()
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp)
                        .testTag("reset_game_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "إعادة جولة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Undo Button
                Button(
                    onClick = {
                        viewModel.undoLastMove()
                    },
                    modifier = Modifier
                        .weight(1.1f)
                        .height(48.dp)
                        .testTag("undo_game_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF14192B),
                        contentColor = Color(0xFF94A3B8)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF2E3B5E))
                ) {
                    Text(text = "تراجع ↩", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Hint Button with badge count
                Button(
                    onClick = {
                        viewModel.requestHint()
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp)
                        .testTag("hint_game_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hintCount > 0) Color(0xFF0F2B3E) else Color(0xFF101426),
                        contentColor = if (hintCount > 0) Color(0xFFFFD700) else Color(0xFF64748B)
                    ),
                    border = BorderStroke(1.dp, if (hintCount > 0) Color(0xFFFFD700).copy(alpha = 0.4f) else Color(0xFF1E293B))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "تلميح ✨ ($hintCount)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp)) // Padding for floating BottomBar
        }

        // 5. High-Fidelity Victory Overlay (Renders full-screen celebration when Game is Over)
        if (isGameOver) {
            VictoryOverlay(
                winner = winner,
                gameMode = gameMode,
                scoreX = scoreX,
                scoreO = scoreO,
                scoreDraws = scoreDraws,
                onPlayAgain = {
                    viewModel.resetGame()
                },
                onMainMenu = {
                    viewModel.resetGame()
                    onBackClicked()
                }
            )
        }
    }
}

@Composable
fun ScoreItem(
    label: String,
    score: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = label, color = Color(0xFF94A3B8), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = score.toString(),
            color = color,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun VerticalDivider(color: Color) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(48.dp)
            .background(color)
    )
}

@Composable
fun GameStatusBanner(
    winner: String?,
    currentTurn: String,
    isGameOver: Boolean,
    gameMode: String,
    isBotThinking: Boolean,
    botDifficulty: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bannerPulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alphaBanner"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 500.dp)
            .height(60.dp)
            .background(Color(0xFF121829), RoundedCornerShape(16.dp))
            .border(BorderStroke(1.dp, Color(0xFF1E293B)), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isGameOver) {
            if (winner != null) {
                val winnerColor = if (winner == "X") Color(0xFF00E5FF) else Color(0xFFFF2D55)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "فاز اللاعب ",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " $winner ",
                        color = winnerColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "بجدارة! 🎉",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "انتهت الجولة بالتعادل! 🤝",
                        color = Color(0xFF94A3B8),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            if (isBotThinking) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = Color(0xFFFF2D55),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "الكمبيوتر يفكّر في نقلته الفائقة...",
                        color = Color(0xFFFF2D55).copy(alpha = alphaAnim),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                val turnColor = if (currentTurn == "X") Color(0xFF00E5FF) else Color(0xFFFF2D55)
                val isYourTurn = gameMode == "VS_BOT" && currentTurn == "X"
                val displayMsg = if (gameMode == "VS_BOT") {
                    if (isYourTurn) "دورك الآن لتباغت الخصم!" else "دور البوت..."
                } else {
                    "دور اللاعب "
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (gameMode == "VS_BOT" && isYourTurn) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = displayMsg,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (gameMode == "VS_FRIEND" || (gameMode == "VS_BOT" && !isYourTurn)) {
                        Text(
                            text = " $currentTurn ",
                            color = turnColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}


// 3. STATS SCREEN
@Composable
fun StatsScreen(viewModel: GameViewModel) {
    val history by viewModel.history.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "سجل الإحصائيات والمعارك",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        val totalGames = history.size
        val wins = history.count { it.winnerSymbol != null && it.winnerSymbol == it.playerSymbol }
        val ties = history.count { it.winnerSymbol == null }
        val losses = totalGames - wins - ties

        // History Chart Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121829)),
            border = BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "الرسم البياني للنتائج الأخيرة",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                StatsRingChart(wins = wins, losses = losses, ties = ties)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "المعارك السابقة ($totalGames)",
                fontSize = 15.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Bold
            )

            if (totalGames > 0) {
                IconButton(
                    onClick = {
                        GameSoundPlayer.playClick()
                        viewModel.clearHistory()
                    },
                    modifier = Modifier.testTag("clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف السجل",
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF1E293B),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "لا توجد معارك مسجلة بعد. العب بضع جولات لتظهر هنا!",
                        color = Color(0xFF475569),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(history) { record ->
                    HistoryItemCard(record = record)
                }
                item {
                    Spacer(modifier = Modifier.height(110.dp)) // Padding for bottom floating bar
                }
            }
        }
    }
}

@Composable
fun StatsRingChart(wins: Int, losses: Int, ties: Int) {
    val total = wins + losses + ties
    if (total == 0) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ابدأ اللعب أولاً لتسجيل إحصائياتك!",
                color = Color(0xFF475569),
                style = MaterialTheme.typography.bodyMedium,
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
                text = "$total\nجولة",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LegendItem(color = Color(0xFF00E5FF), label = "فوز", count = wins, percent = (wins * 100 / total))
            LegendItem(color = Color(0xFF64748B), label = "تعادل", count = ties, percent = (ties * 100 / total))
            LegendItem(color = Color(0xFFFF2D55), label = "خسارة", count = losses, percent = (losses * 100 / total))
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, count: Int, percent: Int) {
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
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun HistoryItemCard(record: GameRecord) {
    val isWin = record.winnerSymbol != null && record.winnerSymbol == record.playerSymbol
    val isTie = record.winnerSymbol == null

    val statusText = when {
        isWin -> "فوز ساحق"
        isTie -> "تعادل عادل"
        else -> "هزيمة"
    }

    val statusColor = when {
        isWin -> Color(0xFF4ADE80) // Green
        isTie -> Color(0xFF94A3B8) // Gray
        else -> Color(0xFFFF2D55) // Pink Red
    }

    val modeText = when (record.gameMode) {
        "BOT_EASY" -> "ضد البوت (سهل)"
        "BOT_MEDIUM" -> "ضد البوت (متوسط)"
        "BOT_HARD" -> "ضد البوت (صعب)"
        "FRIEND" -> "ضد لاعب (ثنائي)"
        else -> "تحدي"
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1422)),
        border = BorderStroke(1.dp, Color(0xFF1E293B).copy(alpha = 0.5f))
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
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedDate,
                    color = Color(0xFF475569),
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

        // Draw neon glow first
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

        // Draw solid foreground line
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

        // Draw neon glow first
        drawCircle(
            color = color.copy(alpha = 0.35f),
            radius = radius,
            center = Offset(size / 2, size / 2),
            style = Stroke(width = strokeWidth + glowRadius, cap = StrokeCap.Round)
        )

        // Draw solid foreground
        drawCircle(
            color = color,
            radius = radius,
            center = Offset(size / 2, size / 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

// ==========================================
// CUSTOM HIGH-FIDELITY NEON GRAPHICS
// ==========================================

@Composable
fun RobotFaceIcon(difficulty: String, color: Color) {
    Canvas(modifier = Modifier.size(54.dp)) {
        val w = size.width
        val h = size.height
        val strokeW = 3f
        
        // Antenna
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
        
        // Ears / Antennas on sides
        if (difficulty == "HARD") {
            // High-tech angular side pieces
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
            // Cute round side ears
            drawCircle(color = color, radius = 4f, center = Offset(w * 0.15f, h * 0.55f))
            drawCircle(color = color, radius = 4f, center = Offset(w * 0.85f, h * 0.55f))
        }

        // Head box
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.2f, h * 0.25f),
            size = androidx.compose.ui.geometry.Size(w * 0.6f, h * 0.55f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
            style = Stroke(width = strokeW)
        )
        
        // Eyes and mouth details
        when (difficulty) {
            "EASY" -> {
                // Happy curved eyes
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
                // Happy smile
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
                // Round solid circular eyes
                drawCircle(color = color, radius = 5f, center = Offset(w * 0.38f, h * 0.45f))
                drawCircle(color = color, radius = 5f, center = Offset(w * 0.62f, h * 0.45f))
                // Straight mouth
                drawLine(
                    color = color,
                    start = Offset(w * 0.42f, h * 0.62f),
                    end = Offset(w * 0.58f, h * 0.62f),
                    strokeWidth = strokeW * 1.5f,
                    cap = StrokeCap.Round
                )
            }
            "HARD" -> {
                // Slanted angry eyes
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
                // Small red glowing circular iris inside
                drawCircle(color = color, radius = 3f, center = Offset(w * 0.37f, h * 0.47f))
                drawCircle(color = color, radius = 3f, center = Offset(w * 0.63f, h * 0.47f))
                // Frown mouth
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
        // Points towards left in RTL layout (which is forward in Arabic reading)
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
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "borderGlow")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable {
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.08f) else Color(0xFF101426)
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) color.copy(alpha = borderAlpha) else Color(0xFF1E293B)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Robot Face on left
            RobotFaceIcon(difficulty = difficulty, color = color)

            // Info in middle
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
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            // Arrow circle on right
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (isSelected) color.copy(alpha = 0.15f) else Color(0xFF1B223C),
                        CircleShape
                    )
                    .border(
                        BorderStroke(1.dp, if (isSelected) color else Color(0xFF2E3B5E)),
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
fun NeonPedestalLogo() {
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
            modifier = Modifier
                .size(width = 240.dp, height = 150.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background glowing eclipse light
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                
                // Draw a beautiful soft radial gradient glow behind
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

            // The Pedestal Platform
            Canvas(
                modifier = Modifier
                    .width(180.dp)
                    .height(60.dp)
                    .align(Alignment.BottomCenter)
            ) {
                val w = size.width
                val h = size.height
                
                // Draw bottom shadow/glow of pedestal
                drawOval(
                    color = Color(0xFF00E5FF).copy(alpha = 0.25f * glowAlpha),
                    topLeft = Offset(w * 0.1f, h * 0.4f),
                    size = androidx.compose.ui.geometry.Size(w * 0.8f, h * 0.5f)
                )
                
                // Draw cylinder base layers for 3D look
                drawOval(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(w * 0.12f, h * 0.35f),
                    size = androidx.compose.ui.geometry.Size(w * 0.76f, h * 0.45f)
                )
                
                // Draw main rim with cyan glow
                drawOval(
                    color = Color(0xFF0D1527),
                    topLeft = Offset(w * 0.12f, h * 0.3f),
                    size = androidx.compose.ui.geometry.Size(w * 0.76f, h * 0.45f)
                )
                drawOval(
                    color = Color(0xFF00E5FF),
                    topLeft = Offset(w * 0.12f, h * 0.3f),
                    size = androidx.compose.ui.geometry.Size(w * 0.76f, h * 0.45f),
                    style = Stroke(width = 3f)
                )
                
                // Top platform face
                drawOval(
                    color = Color(0xFF121829),
                    topLeft = Offset(w * 0.14f, h * 0.32f),
                    size = androidx.compose.ui.geometry.Size(w * 0.72f, h * 0.41f)
                )
            }

            // Floating X and O
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
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit
) {
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
            .background(Color(0xFF090C15).copy(alpha = 0.96f))
            .clickable(enabled = false) {}, // block clicks to background
        contentAlignment = Alignment.Center
    ) {
        // Star & Confetti particles background
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
                    if (gameMode == "VS_BOT" && winner == "O") "البوت انتصر!" else "تهانينا! فوز ساحق"
                }
                else -> "تعادل عادل!"
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
                text = if (winner != null) "لقد انتهت الجولة بانتصار أسطوري!" else "كانت معركة حامية بين الطرفين!",
                fontSize = 14.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Giant Winning Symbol with Laurel Wreath
            Box(
                modifier = Modifier
                    .size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val wreathColor = titleColor.copy(alpha = 0.4f)
                    
                    // Left arc
                    drawArc(
                        color = wreathColor,
                        startAngle = 100f,
                        sweepAngle = 160f,
                        useCenter = false,
                        topLeft = Offset(w * 0.1f, h * 0.1f),
                        size = androidx.compose.ui.geometry.Size(w * 0.8f, h * 0.8f),
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )
                    
                    // Right arc
                    drawArc(
                        color = wreathColor,
                        startAngle = -80f,
                        sweepAngle = 160f,
                        useCenter = false,
                        topLeft = Offset(w * 0.1f, h * 0.1f),
                        size = androidx.compose.ui.geometry.Size(w * 0.8f, h * 0.8f),
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )
                    
                    // Leaf ovals
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
                    modifier = Modifier
                        .size(90.dp * glowScale)
                ) {
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121829)),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تفاصيل جولة المعركة",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ScoreDetailItem(label = "اللاعب X", score = scoreX, color = Color(0xFF00E5FF))
                        ScoreDetailItem(label = "التعادلات", score = scoreDraws, color = Color(0xFF94A3B8))
                        ScoreDetailItem(label = "اللاعب O", score = scoreO, color = Color(0xFFFF2D55))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Button(
                onClick = {
                    onPlayAgain()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5FF),
                    contentColor = Color(0xFF090C15)
                )
            ) {
                Text(text = "العب مجدداً", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    onMainMenu()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Text(text = "القائمة الرئيسية", fontSize = 16.sp, fontWeight = FontWeight.Medium)
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

