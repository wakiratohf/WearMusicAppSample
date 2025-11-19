package com.toh.wearmusicapp.presentation.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.google.android.gms.wearable.Node
import com.toh.shared.WearableActionPath
import com.toh.shared.WearableActionPath.PING_PHONE
import com.toh.shared.model.AudioItem
import com.toh.shared.utils.LocaleManager
import com.toh.wearmusicapp.WearApplicationModules
import com.toh.wearmusicapp.base.BaseThemeViewModel
import com.toh.wearmusicapp.eventbus.Event
import com.toh.wearmusicapp.utils.WearableUtils
import com.toh.wearmusicapp.eventbus.MessageEventBus
import com.toh.wearmusicapp.services.json
import com.toh.wearmusicapp.utils.AudioDataCache
import com.utility.DebugLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Locale

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(context) as T
    }
}

class MainViewModel(context: Context) : BaseThemeViewModel() {
    private val appContext = context.applicationContext
    private val appPref by lazy { WearApplicationModules.instant.getPreferencesHelper(appContext) }

    // State lưu trữ Locale hiện tại
    private val _currentLocale = MutableStateFlow(LocaleManager.getLocale(context.resources))
    val currentLocale: StateFlow<Locale> = _currentLocale

    private val _currentSettingData = MutableStateFlow("")
    val currentSettingData: StateFlow<String> = _currentSettingData

    private val _pairedDevices = MutableStateFlow<List<Node>>(emptyList())
    val pairedDevices: StateFlow<List<Node>> = _pairedDevices

    private val _audioItemsData = MutableStateFlow<List<AudioItem>>(emptyList())
    val audioItemsData: StateFlow<List<AudioItem>> = _audioItemsData

    override fun languageChanged(newLocale: Locale) {
        _currentLocale.value = newLocale
    }

    fun pingPhone() {
        getPairedNodes()
        viewModelScope.launch(IODispatchers) {
            WearableUtils.sendCustomMessageToPhone(appContext, PING_PHONE)
        }
    }

    override fun onAppSettingChanged() {
        super.onAppSettingChanged()
        _currentSettingData.value = appPref.getAppSettings()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPingPhoneReply(messageEvent: MessageEventBus) {
        if (messageEvent.event == Event.PING_PHONE_REPLY) {
            (messageEvent.extraValue as? String)?.let {
                ToastUtils.showShort("Phone reply: \n$it")
            }
        } else if (messageEvent.event == Event.SYNC_LIST_AUDIO_SUCCESS) {
            (messageEvent.extraValue as? String)?.let {
                ToastUtils.showShort("Sync audio success")
            }
            viewModelScope.launch(IODispatchers) {
                val audioItems = AudioDataCache.getAudioListFromPreferences()
                DebugLog.logd("Wear audio list size: ${audioItems.size}")
                _audioItemsData.value = audioItems
            }
        }
    }

    fun init() {
        getPairedNodes()
    }

    private fun getPairedNodes() {
        viewModelScope.launch(IODispatchers) {
            _pairedDevices.value = WearableUtils.getPhonePairedList(appContext)
        }
    }

    fun requestDownloadFile(audioItem: AudioItem) {
        viewModelScope.launch(IODispatchers) {
            val payload = audioItem.json()?.toByteArray()
            WearableUtils.sendCustomMessageToPhone(appContext, WearableActionPath.REQUEST_DOWNLOAD_FILE, payload)
        }

    }

    fun startSyncAudio() {
        viewModelScope.launch(IODispatchers) {
            WearableUtils.sendCustomMessageToPhone(appContext, WearableActionPath.SYNC_LIST_AUDIO)
        }
    }
}