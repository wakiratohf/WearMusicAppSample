package com.toh.phone.data.preference

import android.content.Context
import com.toh.shared.data.preference.CommonPreferencesHelper

/**
 * Created by Phong on 11/9/2016.
 */
class PreferencesHelper(override val context: Context) : CommonPreferencesHelper(context) {

    /**
     * Language code cuối dùng của mode auto language mà phone đồng bộ cho wear
     */
    fun setLastAutoLanguageForWear(language: String) {
        setString(PreferenceKeys.PREF_LAST_AUTO_LANGUAGE_CODE_FOR_WEAR, language)
    }

    fun getLastAutoLanguageForWear(): String {
        return getString(PreferenceKeys.PREF_LAST_AUTO_LANGUAGE_CODE_FOR_WEAR, "") ?: ""
    }
}