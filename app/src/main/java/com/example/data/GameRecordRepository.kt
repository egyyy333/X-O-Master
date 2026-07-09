package com.example.data

import kotlinx.coroutines.flow.Flow

class GameRecordRepository(private val gameRecordDao: GameRecordDao) {
    val allRecords: Flow<List<GameRecord>> = gameRecordDao.getAllRecords()
    val allTournaments: Flow<List<Tournament>> = gameRecordDao.getAllTournaments()

    suspend fun insertRecord(record: GameRecord) {
        gameRecordDao.insertRecord(record)
    }

    suspend fun clearHistory() {
        gameRecordDao.deleteAllRecords()
    }

    suspend fun insertTournament(tournament: Tournament): Long {
        return gameRecordDao.insertTournament(tournament)
    }

    suspend fun getTournamentById(id: Long): Tournament? {
        return gameRecordDao.getTournamentById(id)
    }

    suspend fun deleteTournamentById(id: Long) {
        gameRecordDao.deleteTournamentById(id)
    }

    suspend fun clearTournaments() {
        gameRecordDao.deleteAllTournaments()
    }
}
