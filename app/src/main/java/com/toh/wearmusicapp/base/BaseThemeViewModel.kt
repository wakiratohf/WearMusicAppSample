package com.toh.wearmusicapp.base

import com.toh.wearmusicapp.eventbus.Event
import com.toh.wearmusicapp.eventbus.MessageEventBus
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.Locale

open class BaseThemeViewModel : BaseViewModel() {
    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    @Subscribe
    fun onMessageEvent(messageEvent: MessageEventBus) {
        if (messageEvent.event == Event.LANGUAGE_CHANGED) {
            (messageEvent.extraValue as? Locale)?.let {
                languageChanged(it)
            }
            return
        }
        if (messageEvent.event == Event.APP_SETTING_CHANGED) {
            onAppSettingChanged()
        }
    }

    open fun onAppSettingChanged() {
    }

    open fun languageChanged(newLocale: Locale) {
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }
}