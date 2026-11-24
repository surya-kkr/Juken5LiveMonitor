package com.kkr.juken5livemonitor.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kkr.juken5livemonitor.data.db.LogDatabase
import com.kkr.juken5livemonitor.data.db.LogEntry
import com.kkr.juken5livemonitor.data.db.LogSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class LogViewModel(application: Application) : AndroidViewModel(application) {
    private val db = LogDatabase.getDatabase(application)
    private val logDao = db.logDao()

    val sessions: Flow<List<LogSession>> = logDao.getAllSessions()

    suspend fun getEntries(sessionId: Long): List<LogEntry> {
        return logDao.getEntriesForSession(sessionId)
    }

    suspend fun getSession(sessionId: Long): LogSession? {
        return logDao.getSessionById(sessionId)
    }

    fun deleteSession(session: LogSession) {
        viewModelScope.launch {
            logDao.deleteSession(session)
        }
    }

    fun deleteSessions(ids: List<Long>) {
        viewModelScope.launch {
            logDao.deleteSessionsByIds(ids)
        }
    }

    fun renameSession(session: LogSession, newTitle: String) {
        viewModelScope.launch {
            logDao.updateSession(session.copy(title = newTitle))
        }
    }
}
