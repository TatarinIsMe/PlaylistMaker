package com.example.playlistmaker.presentation.search

/**
 * Wraps data that should be consumed only once (for navigation or one-time messages).
 */
class Event<out T>(private val content: T) {
    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? =
        if (hasBeenHandled) null
        else {
            hasBeenHandled = true
            content
        }

    fun peekContent(): T = content
}
