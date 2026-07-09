package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_records")
data class GameRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameMode: String, // "BOT_EASY", "BOT_MEDIUM", "BOT_HARD", "FRIEND", "TOURNAMENT"
    val winnerSymbol: String?, // "X", "O", or null for Draw/Tie
    val playerSymbol: String, // Player's chosen symbol ("X" or "O")
    val timestamp: Long = System.currentTimeMillis(),
    
    // Detailed stats for victory screen
    val winnerName: String? = null,
    val winnerTime: Int = 0,
    val winnerClicks: Int = 0,
    val loserName: String? = null,
    val loserTime: Int = 0
)
