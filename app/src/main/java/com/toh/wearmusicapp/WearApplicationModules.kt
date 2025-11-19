package com.toh.wearmusicapp

import android.annotation.SuppressLint
import android.content.Context
import androidx.wear.phone.interactions.notifications.BridgingConfig
import androidx.wear.phone.interactions.notifications.BridgingManager

import com.toh.shared.WearableBridgeTag
import com.toh.wearmusicapp.helper.networkstate.NetworkStateHelper
import com.toh.wearmusicapp.utils.WearPreferencesHelper

class WearApplicationModules {
    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var sApplicationModules: WearApplicationModules? = null

        val instant: WearApplicationModules
            get() {
                if (sApplicationModules == null) {
                    sApplicationModules = WearApplicationModules()
                }
                return sApplicationModules!!
            }
    }

    private var mContext: Context? = null
    private var mPreferencesHelper: WearPreferencesHelper? = null
    private var mNetworkStateHelper: NetworkStateHelper? = null

    fun getPreferencesHelper(context: Context): WearPreferencesHelper {
        if (mPreferencesHelper == null) mPreferencesHelper = WearPreferencesHelper(context)
        return mPreferencesHelper!!
    }

    fun getNetworkStateHelper(context: Context): NetworkStateHelper {
        if (mNetworkStateHelper == null ) {
            mNetworkStateHelper = NetworkStateHelper(context)
        }
        return mNetworkStateHelper!!
    }

    fun initModules(context: Context) {
        mContext = context
        mPreferencesHelper = WearPreferencesHelper(context)
        mNetworkStateHelper = NetworkStateHelper(context.applicationContext).apply { registerNetworkReceiver() }
        BridgingManager.fromContext(context).setConfig(
            BridgingConfig.Builder(context, true)
                .addExcludedTag(WearableBridgeTag.Excluded.HOME)
                .build()
        )
    }


    fun onDestroy() {
        mNetworkStateHelper?.destroy()
        mNetworkStateHelper = null
        sApplicationModules = null
    }
}