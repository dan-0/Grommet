package com.rockthevote.grommet.ui.eventFlow

/**
 * Created by Mechanical Man on 5/16/20.
 */
sealed class SessionTimeTrackingState {

    object Loading : SessionTimeTrackingState()
    

    abstract class Effect : SessionTimeTrackingState()
    class NetworkError(errorMsg: String) : Effect()
}