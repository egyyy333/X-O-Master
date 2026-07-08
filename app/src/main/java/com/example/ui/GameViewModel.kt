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
            "app_name" -> if (isAr) "إكس أو المحترف" else "XO Master Pro"
            "app_subtitle" -> if (isAr) "تحدّ الذكاء الاصطناعي بمستويات مذهلة أو العب مع أصدقائك بنقرة سريعة" else "Challenge elite AI or battle friends with style"
            "mode_title" -> if (isAr) "١. نمط اللعب" else "1. Game Mode"
            "mode_bot" -> if (isAr) "ضد الكمبيوتر (البوت)" else "VS Computer (Bot)"
            "mode_friend" -> if (isAr) "ضد لاعب (ثنائي)" else "Local Multiplayer"
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
            "re_round" -> if (isAr) "إعادة جولة" else "Reset Round"
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
            "win_congrats" -> if (isAr) "تهانينا! فوز ساحق" else "Victory! Spectacular Win"
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

class GameViewModel(
    application: Application,
    private val repository: GameRecordRepository
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("xo_master_prefs", Context.MODE_PRIVATE)

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
    private val _hintCount = MutableStateFlow(3)
    val hintCount: StateFlow<Int> = _hintCount.asStateFlow()

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

    fun setAppLanguage(language: String) {
        _appLanguage.value = language
        Localizer.currentLanguage = language
        prefs.edit().putString("app_language", language).apply()
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
        editor.putInt("saved_hint_count", _hintCount.value)
        editor.putInt("saved_hints_used", hintsUsedInCurrentGame)
        
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
                    _hintCount.value = prefs.getInt("saved_hint_count", 3)
                    hintsUsedInCurrentGame = prefs.getInt("saved_hints_used", 0)
                    
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
        saveGameState()
    }

    fun resetGame() {
        _board.value = List(9) { null }
        _currentTurn.value = "X"
        _winner.value = null
        _winningLine.value = null
        _isGameOver.value = false
        _isBotThinking.value = false
        
        // Reset undo and hint states
        boardHistory.clear()
        turnHistory.clear()
        _highlightedCell.value = null
        _hintCount.value = 3
        hintsUsedInCurrentGame = 0

        clearSavedGame()
        saveGameState()

        // If Bot's turn is first in bot mode (Bot is X), let bot play!
        if (_gameMode.value == "VS_BOT" && botSymbol == "X") {
            triggerBotMove()
        }
    }

    fun onCellClick(index: Int) {
        // Prevent click if game is over, cell already has a symbol, or bot is thinking
        if (_isGameOver.value || _board.value[index] != null || _isBotThinking.value) {
            return
        }

        // Record history before move
        boardHistory.add(_board.value)
        turnHistory.add(_currentTurn.value)

        // Clear any active highlight hint
        _highlightedCell.value = null

        val activeSymbol = _currentTurn.value

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

                // Play custom symbol sound for bot action
                if (botSymbol == "X") {
                    GameSoundPlayer.playXSound()
                } else {
                    GameSoundPlayer.playOSound()
                }

                val updatedBoard = currentBoard.toMutableList()
                updatedBoard[move] = botSymbol
                _board.value = updatedBoard

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
        viewModelScope.launch {
            val record = GameRecord(
                gameMode = if (_gameMode.value == "VS_BOT") "BOT_${_botDifficulty.value}" else "FRIEND",
                winnerSymbol = winnerSymbol,
                playerSymbol = _playerSymbol.value
            )
            repository.insertRecord(record)
        }

        clearSavedGame()
        saveGameState()
    }

    private fun handleTie() {
        _isGameOver.value = true
        _scoreDraws.value += 1

        GameSoundPlayer.playTie()

        // Save records to DB
        viewModelScope.launch {
            val record = GameRecord(
                gameMode = if (_gameMode.value == "VS_BOT") "BOT_${_botDifficulty.value}" else "FRIEND",
                winnerSymbol = null,
                playerSymbol = _playerSymbol.value
            )
            repository.insertRecord(record)
        }

        clearSavedGame()
        saveGameState()
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun wipeAllData() {
        viewModelScope.launch {
            repository.clearHistory()
            resetScores()
            resetGame()
            
            // Restore default preferences
            setSoundEnabled(true)
            setHapticEnabled(true)
            setAppTheme("COSMIC_FANTASY")
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
        if (_isGameOver.value || _isBotThinking.value || _hintCount.value <= 0) return

        GameSoundPlayer.playClick()
        _hintCount.value -= 1
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
