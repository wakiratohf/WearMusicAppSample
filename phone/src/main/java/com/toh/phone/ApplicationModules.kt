package com.toh.phone

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.toh.phone.data.preference.PreferencesHelper
import com.toh.phone.helper.wearable.WearableHelper
import com.toh.wearossample.helper.wearable.WearableManager
import kotlin.apply
import kotlin.let

class ApplicationModules {
    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var sApplicationModules: ApplicationModules? = null

        val instant: ApplicationModules
            get() {
                if (sApplicationModules == null) {
                    sApplicationModules = ApplicationModules()
                }
                return sApplicationModules!!
            }
    }

    private var mContext: Context? = null
    private var mPreferencesHelper: PreferencesHelper? = null

    private var mWearableManager: WearableManager? = null
    private var mWearableHelper: WearableHelper? = null

    fun getPreferencesHelper(context: Context): PreferencesHelper {
        if (mPreferencesHelper == null) mPreferencesHelper = PreferencesHelper(context)
        return mPreferencesHelper!!
    }


    val wearableHelper: WearableHelper?
        get() = mWearableHelper

    val preferencesHelper: PreferencesHelper?
        get() = mPreferencesHelper

    val wearableManager: WearableManager?
        get() = mWearableManager

    /*
     *Initialize modules for app
     */
    fun initModules(context: Context) {
        mContext = context
        mPreferencesHelper = PreferencesHelper(context)
        mWearableManager = WearableManager(context)
        mWearableManager?.connect()
    }

    fun initWearable() {
        mContext?.let { context ->
            if (mWearableManager?.isWearApiAvailable == true && mWearableHelper == null) {
                Log.i("thoinv", "initWearable: ava")
                mWearableHelper = WearableHelper(context).apply { addClientListener() }
            }
        }
    }

    fun onDestroy() {
        mWearableHelper?.removeClientListener()
        mWearableHelper = null
        sApplicationModules = null
    }
}