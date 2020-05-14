package com.rockthevote.grommet.util

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Provides Coroutine dispatchers for better control within the application and tests
 */
interface DispatcherProvider {
    val io: CoroutineDispatcher
    val main: CoroutineDispatcher
    val default: CoroutineDispatcher
}