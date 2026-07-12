package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.GameRecord
import com.example.data.GameRecordRepository
import com.example.game.GameEngine
import com.example.sound.GameSoundPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ==========================================
// BILINGUAL TRANSLATION SYSTEM
// ==========================================
object Localizer {
    var currentLanguage = "AR" // "AR" or "EN"

    fun get(key: String): String {
        val isAr = currentLanguage == "AR"
        return when (key) {
            "app_name" -> "X-O Master"
            "app_subtitle" -> if (isAr) "تحدّ الذكاء الاصطناعي بمستويات مذهلة أو العب مع أصدقائك بنقرة سريعة" else "Challenge elite AI or battle friends with style"
            "mode_title" -> if (isAr) "١. نمط اللعب" else "1. Game Mode"
            "mode_bot" -> if (isAr) "ضد الكمبيوتر (البوت)" else "VS Computer (Bot)"
            "mode_friend" -> if (isAr) "ضد لاعب (ثنائي)" else "Local Multiplayer"
            "mode_tournament" -> if (isAr) "البطولة الأسطورية 🏆" else "Epic Tournament 🏆"
            "difficulty_title" -> if (isAr) "٢. اختر مستوى الصعوبة للذكاء الاصطناعي" else "2. Select AI Difficulty Level"
            "difficulty_easy" -> if (isAr) "مستوى سهل 🤖" else "Easy Mode 🤖"
            "difficulty_easy_desc" -> if (isAr) "الكمبيوتر يرتكب أخطاء عشوائية وهو مناسب للتدريب والتعلم السريع." else "The computer makes random mistakes, great for learning."
            "difficulty_medium" -> if (isAr) "مستوى متوسط 🧠" else "Medium Mode 🧠"
            "difficulty_medium_desc" -> if (isAr) "الكمبيوتر يلعب بنقلات أذكى ويبادر بالدفاع المنظم والذكي." else "The computer plays smarter, blocking wins and defending well."
            "difficulty_hard" -> if (isAr) "مستوى مستحيل 🔥" else "Impossible Mode 🔥"
            "difficulty_hard_desc" -> if (isAr) "الذكاء الاصطناعي الكامل (مينيمكس + ألفا بيتا). لا يخطئ أبداً، جرب هزيمته!" else "Ultimate AI (Minimax + Alpha-Beta). Unbeatable!"
            "symbol_title" -> if (isAr) "٣. اختر رمزك الأساسي" else "3. Choose Your Primary Symbol"
            "start_battle" -> if (isAr) "ابدأ المعركة الحماسية" else "Launch Epic Battle"
            "tab_home" -> if (isAr) "الرئيسية" else "Home"
            "tab_game" -> if (isAr) "الملعب" else "Arena"
            "tab_stats" -> if (isAr) "الإحصائيات" else "Stats"
            "tab_settings" -> if (isAr) "الإعدادات" else "Settings"
            "player_x" -> if (isAr) "اللاعب X" else "Player X"
            "player_o" -> if (isAr) "اللاعب O" else "Player O"
            "bot_o" -> if (isAr) "البوت O" else "Bot O"
            "turn_your" -> if (isAr) "دورك لتباغت الخصم!" else "Your turn to make a move!"
            "turn_bot" -> if (isAr) "جاري تفكير البوت..." else "Bot is calculating..."
            "turn_player" -> if (isAr) "دور اللاعب %s الآن" else "Player %s's turn"
            "game_over" -> if (isAr) "الجولة انتهت!" else "Round Over!"
            "re_round" -> if (isAr) "إعادة الجولة" else "Reset Round"
            "undo" -> if (isAr) "تراجع" else "Undo"
            "hint" -> if (isAr) "تلميح" else "Hint"
            "stats_title" -> if (isAr) "سجل الانتصارات والإنجازات" else "Battle Records & Achievements"
            "stats_subtitle" -> if (isAr) "لوحة صدارة المعارك وسجل المواجهات السابقة" else "Battle leaderboard and game history log"
            "stats_played" -> if (isAr) "المباريات" else "Played"
            "stats_wins" -> if (isAr) "انتصارات" else "Wins"
            "stats_draws" -> if (isAr) "تعادلات" else "Draws"
            "stats_losses" -> if (isAr) "خسائر" else "Losses"
            "stats_win_rate" -> if (isAr) "نسبة الفوز" else "Win Rate"
            "stats_streak" -> if (isAr) "أعلى سلسلة" else "Max Streak"
            "history_log" -> if (isAr) "سجل المعارك الأخير" else "Recent Battle Log"
            "no_records" -> if (isAr) "لا توجد سجلات معارك حتى الآن! ابدأ اللعب لتسجيل نتائجك." else "No battle records yet! Start playing to record your battles."
            "clear_history" -> if (isAr) "مسح السجل" else "Clear Log"
            "settings_sound" -> if (isAr) "صوت المؤثرات" else "Sound Effects"
            "settings_sound_desc" -> if (isAr) "تفعيل النغمات السيمفونية التناظرية عند اللعب" else "Enable analog synth sonifications on actions"
            "settings_haptic" -> if (isAr) "الاهتزاز اللمسي" else "Haptic Feedback"
            "settings_haptic_desc" -> if (isAr) "اهتزاز تفاعلي عند الضغط واللعب" else "Interactive tactile rumble on clicks"
            "settings_theme" -> if (isAr) "السمة والمظهر" else "Theme & Aesthetics"
            "settings_theme_desc" -> if (isAr) "تخصيص ثيم وألوان واجهة المستخدم" else "Customize interface appearance and colors"
            "settings_theme_fantasy" -> if (isAr) "كوني خيالي 🌌" else "Fantasy Cosmic 🌌"
            "settings_theme_dark" -> if (isAr) "داكن احترافي 🖤" else "Pro Dark 🖤"
            "settings_theme_light" -> if (isAr) "مضيء عصري 🤍" else "Modern Light 🤍"
            "settings_lang" -> if (isAr) "لغة التطبيق" else "App Language"
            "settings_reset" -> if (isAr) "مسح جميع البيانات" else "Wipe All Data"
            "settings_reset_desc" -> if (isAr) "حذف السجلات، الإحصائيات، والإنجازات نهائياً" else "Delete all history, stats, and achievements"
            "confirm_reset_title" -> if (isAr) "هل أنت متأكد؟" else "Are you sure?"
            "confirm_reset_desc" -> if (isAr) "هذا الإجراء سيقوم بحذف جميع البيانات بشكل نهائي ولا يمكن التراجع عنه!" else "This action will permanently delete all your data and cannot be undone!"
            "cancel" -> if (isAr) "إلغاء" else "Cancel"
            "confirm" -> if (isAr) "تأكيد" else "Confirm"
            "win_congrats" -> if (isAr) "تهانينا!" else "Victory!"
            "win_bot" -> if (isAr) "البوت انتصر!" else "AI Bot Triumphed!"
            "draw_title" -> if (isAr) "تعادل عادل!" else "Fair Draw!"
            "win_desc" -> if (isAr) "لقد انتهت الجولة بانتصار أسطوري!" else "The round ended with a legendary win!"
            "draw_desc" -> if (isAr) "كانت معركة حامية بين الطرفين!" else "It was a fierce battle on both sides!"
            "round_details" -> if (isAr) "تفاصيل جولة المعركة" else "Battle Round Details"
            "play_again" -> if (isAr) "العب مجدداً" else "Play Again"
            "main_menu" -> if (isAr) "القائمة الرئيسية" else "Main Menu"
            "achievements" -> if (isAr) "نظام الإنجازات والأوسمة" else "Achievements & Badges"
            "ach_first_win" -> if (isAr) "البداية الواعدة 🌟" else "Promising Start 🌟"
            "ach_first_win_desc" -> if (isAr) "حقق أول فوز لك ضد البوت أو صديق" else "Achieve your first win ever"
            "ach_hard_draw" -> if (isAr) "قاهر المستحيل 🏆" else "Impossible Conqueror 🏆"
            "ach_hard_draw_desc" -> if (isAr) "حقق فوزاً أو تعادلاً ضد البوت المستحيل" else "Achieve a win or draw against Impossible Bot"
            "ach_streak" -> if (isAr) "العقل المدبر 🔥" else "Mastermind 🔥"
            "ach_streak_desc" -> if (isAr) "حقق سلسلة فوز متتالية من 3 انتصارات" else "Achieve a 3-match win streak"
            "ach_hint" -> if (isAr) "مكتشف الأسرار 💡" else "Secret Explorer 💡"
            "ach_hint_desc" -> if (isAr) "استخدم جميع التلميحات الثلاثة في جولة واحدة" else "Use all 3 hints in a single round"
            "ach_legend" -> if (isAr) "اللاعب الأسطوري 👑" else "Legendary Player 👑"
            "ach_legend_desc" -> if (isAr) "خض 10 معارك حامية الوطيس" else "Fight 10 intense battles in total"
            "unlocked" -> if (isAr) "مفتوح" else "Unlocked"
            "locked" -> if (isAr) "مغلق" else "Locked"
            else -> key
        }
    }
}

// ==========================================
// TOURNAMENT MATCH MODELS & SERIALIZERS
// ==========================================
data class TournamentMatch(
    val player1Idx: Int,
    val player2Idx: Int,
    var winnerIdx: Int = -1, // -1 = pending, 0 = Player1, 1 = Player2, 2 = Draw
    var score1: Int = 0,
    var score2: Int = 0,
    var p1Time: Int = 0,
    var p2Time: Int = 0,
    var p1Hints: Int = 0,
    var p2Hints: Int = 0,
    var r1Time: Int = 0,
    var r2Time: Int = 0,
    var r1Clicks: Int = 0,
    var r2Clicks: Int = 0,
    var r1Hints: Int = 0,
    var r2Hints: Int = 0,
    var rDraw: Boolean = false
)

fun serializePlayers(players: List<Pair<String, String>>): String {
    return players.joinToString("|") { "${it.first}:${it.second}" }
}

fun deserializePlayers(data: String): List<Pair<String, String>> {
    if (data.isEmpty()) return emptyList()
    return data.split("|").map {
        val parts = it.split(":")
        val name = parts.getOrNull(0) ?: ""
        val title = parts.getOrNull(1) ?: ""
        Pair(name, title)
    }
}

fun serializeMatches(matches: List<TournamentMatch>): String {
    return matches.joinToString(";") { m ->
        "${m.player1Idx},${m.player2Idx},${m.winnerIdx},${m.score1},${m.score2},${m.p1Time},${m.p2Time},${m.p1Hints},${m.p2Hints},${m.r1Time},${m.r2Time},${m.r1Clicks},${m.r2Clicks},${m.r1Hints},${m.r2Hints},${if (m.rDraw) 1 else 0}"
    }
}

fun deserializeMatches(data: String): List<TournamentMatch> {
    if (data.isEmpty()) return emptyList()
    return data.split(";").mapNotNull {
        val parts = it.split(",")
        if (parts.size >= 9) {
            TournamentMatch(
                player1Idx = parts[0].toInt(),
                player2Idx = parts[1].toInt(),
                winnerIdx = parts[2].toInt(),
                score1 = parts[3].toInt(),
                score2 = parts[4].toInt(),
                p1Time = parts[5].toInt(),
                p2Time = parts[6].toInt(),
                p1Hints = parts[7].toInt(),
                p2Hints = parts[8].toInt()
            ).apply {
                if (parts.size >= 16) {
                    r1Time = parts[9].toInt()
                    r2Time = parts[10].toInt()
                    r1Clicks = parts[11].toInt()
                    r2Clicks = parts[12].toInt()
                    r1Hints = parts[13].toInt()
                    r2Hints = parts[14].toInt()
                    rDraw = parts[15] == "1"
                }
            }
        } else null
    }
}

class GameViewModel(
    application: Application,
    private val repository: GameRecordRepository
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("xo_master_prefs", Context.MODE_PRIVATE)

    // VS_FRIEND (Local Multiplayer) Name & Title
    private val _friendP1Name = MutableStateFlow(prefs.getString("friend_p1_name", prefs.getString("p1_name", "") ?: "") ?: "")
    val friendP1Name: StateFlow<String> = _friendP1Name.asStateFlow()

    private val _friendP1Title = MutableStateFlow(prefs.getString("friend_p1_title", prefs.getString("p1_title", "") ?: "") ?: "")
    val friendP1Title: StateFlow<String> = _friendP1Title.asStateFlow()

    private val _friendP2Name = MutableStateFlow(prefs.getString("friend_p2_name", prefs.getString("p2_name", "") ?: "") ?: "")
    val friendP2Name: StateFlow<String> = _friendP2Name.asStateFlow()

    private val _friendP2Title = MutableStateFlow(prefs.getString("friend_p2_title", prefs.getString("p2_title", "") ?: "") ?: "")
    val friendP2Title: StateFlow<String> = _friendP2Title.asStateFlow()

    // VS_BOT Name & Title
    private val _botPlayerName = MutableStateFlow(prefs.getString("bot_player_name", "") ?: "")
    val botPlayerName: StateFlow<String> = _botPlayerName.asStateFlow()

    private val _botPlayerTitle = MutableStateFlow(prefs.getString("bot_player_title", "") ?: "")
    val botPlayerTitle: StateFlow<String> = _botPlayerTitle.asStateFlow()

    // Active TOURNAMENT match Name & Title
    private val _tournamentP1Name = MutableStateFlow("")
    val tournamentP1Name: StateFlow<String> = _tournamentP1Name.asStateFlow()

    private val _tournamentP1Title = MutableStateFlow("")
    val tournamentP1Title: StateFlow<String> = _tournamentP1Title.asStateFlow()

    private val _tournamentP2Name = MutableStateFlow("")
    val tournamentP2Name: StateFlow<String> = _tournamentP2Name.asStateFlow()

    private val _tournamentP2Title = MutableStateFlow("")
    val tournamentP2Title: StateFlow<String> = _tournamentP2Title.asStateFlow()

    // Aliases/Legacy for backward compatibility
    val p1Name: StateFlow<String> get() = _friendP1Name.asStateFlow()
    val p1Title: StateFlow<String> get() = _friendP1Title.asStateFlow()
    val p2Name: StateFlow<String> get() = _friendP2Name.asStateFlow()
    val p2Title: StateFlow<String> get() = _friendP2Title.asStateFlow()

    // Clicks and thinking time statistics
    private val _p1MovesCount = MutableStateFlow(0)
    val p1MovesCount: StateFlow<Int> = _p1MovesCount.asStateFlow()

    private val _p2MovesCount = MutableStateFlow(0)
    val p2MovesCount: StateFlow<Int> = _p2MovesCount.asStateFlow()

    private val _p1ThinkingSeconds = MutableStateFlow(0)
    val p1ThinkingSeconds: StateFlow<Int> = _p1ThinkingSeconds.asStateFlow()

    private val _p2ThinkingSeconds = MutableStateFlow(0)
    val p2ThinkingSeconds: StateFlow<Int> = _p2ThinkingSeconds.asStateFlow()

    // Dual hint counters
    private val _p1HintCount = MutableStateFlow(3)
    val p1HintCount: StateFlow<Int> = _p1HintCount.asStateFlow()

    private val _p2HintCount = MutableStateFlow(2)
    val p2HintCount: StateFlow<Int> = _p2HintCount.asStateFlow()

    // Background theme vs application theme
    private val _backgroundTheme = MutableStateFlow(prefs.getString("background_theme", "DEFAULT") ?: "DEFAULT")
    val backgroundTheme: StateFlow<String> = _backgroundTheme.asStateFlow()

    // Tournament States
    private val _activeTournamentId = MutableStateFlow<Long?>(null)
    val activeTournamentId: StateFlow<Long?> = _activeTournamentId.asStateFlow()

    private val _tournamentType = MutableStateFlow("LEAGUE")
    val tournamentType: StateFlow<String> = _tournamentType.asStateFlow()

    private val _tournamentPlayers = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val tournamentPlayers: StateFlow<List<Pair<String, String>>> = _tournamentPlayers.asStateFlow()

    private val _tournamentMatches = MutableStateFlow<List<TournamentMatch>>(emptyList())
    val tournamentMatches: StateFlow<List<TournamentMatch>> = _tournamentMatches.asStateFlow()

    private val _currentTournamentMatchIndex = MutableStateFlow(-1)
    val currentTournamentMatchIndex: StateFlow<Int> = _currentTournamentMatchIndex.asStateFlow()

    private val _isTournamentReplay = MutableStateFlow(false)
    val isTournamentReplay: StateFlow<Boolean> = _isTournamentReplay.asStateFlow()

    private val _tournamentTieBreakerInfo = MutableStateFlow<String?>(null)
    val tournamentTieBreakerInfo: StateFlow<String?> = _tournamentTieBreakerInfo.asStateFlow()

    private val _tournamentWinnerName = MutableStateFlow<String?>(null)
    val tournamentWinnerName: StateFlow<String?> = _tournamentWinnerName.asStateFlow()

    val tournamentsList: StateFlow<List<com.example.data.Tournament>> = repository.allTournaments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Board state: 9 cells, null = empty, "X" or "O"
    private val _board = MutableStateFlow(List<String?>(9) { null })
    val board: StateFlow<List<String?>> = _board.asStateFlow()

    // Current turn ("X" or "O")
    private val _currentTurn = MutableStateFlow("X")
    val currentTurn: StateFlow<String> = _currentTurn.asStateFlow()

    // Winner state (null, "X", "O")
    private val _winner = MutableStateFlow<String?>(null)
    val winner: StateFlow<String?> = _winner.asStateFlow()

    // Winning line cell indexes (e.g. [0, 1, 2], null if no winner yet)
    private val _winningLine = MutableStateFlow<List<Int>?>(null)
    val winningLine: StateFlow<List<Int>?> = _winningLine.asStateFlow()

    // Game over status
    private val _isGameOver = MutableStateFlow(false)
    val isGameOver: StateFlow<Boolean> = _isGameOver.asStateFlow()

    // Game configuration
    private val _gameMode = MutableStateFlow("VS_BOT") // "VS_BOT", "VS_FRIEND"
    val gameMode: StateFlow<String> = _gameMode.asStateFlow()

    private val _botDifficulty = MutableStateFlow("MEDIUM") // "EASY", "MEDIUM", "HARD"
    val botDifficulty: StateFlow<String> = _botDifficulty.asStateFlow()

    private val _playerSymbol = MutableStateFlow("X") // Player 1's symbol ("X" or "O")
    val playerSymbol: StateFlow<String> = _playerSymbol.asStateFlow()

    // Bot status
    private val _isBotThinking = MutableStateFlow(false)
    val isBotThinking: StateFlow<Boolean> = _isBotThinking.asStateFlow()

    // Score states
    private val _scoreX = MutableStateFlow(0)
    val scoreX: StateFlow<Int> = _scoreX.asStateFlow()

    private val _scoreO = MutableStateFlow(0)
    val scoreO: StateFlow<Int> = _scoreO.asStateFlow()

    private val _scoreDraws = MutableStateFlow(0)
    val scoreDraws: StateFlow<Int> = _scoreDraws.asStateFlow()

    // Settings
    private val _isSoundEnabled = MutableStateFlow(prefs.getBoolean("is_sound_enabled", true))
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    private val _isHapticEnabled = MutableStateFlow(prefs.getBoolean("is_haptic_enabled", true))
    val isHapticEnabled: StateFlow<Boolean> = _isHapticEnabled.asStateFlow()

    private val _appTheme = MutableStateFlow(prefs.getString("app_theme", "COSMIC_FANTASY") ?: "COSMIC_FANTASY")
    val appTheme: StateFlow<String> = _appTheme.asStateFlow()

    private val _appLanguage = MutableStateFlow(prefs.getString("app_language", "AR") ?: "AR")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    // Track if all hints are used in a single game for the achievement
    private var hintsUsedInCurrentGame = 0

    private var lastFriendStartingSymbol = "O"

    private var turnStartTime: Long = 0L
    private var p1TimeAccumulated: Int = 0
    private var p2TimeAccumulated: Int = 0

    // Game record history retrieved reactively from Room DB
    val history: StateFlow<List<GameRecord>> = repository.allRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // History lists to support undo
    private val boardHistory = mutableListOf<List<String?>>()
    private val turnHistory = mutableListOf<String>()

    // Hint status
    private val _highlightedCell = MutableStateFlow<Int?>(null)
    val highlightedCell: StateFlow<Int?> = _highlightedCell.asStateFlow()

    // Remaining hint count
    val hintCount: StateFlow<Int> = kotlinx.coroutines.flow.combine(_currentTurn, _p1HintCount, _p2HintCount) { turn, p1, p2 ->
        if (turn == "X") p1 else p2
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    val botSymbol: String
        get() = if (_playerSymbol.value == "X") "O" else "X"

    init {
        // Configure singleton players according to loaded preferences
        GameSoundPlayer.isSoundEnabled = _isSoundEnabled.value
        Localizer.currentLanguage = _appLanguage.value

        // Auto-save: check if there is a saved board state, if so, load it
        val savedBoardStr = prefs.getString("saved_board", null)
        if (savedBoardStr != null && savedBoardStr.isNotEmpty()) {
            loadGameState()
        } else {
            resetGame()
        }
    }

    // ==========================================
    // SETTINGS SETTERS
    // ==========================================
    fun setSoundEnabled(enabled: Boolean) {
        _isSoundEnabled.value = enabled
        GameSoundPlayer.isSoundEnabled = enabled
        prefs.edit().putBoolean("is_sound_enabled", enabled).apply()
    }

    fun setHapticEnabled(enabled: Boolean) {
        _isHapticEnabled.value = enabled
        prefs.edit().putBoolean("is_haptic_enabled", enabled).apply()
    }

    fun setAppTheme(theme: String) {
        _appTheme.value = theme
        prefs.edit().putString("app_theme", theme).apply()
    }

    fun setBackgroundTheme(theme: String) {
        _backgroundTheme.value = theme
        prefs.edit().putString("background_theme", theme).apply()
    }

    fun setAppLanguage(language: String) {
        _appLanguage.value = language
        Localizer.currentLanguage = language
        prefs.edit().putString("app_language", language).apply()
    }

    fun setFriendP1Info(name: String, title: String) {
        _friendP1Name.value = name
        _friendP1Title.value = title
        prefs.edit().putString("friend_p1_name", name).putString("friend_p1_title", title).apply()
    }

    fun setFriendP2Info(name: String, title: String) {
        _friendP2Name.value = name
        _friendP2Title.value = title
        prefs.edit().putString("friend_p2_name", name).putString("friend_p2_title", title).apply()
    }

    fun setBotPlayerInfo(name: String, title: String) {
        _botPlayerName.value = name
        _botPlayerTitle.value = title
        prefs.edit().putString("bot_player_name", name).putString("bot_player_title", title).apply()
    }

    fun setPlayer1Info(name: String, title: String) {
        setFriendP1Info(name, title)
    }

    fun setPlayer2Info(name: String, title: String) {
        setFriendP2Info(name, title)
    }

    // ==========================================
    // AUTO-SAVE LOGIC
    // ==========================================
    private fun saveGameState() {
        val editor = prefs.edit()
        
        // Serialize board list to string "X,,O,X,,O,,,"
        val boardStr = _board.value.joinToString(",") { it ?: "" }
        editor.putString("saved_board", boardStr)
        editor.putString("saved_current_turn", _currentTurn.value)
        editor.putString("saved_winner", _winner.value ?: "")
        editor.putString("saved_winning_line", _winningLine.value?.joinToString(",") ?: "")
        editor.putBoolean("saved_is_game_over", _isGameOver.value)
        editor.putString("saved_game_mode", _gameMode.value)
        editor.putString("saved_bot_difficulty", _botDifficulty.value)
        editor.putString("saved_player_symbol", _playerSymbol.value)
        editor.putInt("saved_hints_used", hintsUsedInCurrentGame)
        editor.putInt("saved_p1_hint_count", _p1HintCount.value)
        editor.putInt("saved_p2_hint_count", _p2HintCount.value)
        
        // Save temporary round scores
        editor.putInt("saved_score_x", _scoreX.value)
        editor.putInt("saved_score_o", _scoreO.value)
        editor.putInt("saved_score_draws", _scoreDraws.value)
        
        editor.apply()
    }

    private fun loadGameState() {
        try {
            val savedBoardStr = prefs.getString("saved_board", "") ?: ""
            if (savedBoardStr.isNotEmpty()) {
                val boardList = savedBoardStr.split(",").map { if (it.isEmpty()) null else it }
                if (boardList.size == 9) {
                    _board.value = boardList
                    _currentTurn.value = prefs.getString("saved_current_turn", "X") ?: "X"
                    val winnerStr = prefs.getString("saved_winner", "") ?: ""
                    _winner.value = if (winnerStr.isEmpty()) null else winnerStr
                    
                    val winningLineStr = prefs.getString("saved_winning_line", "") ?: ""
                    _winningLine.value = if (winningLineStr.isEmpty()) null else winningLineStr.split(",").map { it.toInt() }
                    
                    _isGameOver.value = prefs.getBoolean("saved_is_game_over", false)
                    _gameMode.value = prefs.getString("saved_game_mode", "VS_BOT") ?: "VS_BOT"
                    _botDifficulty.value = prefs.getString("saved_bot_difficulty", "MEDIUM") ?: "MEDIUM"
                    _playerSymbol.value = prefs.getString("saved_player_symbol", "X") ?: "X"
                    hintsUsedInCurrentGame = prefs.getInt("saved_hints_used", 0)
                    _p1HintCount.value = prefs.getInt("saved_p1_hint_count", 3)
                    _p2HintCount.value = prefs.getInt("saved_p2_hint_count", 2)
                    
                    _scoreX.value = prefs.getInt("saved_score_x", 0)
                    _scoreO.value = prefs.getInt("saved_score_o", 0)
                    _scoreDraws.value = prefs.getInt("saved_score_draws", 0)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            resetGame()
        }
    }

    private fun clearSavedGame() {
        prefs.edit().apply {
            remove("saved_board")
            remove("saved_current_turn")
            remove("saved_winner")
            remove("saved_winning_line")
            remove("saved_is_game_over")
            remove("saved_hints_used")
            remove("saved_p1_hint_count")
            remove("saved_p2_hint_count")
        }.apply()
    }

    // ==========================================
    // GAME CORE ACTION COMMANDS
    // ==========================================
    fun setGameMode(mode: String) {
        if (_gameMode.value != mode) {
            _gameMode.value = mode
            resetScores()
            resetGame()
        }
    }

    fun setBotDifficulty(difficulty: String) {
        if (_botDifficulty.value != difficulty) {
            _botDifficulty.value = difficulty
            resetScores()
            resetGame()
        }
    }

    fun setPlayerSymbol(symbol: String) {
        if (_playerSymbol.value != symbol) {
            _playerSymbol.value = symbol
            resetScores()
            resetGame()
        }
    }

    fun resetScores() {
        _scoreX.value = 0
        _scoreO.value = 0
        _scoreDraws.value = 0
        lastFriendStartingSymbol = "O"
        saveGameState()
    }

    fun resetGame() {
        if (_gameMode.value == "TOURNAMENT") {
            setupActiveTournamentMatch()
            return
        }

        _board.value = List(9) { null }
        if (_gameMode.value == "VS_FRIEND") {
            lastFriendStartingSymbol = if (lastFriendStartingSymbol == "X") "O" else "X"
            _currentTurn.value = lastFriendStartingSymbol
        } else {
            _currentTurn.value = "X"
        }
        _winner.value = null
        _winningLine.value = null
        _isGameOver.value = false
        _isBotThinking.value = false
        
        // Reset undo and hint states
        boardHistory.clear()
        turnHistory.clear()
        _highlightedCell.value = null
        
        // Clicks/Timers reset
        _p1MovesCount.value = 0
        _p2MovesCount.value = 0
        _p1ThinkingSeconds.value = 0
        _p2ThinkingSeconds.value = 0
        p1TimeAccumulated = 0
        p2TimeAccumulated = 0
        turnStartTime = System.currentTimeMillis()
        
        if (_gameMode.value == "VS_BOT") {
            _p1HintCount.value = 3
            _p2HintCount.value = 0
        } else {
            _p1HintCount.value = 2
            _p2HintCount.value = 2
        }
        hintsUsedInCurrentGame = 0

        clearSavedGame()
        saveGameState()

        // If Bot's turn is first in bot mode (Bot is X), let bot play!
        if (_gameMode.value == "VS_BOT" && botSymbol == "X") {
            triggerBotMove()
        }
    }

    private var stopwatchJob: kotlinx.coroutines.Job? = null

    fun startStopwatch() {
        stopwatchJob?.cancel()
        turnStartTime = System.currentTimeMillis()
        stopwatchJob = viewModelScope.launch {
            while (true) {
                delay(100)
                if (!_isGameOver.value) {
                    val isP1 = if (_gameMode.value == "VS_BOT") {
                        _currentTurn.value == _playerSymbol.value
                    } else {
                        _currentTurn.value == "X"
                    }
                    val elapsedSecs = ((System.currentTimeMillis() - turnStartTime) / 1000).toInt()
                    if (isP1) {
                        _p1ThinkingSeconds.value = p1TimeAccumulated + elapsedSecs
                    } else {
                        _p2ThinkingSeconds.value = p2TimeAccumulated + elapsedSecs
                    }
                }
            }
        }
    }

    fun finalizeTurnTime() {
        val turnDurationMs = System.currentTimeMillis() - turnStartTime
        val turnDurationSecs = Math.max(1, Math.round(turnDurationMs / 1000.0).toInt())
        
        val isP1 = if (_gameMode.value == "VS_BOT") {
            _currentTurn.value == _playerSymbol.value
        } else {
            _currentTurn.value == "X"
        }
        
        if (isP1) {
            p1TimeAccumulated += turnDurationSecs
            _p1ThinkingSeconds.value = p1TimeAccumulated
        } else {
            p2TimeAccumulated += turnDurationSecs
            _p2ThinkingSeconds.value = p2TimeAccumulated
        }
        
        turnStartTime = System.currentTimeMillis()
    }

    fun stopStopwatch() {
        stopwatchJob?.cancel()
        stopwatchJob = null
    }

    fun getPlayer1DisplayName(): String {
        return when (_gameMode.value) {
            "VS_BOT" -> {
                val name = _botPlayerName.value.trim()
                val title = _botPlayerTitle.value.trim()
                if (name.isNotEmpty()) {
                    if (title.isNotEmpty()) "$title $name" else name
                } else {
                    if (Localizer.currentLanguage == "AR") "اللاعب" else "Player"
                }
            }
            "VS_FRIEND" -> {
                val name = _friendP1Name.value.trim()
                val title = _friendP1Title.value.trim()
                if (name.isNotEmpty()) {
                    if (title.isNotEmpty()) "$title $name" else name
                } else {
                    if (Localizer.currentLanguage == "AR") "اللاعب X" else "Player X"
                }
            }
            "TOURNAMENT" -> {
                val name = _tournamentP1Name.value.trim()
                val title = _tournamentP1Title.value.trim()
                if (name.isNotEmpty()) {
                    if (title.isNotEmpty()) "$title $name" else name
                } else {
                    if (Localizer.currentLanguage == "AR") "اللاعب X" else "Player X"
                }
            }
            else -> {
                if (Localizer.currentLanguage == "AR") "اللاعب X" else "Player X"
            }
        }
    }

    fun getPlayer2DisplayName(): String {
        return when (_gameMode.value) {
            "VS_BOT" -> {
                if (Localizer.currentLanguage == "AR") "البوت 🤖" else "Bot 🤖"
            }
            "VS_FRIEND" -> {
                val name = _friendP2Name.value.trim()
                val title = _friendP2Title.value.trim()
                if (name.isNotEmpty()) {
                    if (title.isNotEmpty()) "$title $name" else name
                } else {
                    if (Localizer.currentLanguage == "AR") "اللاعب O" else "Player O"
                }
            }
            "TOURNAMENT" -> {
                val name = _tournamentP2Name.value.trim()
                val title = _tournamentP2Title.value.trim()
                if (name.isNotEmpty()) {
                    if (title.isNotEmpty()) "$title $name" else name
                } else {
                    if (Localizer.currentLanguage == "AR") "اللاعب O" else "Player O"
                }
            }
            else -> {
                if (Localizer.currentLanguage == "AR") "اللاعب O" else "Player O"
            }
        }
    }

    fun isLeague(): Boolean {
        return _tournamentType.value == "LEAGUE"
    }

    fun onCellClick(index: Int) {
        // Prevent click if game is over, cell already has a symbol, or bot is thinking
        if (_isGameOver.value || _board.value[index] != null || _isBotThinking.value) {
            return
        }

        finalizeTurnTime()

        // Record history before move
        boardHistory.add(_board.value)
        turnHistory.add(_currentTurn.value)

        // Clear any active highlight hint
        _highlightedCell.value = null

        val activeSymbol = _currentTurn.value

        // Track moves count
        if (activeSymbol == "X") {
            _p1MovesCount.value += 1
        } else {
            _p2MovesCount.value += 1
        }

        // Play custom symbol sound
        if (activeSymbol == "X") {
            GameSoundPlayer.playXSound()
        } else {
            GameSoundPlayer.playOSound()
        }

        val updatedBoard = _board.value.toMutableList()
        updatedBoard[index] = activeSymbol
        _board.value = updatedBoard

        // Check for victory
        val (winnerSymbol, winningLineIndexes) = GameEngine.checkWinner(updatedBoard)
        if (winnerSymbol != null) {
            handleWinner(winnerSymbol, winningLineIndexes)
            return
        }

        // Check for tie
        if (GameEngine.isBoardFull(updatedBoard)) {
            handleTie()
            return
        }

        // Switch turns
        _currentTurn.value = if (activeSymbol == "X") "O" else "X"
        saveGameState()

        // If next is bot's turn, trigger bot play
        if (_gameMode.value == "VS_BOT" && _currentTurn.value == botSymbol) {
            triggerBotMove()
        }
    }

    private fun triggerBotMove() {
        _isBotThinking.value = true
        GameSoundPlayer.playBotThinking()
        viewModelScope.launch {
            // A small realistic thinking delay so user can see it's active
            delay(550)

            val currentBoard = _board.value
            val move = GameEngine.getBotMove(
                board = currentBoard,
                botSymbol = botSymbol,
                playerSymbol = _playerSymbol.value,
                difficulty = _botDifficulty.value
            )

            if (move != -1) {
                // Record history before bot move so we can undo both human & bot
                boardHistory.add(currentBoard)
                turnHistory.add(botSymbol)

                // Track move for bot
                _p2MovesCount.value += 1

                // Play custom symbol sound for bot action
                if (botSymbol == "X") {
                    GameSoundPlayer.playXSound()
                } else {
                    GameSoundPlayer.playOSound()
                }

                val updatedBoard = currentBoard.toMutableList()
                updatedBoard[move] = botSymbol
                _board.value = updatedBoard

                finalizeTurnTime()

                // Check winner
                val (winnerSymbol, winningLineIndexes) = GameEngine.checkWinner(updatedBoard)
                if (winnerSymbol != null) {
                    handleWinner(winnerSymbol, winningLineIndexes)
                    _isBotThinking.value = false
                    return@launch
                }

                // Check tie
                if (GameEngine.isBoardFull(updatedBoard)) {
                    handleTie()
                    _isBotThinking.value = false
                    return@launch
                }

                // Switch turn back to human
                _currentTurn.value = _playerSymbol.value
            }
            _isBotThinking.value = false
            saveGameState()
        }
    }

    private fun handleWinner(winnerSymbol: String, winningLineIndexes: List<Int>?) {
        _winner.value = winnerSymbol
        _winningLine.value = winningLineIndexes
        _isGameOver.value = true

        if (winnerSymbol == "X") {
            _scoreX.value += 1
        } else {
            _scoreO.value += 1
        }

        // Audio notification
        if (_gameMode.value == "VS_BOT") {
            if (winnerSymbol == _playerSymbol.value) {
                GameSoundPlayer.playWin()
            } else {
                GameSoundPlayer.playLose()
            }
        } else {
            GameSoundPlayer.playWin()
        }

        // Save records to DB
        val wName = if (winnerSymbol == "X") getPlayer1DisplayName() else getPlayer2DisplayName()
        val wTime = if (winnerSymbol == "X") _p1ThinkingSeconds.value else _p2ThinkingSeconds.value
        val wClicks = if (winnerSymbol == "X") _p1MovesCount.value else _p2MovesCount.value
        val lName = if (winnerSymbol == "X") getPlayer2DisplayName() else getPlayer1DisplayName()
        val lTime = if (winnerSymbol == "X") _p2ThinkingSeconds.value else _p1ThinkingSeconds.value

        viewModelScope.launch {
            val record = GameRecord(
                gameMode = getDBGameMode(),
                winnerSymbol = winnerSymbol,
                playerSymbol = _playerSymbol.value,
                winnerName = wName,
                winnerTime = wTime,
                winnerClicks = wClicks,
                loserName = lName,
                loserTime = lTime
            )
            repository.insertRecord(record)
        }

        if (_gameMode.value == "TOURNAMENT") {
            processTournamentMatchOutcome(winnerSymbol)
        } else {
            clearSavedGame()
            saveGameState()
        }
    }

    private fun handleTie() {
        _isGameOver.value = true
        _scoreDraws.value += 1

        GameSoundPlayer.playTie()

        // Save records to DB
        viewModelScope.launch {
            val record = GameRecord(
                gameMode = getDBGameMode(),
                winnerSymbol = null,
                playerSymbol = _playerSymbol.value,
                winnerName = null,
                winnerTime = 0,
                winnerClicks = 0,
                loserName = null,
                loserTime = 0
            )
            repository.insertRecord(record)
        }

        if (_gameMode.value == "TOURNAMENT") {
            processTournamentMatchOutcome(null)
        } else {
            clearSavedGame()
            saveGameState()
        }
    }

    private fun getDBGameMode(): String {
        return when (_gameMode.value) {
            "VS_BOT" -> "BOT_${_botDifficulty.value}"
            "VS_FRIEND" -> "FRIEND"
            "TOURNAMENT" -> "TOURNAMENT"
            else -> "FRIEND"
        }
    }

    private fun scheduleLeagueMatches(playersCount: Int): List<TournamentMatch> {
        val pool = mutableListOf<TournamentMatch>()
        for (i in 0 until playersCount) {
            for (j in i + 1 until playersCount) {
                // Alternating starts: if (i + j) is odd, i is player1, else j is player1
                if ((i + j) % 2 == 1) {
                    pool.add(TournamentMatch(player1Idx = i, player2Idx = j))
                } else {
                    pool.add(TournamentMatch(player1Idx = j, player2Idx = i))
                }
            }
        }

        // Greedy scheduling to minimize consecutive matches for any player
        val scheduled = mutableListOf<TournamentMatch>()
        val lastPlayed = IntArray(playersCount) { -1 }

        var k = 0
        while (pool.isNotEmpty()) {
            var bestMatchIdx = 0
            var maxMinGap = -1
            var maxSumGap = -1

            for (idx in pool.indices) {
                val match = pool[idx]
                val p1 = match.player1Idx
                val p2 = match.player2Idx

                val gap1 = if (lastPlayed[p1] == -1) 999 else k - lastPlayed[p1]
                val gap2 = if (lastPlayed[p2] == -1) 999 else k - lastPlayed[p2]

                val minGap = Math.min(gap1, gap2)
                val sumGap = gap1 + gap2

                if (minGap > maxMinGap) {
                    maxMinGap = minGap
                    maxSumGap = sumGap
                    bestMatchIdx = idx
                } else if (minGap == maxMinGap && sumGap > maxSumGap) {
                    maxSumGap = sumGap
                    bestMatchIdx = idx
                }
            }

            val chosen = pool.removeAt(bestMatchIdx)
            scheduled.add(chosen)
            lastPlayed[chosen.player1Idx] = k
            lastPlayed[chosen.player2Idx] = k
            k++
        }

        return scheduled
    }

    // ==========================================
    // TOURNAMENT CONTROLLER LOGIC
    // ==========================================
    fun startNewTournament(
        name: String,
        type: String, // "LEAGUE" or "KNOCKOUT"
        players: List<Pair<String, String>>
    ) {
        _tournamentPlayers.value = players
        _tournamentType.value = type
        _currentTournamentMatchIndex.value = 0
        _isTournamentReplay.value = false
        _tournamentTieBreakerInfo.value = null
        _tournamentWinnerName.value = null
        _gameMode.value = "TOURNAMENT"

        // Generate Matches
        val generated = mutableListOf<TournamentMatch>()
        if (type == "LEAGUE") {
            generated.addAll(scheduleLeagueMatches(players.size))
        } else {
            // KNOCKOUT MATCH GENERATION
            if (players.size == 8) {
                // Quarter-finals
                generated.add(TournamentMatch(player1Idx = 0, player2Idx = 1))
                generated.add(TournamentMatch(player1Idx = 2, player2Idx = 3))
                generated.add(TournamentMatch(player1Idx = 4, player2Idx = 5))
                generated.add(TournamentMatch(player1Idx = 6, player2Idx = 7))
                // Semi-finals (virtual indices 8, 9, 10, 11 represent winners of Match 0, 1, 2, 3 respectively)
                generated.add(TournamentMatch(player1Idx = 8, player2Idx = 9))
                generated.add(TournamentMatch(player1Idx = 10, player2Idx = 11))
                // Final (virtual indices 12, 13 represent winners of Match 4, 5 respectively)
                generated.add(TournamentMatch(player1Idx = 12, player2Idx = 13))
            } else {
                // Default to 4 players (since tournament setup only allows 4 or 8)
                // Semi-finals
                generated.add(TournamentMatch(player1Idx = 0, player2Idx = 1))
                generated.add(TournamentMatch(player1Idx = 2, player2Idx = 3))
                // Final (virtual indices 4, 5 represent winners of Match 0, 1 respectively)
                generated.add(TournamentMatch(player1Idx = 4, player2Idx = 5))
            }
        }

        _tournamentMatches.value = generated

        viewModelScope.launch {
            val dbTournament = com.example.data.Tournament(
                name = name,
                type = type,
                playersData = serializePlayers(players),
                matchesData = serializeMatches(generated),
                currentMatchIndex = 0,
                winnerName = null
            )
            val generatedId = repository.insertTournament(dbTournament)
            _activeTournamentId.value = generatedId
        }

        setupActiveTournamentMatch()
    }

    fun getActualPlayerIndexForMatch(virtualIdx: Int, matches: List<TournamentMatch>): Int {
        if (virtualIdx < 0) return -1
        val playerCount = if (matches.size == 3) 4 else 8
        if (virtualIdx < playerCount) {
            return virtualIdx
        }
        val parentMatchIdx = virtualIdx - playerCount
        if (parentMatchIdx in matches.indices) {
            val parentMatch = matches[parentMatchIdx]
            if (parentMatch.winnerIdx == 0) {
                return getActualPlayerIndexForMatch(parentMatch.player1Idx, matches)
            } else if (parentMatch.winnerIdx == 1) {
                return getActualPlayerIndexForMatch(parentMatch.player2Idx, matches)
            }
        }
        return -1
    }

    private fun setupActiveTournamentMatch() {
        val idx = _currentTournamentMatchIndex.value
        val matches = _tournamentMatches.value
        if (idx in matches.indices) {
            val match = matches[idx]
            
            // Swap player assignments to be fair in case of a replay (tie break)
            val isReplay = _isTournamentReplay.value
            val p1IdxRaw = getActualPlayerIndexForMatch(match.player1Idx, matches)
            val p2IdxRaw = getActualPlayerIndexForMatch(match.player2Idx, matches)
            
            val p1Idx = if (isReplay) p2IdxRaw else p1IdxRaw
            val p2Idx = if (isReplay) p1IdxRaw else p2IdxRaw

            val p1 = if (p1Idx in _tournamentPlayers.value.indices) _tournamentPlayers.value[p1Idx] else Pair("?", "?")
            val p2 = if (p2Idx in _tournamentPlayers.value.indices) _tournamentPlayers.value[p2Idx] else Pair("?", "?")

            _tournamentP1Name.value = p1.first
            _tournamentP1Title.value = p1.second
            _tournamentP2Name.value = p2.first
            _tournamentP2Title.value = p2.second

            _playerSymbol.value = "X"
            _p1HintCount.value = 2
            _p2HintCount.value = 2

            resetGameForNextTournamentMatch()
        }
    }

    private fun resetGameForNextTournamentMatch() {
        _board.value = List(9) { null }
        _currentTurn.value = "X"
        _winner.value = null
        _winningLine.value = null
        _isGameOver.value = false
        _isBotThinking.value = false
        
        boardHistory.clear()
        turnHistory.clear()
        _highlightedCell.value = null
        
        _p1MovesCount.value = 0
        _p2MovesCount.value = 0
        _p1ThinkingSeconds.value = 0
        _p2ThinkingSeconds.value = 0
        p1TimeAccumulated = 0
        p2TimeAccumulated = 0
        turnStartTime = System.currentTimeMillis()
        
        hintsUsedInCurrentGame = 0
        
        clearSavedGame()
        saveGameState()
    }

    fun resumeTournament(tournament: com.example.data.Tournament) {
        _activeTournamentId.value = tournament.id
        _tournamentPlayers.value = deserializePlayers(tournament.playersData)
        _tournamentMatches.value = deserializeMatches(tournament.matchesData)
        _tournamentType.value = tournament.type
        _currentTournamentMatchIndex.value = tournament.currentMatchIndex
        _isTournamentReplay.value = false
        _tournamentTieBreakerInfo.value = null
        _tournamentWinnerName.value = null
        _gameMode.value = "TOURNAMENT"

        if (tournament.currentMatchIndex < _tournamentMatches.value.size) {
            setupActiveTournamentMatch()
        } else {
            calculateAndShowTournamentWinner()
        }
    }

    private fun processTournamentMatchOutcome(winnerSymbol: String?) {
        val idx = _currentTournamentMatchIndex.value
        val matches = _tournamentMatches.value.toMutableList()
        if (idx !in matches.indices) return

        val match = matches[idx]

        if (winnerSymbol != null) {
            val wonIdx = if (winnerSymbol == "X") 0 else 1
            match.winnerIdx = wonIdx
            match.score1 = if (wonIdx == 0) 3 else 0
            match.score2 = if (wonIdx == 1) 3 else 0
            match.p1Time = _p1ThinkingSeconds.value
            match.p2Time = _p2ThinkingSeconds.value
            match.p1Hints = 2 - _p1HintCount.value
            match.p2Hints = 2 - _p2HintCount.value
            _isTournamentReplay.value = false
            _tournamentTieBreakerInfo.value = null

            advanceTournamentMatch(matches)
        } else {
            if (_tournamentType.value == "LEAGUE") {
                // League draws: both get 1 point, no replay
                match.winnerIdx = 2 // 2 indicates Draw
                match.score1 = 1
                match.score2 = 1
                match.p1Time = _p1ThinkingSeconds.value
                match.p2Time = _p2ThinkingSeconds.value
                match.p1Hints = 2 - _p1HintCount.value
                match.p2Hints = 2 - _p2HintCount.value
                _isTournamentReplay.value = false
                _tournamentTieBreakerInfo.value = null

                advanceTournamentMatch(matches)
            } else {
                // Knockout stage draws
                if (!_isTournamentReplay.value) {
                    match.r1Time = _p1ThinkingSeconds.value
                    match.r2Time = _p2ThinkingSeconds.value
                    match.r1Clicks = _p1MovesCount.value
                    match.r2Clicks = _p2MovesCount.value
                    match.r1Hints = 2 - _p1HintCount.value
                    match.r2Hints = 2 - _p2HintCount.value
                    match.rDraw = true

                    _isTournamentReplay.value = true
                    _tournamentTieBreakerInfo.value = if (Localizer.currentLanguage == "AR") {
                        "تعادل اللاعبين! تعاد المباراة لحسم النتيجة في مرحلة خروج المغلوب ⚔️"
                    } else {
                        "Tie game! Match will replay to break the tie in Knockout phase."
                    }
                } else {
                    val p1Time2 = _p1ThinkingSeconds.value
                    val p2Time2 = _p2ThinkingSeconds.value
                    val p1Clicks2 = _p1MovesCount.value
                    val p2Clicks2 = _p2MovesCount.value
                    val p1Hints2 = 2 - _p1HintCount.value
                    val p2Hints2 = 2 - _p2HintCount.value

                    val totalHints1 = match.r1Hints + p1Hints2
                    val totalHints2 = match.r2Hints + p2Hints2

                    val totalTime1 = match.r1Time + p1Time2
                    val totalTime2 = match.r2Time + p2Time2

                    val totalClicks1 = match.r1Clicks + p1Clicks2
                    val totalClicks2 = match.r2Clicks + p2Clicks2

                    val wonIdx = when {
                        totalHints1 < totalHints2 -> 0
                        totalHints2 < totalHints1 -> 1
                        totalTime1 < totalTime2 -> 0
                        totalTime2 < totalTime1 -> 1
                        totalClicks1 < totalClicks2 -> 0
                        totalClicks2 < totalClicks1 -> 1
                        else -> 0
                    }

                    match.winnerIdx = wonIdx
                    match.score1 = if (wonIdx == 0) 3 else 0
                    match.score2 = if (wonIdx == 1) 3 else 0
                    match.p1Time = totalTime1
                    match.p2Time = totalTime2
                    match.p1Hints = totalHints1
                    match.p2Hints = totalHints2

                    val p1Actual = getActualPlayerIndexForMatch(match.player1Idx, _tournamentMatches.value)
                    val p2Actual = getActualPlayerIndexForMatch(match.player2Idx, _tournamentMatches.value)
                    val p1NameStr = if (p1Actual in _tournamentPlayers.value.indices) _tournamentPlayers.value[p1Actual].first else "?"
                    val p2NameStr = if (p2Actual in _tournamentPlayers.value.indices) _tournamentPlayers.value[p2Actual].first else "?"
                    val winnerNameStr = if (wonIdx == 0) p1NameStr else p2NameStr

                    val tieBreakerText = if (Localizer.currentLanguage == "AR") {
                        var explanation = "انتهت مباراة الإعادة بالتعادل أيضاً! "
                        explanation += "تم اختيار الفائز ($winnerNameStr) بناءً على الأداء الأكثر كفاءة: "
                        if (totalHints1 != totalHints2) {
                            explanation += "استخدم تلميحات أقل ($totalHints1 ضد $totalHints2 لـ ${if (wonIdx == 0) p2NameStr else p1NameStr})."
                        } else if (totalTime1 != totalTime2) {
                            explanation += "تساوت التلميحات، وتم الحسم بناءً على سرعة التفكير الإجمالية (${totalTime1} ثانية ضد ${totalTime2} ثانية)."
                        } else {
                            explanation += "تساوت التلميحات والوقت، وتم الحسم بناءً على عدد الحركات الأقل (${totalClicks1} حركات ضد ${totalClicks2} حركات)."
                        }
                        explanation
                    } else {
                        "Second replay also ended in a tie! Winner: $winnerNameStr based on stats (Hints: $totalHints1 vs $totalHints2, Time: ${totalTime1}s vs ${totalTime2}s)."
                    }

                    _tournamentTieBreakerInfo.value = tieBreakerText
                    _isTournamentReplay.value = false

                    advanceTournamentMatch(matches)
                }
            }
        }
    }

    private fun advanceTournamentMatch(updatedMatches: List<TournamentMatch>) {
        _tournamentMatches.value = updatedMatches
        
        viewModelScope.launch {
            val currentId = _activeTournamentId.value
            if (currentId != null) {
                val dbTournament = repository.getTournamentById(currentId)
                if (dbTournament != null) {
                    val nextIndex = _currentTournamentMatchIndex.value + 1
                    val isCompleted = nextIndex >= updatedMatches.size
                    
                    var champName: String? = null
                    if (isCompleted) {
                        champName = computeTournamentChampionName(updatedMatches)
                    }

                    val updatedDb = dbTournament.copy(
                        matchesData = serializeMatches(updatedMatches),
                        currentMatchIndex = nextIndex,
                        winnerName = champName
                    )
                    repository.insertTournament(updatedDb)
                }
            }
        }
    }

    fun proceedToNextTournamentMatch() {
        val nextIndex = _currentTournamentMatchIndex.value + 1
        _currentTournamentMatchIndex.value = nextIndex
        _tournamentTieBreakerInfo.value = null
        _isTournamentReplay.value = false

        if (nextIndex < _tournamentMatches.value.size) {
            setupActiveTournamentMatch()
        } else {
            calculateAndShowTournamentWinner()
        }
    }

    private fun calculateAndShowTournamentWinner() {
        val winnerNameStr = computeTournamentChampionName(_tournamentMatches.value)
        _tournamentWinnerName.value = winnerNameStr
        
        GameSoundPlayer.playTournamentChampTheme()
    }

    private fun computeTournamentChampionName(matches: List<TournamentMatch>): String {
        val players = _tournamentPlayers.value
        if (players.isEmpty()) return ""

        val type = _tournamentType.value
        if (type == "KNOCKOUT") {
            val finalMatch = matches.lastOrNull() ?: return ""
            val winnerIdx = finalMatch.winnerIdx
            val actualWinnerIdx = if (winnerIdx == 0) {
                getActualPlayerIndexForMatch(finalMatch.player1Idx, matches)
            } else if (winnerIdx == 1) {
                getActualPlayerIndexForMatch(finalMatch.player2Idx, matches)
            } else {
                -1
            }
            if (actualWinnerIdx in players.indices) {
                val champ = players[actualWinnerIdx]
                return "${champ.second} ${champ.first}".trim()
            }
            return ""
        } else {
            val points = IntArray(players.size) { 0 }
            for (m in matches) {
                if (m.winnerIdx == 0) {
                    points[m.player1Idx] += 3
                } else if (m.winnerIdx == 1) {
                    points[m.player2Idx] += 3
                }
            }

            var maxPoints = -1
            var bestIdx = 0
            for (i in points.indices) {
                if (points[i] > maxPoints) {
                    maxPoints = points[i]
                    bestIdx = i
                }
            }

            val champ = players[bestIdx]
            return "${champ.second} ${champ.first}".trim()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun deleteTournamentById(id: Long) {
        viewModelScope.launch {
            repository.deleteTournamentById(id)
        }
    }

    fun clearAllTournaments() {
        viewModelScope.launch {
            repository.clearTournaments()
        }
    }

    fun wipeAllData() {
        viewModelScope.launch {
            repository.clearHistory()
            repository.clearTournaments()
            resetScores()
            resetGame()
            
            // Restore default preferences
            setSoundEnabled(true)
            setHapticEnabled(true)
            setAppTheme("DEFAULT")
            setBackgroundTheme("DEFAULT")
            setAppLanguage("AR")
            hintsUsedInCurrentGame = 0
            
            clearSavedGame()
        }
    }

    fun undoLastMove() {
        if (boardHistory.isEmpty() || _isGameOver.value || _isBotThinking.value) return

        GameSoundPlayer.playClick()
        
        if (_gameMode.value == "VS_BOT") {
            // In VS Bot, we need to undo both the bot's move and the player's move
            if (boardHistory.size >= 2) {
                // Remove last (which was bot's move)
                boardHistory.removeAt(boardHistory.lastIndex)
                turnHistory.removeAt(turnHistory.lastIndex)
                
                // Get previous human turn's state
                val prevBoard = boardHistory.removeAt(boardHistory.lastIndex)
                val prevTurn = turnHistory.removeAt(turnHistory.lastIndex)
                
                _board.value = prevBoard
                _currentTurn.value = prevTurn
            } else if (boardHistory.size == 1) {
                // If only 1 move was made
                val prevBoard = boardHistory.removeAt(0)
                val prevTurn = turnHistory.removeAt(0)
                _board.value = prevBoard
                _currentTurn.value = prevTurn
            }
        } else {
            // In VS Friend, we undo exactly 1 move
            val prevBoard = boardHistory.removeAt(boardHistory.lastIndex)
            val prevTurn = turnHistory.removeAt(turnHistory.lastIndex)
            _board.value = prevBoard
            _currentTurn.value = prevTurn
        }
        
        _highlightedCell.value = null
        saveGameState()
    }

    fun requestHint() {
        if (_isGameOver.value || _isBotThinking.value) return

        val isP1 = _currentTurn.value == "X"
        val count = if (isP1) _p1HintCount.value else _p2HintCount.value
        if (count <= 0) return

        GameSoundPlayer.playClick()
        if (isP1) {
            _p1HintCount.value -= 1
        } else {
            _p2HintCount.value -= 1
        }
        hintsUsedInCurrentGame += 1

        val activeSymbol = _currentTurn.value
        val opponentSymbol = if (activeSymbol == "X") "O" else "X"
        
        val bestMove = GameEngine.getBotMove(
            board = _board.value,
            botSymbol = activeSymbol,
            playerSymbol = opponentSymbol,
            difficulty = "HARD"
        )

        if (bestMove != -1) {
            _highlightedCell.value = bestMove
            saveGameState()
            
            viewModelScope.launch {
                delay(2500)
                if (_highlightedCell.value == bestMove) {
                    _highlightedCell.value = null
                }
            }
        }
    }

    // Return the status of used hints in the current round
    fun didUseAllThreeHints(): Boolean = hintsUsedInCurrentGame >= 3

    fun clearActiveTournament() {
        _activeTournamentId.value = null
        _tournamentPlayers.value = emptyList()
        _tournamentMatches.value = emptyList()
        _currentTournamentMatchIndex.value = 0
        _tournamentWinnerName.value = null
        _tournamentTieBreakerInfo.value = null
        _gameMode.value = "TOURNAMENT"
        clearSavedGame()
    }
}

class GameViewModelFactory(
    private val application: Application,
    private val repository: GameRecordRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
