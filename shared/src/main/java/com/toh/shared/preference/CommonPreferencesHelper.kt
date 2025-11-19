package com.toh.shared.data.preference

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.utility.DebugLog
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Phong on 11/9/2016.
 */
open class CommonPreferencesHelper(open val context: Context) {

    private val mPreferenceChangeRegistered = AtomicBoolean(false)
    private val mSharedPreferences: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(context) }
    private val mPreferenceChangedListeners: HashSet<SharedPreferences.OnSharedPreferenceChangeListener?> = hashSetOf()

    private val preferenceChangedListener = SharedPreferences.OnSharedPreferenceChangeListener { preference, key ->
        val listeners = mPreferenceChangedListeners
        listeners.forEach { listener ->
            listener?.onSharedPreferenceChanged(preference, key)
        }
    }

    fun registerPreferenceChanged(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        if (!mPreferenceChangeRegistered.getAndSet(true)) {
            mSharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangedListener)
        }
        mPreferenceChangedListeners.add(listener)
    }

    fun unregisterPreferenceChanged(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        mPreferenceChangedListeners.remove(listener)
    }

    // region ========================================Preference get & set=====================================================
    fun getString(key: String?, defValue: String?): String? {
        return mSharedPreferences.getString(key, defValue)
    }

    fun setString(key: String?, value: String?) {
        mSharedPreferences.edit().apply {
            putString(key, value)
            apply()
        }
    }

    protected fun getInt(key: String, defValue: Int): Int {
        return mSharedPreferences.getInt(key, defValue)
    }

    protected fun setInt(key: String, value: Int) {
        mSharedPreferences.edit().apply {
            putInt(key, value)
            apply()
        }
    }

    protected fun getLong(key: String, defValue: Long): Long {
        return mSharedPreferences.getLong(key, defValue)
    }

    protected fun setLong(key: String, value: Long) {
        mSharedPreferences.edit().apply {
            putLong(key, value)
            apply()
        }
    }

    protected fun getFloat(key: String, defValue: Float): Float {
        return mSharedPreferences.getFloat(key, defValue)
    }

    protected fun setFloat(key: String, value: Float) {
        mSharedPreferences.edit().apply {
            putFloat(key, value)
            apply()
        }
    }

    protected fun getBoolean(key: String, defValue: Boolean): Boolean {
        return mSharedPreferences.getBoolean(key, defValue)
    }

    protected fun setBoolean(key: String, value: Boolean) {
        mSharedPreferences.edit().apply {
            putBoolean(key, value)
            apply()
        }
    }

    fun hasKey(key: String): Boolean {
        return mSharedPreferences.contains(key)
    }
    //endregion
}