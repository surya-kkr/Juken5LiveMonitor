package com.kkr.juken5livemonitor.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Insert
    suspend fun insertSession(session: LogSession): Long

    @Insert
    suspend fun insertEntry(entry: LogEntry)

    @Query("SELECT * FROM log_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<LogSession>>

    @Query("SELECT * FROM log_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: Long): LogSession?

    @Query("SELECT * FROM log_entries WHERE sessionId = :sessionId ORDER BY timeOffset ASC")
    suspend fun getEntriesForSession(sessionId: Long): List<LogEntry>

    @Delete
    suspend fun deleteSession(session: LogSession)

    @Query("DELETE FROM log_sessions WHERE id IN (:ids)")
    suspend fun deleteSessionsByIds(ids: List<Long>)

    @Update
    suspend fun updateSession(session: LogSession)

    @Query("UPDATE log_sessions SET durationMillis = :duration WHERE id = :id")
    suspend fun updateSessionDuration(id: Long, duration: Long)
}
