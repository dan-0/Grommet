package com.rockthevote.grommet.util

import android.content.Context
import com.rockthevote.grommet.ui.MainActivity
import com.rockthevote.grommet.util.extensions.getActivity

object ContextUtil {
    /**
     * Gets [MainActivity] from the given context, or null if not found
     *
     * This is a helper method as the inline function with reified type [getActivity]
     * can't be called from Java
     */
    @JvmStatic
    fun getMainActivityFromContext(context: Context): MainActivity? {
        return context.getActivity()
    }
}