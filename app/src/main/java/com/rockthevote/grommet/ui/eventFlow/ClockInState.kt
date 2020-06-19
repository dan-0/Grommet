package com.rockthevote.grommet.ui.eventFlow

/**
 * Created by Mechanical Man on 5/16/20.
 */
sealed class ClockInState {
    object ClockedIn : ClockInState()
    object CLockedOut : ClockInState()
    object Loading : ClockInState()

    abstract class Effect : ClockInState()
    class NetworkError(errorMsg: String) : Effect()
}