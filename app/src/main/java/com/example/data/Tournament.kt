package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tournaments")
data class Tournament(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "LEAGUE" or "KNOCKOUT"
    val playersData: String, // format: "Name1:Title1|Name2:Title2|..."
    val matchesData: String, // format: "p1Idx,p2Idx,winnerIdx,score1,score2,p1Time,p2Time,p1Hints,p2Hints;..."
    val currentMatchIndex: Int,
    val winnerName: String?, // name of tournament champion, or null if incomplete (غير مكتملة)
    val timestamp: Long = System.currentTimeMillis()
)
