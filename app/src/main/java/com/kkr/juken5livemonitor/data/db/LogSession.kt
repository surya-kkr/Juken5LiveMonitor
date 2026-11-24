package com.kkr.juken5livemonitor.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "log_sessions")
data class LogSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val title: String,
    val durationMillis: Long = 0
)
