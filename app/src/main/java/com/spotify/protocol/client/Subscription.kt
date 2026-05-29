package com.spotify.protocol.client

class Subscription<T> {
    private var callback: ((T) -> Unit)? = null

    fun setEventCallback(callback: (T) -> Unit): Subscription<T> {
        this.callback = callback
        return this
    }

    fun triggerEvent(event: T) {
        callback?.invoke(event)
    }
}
