package com.kkr.juken5livemonitor.screen.monitor.center.androidauto.engine

sealed interface ProjectionState {

    data object Disconnected : ProjectionState

    data object Initializing : ProjectionState

    data object WaitingProjection : ProjectionState

    data object Running : ProjectionState

    data object Stopping : ProjectionState

    data class Error(
        val message: String
    ) : ProjectionState

}