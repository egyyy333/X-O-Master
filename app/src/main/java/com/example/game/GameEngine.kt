package com.example.game

import kotlin.random.Random

object GameEngine {

    val WINNING_LINES = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Rows
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Columns
        listOf(0, 4, 8), listOf(2, 4, 6)                  // Diagonals
    )

    fun checkWinner(board: List<String?>): Pair<String?, List<Int>?> {
        for (line in WINNING_LINES) {
            val a = board[line[0]]
            val b = board[line[1]]
            val c = board[line[2]]
            if (a != null && a == b && a == c) {
                return Pair(a, line)
            }
        }
        return Pair(null, null)
    }

    fun isBoardFull(board: List<String?>): Boolean {
        return board.all { it != null }
    }

    /**
     * Calculates the computer bot's next move based on difficulty.
     */
    fun getBotMove(board: List<String?>, botSymbol: String, playerSymbol: String, difficulty: String): Int {
        val emptyIndices = board.indices.filter { board[it] == null }
        if (emptyIndices.isEmpty()) return -1

        return when (difficulty) {
            "EASY" -> {
                // Easy: Random move
                emptyIndices.random()
            }
            "MEDIUM" -> {
                // Medium: Play smart 75% of the time, or check win/block, else random.
                // 1. Can bot win in 1 move?
                for (index in emptyIndices) {
                    val nextBoard = board.toMutableList()
                    nextBoard[index] = botSymbol
                    if (checkWinner(nextBoard).first == botSymbol) {
                        return index
                    }
                }

                // 2. Can player win in 1 move? (Block it)
                for (index in emptyIndices) {
                    val nextBoard = board.toMutableList()
                    nextBoard[index] = playerSymbol
                    if (checkWinner(nextBoard).first == playerSymbol) {
                        return index
                    }
                }

                // 3. Take center if available
                if (board[4] == null && Random.nextFloat() < 0.6f) {
                    return 4
                }

                // 4. Fall back to random
                emptyIndices.random()
            }
            "HARD" -> {
                // Hard: Minimax + Alpha-Beta (Perfect AI, impossible to beat)
                getBestMoveAlphaBeta(board, botSymbol, playerSymbol)
            }
            else -> emptyIndices.random()
        }
    }

    private fun getBestMoveAlphaBeta(board: List<String?>, botSymbol: String, playerSymbol: String): Int {
        var bestScore = Int.MIN_VALUE
        var bestMove = -1

        for (i in board.indices) {
            if (board[i] == null) {
                val nextBoard = board.toMutableList()
                nextBoard[i] = botSymbol
                val score = minimaxAlphaBeta(nextBoard, 0, false, Int.MIN_VALUE, Int.MAX_VALUE, botSymbol, playerSymbol)
                if (score > bestScore) {
                    bestScore = score
                    bestMove = i
                }
            }
        }
        return if (bestMove != -1) bestMove else board.indices.first { board[it] == null }
    }

    private fun minimaxAlphaBeta(
        board: List<String?>,
        depth: Int,
        isMaximizing: Boolean,
        alpha: Int,
        beta: Int,
        botSymbol: String,
        playerSymbol: String
    ): Int {
        val winner = checkWinner(board).first
        if (winner == botSymbol) return 10 - depth
        if (winner == playerSymbol) return depth - 10
        if (isBoardFull(board)) return 0

        var currentAlpha = alpha
        var currentBeta = beta

        if (isMaximizing) {
            var bestScore = Int.MIN_VALUE
            for (i in board.indices) {
                if (board[i] == null) {
                    val nextBoard = board.toMutableList()
                    nextBoard[i] = botSymbol
                    val score = minimaxAlphaBeta(nextBoard, depth + 1, false, currentAlpha, currentBeta, botSymbol, playerSymbol)
                    bestScore = maxOf(bestScore, score)
                    currentAlpha = maxOf(currentAlpha, bestScore)
                    if (currentBeta <= currentAlpha) {
                        break // Beta cut-off
                    }
                }
            }
            return bestScore
        } else {
            var bestScore = Int.MAX_VALUE
            for (i in board.indices) {
                if (board[i] == null) {
                    val nextBoard = board.toMutableList()
                    nextBoard[i] = playerSymbol
                    val score = minimaxAlphaBeta(nextBoard, depth + 1, true, currentAlpha, currentBeta, botSymbol, playerSymbol)
                    bestScore = minOf(bestScore, score)
                    currentBeta = minOf(currentBeta, bestScore)
                    if (currentBeta <= currentAlpha) {
                        break // Alpha cut-off
                    }
                }
            }
            return bestScore
        }
    }
}
