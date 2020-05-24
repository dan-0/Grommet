package com.rockthevote.grommet.util.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Derives an [Activity] of type [T] from the given [Context]
 */
inline fun <reified T: Activity> Context.getActivity(): T? {
    var localContext = this
    while (localContext is ContextWrapper) {
        if (localContext is T) {
            return localContext
        }

        localContext = localContext.baseContext
    }

    return null
}