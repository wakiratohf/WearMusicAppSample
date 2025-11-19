package com.toh.wearmusicapp.utils

import android.content.Context
import android.util.Log
import com.blankj.utilcode.util.GsonUtils
import com.toh.shared.config.AppSetting
import com.toh.shared.data.preference.CommonPreferencesHelper

/**
 * Created by Phong on 11/9/2016.
 */
class WearPreferencesHelper(override val context: Context) : CommonPreferencesHelper(context) {

    /**
     * App settings
     * */
    fun getAppSettings(): String {
        return getString(WearPreferenceKeys.FREF_APP_SETTINGS, "") ?: ""
    }

    fun setAppSettings(appSetting: AppSetting) {
        val json = GsonUtils.toJson(appSetting)
        setString(WearPreferenceKeys.FREF_APP_SETTINGS, json)
    }
}