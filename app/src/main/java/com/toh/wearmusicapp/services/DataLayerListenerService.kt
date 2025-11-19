package com.toh.wearmusicapp.services

import android.content.Context
import android.net.Uri
import android.util.Log
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.toh.shared.WearableActionPath.PING_PHONE
import com.toh.shared.WearableActionPath.SYNC_APP_SETTING
import com.toh.shared.WearableActionPath.SYNC_LIST_AUDIO
import com.toh.shared.WearableChannelPath
import com.toh.shared.WearableDataParam
import com.toh.shared.config.AppSetting
import com.toh.shared.model.AudioItem
import com.toh.shared.utils.LocaleManager
import com.toh.wearmusicapp.WearApplicationModules
import com.toh.wearmusicapp.eventbus.Event
import com.toh.wearmusicapp.eventbus.MessageEventBus
import com.toh.wearmusicapp.utils.AudioDataCache
import com.toh.wearmusicapp.utils.WearableUtils
import com.utility.DataUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.net.URLDecoder

class DataLayerListenerService : WearableListenerService() {
    companion object {
        private const val TAG = "DataLayerListenerSvc"
    }

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val appPref by lazy { WearApplicationModules.instant.getPreferencesHelper(this.applicationContext) }

    override fun attachBaseContext(newBase: Context?) {
        newBase?.let {
            super.attachBaseContext(LocaleManager.setLocale(newBase))
        } ?: let {
            super.attachBaseContext(null)
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        dataEvents.forEach { dataEvent ->
            Log.d(TAG, "Data changed: ${dataEvent.dataItem.uri}")
            val uri = dataEvent.dataItem.uri
            when (uri.path) {
                SYNC_APP_SETTING -> {
                    onSyncAppSetting(dataEvent)
                }

                PING_PHONE -> {
                    doOnPingPhone(dataEvent)
                }

                SYNC_LIST_AUDIO -> {
                    doOnSyncListAudio(dataEvent)
                }
            }
        }
    }

    override fun onChannelOpened(channel: ChannelClient.Channel) {
        super.onChannelOpened(channel)
        val path = URLDecoder.decode(channel.path, Charsets.UTF_8)
        val fakeUri = Uri.parse("wear://${channel.path}")
        val fileName = try {
            URLDecoder.decode(fakeUri.getQueryParameter("name").toString(), Charsets.UTF_8)
        } catch (_: Exception) {
            "${System.currentTimeMillis()}.audio"
        }
        Log.d(TAG, "onChannelOpened: $path")
        if (path.startsWith(WearableChannelPath.DOWNLOAD_FILE, ignoreCase = true)) {
            Log.d(TAG, "Received file: $fileName")
            receiveFileChannel(channel, fileName)
        }
    }

    private fun receiveFileChannel(channel: ChannelClient.Channel, fileName: String) {
        val destFile = File(cacheDir, fileName)
        val destUri = Uri.fromFile(destFile)

        Wearable.getChannelClient(this)
            .receiveFile(channel, destUri, false)
            .addOnCanceledListener {
                Log.i(TAG, "receiveFileWithProgress: cancel")
            }
            .addOnSuccessListener {
                Log.d(TAG, "Download finished")
                ToastUtils.showShort("Receive file $fileName success.")
                Wearable.getChannelClient(this).close(channel)
            }.addOnFailureListener {
                Log.i(TAG, "receiveFileWithProgress: " + it.message)
                ToastUtils.showShort("Receive file $fileName failure. Reason: ${it.message}")
            }
    }

    override fun onChannelClosed(channel: ChannelClient.Channel, p1: Int, p2: Int) {
        super.onChannelClosed(channel, p1, p2)
        Log.i(TAG, "onChannelClosed: ${channel.path}")
    }

    private fun doOnSyncListAudio(dataEvent: DataEvent) {
        DataMapItem.fromDataItem(dataEvent.dataItem).dataMap.let { dataMap ->
            val chunkIndex = dataMap.getInt(WearableDataParam.CHUNK_INDEX)
            val totalChunks = dataMap.getInt(WearableDataParam.CHUNK_TOTAL)
            val chunkData = dataMap.getString(WearableDataParam.LIST_AUDIO_DATA)

            // Lưu từng chunk vào một file tạm trong cache
            val chunkFile = getChunkFile(this, chunkIndex)
            chunkFile.writeText(chunkData ?: "")

            Log.d(TAG, "Received chunk ${chunkIndex + 1} of $totalChunks")

            // Nếu đã nhận đủ tất cả các chunk
            if (areAllChunksReceived(this, totalChunks)) {
                Log.d(TAG, "All chunks received. Merging now.")
                ioScope.launch {
                    val fullAudioList = mutableListOf<AudioItem>()
                    var success = true

                    for (i in 0 until totalChunks) {
                        val file = getChunkFile(this@DataLayerListenerService, i)
                        if (file.exists()) {
                            val chunkJson = file.readText()
                            val audioChunk = chunkJson.fromJson<List<AudioItem>>()
                            if (audioChunk != null) {
                                fullAudioList.addAll(audioChunk)
                            } else {
                                Log.e(TAG, "Failed to parse chunk $i")
                                success = false
                                break // Lỗi khi parse, dừng quá trình
                            }
                            file.delete() // Dọn dẹp file chunk sau khi xử lý
                        } else {
                            Log.e(TAG, "Chunk file $i does not exist!")
                            success = false
                            break // Thiếu chunk, dừng quá trình
                        }
                    }

                    if (success) {
                        // Lưu toàn bộ danh sách audio vào preferences
                        Log.i(TAG, "Successfully synced ${fullAudioList.size} audio items.")
                        AudioDataCache.saveAudioListToPreferences(fullAudioList)

                        // Gửi sự kiện để cập nhật UI
                        EventBus.getDefault().post(MessageEventBus(Event.SYNC_LIST_AUDIO_SUCCESS))
                    } else {
                        Log.e(
                            TAG,
                            "Failed to merge and save audio list due to missing or corrupt chunks."
                        )
                        // Xóa các file chunk còn lại nếu có lỗi
                        cleanupChunks(this@DataLayerListenerService, totalChunks)
                    }
                }
            }
        }
    }


    /**
     * Trả về một file để lưu trữ tạm một chunk dữ liệu.
     */
    private fun getChunkFile(context: Context, index: Int): java.io.File {
        val directory = java.io.File(context.cacheDir, "audio_chunks")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return java.io.File(directory, "chunk_$index.json")
    }

    /**
     * Kiểm tra xem tất cả các chunk đã được nhận hay chưa.
     */
    private fun areAllChunksReceived(context: Context, totalChunks: Int): Boolean {
        for (i in 0 until totalChunks) {
            if (!getChunkFile(context, i).exists()) {
                return false
            }
        }
        return true
    }

    /**
     * Dọn dẹp tất cả các file chunk tạm.
     */
    private fun cleanupChunks(context: Context, totalChunks: Int) {
        for (i in 0 until totalChunks) {
            val file = getChunkFile(context, i)
            if (file.exists()) {
                file.delete()
            }
        }
        val directory = java.io.File(context.cacheDir, "audio_chunks")
        if (directory.exists()) {
            directory.delete()
        }
        Log.d(TAG, "Cleaned up all temporary chunk files.")
    }


    private fun doOnPingPhone(dataEvent: DataEvent) {
        DataMapItem.fromDataItem(dataEvent.dataItem).dataMap.let { dataMap ->
            val pingResultMessage = dataMap.getString(WearableDataParam.MESSAGE)
            EventBus.getDefault().post(MessageEventBus(Event.PING_PHONE_REPLY, pingResultMessage))
        }
    }

    private fun onSyncAppSetting(dataEvent: DataEvent) {
        DataMapItem.fromDataItem(dataEvent.dataItem).dataMap.let { dataMap ->
            parseJsonOrNull<AppSetting>(dataMap.getString(WearableDataParam.APP_SETTING_DATA))?.let { appSetting ->
                var languageChanged = false
                appSetting.language?.let { language ->
                    val oldLanguage = LocaleManager.getLanguage(this@DataLayerListenerService)
                    if (oldLanguage != language) {
                        Log.e(
                            TAG,
                            "LANGUAGE_CHANGED: AppSettings language: $language, oldLanguage: $oldLanguage"
                        )
                        languageChanged = true
                        LocaleManager.setNewLocale(this@DataLayerListenerService, language)
                    }
                }

                // Check if app setting changed
                val oldAppSetting = appPref.getAppSettings()
                val newAppSetting = GsonUtils.toJson(appSetting)
                if (oldAppSetting != newAppSetting) {
                    EventBus.getDefault().post(MessageEventBus(Event.APP_SETTING_CHANGED))
                    Log.i(TAG, "Sync app setting from phone: $appSetting")
                }
                appPref.setAppSettings(appSetting)

                if (languageChanged) {
                    EventBus.getDefault().post(
                        MessageEventBus(
                            Event.LANGUAGE_CHANGED,
                            LocaleManager.getLocale(this@DataLayerListenerService.resources)
                        )
                    )
                } else {
                    ToastUtils.showShort("Sync successful!")
                }
            }
        }
    }

    private fun checkToSyncAppSetting() {
        ioScope.launch {
            val appSetting = appPref.getAppSettings()
            if (appSetting.isEmpty()) {
                WearableUtils.sendCustomMessageToPhone(
                    this@DataLayerListenerService,
                    SYNC_APP_SETTING
                )
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
    }

    override fun onDestroy() {
        super.onDestroy()
        ioScope.cancel()
    }

    private inline fun <reified T> parseJsonOrNull(json: String?): T? {
        if (json.isNullOrEmpty()) return null
        return try {
            DataUtils.parserObject(json, T::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse JSON for type ${T::class.java.simpleName}: ${e.message}")
            null
        }
    }

}
