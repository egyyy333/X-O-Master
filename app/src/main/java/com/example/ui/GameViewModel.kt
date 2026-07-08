package com.example.ui

import android.app.Application
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

class GameViewModel(
    application: Application,
    private val repository: GameRecordRepository
) : AndroidViewModel(application) {

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
        resetGame()
    }

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
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
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
    }

    fun requestHint() {
        if (_isGameOver.value || _isBotThinking.value || _hintCount.value <= 0) return

        GameSoundPlayer.playClick()
        _hintCount.value -= 1

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
            
            viewModelScope.launch {
                delay(2500)
                if (_highlightedCell.value == bestMove) {
                    _highlightedCell.value = null
                }
            }
        }
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
