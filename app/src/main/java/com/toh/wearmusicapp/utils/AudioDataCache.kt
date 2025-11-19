package com.toh.wearmusicapp.utils

import com.blankj.utilcode.util.GsonUtils
import com.toh.shared.model.AudioItem
import com.toh.wearmusicapp.BaseWearApplication
import com.toh.wearmusicapp.services.fromJson

object AudioDataCache {

    private const val DATA_FILE_NAME = "final_audio_list.json"

    fun saveAudioListToPreferences(audioList: List<AudioItem>) {
        BaseWearApplication.instance?.let {
            java.io.File(it.cacheDir, DATA_FILE_NAME).apply {
                writeText(GsonUtils.toJson(audioList))
            }
        }
    }

    fun getAudioListFromPreferences(): List<AudioItem> {
        BaseWearApplication.instance?.let {
            val file = java.io.File(it.cacheDir, DATA_FILE_NAME)
            if (!file.exists()) return emptyList()
            val json = file.readText()
            return json.fromJson() ?: emptyList()
        }
        return emptyList()
    }

}