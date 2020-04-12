package com.rockthevote.grommet

import javax.inject.Qualifier

/**
 * Created by Mechanical Man on 3/24/20.
 */
@Qualifier
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class IsInstrumentationTest {
}