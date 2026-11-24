package com.kkr.juken5livemonitor.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "log_entries",
    foreignKeys = [
        ForeignKey(
            entity = LogSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LogEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val timeOffset: Long, // milliseconds from start
    
    // Recorded Data
    val battery: Double,
    val rpm: Int,
    val speed: Float,
    val baseMap: String,
    val ignition: Double,
    val map: Int,
    val iat: Double,
    val tps: Int,
    val afr: Double,
    val eot: Double,
    val fuel: String,
    val injector: String
)
