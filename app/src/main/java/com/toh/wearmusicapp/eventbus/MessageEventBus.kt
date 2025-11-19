package com.toh.wearmusicapp.eventbus

data class MessageEventBus(val event: Event, val extraValue: Any? = null)
